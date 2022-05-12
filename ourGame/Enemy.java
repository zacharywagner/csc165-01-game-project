package ourGame;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import tage.*;

public class Enemy extends Spaceship {

    private OurGame ourGame;
    private float speed = 12f;

    public Enemy(OurGame ourGame, Vector3f location){
        super(GameObject.root(), ourGame.getEnemyModel(), ourGame, new float[]{1f, 1f, 1f}, ourGame.getEnemyTexture());
        setLocalRotation(new Matrix4f().rotate((float)Math.toRadians(90d), 0f, 1f, 0f));
        setLocalLocation(location);
    }

    public float getSpeed(){
        return speed;
    }

    public void setSpeed(float speed){
        this.speed = speed;
    }
}