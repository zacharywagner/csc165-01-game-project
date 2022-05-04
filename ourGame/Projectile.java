package ourGame;

import org.joml.*;
import tage.*;

public class Projectile {
    //Private Variable(s)
    private Vector2f direction;
    private GameObject gameObject;
    private int layer = 0xffffffff;
    private OurGame ourGame;
    private float speed;
    private float timer;
    
    //Constructor(s)
    public Projectile(GameObject gameObject, OurGame ourGame){
        this.gameObject = gameObject;
        this.ourGame = ourGame;
    }

    //Accessor(s) and Mutator(s)
    public GameObject getGameObject(){
        return gameObject;
    }

    //Function(s)
    public void initialize(Vector2f direction, int layer, Vector2f position, float speed){
        direction = direction.normalize();
        System.out.println(direction);
        this.direction = direction;
        this.layer = layer;
        gameObject.setLocalLocation(new Vector3f(position.x, 0f, position.y));
        this.speed = speed;
        syncronizePhysicsObjectToGameObject();
        timer = 0f;
    }

    public void update(float deltaTime){
        timer += deltaTime;
        if(timer > 10f){
            ourGame.deactivateProjectile(this);
            return;
        }
        Vector2f deltaPosition = new Vector2f(direction.x, direction.y);
        deltaPosition.mul(speed).mul(deltaTime);
        Vector3f localLocation = gameObject.getLocalLocation();
        gameObject.setLocalLocation(new Vector3f(localLocation.x + deltaPosition.x, localLocation.y, localLocation.z + deltaPosition.y));
        syncronizePhysicsObjectToGameObject();
    }

    public void syncronizePhysicsObjectToGameObject(){
        float values[] = new float[16];
        double transform[] = OurGame.toDoubleArray(gameObject.getLocalTranslation().get(values));
        gameObject.getPhysicsObject().setTransform(transform);
    }
}
