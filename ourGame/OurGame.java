package ourGame;

import tage.*;
import tage.input.action.AbstractInputAction;
import tage.input.InputManager;
import tage.input.IInputManager.INPUT_ACTION_TYPE;
import tage.networking.IGameConnection.ProtocolType;
import tage.physics.PhysicsObject;
import tage.shapes.*;
import tage.shapes.AnimatedShape.EndType;

import java.awt.event.*;
import java.io.IOException;
import java.lang.Math;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.*;
import java.util.*;

import org.joml.*;

import net.java.games.input.*;
import net.java.games.input.Component.Identifier.*;

import tage.physics.PhysicsEngine;
import tage.physics.PhysicsObject;
import tage.physics.PhysicsEngineFactory;
import tage.physics.JBullet.*;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.collision.dispatch.CollisionObject;

public class OurGame extends VariableFrameRateGame {
    private static Engine engine;
    private InputManager im;
    public static Engine getEngine() { return engine; }

    private double elapsedTime, prevTime;

    // GameObject declarations
    private GameObject avatar, mechMan;
    // ObjShape declarations
    private ObjShape avatarS, ghostS;
    // AnimatedShape declarations
    private AnimatedShape mechManS;
    // TextureImage declarations
    private TextureImage avatartx, ghosttx, mechMantx;
    // Light declarations
    private Light light1;
    // Skybox
    private int redSpace;
    // Orbit camera controller
    private CameraOrbit3D orbitController;

    // Networking
    private GhostManager gm;
    private String serverAddress;
    private int serverPort;
    private ProtocolType serverProtocol;
    private ProtocolClient protocolClient;
    private boolean isConnected = false;

    /*==================================================
      Terrain
      ==================================================*/
    private GameObject terrainGameObject;
    private ObjShape terrainObjShape;
    private TextureImage terrainTextureImage;
    private TextureImage heightMapTextureImage;

    /*==================================================
      Scripting
      ==================================================*/
    private ScriptEngine jsEngine;
    private File initScript;
    private long fileLastModifiedTime = 0;

    //
    // Physics
    //
    private PhysicsEngine physicsEngine;
    private boolean running = true;
    private PhysicsObject playerPhysicsObject;
    private float vals[] = new float[16];

    //
    // Enemies
    //

    private GameObject rammerGameObject;
    private ImportedModel rammerImportedModel;
    private TextureImage rammerTextureImage;
    private PhysicsObject rammerPhysicsObject;


    //
    //  PhysicsObjects
    //

    private HashMap<Integer, GameObject> physicsObjects;
    
    public OurGame(String serverAddress, int serverPort) {
        super();
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.serverProtocol = ProtocolType.UDP;
    }
    public static void main(String[] args) {
        OurGame game = new OurGame(args[0], Integer.parseInt(args[1]));
        engine = new Engine(game);
        game.initializeSystem();
        game.game_loop();
    }
    
    @Override
    public void loadSkyBoxes() {
        redSpace = (engine.getSceneGraph()).loadCubeMap("redSpace");
        (engine.getSceneGraph()).setActiveSkyBoxTexture(redSpace);
        (engine.getSceneGraph()).setSkyBoxEnabled(true);
    }

    @Override
    public void loadShapes() {
        avatarS = new ImportedModel("playership.obj");
        ghostS = new ImportedModel("playership.obj");

        //Load mechMan shape
        mechManS = new AnimatedShape("mechman.rkm", "mechman.rks");
        mechManS.loadAnimation("KICK", "mech_kick.rka");
        mechManS.loadAnimation("SUMMON", "summon.rka");
        mechManS.loadAnimation("DEAD", "dead.rka");
    
        //Load terrain shape(s).
        terrainObjShape = new TerrainPlane(2048);

        //Load the enemies' models.
        rammerImportedModel = new ImportedModel("rammer.obj");
    }
    
    @Override
    public void loadTextures() {
        avatartx = new TextureImage("playership.png");
        ghosttx = new TextureImage("playership.png");
        mechMantx = new TextureImage("mechman.png");

        //Load terrain texture image(s).
        terrainTextureImage = new TextureImage("mountains1.png");
        heightMapTextureImage = new TextureImage("mountains1.png");

        //Load the enemies' texture images.
        rammerTextureImage = new TextureImage("rammer.png");
    }

