package ourGame;

import java.io.*;
import java.lang.Math;
import java.util.*;
import javax.script.*;

import net.java.games.input.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.joml.*;
import ourGame.inputActions.*;
import tage.*;
import tage.input.*;
import tage.input.IInputManager.INPUT_ACTION_TYPE;
import tage.networking.IGameConnection.ProtocolType;
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
        OurGame ourGame = new OurGame(args[0], Integer.parseInt(args[1]));
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

    // networking stuff
    private GhostManager gm;
    private ProtocolClient protocolClient;
    private String serverAddress;
    private ProtocolType serverProtocol;
    private int serverPort;
    private ImportedModel ghostModel;
    private TextureImage ghostTexture;
    private boolean isConnected = false;
    private boolean isHost = false;

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
    private HashMap<Integer, Spaceship> spaceships = new HashMap<Integer, Spaceship>();
    private HashMap<Integer, Projectile> projectiles = new HashMap<Integer, Projectile>();
    private int skybox;
    private AnimatedShape bossAnimatedShape;
    private TextureImage bossTexture;
    private Boss boss;


    public void registerSpaceship(Spaceship spaceship){
        spaceships.put(spaceship.getPhysicsObject().getUID(), spaceship);
    }

    public void registerProjectile(Projectile projectile){
        projectiles.put(projectile.getPhysicsObject().getUID(), projectile);
    }

    public Player getAvatar(){
        return avatar;
    }

    public GhostManager getGhostManager() {
        return gm;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setIsConnected(boolean value) {
        isConnected = value;
    }

    public void setIsHost(boolean value) {
        isHost = value;
    }

    public Vector3f getPlayerPosition() {
        return avatar.getLocalLocation();
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

    public ImportedModel getGhostModel() {
        return ghostModel;
    }

    public TextureImage getPlayerTexture(){
        return playerTexture;
    }

    public TextureImage getGhostTexture() {
        return ghostTexture;
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

    public AnimatedShape getBossAnimatedShape(){
        return bossAnimatedShape;
    }

    public TextureImage getBossTexture(){
        return bossTexture;
    }

    public OurGame(String serverAddress, int serverPort){
        super();
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.serverProtocol = ProtocolType.UDP;
    }

    @Override
    public void loadSkyBoxes() {
        skybox = (engine.getSceneGraph()).loadCubeMap("redSpace");
        (engine.getSceneGraph()).setActiveSkyBoxTexture(skybox);
        (engine.getSceneGraph()).setSkyBoxEnabled(true);
    }

    @Override
    public void loadShapes() {
        playerModel = new ImportedModel("player.obj");
        ghostModel = new ImportedModel("player.obj");
        enemyModel = new ImportedModel("enemy.obj");
        greenSphere = new Sphere();
        greenSphere.setMatAmb(new float[]{0f, 1f, 0f, 1f});
        redSphere = new Sphere();
        redSphere.setMatAmb(new float[]{1f, 1f, 0f, 1f});
        bossAnimatedShape = new AnimatedShape("mechman.rkm", "mechman.rks");
        bossAnimatedShape.loadAnimation("KICK", "mech_kick.rka");
        bossAnimatedShape.loadAnimation("SUMMON", "summon.rka");
        bossAnimatedShape.loadAnimation("DEAD", "dead.rka");
    }

    @Override
    public void loadTextures() {
        playerTexture = new TextureImage("player.png");
        ghostTexture = new TextureImage("player.png");
        enemyTexture = new TextureImage("enemy.png");
        bossTexture = new TextureImage("mechman.png");

    }

    @Override
    public void buildObjects() {
        initializePhysics();
        avatar = new Player(this);
        boss = new Boss(this);
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
        initializeNetworking();
        initializeInputs();
        initializeAudio();
        instantiateEnemy(new Vector3f(0f, 0f, 0f));
    }

    @Override
    public void update() {
        long elapsedTime = System.currentTimeMillis() - previousTime;
        previousTime = currentTime;
        currentTime = System.currentTimeMillis();
        setEarParameters();
        inputManager.update((float)getDeltaTime());
        //System.out.println(avatar.getWorldLocation());
        avatar.movePlayerInBounds();
        enemies.forEach((key, value) -> {
            value.updateEnemy();
        });
        bossAnimatedShape.updateAnimation();
        updateProjectiles();
        for (GameObject go:engine.getSceneGraph().getGameObjects()){
            if (go.getPhysicsObject() != null){ 
                go.getPhysicsObject().setLinearVelocity(new float[]{0f, 1f, 0f});
                go.getPhysicsObject().setAngularVelocity(new float[]{0f, 1f, 0f});
                float values[] = new float[16];
                double[] transform = toDoubleArray(go.getLocalTranslation().get(values));
                go.getPhysicsObject().setTransform(transform);
            } 
        }
        checkForCollisions();
        physicsEngine.update((float)(currentTime - previousTime));
        processNetworking((float)elapsedTime);
    }

    //
    //PHYSICS
    //

    private void checkForCollisions(){ 
        com.bulletphysics.dynamics.DynamicsWorld dynamicsWorld;
        com.bulletphysics.collision.broadphase.Dispatcher dispatcher;
        com.bulletphysics.collision.narrowphase.PersistentManifold manifold;
        com.bulletphysics.dynamics.RigidBody object1, object2;
        com.bulletphysics.collision.narrowphase.ManifoldPoint contactPoint;
        dynamicsWorld = ((JBulletPhysicsEngine)physicsEngine).getDynamicsWorld();
        dispatcher = dynamicsWorld.getDispatcher();
        int manifoldCount = dispatcher.getNumManifolds();
        for (int i=0; i<manifoldCount; i++){ 
            manifold = dispatcher.getManifoldByIndexInternal(i);
            object1 = (com.bulletphysics.dynamics.RigidBody)manifold.getBody0();
            object2 = (com.bulletphysics.dynamics.RigidBody)manifold.getBody1();
            JBulletPhysicsObject obj1 = JBulletPhysicsObject.getJBulletPhysicsObject(object1);
            JBulletPhysicsObject obj2 = JBulletPhysicsObject.getJBulletPhysicsObject(object2);
            for (int j = 0; j < manifold.getNumContacts(); j++){ 
                contactPoint = manifold.getContactPoint(j);
                if (contactPoint.getDistance() < 0.0f){ 
                    int uid1 = obj1.getUID();
                    int uid2 = obj2.getUID();
                    resolveCollision(uid1, uid2);
                    break;
                } 
            } 
        } 
    }

    private void resolveCollision(int uid1, int uid2){

        Spaceship spaceship1 = spaceships.get(uid1), spaceship2 = spaceships.get(uid2);
        Projectile projectile1 = projectiles.get(uid1), projectile2 = projectiles.get(uid2);
        if(spaceship1 != null){
            if(spaceship2 != null){
                if(spaceship1.getIsFriend() != spaceship2.getIsFriend()){
                    // System.out.println("Two spacesgips collided!");
                }
            }
            else if(projectile1 != null && activeProjectiles.contains(projectile1)){
                if(spaceship1.getIsFriend() != projectile1.getIsPlayers()){
                    // System.out.println("A spaceship was hit by a projectile!");
                    //projectile1.setTimer(8.1f);
                    deactivateProjectile(projectile1);
                }
            }
            else if(projectile2 != null && activeProjectiles.contains(projectile2)){
                if(spaceship1.getIsFriend() != projectile2.getIsPlayers()){
                    // System.out.println("A spaceship was hit by a projectile!");
                    //projectile2.setTimer(8.1f);
                    deactivateProjectile(projectile2);
                }
            }
        }
        else if(spaceship2 != null){
            if(projectile1 != null && activeProjectiles.contains(projectile1)){
                if(spaceship2.getIsFriend() != projectile1.getIsPlayers()){
                    // System.out.println("A spaceship was hit by a projectile!");
                    //projectile1.setTimer(8.1f);
                    deactivateProjectile(projectile1);
                }
            }
            else if(projectile2 != null && activeProjectiles.contains(projectile2)){
                if(spaceship2.getIsFriend() != projectile2.getIsPlayers()){
                    // System.out.println("A spaceship was hit by a projectile!");
                    //projectile2.setTimer(8.1f);
                    deactivateProjectile(projectile2);
                }
            }
        }
        //System.out.println("At " + currentTime + " a collision between " + uid1 + " and " + uid2 + " occured.");
        /*
        for(int i  = 0; i < 16; i++){
            System.out.print(avatar.getPhysicsObject().getTransform()[i] + ", ");
        }
        System.out.println();
        for(int i  = 0; i < 16; i++){
            System.out.print(enemies.get(1).getPhysicsObject().getTransform()[i] + ", ");
        }
        System.out.println();
        */
    }

    //
    //PHYSICS
    //

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
        MoveAvatarAction moveAvatarAction = new MoveAvatarAction(this, protocolClient);
        AvatarFireAction avatarFireAction = new AvatarFireAction(this, protocolClient);
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

    private void initializeNetworking() {
        gm = new GhostManager(this);
        isConnected = false;
        try {
            protocolClient = new ProtocolClient(InetAddress.getByName(serverAddress), serverPort, serverProtocol, this);
            isConnected = true;
        }
        catch (UnknownHostException e) { e.printStackTrace(); }
        catch (IOException e) { e.printStackTrace(); }

        if(protocolClient == null) {
            System.out.println("missing protocol host");
        }
        else {
            System.out.println("sending join message to protocol host");
            protocolClient.sendJoinMessage();
        }
    }

    private void processNetworking(float elapsedTime) {
        if (protocolClient != null) {
            protocolClient.processPackets();
        }
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
                projectile.deactivate();
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
