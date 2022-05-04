package ourGame.spaceships;

//Import(s)
import ourGame.*;
import ourGame.spaceships.controllers.*;
import tage.GameObject;

public class Red extends Spaceship{
    private RedController redController;
    //Constructor(s)
    public Red(GameObject gameObject, int layer, OurGame ourGame){
        super(gameObject, layer, ourGame);
        redController = new RedController(this);
        redController.start();
    }

    public RedController getRedController(){
        return redController;
    }
}