    @Override
    public void buildObjects() {
        // build avatar
        avatar = new GameObject(GameObject.root(), avatarS, avatartx);
        avatar.setLocalTranslation((new Matrix4f()).translation(0,0,0));
        avatar.setLocalScale((new Matrix4f()).scaling(0.5f));

        // build mechman
        mechMan = new GameObject(GameObject.root(), mechManS, mechMantx);
        mechMan.setLocalTranslation((new Matrix4f()).translation(0,0,10));
        mechManS.playAnimation("KICK", 0.5f, EndType.LOOP, 0);

        //Build terrain game object(s).
        terrainGameObject = new GameObject(GameObject.root(), terrainObjShape, terrainTextureImage);
        terrainGameObject.setLocalTranslation(new Matrix4f().translation(0f, 0f, 0f));
        terrainGameObject.setLocalScale(new Matrix4f().scaling(1f, 1f, 1f));
        terrainGameObject.setHeightMap(heightMapTextureImage);

        //Build an enemy as a test.
        rammerGameObject = new GameObject(GameObject.root(), rammerImportedModel, rammerTextureImage);
        rammerGameObject.setLocalTranslation(new Matrix4f().translation(0f, 0f, 0f));

    }

    @Override
    public void initializeGame() {
        // Initialize the JavaScript scripting engine.
        ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
        List<ScriptEngineFactory> scriptEngineFactories = scriptEngineManager.getEngineFactories();
        jsEngine = scriptEngineManager.getEngineByName("js");
        
        // Get the init.js JavaScript file and initialize parameters.
        initScript = new File("assets/scripts/init.js");
        this.runScript(initScript);

        Vector3f terrainLocalScale = (Vector3f)(jsEngine.get("terrainLocalScale"));
        terrainGameObject.setLocalScale(new Matrix4f().scaling(terrainLocalScale));
        Vector3f terrainLocalLocation = (Vector3f)(jsEngine.get("terrainLocalLocation"));
        System.out.println(terrainLocalLocation.y);
        terrainGameObject.setLocalLocation(terrainLocalLocation);

        // setup window
        (engine.getRenderSystem()).setWindowDimensions(1900,1000);

        // setup light
        Light.setGlobalAmbient(0.5f, 0.5f, 0.5f);
        (engine.getSceneGraph()).addLight((Light)jsEngine.get("light"));

        // setup camera
        Camera cam = (engine.getRenderSystem()).getViewport("MAIN").getCamera();
        orbitController = new CameraOrbit3D(
                                  cam,
                                  avatar,
                                  (float)(((Double)(jsEngine.get("startingAzimuth"))).floatValue()),
                                  (float)(((Double)(jsEngine.get("startingElevation"))).floatValue()),
                                  (float)(((Double)(jsEngine.get("startingRadius"))).floatValue())
                                  );

        // setup inputs
        im = engine.getInputManager();
        ArrayList<Controller> controllers = im.getControllers();

        // networking
        gm = new GhostManager(this);
        setupNetworking();

        // keyboard actions
        LeftAction leftAction = new LeftAction(this, protocolClient);
        RightAction rightAction = new RightAction(this, protocolClient);
        FwdAction fwdAction = new FwdAction(this, protocolClient);
        BackAction backAction = new BackAction(this, protocolClient);

        // controller actions
        BackNForthAction backNForthAction = new BackNForthAction(this, protocolClient);
        LeftNRightAction leftNRightAction = new LeftNRightAction(this, protocolClient);
        OrbitAzimuthAction orbitAzimuthAction = new OrbitAzimuthAction(this);
        OrbitElevationAction orbitElevationAction = new OrbitElevationAction(this);
        OrbitZoomAction orbitZoomAction = new OrbitZoomAction(this);
        SendCloseConnectionPacketAction sendCloseConnectionPacketAction = new SendCloseConnectionPacketAction();

        for(Controller c : controllers) {
            if(c.getType() == Controller.Type.KEYBOARD) {
                // keyboard left
                im.associateAction(
                    c,
                    net.java.games.input.Component.Identifier.Key.LEFT,
                    leftAction,
                    INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN
                );
                // keyboard right
                im.associateAction(
                    c,
                    net.java.games.input.Component.Identifier.Key.RIGHT,
                    rightAction,
                    INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN
                );
                // keyboard fwd
                im.associateAction(
                    c,
                    net.java.games.input.Component.Identifier.Key.UP,
                    fwdAction,
                    INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN
                );
                // keyboard back
                im.associateAction(
                    c,
                    net.java.games.input.Component.Identifier.Key.DOWN,
                    backAction,
                    INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN
                );
            }
            if(c.getType() == Controller.Type.GAMEPAD || c.getType() == Controller.Type.STICK) {
                // controller backnforth
                im.associateAction(
                    c,
                    net.java.games.input.Component.Identifier.Axis.Y,
                    backNForthAction,
                    INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN
                );
                // controller leftnright
                im.associateAction(
                    c,
                    net.java.games.input.Component.Identifier.Axis.X,
                    leftNRightAction,
                    INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN
                );
                // controller orbit azimuth
                
                im.associateAction(
                    c,
                    net.java.games.input.Component.Identifier.Axis.RX,
                    orbitAzimuthAction,
                    INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN
                );
                
                // controller orbit elevation
                im.associateAction(
                    c,
                    net.java.games.input.Component.Identifier.Axis.RY,
                    orbitElevationAction,
                    INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN
                );
                // controller orbit zoom
                
                im.associateAction(
                    c,
                    net.java.games.input.Component.Identifier.Axis.Z,
                    orbitZoomAction,
                    INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN
                );
                
                im.associateAction(
                    c,
                    net.java.games.input.Component.Identifier.Button._1,
                    sendCloseConnectionPacketAction,
                    INPUT_ACTION_TYPE.ON_PRESS_ONLY
                );
            }
        }

        //PHYSICS
        physicsObjects = new HashMap<Integer, GameObject>();
        String engine = "tage.physics.JBullet.JBulletPhysicsEngine";
        float[] gravity = {0f, 0f, 0f};
        physicsEngine = PhysicsEngineFactory.createPhysicsEngine(engine);
        physicsEngine.initSystem();
        physicsEngine.setGravity(gravity);

        //
        //  These are for testing purposes. Enemies will be instantiated with a script in future sprints.
        //

        //Addin a collider to the player!
        float vals[] = new float[16];
        int uid = physicsEngine.nextUID();
        double[] transform = toDoubleArray(avatar.getLocalTranslation().get(vals));
        playerPhysicsObject = physicsEngine.addBoxObject(uid, 1f, transform, new float[]{1f, 1f, 1f});
        playerPhysicsObject.setBounciness(0f);
        playerPhysicsObject.setFriction(0f);
        avatar.setPhysicsObject(playerPhysicsObject);
        physicsObjects.put(uid, avatar);


        //Adding a collider to the test enemy.
        uid = physicsEngine.nextUID();
        transform = toDoubleArray(rammerGameObject.getLocalTranslation().get(vals));
        rammerPhysicsObject = physicsEngine.addBoxObject(uid, 1f, transform, new float[]{8f, 1f, 4f});
        rammerPhysicsObject.setBounciness(0f);
        rammerPhysicsObject.setFriction(0f);
        rammerGameObject.setPhysicsObject(rammerPhysicsObject);
        physicsObjects.put(uid, rammerGameObject);
    }

