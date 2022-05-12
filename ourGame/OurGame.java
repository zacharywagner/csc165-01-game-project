package ourGame;

import java.io.*;
import java.util.*;
import javax.script.*;
import net.java.games.input.*;
import org.joml.Vector3f;
import ourGame.inputActions.*;
import tage.*;
import tage.input.*;
import tage.input.IInputManager.INPUT_ACTION_TYPE;
import tage.physics.*;
import tage.physics.JBullet.*;
import tage.shapes.*;
import tage.audio.*;
import tage.audio.AudioManagerFactory;
import tage.audio.AudioResource;

public class OurGame extends VariableFrameRateGame{

    private static Engine engine;

    public static Engine getEngine(){
        return engine;
    }

    public static void main(String args[]){
        OurGame ourGame = new OurGame();
        engine = new Engine(ourGame);
        ourGame.initializeSystem();
        ourGame.game_loop();
    }

    public static float[] toFloatArray(double[] arr){ 
        if (arr == null) return null;
        int n = arr.length;
        float[] ret = new float[n];
        for (int i = 0; i < n; i++){ 
            ret[i] = (float)arr[i];
        }
        return ret;
    }

    public static double[] toDoubleArray(float[] arr){ 
        if (arr == null) return null;
        int n = arr.length;
        double[] ret = new double[n];
        for (int i = 0; i < n; i++){ 
            ret[i] = (double)arr[i];
        }
        return ret;
    }

    private LinkedList<Projectile> activeProjectiles = new LinkedList<Projectile>();
    private LinkedList<Projectile> inactiveProjectiles = new LinkedList<Projectile>();
    private Player avatar;
    private long currentTime, previousTime;
    private InputManager inputManager;
    private ScriptEngine javaScriptEngine;
    private PhysicsEngine physicsEngine;
    private ImportedModel playerModel;
    private TextureImage playerTexture;
    private Sphere greenSphere;
    private Sphere redSphere;
    private ImportedModel enemyModel;
    private TextureImage enemyTexture;
    private HashMap<Integer, Enemy> enemies = new HashMap<Integer, Enemy>();

    public Player getAvatar(){
        return avatar;
    }

    public double getDeltaTime(){
        long deltaTime = currentTime - previousTime;
        return (double)deltaTime / 1000d;
    }

    public PhysicsEngine getPhysicsEngine(){
        if(physicsEngine == null) initializePhysics();
        return physicsEngine;
    }

    public ImportedModel getPlayerModel(){
        return playerModel;
    }

    public TextureImage getPlayerTexture(){
        return playerTexture;
    }

    public Sphere getGreenSphere(){
        return greenSphere;
    }

    public Sphere getRedSphere(){
        return redSphere;
    }

    public ImportedModel getEnemyModel(){
        return enemyModel;
    }

    public TextureImage getEnemyTexture(){
        return enemyTexture;
    }

    public OurGame(){
        super();
    }

    @Override
    public void loadShapes() {
        playerModel = new ImportedModel("player.obj");
        enemyModel = new ImportedModel("enemy.obj");
        greenSphere = new Sphere();
        greenSphere.setMatAmb(new float[]{0f, 1f, 0f, 1f});
        redSphere = new Sphere();
        redSphere.setMatAmb(new float[]{1f, 0f, 0f, 1f});
    }

    @Override
    public void loadTextures() {
        playerTexture = new TextureImage("player.png");
        enemyTexture = new TextureImage("enemy.png");
    }

    @Override
    public void buildObjects() {
        avatar = new Player(this);
    }

    @Override
    public void initializeGame() {
        currentTime = System.currentTimeMillis();
        initializeScriptEngines();
        File file = new File("assets/scripts/initializeOurGame.js");
        runScript(file);
        avatar.setSpeed((float)(double)javaScriptEngine.get("playerSpeed"));
        initializeCameras();
        initializeLights();
        initializeInputs();
        initializeAudio();
        instantiateEnemy(new Vector3f());
    }

