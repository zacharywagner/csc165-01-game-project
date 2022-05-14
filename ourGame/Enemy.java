package ourGame;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import tage.*;

public class Enemy extends Spaceship {

    private float speed = 12f;
    private long lastFireTime;

    public Enemy(OurGame ourGame, Vector3f location, GameObject parent){
        super(parent, ourGame.getEnemyModel(), ourGame, new float[]{12f, 1f, 3.5f}, ourGame.getEnemyTexture(), false);
        setLocalRotation(new Matrix4f().rotate((float)Math.toRadians(90d), 0f, 1f, 0f));
        setLocalLocation(location);
        ourGame.registerEnemy(this);
    }

    public float getSpeed(){
        return speed;
    }

    public void setSpeed(float speed){
        this.speed = speed;
    }

    public void updateEnemy(){
        if(System.currentTimeMillis() - lastFireTime > 50){
            lastFireTime = System.currentTimeMillis();
            Vector3f position = getWorldLocation();
            Vector3f direction = getOurGame().getAvatar().getWorldLocation().sub(position);
            direction.normalize();
            position.z += 3f;
            getOurGame().getOrCreateProjectile(direction, false, position, 36f);
        }
    }
}