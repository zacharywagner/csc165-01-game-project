package ourGame;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import tage.*;

/*
{\__/}
(●_●)
(  >🌮  Want a taco?
*/

public class Player extends Spaceship {

    private float speed;

    public Player(OurGame ourGame){
        super(GameObject.root(), ourGame.getPlayerModel(), ourGame, new float[]{1f, 20f, 1f}, ourGame.getPlayerTexture(), true);
        setLocalRotation(new Matrix4f().rotate((float)Math.toRadians(180d), 0f, 1f, 0f));
        System.out.println(getPhysicsObject().getUID());
    }

    public float getSpeed(){
        return speed;
    }

    public void setSpeed(float speed){
        this.speed = speed;
    }

    public boolean isAvatar(){
        return this.equals(getOurGame().getAvatar());
    }

    public void movePlayerInBounds(){
        Vector3f localLocation = getLocalLocation();
        if(localLocation.x > 63.18f) localLocation.x = 63.18f;
        else if(localLocation.x < -63.18f) localLocation.x = -63.18f;
        if(localLocation.z <- 34.6f) localLocation.z = -34.6f;
        else if(localLocation.z > 33.75f) localLocation.z = 33.75f;
        setLocalLocation(localLocation);
    }

    @Override
    public void onDeath(){
        System.out.println("You died!");
        setHealth(10);
        setIsDead(false);
    }
}