    @Override
    public void update() {
        previousTime = currentTime;
        currentTime = System.currentTimeMillis();
        inputManager.update((float)getDeltaTime());
        //System.out.println(avatar.getWorldLocation());
        avatar.movePlayerInBounds();
        setEarParameters();
        updateProjectiles();
    }

    private void initializeCameras(){
        Camera camera = engine.getRenderSystem().getViewport("MAIN").getCamera();
        camera.setLocation((Vector3f)javaScriptEngine.get("cameraLocation"));
        camera.setV(camera.getV().rotateAxis((float)Math.toRadians(-90f), camera.getU().x, camera.getU().y, camera.getU().z));
        camera.setN(camera.getN().rotateAxis((float)Math.toRadians(-90f), camera.getU().x, camera.getU().y, camera.getU().z));
    }

    private void initializeLights(){
        Vector3f globalAmbient = (Vector3f)javaScriptEngine.get("lightGlobalAmbient");
        Light.setGlobalAmbient(globalAmbient.x, globalAmbient.y, globalAmbient.z);
        (engine.getSceneGraph()).addLight((Light)javaScriptEngine.get("light"));
    }

    private void initializeInputs(){
        inputManager = engine.getInputManager();
        MoveAvatarAction moveAvatarAction = new MoveAvatarAction(this);
        AvatarFireAction avatarFireAction = new AvatarFireAction(this);
        ArrayList<Controller> controllers = inputManager.getControllers();
        for (Controller controller : controllers) {
            if(controller.getType().equals(Controller.Type.KEYBOARD)){
                inputManager.associateAction(
                    controller,
                    net.java.games.input.Component.Identifier.Key.W,
                    moveAvatarAction,
                    INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN
                );
            }
            if(controller.getType().equals(Controller.Type.KEYBOARD)){
                inputManager.associateAction(
                    controller,
                    net.java.games.input.Component.Identifier.Key.A,
                    moveAvatarAction,
                    INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN
                );
            }
            if(controller.getType().equals(Controller.Type.KEYBOARD)){
                inputManager.associateAction(
                    controller,
                    net.java.games.input.Component.Identifier.Key.S,
                    moveAvatarAction,
                    INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN
                );
            }
            if(controller.getType().equals(Controller.Type.KEYBOARD)){
                inputManager.associateAction(
                    controller,
                    net.java.games.input.Component.Identifier.Key.D,
                    moveAvatarAction,
                    INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN
                );
            }
            if(controller.getType().equals(Controller.Type.KEYBOARD)){
                inputManager.associateAction(
                    controller,
                    net.java.games.input.Component.Identifier.Key.SPACE,
                    avatarFireAction,
                    INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN
                );
            }
        }
    }

    private void initializeScriptEngines(){
        ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
        List<ScriptEngineFactory> scriptEngineFactories = scriptEngineManager.getEngineFactories();
        javaScriptEngine = scriptEngineManager.getEngineByName("js");
    }

    private void runScript(File scriptFile){ 
		try{ 
			FileReader fileReader = new FileReader(scriptFile);
		    javaScriptEngine.eval(fileReader);
	        fileReader.close();
	    }
	    catch (FileNotFoundException e1){ 
            System.out.println(scriptFile + " not found " + e1); }
	    catch (IOException e2){
            System.out.println("IO problem with " + scriptFile + e2); }
	    catch (ScriptException e3){ 
            System.out.println("ScriptException in " + scriptFile + e3); }
	    catch (NullPointerException e4){ 
            System.out.println ("Null ptr exception reading " + scriptFile + e4);
	    } 
    }

    private void initializePhysics(){
        String engine = "tage.physics.JBullet.JBulletPhysicsEngine";
        float[] gravity = {0f, 0f, 0f};
        physicsEngine = PhysicsEngineFactory.createPhysicsEngine(engine);
        physicsEngine.initSystem();
        physicsEngine.setGravity(gravity);
    }

