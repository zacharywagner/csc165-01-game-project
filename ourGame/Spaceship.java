package ourGame;

import tage.*;
import tage.physics.*;

public class Spaceship extends GameObject{
    private OurGame ourGame;
    private int uid;
    private boolean isFriend;
    private int health;

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
    }

    public void dealDamage(int damage){
        health -= damage;
        if(health <= 0){
            onDeath();    
        }
    }

    public void onDeath(){
        System.out.println("I am dead!");
    }
}
