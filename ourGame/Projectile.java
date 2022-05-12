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

    public Projectile(OurGame ourGame){
        super(GameObject.root(), ourGame.getRedSphere());
        this.ourGame = ourGame;
        setLocalScale(new Matrix4f().scale(1f));
        PhysicsEngine physicsEngine = ourGame.getPhysicsEngine();
        float values[] = new float[16];
        int uid = physicsEngine.nextUID();
        double[] transform = OurGame.toDoubleArray(getLocalTranslation().get(values));
        PhysicsObject physicsObject = physicsEngine.addSphereObject(uid, 1f, transform, 1f);
        physicsObject.setBounciness(0f);
        physicsObject.setFriction(0f);
        setPhysicsObject(physicsObject);
        sound = ourGame.createSound(ourGame.getAudioResource("laser9"), SoundType.SOUND_EFFECT, 100, false);
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
        sound.setLocation(location);
        sound.play();
    }

    public void updateProjectile(){
        timer += ourGame.getDeltaTime();
        if(timer > 8f){
            ourGame.deactivateProjectile(this);
            return;
        }
        Vector3f deltaPosition = new Vector3f(direction.x, direction.y, direction.z);
        deltaPosition.mul(speed).mul((float)ourGame.getDeltaTime());
        Vector3f localLocation = getLocalLocation();
        setLocalLocation(new Vector3f(localLocation.x + deltaPosition.x, localLocation.y + deltaPosition.y, localLocation.z + deltaPosition.z));
    }
}
