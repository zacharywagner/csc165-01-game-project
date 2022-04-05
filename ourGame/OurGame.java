package ourGame;

import tage.*;
import tage.input.action.AbstractInputAction;
import tage.input.InputManager;
import tage.input.IInputManager.INPUT_ACTION_TYPE;
import tage.networking.IGameConnection.ProtocolType;
import tage.shapes.*;

import java.io.IOException;
import java.lang.Math;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.joml.*;

import net.java.games.input.*;
import net.java.games.input.Component.Identifier.*;

public class OurGame extends VariableFrameRateGame {
    private static Engine engine;
    private InputManager im;
    public static Engine getEngine() { return engine; }

    private double elapsedTime, prevTime;

    // GameObject declarations
    private GameObject avatar;
    // ObjShape declarations
    private ObjShape avatarS, ghostS;
    // TextureImage declarations
    private TextureImage avatartx, ghosttx;
    // Light declarations
    private Light light1;
    // Skybox
    private int fluffyClouds, lakeIslands;
    // Orbit camera controller
    private CameraOrbit3D orbitController;

    // Networking
    private GhostManager gm;
    private String serverAddress;
    private int serverPort;
    private ProtocolType serverProtocol;
    private ProtocolClient protocolClient;
    private boolean isConnected = false;

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
        fluffyClouds = (engine.getSceneGraph()).loadCubeMap("fluffyClouds");
        lakeIslands = (engine.getSceneGraph()).loadCubeMap("lakeIslands");
        (engine.getSceneGraph()).setActiveSkyBoxTexture(fluffyClouds);
        (engine.getSceneGraph()).setSkyBoxEnabled(true);
    }

    @Override
    public void loadShapes() {
        avatarS = new ImportedModel("dolphinHighPoly.obj");
        ghostS = new Sphere();
        // ghostS = new ImportedModel("dolphinHighPoly.obj");
    }
    
    @Override
    public void loadTextures() {
        avatartx = new TextureImage("Dolphin_HighPolyUV.png");
        ghosttx = new TextureImage("stripe.png");
    }

    @Override
    public void buildObjects() {
        // build avatar
        avatar = new GameObject(GameObject.root(), avatarS, avatartx);
        avatar.setLocalTranslation((new Matrix4f()).translation(0,0,0));
        avatar.setLocalScale((new Matrix4f()).scaling(3.0f));
    }

    @Override
    public void initializeGame() {
        // setup window
        (engine.getRenderSystem()).setWindowDimensions(1900,1000);

        // setup light
        Light.setGlobalAmbient(0.5f, 0.5f, 0.5f);
        light1 = new Light();
        light1.setLocation(new Vector3f(5.0f, 10.0f, 2.0f));
        (engine.getSceneGraph()).addLight(light1);

        // setup camera
        Camera cam = (engine.getRenderSystem()).getViewport("MAIN").getCamera();
        orbitController = new CameraOrbit3D(cam, avatar);

        // setup inputs
        im = engine.getInputManager();
        ArrayList<Controller> controllers = im.getControllers();

        // movement actions
        BackNForthAction backNForthAction = new BackNForthAction(this, protocolClient);
        TurnAction turnAction = new TurnAction(this);
        OrbitAzimuthAction orbitAzimuthAction = new OrbitAzimuthAction(this);
        OrbitElevationAction orbitElevationAction = new OrbitElevationAction(this);
        OrbitZoomAction orbitZoomAction = new OrbitZoomAction(this);

        for(Controller c : controllers) {
            if(c.getType() == Controller.Type.GAMEPAD || c.getType() == Controller.Type.STICK) {
                // controller backnforth
                im.associateAction(
                    c,
                    net.java.games.input.Component.Identifier.Axis.Y,
                    backNForthAction,
                    INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN
                );
                // controller turn
                im.associateAction(
                    c,
                    net.java.games.input.Component.Identifier.Axis.X,
                    turnAction,
                    INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN
                );
                im.associateAction(
                    c,
                    net.java.games.input.Component.Identifier.Axis.RX,
                    orbitAzimuthAction,
                    INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN
                );
                im.associateAction(
                    c,
                    net.java.games.input.Component.Identifier.Axis.RY,
                    orbitElevationAction,
                    INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN
                );
                im.associateAction(
                    c,
                    net.java.games.input.Component.Identifier.Axis.Z,
                    orbitZoomAction,
                    INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN
                );
            }
        }

        setupNetworking();
    }

    @Override
    public void update() {
        elapsedTime = System.currentTimeMillis() - prevTime;
        prevTime = System.currentTimeMillis();

        orbitController.updateCameraPosition();

        im.update((float)elapsedTime);
        processNetworking((float)elapsedTime);
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
}
