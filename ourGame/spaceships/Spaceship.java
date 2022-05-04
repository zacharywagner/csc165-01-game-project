package ourGame.spaceships;

//Import(s)
import ourGame.*;
import tage.GameObject;
import tage.physics.PhysicsObject;

public abstract class Spaceship {
    //Private Variable(s)
    private GameObject gameObject;
    private int layer = 0;
    private OurGame ourGame;

    //Constructor(s)
    public Spaceship(GameObject gameObject, int layer, OurGame ourGame){
        this.gameObject = gameObject;
        this.layer = layer;
        this.ourGame = ourGame;
    }

    //Accessor(s) and Mutator(s)
    public GameObject getGameObject(){
        return gameObject;
    }

    public int getLayer(){
        return layer;
    }

    public OurGame getOurGame(){
        return ourGame;
    }

    public void setLayer(int layer){
        this.layer = layer;
    }
}
