package ourGame;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import tage.*;

public class Player extends Spaceship {

    private OurGame ourGame;
    private float speed;

    public Player(OurGame ourGame){
        super(GameObject.root(), ourGame.getPlayerModel(), ourGame, new float[]{1f, 1f, 1f}, ourGame.getPlayerTexture());
        setLocalRotation(new Matrix4f().rotate((float)Math.toRadians(180d), 0f, 1f, 0f));
    }

    public float getSpeed(){
        return speed;
    }

    public void setSpeed(float speed){
        this.speed = speed;
    }

    public boolean isAvatar(){
        return this.equals(ourGame.getAvatar());
    }

    public void movePlayerInBounds(){
        Vector3f localLocation = getLocalLocation();
        if(localLocation.x > 63.18f) localLocation.x = 63.18f;
        else if(localLocation.x < -63.18f) localLocation.x = -63.18f;
        if(localLocation.z <- 34.6f) localLocation.z = -34.6f;
        else if(localLocation.z > 33.75f) localLocation.z = 33.75f;
        setLocalLocation(localLocation);
    }
}