    public Projectile getOrCreateProjectile(Vector3f direction, boolean isPlayers, Vector3f position, float speed){
        Projectile projectile = null;
        if(inactiveProjectiles.size() > 0){
            projectile = inactiveProjectiles.getFirst();
            projectile.getRenderStates().enableRendering();
            activeProjectiles.addLast(projectile);
            inactiveProjectiles.removeFirst();
        }
        else{
            projectile = new Projectile(this);
            activeProjectiles.addLast(projectile);
        }
        projectile.initialize(direction, isPlayers, position, speed);
        return projectile;
    }

    public void deactivateProjectile(Projectile projectile){
        for(int i = 0; i < activeProjectiles.size(); i++){
            if(activeProjectiles.get(i) == projectile){
                inactiveProjectiles.addLast(activeProjectiles.remove(i));
                projectile.getRenderStates().disableRendering();
                return;
            }
        }
    }

    public void updateProjectiles(){
        Projectile activeProjectiles[] = new Projectile[this.activeProjectiles.size()];
        this.activeProjectiles.toArray(activeProjectiles);
        for (Projectile activeProjectile : activeProjectiles) {
            activeProjectile.updateProjectile();
        }
    }

    public void instantiateEnemy(Vector3f location){
        Enemy enemy = new Enemy(this, location);
        enemies.put(enemy.getUid(), enemy);
    }

    public void removeEnemy(Enemy enemy){
        enemies.remove(enemy.getUid());
        enemy.getRenderStates().disableRendering();
        engine.getSceneGraph().removeGameObject(enemy);
    }

    
    //================================================
    // AUDIO
    //================================================

    //CC3.0 Licenses 
    //https://opengameart.org/content/upbeat-sci-fi-intro
    //Joe Reynolds - Professorlamp
    //jrtheories.webs.com

    //https://opengameart.org/content/boss-battle-theme
    //CC-BY-SA 3.0
    //Music by Cleyton Kauffman 
    //https://soundcloud.com/cleytonkauffman

    private IAudioManager audioManager;
    private HashMap<String, AudioResource> audioResources = new HashMap<String, AudioResource>();
    private Sound backgroundMusicSound;

    public void initializeAudio(){
        audioManager = AudioManagerFactory.createAudioManager("tage.audio.joal.JOALAudioManager");
        if(!audioManager.initialize()){
            System.out.println("The audio manager did not initialize!");
        }
        AudioResource audioResource = createAudioResource("backgroundMusic", AudioResourceType.AUDIO_STREAM);
        backgroundMusicSound = createSound(audioResource, SoundType.SOUND_MUSIC, 25, true);
        backgroundMusicSound.play();
        createAudioResource("laser9", AudioResourceType.AUDIO_SAMPLE);
        setEarParameters();
    }

    private void setEarParameters(){
        audioManager.getEar().setLocation(avatar.getWorldLocation());
        audioManager.getEar().setOrientation(avatar.getWorldForwardVector(), new Vector3f(0.0f, 1.0f, 0.0f));
    }

    public AudioResource getAudioResource(String string){
        AudioResource audioResource = audioResources.get(string);
        return audioResource;
    }

    public AudioResource createAudioResource(String string, AudioResourceType audioResourceType){
        AudioResource audioResource;    
        try{
            audioResource = audioManager.createAudioResource("assets/sounds/" + string + ".wav", audioResourceType);
            audioResources.put(string, audioResource);
        }
        catch(Exception exception){
            throw exception;
        }
        return audioResource;
    }

    public Sound createSound(AudioResource audioResource, SoundType soundType, int volume, boolean looping){
        Sound sound = new Sound(audioResource, soundType, volume, looping);
        sound.initialize(audioManager);
        return sound;
    }
    //================================================
    // AUDIO
    //================================================
}
