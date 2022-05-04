package ourGame.spaceships;

import javax.vecmath.Vector2f;

import org.joml.Vector3f;

//Import(s)
import ourGame.*;
import ourGame.spaceships.controllers.*;
import tage.GameObject;

public class Red extends Spaceship{
    private RedController redController;
    private boolean ramming = false;
    //Constructor(s)
    public Red(GameObject gameObject, int layer, OurGame ourGame){
        super(gameObject, layer, ourGame);
        redController = new RedController(this);
        redController.start();
    }

    public RedController getRedController(){
        return redController;
    }

    public void update(){
        if(ramming){
            Vector3f avatarPosition = getOurGame().getAvatar().getWorldLocation();
            Vector3f position = getGameObject().getWorldLocation();
            Vector2f direction = new Vector2f(avatarPosition.x - position.x, avatarPosition.z - position.z);
            if(direction.x == 0f && direction.y == 0f) return;
            System.out.println("dir " + direction.length());
            direction.normalize();
            direction.x *= getOurGame().getDeltaTime();
            direction.y *= getOurGame().getDeltaTime();
            position.x += direction.x;
            position.z += direction.y;
            getGameObject().setLocalLocation(position);
        }
    }

    public void ram(){
        ramming = true;
    }

    public void fire(){
        ramming = false;
    }
}
