package ourGame.inputActions;

import org.joml.Vector3f;

import net.java.games.input.Event;
import ourGame.*;
import tage.input.action.AbstractInputAction;

public class AvatarFireAction extends AbstractInputAction{

    private OurGame ourGame;
    private long lastActionTime;

    public AvatarFireAction(OurGame ourGame){
        this.ourGame = ourGame;
        lastActionTime = 0;
    }

    public void performAction(float time, Event event){
        if(System.currentTimeMillis() - lastActionTime > 48){
            lastActionTime = System.currentTimeMillis();
        }
        else{
            return;
        }
        Vector3f position = ourGame.getAvatar().getWorldLocation();
        position.z -= 1f;
        ourGame.getOrCreateProjectile(new Vector3f(0f, 0f, -1f), true, position, 64f);
    }
}
