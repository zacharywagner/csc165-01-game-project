package tage;

import org.joml.*;
import java.lang.Math;

/**
 * A camera orbit controller which follows a GameObject. It can be rotated,
 * pitched, and zoomed around the object independently of the object's
 * bearing.
 * 
 * @author Nicholas Appert
 */

public class CameraOrbit3D {
    private Camera camera;
    private GameObject target;
    private float cameraAzimuth;
    private float cameraElevation;
    private float cameraRadius;

    /**
     * Creates a CameraOrbit3D object which acts as an orbit camera controller
     * for the given GameObject.
     * 
     * 
     * @param cam The camera which will orbit the GameObject
     * @param target The GameObject to follow with the camera
     */
    public CameraOrbit3D(Camera cam, GameObject target) {
        camera = cam;
        this.target = target;
        cameraAzimuth = 0.0f;
        cameraElevation = 20.0f;
        cameraRadius = 20.0f;
        updateCameraPosition();
    }
    
    /**
     * Creates a CameraOrbit3D object which acts as an orbit camera controller
     * for the given GameObject with a custom set of starting values.
     * 
     * 
     * @param cam The camera which will orbit the GameObject
     * @param target The GameObject to follow with the camera
     * @param azm The starting azimuth (left and right) value
     * @param elv The starting elevation (up and down) value
     * @param rad The starting radius (zoom) value
     */
    public CameraOrbit3D(Camera cam, GameObject target, float azm, float elv, float rad) {
        camera = cam;
        this.target = target;
        cameraAzimuth = azm;
        cameraElevation = elv;
        cameraRadius = rad;
        updateCameraPosition();
    }

    /**
     * Updates the camera's position in reference to its target GameObject.
     * Takes the current azimuth, elevation and radius to calculate the
     * camera's next position in relation to the target GameObject.
     */
    public void updateCameraPosition() {
        Vector3f targetRot = target.getWorldForwardVector();
		double targetAngle = Math.toDegrees((double)targetRot.angleSigned(new Vector3f(0,0,-1), new Vector3f(0,1,0)));
		float totalAz = cameraAzimuth - (float)targetAngle;
		double theta = Math.toRadians(totalAz);	// rotation around target
		double phi = Math.toRadians(cameraElevation);	// altitude angle
		float x = cameraRadius * (float)(Math.cos(phi) * Math.sin(theta));
		float y = cameraRadius * (float)(Math.sin(phi));
		float z = cameraRadius * (float)(Math.cos(phi) * Math.cos(theta));
		camera.setLocation(new Vector3f(x,y,z).add(target.getWorldLocation()));
		camera.lookAt(target);
	}

    /** Returns the camera's current azimuth. */
    public float getCameraAzimuth() {
        return cameraAzimuth;
    }

    /** Returns the camera's current elevation. */
    public float getCameraElevation() {
        return cameraElevation;
    }

    /** Returns the camera's current radius. */
    public float getCameraRadius() {
        return cameraRadius;
    }

    /** Sets camera's current azimuth to the specified value. */
    public void setCameraAzimuth(float az) {
        cameraAzimuth = az;
    }

    /** Sets camera's current elevation to the specified value. */
    public void setCameraElevation(float el) {
        cameraElevation = el;
    }

    /** Sets camera's current radius to the specified value. */
    public void setCameraRadius(float r) {
        cameraRadius = r;
    }
}