    @Override
    public void update() {
        // update elapsed time
        elapsedTime = System.currentTimeMillis() - prevTime;
        prevTime = System.currentTimeMillis();

        // build HUD
        String playerHealthStr = (jsEngine.get("playerHealth")).toString();
        String display = "Player Health: = " + playerHealthStr;

        double red = ((Double)(jsEngine.get("red"))).floatValue();
        double green = ((Double)(jsEngine.get("green"))).floatValue();
        double blue = ((Double)(jsEngine.get("blue"))).floatValue();

        Vector3f hudColor = new Vector3f((float)red, (float)green, (float)blue);

        float mainRelativeLeft = engine.getRenderSystem().getViewport("MAIN").getRelativeLeft();
        float mainRelativeBottom = engine.getRenderSystem().getViewport("MAIN").getRelativeBottom();
        float mainActualWidth = engine.getRenderSystem().getViewport("MAIN").getActualWidth();
        float mainActualHeight = engine.getRenderSystem().getViewport("MAIN").getActualHeight();

        (engine.getHUDmanager()).setHUD1(
            display,
            hudColor,
            (int)(mainRelativeLeft * mainActualWidth) + 5,
            (int)(mainRelativeBottom * mainActualHeight) + 5
            );

        // update camera
        orbitController.updateCameraPosition();

        // update scripting values
        long modTime = initScript.lastModified();
        if(modTime > fileLastModifiedTime) {
            fileLastModifiedTime = modTime;
            this.runScript(initScript);
        }

        // update inputs
        im.update((float)elapsedTime);

        // update animation
        mechManS.updateAnimation();

        //
        // UPDATE PHYSICS
        //

        /*
        The physics code NEEDS to be cleaned up when we get the time.
        Works right now but won't work in the long run.
        */

        double[] transform = toDoubleArray(avatar.getLocalTranslation().get(vals));
        avatar.getPhysicsObject().setTransform(transform);
        //System.out.println(avatar.getPhysicsObject().getTransform());
        //avatar.getPhysicsObject().setSleepThresholds(0f, 0f);
        avatar.getPhysicsObject().setLinearVelocity(new float[]{0f, 1f, 0f});

        Vector3f loc = rammerGameObject.getLocalLocation();
        loc.x += 0.01f;
        rammerGameObject.setLocalLocation(loc);
        transform = toDoubleArray(rammerGameObject.getLocalTranslation().get(vals));
        rammerGameObject.getPhysicsObject().setTransform(transform);
        //rammerGameObject.getPhysicsObject().setSleepThresholds(0f, 0f);
        rammerGameObject.getPhysicsObject().setLinearVelocity(new float[]{0f, 1f, 0f});

        if(running){
            Matrix4f mat = new Matrix4f();
            Matrix4f mat2 = new Matrix4f().identity();
            checkForCollisions();
            physicsEngine.update((float)elapsedTime);
            for (GameObject go:engine.getSceneGraph().getGameObjects()){
                if (go.getPhysicsObject() != null){ 
                    //mat.set(toFloatArray(go.getPhysicsObject().getTransform()));
                    //mat2.set(3,0,mat.m30());
                    //mat2.set(3,1,0f);
                    //mat2.set(3,2,mat.m32());
                    //go.setLocalTranslation(mat2);
                    go.getPhysicsObject().setLinearVelocity(new float[]{0f, 1f, 0f});
                    go.getPhysicsObject().setAngularVelocity(new float[]{0f, 1f, 0f});
                } 
            }
        }

        transform = toDoubleArray(avatar.getLocalTranslation().get(vals));
        avatar.getPhysicsObject().setTransform(transform);
        //System.out.println(avatar.getPhysicsObject().getTransform());
        //avatar.getPhysicsObject().setSleepThresholds(0f, 0f);

        loc = rammerGameObject.getLocalLocation();
        loc.x += 0.001f;
        rammerGameObject.setLocalLocation(loc);
        transform = toDoubleArray(rammerGameObject.getLocalTranslation().get(vals));
        rammerGameObject.getPhysicsObject().setTransform(transform);
        //rammerGameObject.getPhysicsObject().setSleepThresholds(0f, 0f);


        //PHYSICS WITH TERRAIN
        Vector3f location = avatar.getWorldLocation();
        float height = terrainGameObject.getHeight(location.x, location.z);
        height += terrainGameObject.getWorldLocation().y;
        if(avatar.getWorldLocation().y < height){
            System.out.println("BOOM! The spaceship is colliding with the terrain.");
        }

        //
        // PHYSICS UPDATED
        //

        // update networking
        processNetworking((float)elapsedTime);
    }

