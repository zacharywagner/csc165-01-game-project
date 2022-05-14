package ourGame;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import tage.*;
import tage.audio.*;

public class Enemy extends Spaceship {

    private float speed = 12f;
    private long lastFireTime;
    private Sound laserSound;

    public Enemy(OurGame ourGame, Vector3f location, GameObject parent){
        super(parent, ourGame.getEnemyModel(), ourGame, new float[]{12f, 1f, 3.5f}, ourGame.getEnemyTexture(), false);
        setLocalRotation(new Matrix4f().rotate((float)Math.toRadians(90d), 0f, 1f, 0f));
        setLocalLocation(location);
        setHealth(50);
        setIsDead(true);
        onDeath();
        ourGame.registerEnemy(this);
    }

    public float getSpeed(){
        return speed;
    }

    public void setSpeed(float speed){
        this.speed = speed;
    }

    public void updateEnemy(){
        if(System.currentTimeMillis() - lastFireTime > 200 && !getIsDead() && !getOurGame().getBoss().getIsDead()){
            lastFireTime = System.currentTimeMillis();
            Vector3f position = getWorldLocation();
            Vector3f direction = getOurGame().getAvatar().getWorldLocation().sub(position);
            direction.normalize();
            position.z += 3f;
            getOurGame().getOrCreateProjectile(new Vector3f(0f, 0f, 1f), false, position, 36f);
            laserSound.play();
        }
    }

    public void inite(){
        laserSound = getOurGame().createSound(getOurGame().getAudioResource("laser9"), SoundType.SOUND_EFFECT, 50, false);
	    if(laserSound != null){
            //If your computer is slow reduce the number of sounds set their rolloff.
            if(getOurGame().getProjectileCount() < 100) laserSound.setRollOff(0.1f);
	    }
    }
    @Override
    public void onDeath(){
        super.onDeath();
        getRenderStates().disableRendering();
    }
}