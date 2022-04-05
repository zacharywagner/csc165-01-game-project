package ourGame;

import tage.*;
import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;

import org.joml.*;

public class OrbitZoomAction extends AbstractInputAction {
    private OurGame game;
    private CameraOrbit3D orbitController;

    public OrbitZoomAction(OurGame g) {
        game = g;
    }

    @Override
    public void performAction(float time, Event e) {
        orbitController = game.getCameraController();

        float zoomAmount;

        if(e.getValue() < -0.5) {
            zoomAmount = -0.05f * time;
        }
        else if(e.getValue() > 0.5) {
            zoomAmount = 0.05f * time;
        }
        else {
            zoomAmount = 0.0f * time;
        }

        float currentCameraRadius = orbitController.getCameraRadius();

        if(currentCameraRadius + zoomAmount < 2.0f || currentCameraRadius + zoomAmount > 20.0f) {
            zoomAmount = 0.0f * time;
        }

        orbitController.setCameraRadius(currentCameraRadius + zoomAmount);
        orbitController.updateCameraPosition();
    }
}