    // SHOWCASE ANIMATIONS
    @Override
    public void keyPressed(KeyEvent e) {
        switch(e.getKeyCode()) {
            case KeyEvent.VK_1:
                mechManS.stopAnimation();
                mechManS.playAnimation("KICK", 0.5f, EndType.LOOP, 0);
                break;
            case KeyEvent.VK_2:
                mechManS.stopAnimation();
                mechManS.playAnimation("SUMMON", 0.5f, EndType.LOOP, 0);
                break;
            case KeyEvent.VK_3:
                mechManS.stopAnimation();
                mechManS.playAnimation("DEAD", 0.5f, EndType.PAUSE, 0);
                break;
        }
            super.keyPressed(e);
    }
    
    public CameraOrbit3D getCameraController() {
        return orbitController;
    }

    public GameObject getAvatar() {
        return avatar;
    }

    public ObjShape getGhostShape() {
        return ghostS;
    }

    public TextureImage getGhostTexture() {
        return ghosttx;
    }

    public GhostManager getGhostManager() {
        return gm;
    }

    public Vector3f getPlayerPosition() {
        return avatar.getWorldLocation();
    }

    public double getPlayerSpeed() {
        return ((Double)(jsEngine.get("playerSpeed"))).floatValue();
    }

    public void setIsConnected(boolean set) {
        this.isConnected = set;
    }

