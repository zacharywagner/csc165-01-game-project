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

    private Player avatar;
    private long currentTime, previousTime;
    private InputManager inputManager;
    private ScriptEngine javaScriptEngine;
    private PhysicsEngine physicsEngine;
    private ImportedModel playerModel;
    private TextureImage playerTexture;

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

    public OurGame(){
        super();
    }

    @Override
    public void loadShapes() {
        playerModel = new ImportedModel("player.obj");
    }

    @Override
    public void loadTextures() {
        playerTexture = new TextureImage("player.png");
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
    }

    @Override
    public void update() {
        previousTime = currentTime;
        currentTime = System.currentTimeMillis();
        inputManager.update((float)getDeltaTime());
        System.out.println(avatar.getWorldLocation());
        avatar.movePlayerInBounds();
    }

    public void registerSpaceship(Spaceship spaceship){

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

}
