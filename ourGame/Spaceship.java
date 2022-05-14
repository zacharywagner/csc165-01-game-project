package ourGame;

import org.joml.Vector3f;

import tage.*;
import tage.physics.*;
import tage.audio.*;

public class Spaceship extends GameObject{
    private OurGame ourGame;
    private int uid;
    private boolean isFriend;
    private int health = 1;
    private Sound sound;
    private long diedOn;

    private boolean isDead = false;

    public int getHealth(){
        return health;
    }

    public int getUid(){
        return uid;
    }

    protected OurGame getOurGame(){
        return ourGame;
    }

    public boolean getIsFriend(){
        return isFriend;
    }

    public void setHealth(int health){
        this.health = health;
    }

    public boolean getIsDead(){
        return isDead;
    }

    public void setIsDead(boolean dead){
        isDead = dead;
    }

    public long getDiedOn(){
        return diedOn;
    }

    /*
    public void playSound(){
        Vector3f location = getWorldLocation();
        if(sound != null){
            sound.setLocation(location);
            if(!isFriend){
                sound.setVolume(100);
            }
            else{
                sound.setVolume(50);
            }
            sound.play();
	    }
    }
    */

    public Spaceship(GameObject parent, ObjShape objShape, OurGame ourGame, float[] size, TextureImage textureImage, boolean isFriend){
        super(parent, objShape, textureImage);
        this.ourGame = ourGame;
        PhysicsEngine physicsEngine = ourGame.getPhysicsEngine();
        uid = physicsEngine.nextUID();
        float values[] = new float[16];
        double[] transform = OurGame.toDoubleArray(getLocalTranslation().get(values));
        PhysicsObject physicsObject = ourGame.getPhysicsEngine().addBoxObject(uid, 1f, transform, size);
        physicsObject.setBounciness(0f);
        physicsObject.setFriction(0f);
        setPhysicsObject(physicsObject);
        ourGame.registerSpaceship(this);
        this.isFriend = isFriend;
        /*
        sound = ourGame.createSound(ourGame.getAudioResource("laser9"), SoundType.SOUND_EFFECT, 100, false);
	    if(sound != null){
            //If your computer is slow reduce the number of sounds set their rolloff.
            if(ourGame.getProjectileCount() < 100) sound.setRollOff(0.1f);
        }
        */
    }

    public void dealDamage(int damage){
        if(!isDead){
            health -= damage;
            if(health <= 0){
                onDeath();    
            }
        }
    }

    public void onDeath(){
        isDead = true;
        diedOn = System.currentTimeMillis();
    }
}