    private void setupNetworking() {
        isConnected = false;
        try {
            protocolClient = new ProtocolClient(InetAddress.getByName(serverAddress), serverPort, serverProtocol, this);
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

    private void processNetworking(float elapsTime) {
        if(protocolClient != null) {
            protocolClient.processPackets();
        }
    }


    private class SendCloseConnectionPacketAction extends AbstractInputAction {
        @Override
		public void performAction(float time, net.java.games.input.Event evt) {
            if(protocolClient != null && isConnected == true) {
                protocolClient.sendByeMessage();
			}
		}
	}

    
	/*==================================================
	  Scripting
	  ==================================================*/
	private void runScript(File scriptFile){ 
		try{ 
			FileReader fileReader = new FileReader(scriptFile);
		    jsEngine.eval(fileReader);
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

    //
    // PHYSICS
    //

    private void checkForCollisions()
    { 
        //System.out.println("Checking for collisions.");
        com.bulletphysics.dynamics.DynamicsWorld dynamicsWorld;
        com.bulletphysics.collision.broadphase.Dispatcher dispatcher;
        com.bulletphysics.collision.narrowphase.PersistentManifold manifold;
        com.bulletphysics.dynamics.RigidBody object1, object2;
        com.bulletphysics.collision.narrowphase.ManifoldPoint contactPoint;
        dynamicsWorld = ((JBulletPhysicsEngine)physicsEngine).getDynamicsWorld();
        dispatcher = dynamicsWorld.getDispatcher();
        int manifoldCount = dispatcher.getNumManifolds();
        for (int i=0; i<manifoldCount; i++)
            { manifold = dispatcher.getManifoldByIndexInternal(i);
            object1 =
            (com.bulletphysics.dynamics.RigidBody)manifold.getBody0();
            object2 =
            (com.bulletphysics.dynamics.RigidBody)manifold.getBody1();
            JBulletPhysicsObject obj1 =
            JBulletPhysicsObject.getJBulletPhysicsObject(object1);
            JBulletPhysicsObject obj2 =
            JBulletPhysicsObject.getJBulletPhysicsObject(object2);
            for (int j = 0; j < manifold.getNumContacts(); j++)
            { 
            contactPoint = manifold.getContactPoint(j);
                if (contactPoint.getDistance() < 0.0f)
                { 
                    System.out.println("---- hit between " + obj1 + " and " + obj2);
                    GameObject go = physicsObjects.get(obj1.getUID());
                    if(go != null){
                        System.out.println("The game object " + go.toString() + " was involved with the collision!");
                    }
                    go = physicsObjects.get(obj2.getUID());
                    if(go != null){
                        System.out.println("The game object " + go.toString() + " was involved with the collision!");
                    }
                    break;
                } 
            } 
        } 
    }

    //UTILITY

    private float[] toFloatArray(double[] arr)
    { if (arr == null) return null;
    int n = arr.length;
    float[] ret = new float[n];
    for (int i = 0; i < n; i++)
    { ret[i] = (float)arr[i];
    }
    return ret;
    }

    private double[] toDoubleArray(float[] arr)
    { if (arr == null) return null;
    int n = arr.length;
    double[] ret = new double[n];
    for (int i = 0; i < n; i++)
    { ret[i] = (double)arr[i];
    }
    return ret;
    }
}
