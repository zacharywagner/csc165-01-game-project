package ourGame;

import com.jogamp.openal.sound3d.Vec3f;

import org.joml.*;

import tage.*;
import tage.audio.*;
import tage.physics.*;

public class Projectile extends GameObject{
    private Vector3f direction;
    private boolean isPlayers;
    private OurGame ourGame;
    private Sound sound;
    private float speed;
    private float timer;

    public boolean getIsPlayers(){
        return isPlayers;
    }

    public void setTimer(float timer){
        this.timer = timer;
    }

    public Projectile(OurGame ourGame){
        super(GameObject.root(), ourGame.getRedSphere());
        this.ourGame = ourGame;
        setLocalScale(new Matrix4f().scale(.9f));
        PhysicsEngine physicsEngine = ourGame.getPhysicsEngine();
        float values[] = new float[16];
        int uid = physicsEngine.nextUID();
        double[] transform = OurGame.toDoubleArray(getLocalTranslation().get(values));
        PhysicsObject physicsObject = physicsEngine.addSphereObject(uid, 1f, transform, .9f);
        physicsObject.setBounciness(0f);
        physicsObject.setFriction(0f);
        setPhysicsObject(physicsObject);
        ourGame.registerProjectile(this);
        sound = ourGame.createSound(ourGame.getAudioResource("laser9"), SoundType.SOUND_EFFECT, 100, false);
	    if(sound != null){
            //If your computer is slow reduce the number of sounds set their rolloff.
            if(ourGame.getProjectileCount() < 100) sound.setRollOff(0.1f);
	    }
    }

    public void initialize(Vector3f direction, boolean isPlayers, Vector3f location, float speed){
        direction.normalize();
        if(isPlayers)setShape(ourGame.getGreenSphere());
        else setShape(ourGame.getRedSphere());
        this.direction = direction;
        this.isPlayers = isPlayers;
        setLocalLocation(location);
        this.speed = speed;
        timer = 0f;
	    if(sound != null){
            sound.setLocation(location);
            if(!isPlayers){
                sound.setVolume(100);
            }
            else{
                sound.setVolume(50);
            }
            sound.play();
	    }
        getRenderStates().enableRendering();
        float values[] = new float[16];
        double[] transform = OurGame.toDoubleArray(getLocalTranslation().get(values));
        getPhysicsObject().setTransform(transform);
        
    }

    public void updateProjectile(){
        timer += ourGame.getDeltaTime();
        if(timer > 5.5f){
            //System.out.println(timer);
            ourGame.deactivateProjectile(this);
            return;
        }
        /*
        if(ourGame.isTouchingTerrain(this)){
            ourGame.deactivateProjectile(this);
            return;
        }
        */
        Vector3f deltaPosition = new Vector3f(direction.x, direction.y, direction.z);
        deltaPosition.mul(speed).mul((float)ourGame.getDeltaTime());
        Vector3f localLocation = getLocalLocation();
        setLocalLocation(new Vector3f(localLocation.x + deltaPosition.x, 0f, localLocation.z + deltaPosition.z));
    }

    public void deactivate(){
        Random random = new Random();
        float f = random.nextFloat();
        f *= 1000f;
        setLocalLocation(new Vector3f(1000f + f, 1000f + f, 1000f + f));
        float values[] = new float[16];
        double[] transform = OurGame.toDoubleArray(getLocalTranslation().get(values));
        getPhysicsObject().setTransform(transform);
        getRenderStates().disableRendering();
    }
}
