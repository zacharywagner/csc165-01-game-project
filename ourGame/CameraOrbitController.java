package a2;

import tage.*;
import tage.input.*;
import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;
import org.joml.*;
import java.lang.Math;
import net.java.games.input.*;
import java.util.ArrayList;


import javax.swing.InputMap;

public class CameraOrbitController
{	private Engine engine;
	private Camera camera;		//the camera being controlled
	private GameObject avatar;	//the target avatar the camera looks at
	private float cameraAzimuth;	//rotation of camera around target Y axis
	private float cameraElevation;	//elevation of camera above target
	private float cameraRadius;	//distance between camera and target

	public CameraOrbitController(Camera cam, GameObject av, String gpName, Engine e)
	{	engine = e;
		camera = cam;
		avatar = av;
		cameraAzimuth = 0.0f;		// start from BEHIND and ABOVE the target
		cameraElevation = 20.0f;	// elevation is in degrees
		cameraRadius = 2.0f;		// distance from camera to avatar
		setupInputs(gpName);
		updateCameraPosition();
	}

	private void setupInputs(String gp)
	{	OrbitAzimuthAction azmAction = new OrbitAzimuthAction();
		OrbitRadiusAction orbAction = new OrbitRadiusAction();
		OrbitElevationAction eleAction = new OrbitElevationAction();

		InputManager im = engine.getInputManager();
		if(gp != null){
			}

		ArrayList<Controller> controllers = im.getControllers();
        for (Controller controller : controllers){
            if (controller.getType() == Controller.Type.KEYBOARD){	
                im.associateAction(controller, net.java.games.input.Component.Identifier.Key.Y, orbAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
                im.associateAction(controller, net.java.games.input.Component.Identifier.Key.G, orbAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
                im.associateAction(controller, net.java.games.input.Component.Identifier.Key.U, eleAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
                im.associateAction(controller, net.java.games.input.Component.Identifier.Key.J, eleAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
                im.associateAction(controller, net.java.games.input.Component.Identifier.Key.H, azmAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
                im.associateAction(controller, net.java.games.input.Component.Identifier.Key.K, azmAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

            }
            else if(controller.getType() == Controller.Type.GAMEPAD){
				im.associateAction(controller, net.java.games.input.Component.Identifier.Axis.RX, azmAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
				im.associateAction(controller, net.java.games.input.Component.Identifier.Axis.Z, orbAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
				im.associateAction(gp, net.java.games.input.Component.Identifier.Axis.RY, eleAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
            }
		}
	}

	// Updates the camera position by computing its azimuth, elevation, and distance 
	// relative to the target in spherical coordinates, then converting those spherical 
	// coords to world Cartesian coordinates and setting the camera position from that.

	public void updateCameraPosition()
	{	Vector3f avatarRot = avatar.getWorldForwardVector();
		double avatarAngle = Math.toDegrees((double)avatarRot.angleSigned(new Vector3f(0,0,-1), new Vector3f(0,1,0)));
		float totalAz = cameraAzimuth - (float)avatarAngle;
		double theta = Math.toRadians(totalAz);	// rotation around target
		double phi = Math.toRadians(cameraElevation);	// altitude angle
		float x = cameraRadius * (float)(Math.cos(phi) * Math.sin(theta));
		float y = cameraRadius * (float)(Math.sin(phi));
		float z = cameraRadius * (float)(Math.cos(phi) * Math.cos(theta));
		camera.setLocation(new Vector3f(x,y,z).add(avatar.getWorldLocation()));
		camera.lookAt(avatar);
	}

	private class OrbitAzimuthAction extends AbstractInputAction
	{	public void performAction(float time, Event event)
		{	float rotAmount = 0f;
			if(event.getComponent().toString() == "H"){
				rotAmount = -20f;
			}
			else if(event.getComponent().toString() == "K"){
				rotAmount = 20f;
			}
			else{
				if (event.getValue() < -0.2)
				{	rotAmount=-20f;
				}
				else
				{	if (event.getValue() > 0.2)
					{	rotAmount=20f;
					}
				}
			}
			rotAmount *= time / 1000f;
			cameraAzimuth += rotAmount;
			cameraAzimuth = cameraAzimuth % 360;

			updateCameraPosition();
		}
	}
  
	// private class OrbitRadiusAction extends AbstractInputAction
	// (needs to be written)
	private class OrbitRadiusAction extends AbstractInputAction{
		public void performAction(float time, Event event){
			float radiusAmount = 0f;
			
			if(event.getComponent().toString() == "G"){
				radiusAmount = 2f;
			}
			else if(event.getComponent().toString() == "Y"){
				radiusAmount = -2f;
			}
			else{
				if (event.getValue() > 0.2f)
				{	
					radiusAmount=2f;
				}
				else
				{	
					if (event.getValue() < -0.2f)
					{	radiusAmount=-2;
					}
					else
					{	radiusAmount=0.0f;
					}
				}
			}
			
			radiusAmount *= time / 1000f;
			cameraRadius += radiusAmount;
			if(cameraRadius < 1f) cameraRadius = 1f;
			if(cameraRadius > 10) cameraRadius = 10;

			updateCameraPosition();
		}
	}
  
	// private class OrbitElevationAction extends AbstractInputAction
	// (needs to be written)
	private class OrbitElevationAction extends AbstractInputAction{
		public void performAction(float time, Event event){
			float elevationAmount = 0f;
			
			if(event.getComponent().toString() == "U"){
				elevationAmount = -20f;
			}
			else if(event.getComponent().toString() == "J"){
				elevationAmount = 20f;
			}
			else{
				if (event.getValue() < -0.2)
				{	
					elevationAmount=-20;
				}
				else
				{	
					if (event.getValue() > 0.2)
					{	elevationAmount=20;
					}
					else
					{	elevationAmount=0.0f;
					}
				}
			}
			

			elevationAmount	 *= time / 1000f;
			cameraElevation += elevationAmount;
			if(cameraElevation < 0f) cameraElevation = 0f;
			if(cameraElevation > 60f) cameraElevation = 60f;

			updateCameraPosition();
		}
	}
}
