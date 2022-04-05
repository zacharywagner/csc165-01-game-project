package ourGame;

import tage.*;
import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;
import org.joml.*;

public class TurnAction extends AbstractInputAction {
    private OurGame game;
    private GameObject avatar;

    public TurnAction(OurGame g) {
        game = g;
    }

    @Override
    public void performAction(float time, Event e) {
        float keyValue = e.getValue();
        if (keyValue > -0.5 && keyValue < 0.5) {
            return;
        }

        avatar = game.getAvatar();

        if(keyValue > 0.3) {
            avatar.yaw(-0.003f * time);
        }
        else {
            avatar.yaw(0.003f * time);
        }
    }
}