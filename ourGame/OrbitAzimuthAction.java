package ourGame;

import tage.*;
import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;

import org.joml.*;

public class OrbitAzimuthAction extends AbstractInputAction {
    private OurGame game;
    private CameraOrbit3D orbitController;

    public OrbitAzimuthAction(OurGame g) {
        game = g;
    }

    @Override
    public void performAction(float time, Event e) {
        orbitController = game.getCameraController();

        float rotAmount;

        if (e.getValue() < -0.5) {
            rotAmount = -0.1f * time;
        }
        else if (e.getValue() > 0.5) {
            rotAmount = 0.1f * time;
        }
        else {
            rotAmount = 0.0f * time;
        }

        float currentCameraAzimuth = orbitController.getCameraAzimuth();
        orbitController.setCameraAzimuth((currentCameraAzimuth + rotAmount) % 360);
        orbitController.updateCameraPosition();
    }
}
