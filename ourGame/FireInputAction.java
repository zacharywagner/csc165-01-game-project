package ourGame;

import tage.*;
import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;
import org.joml.*;

public class FireInputAction extends AbstractInputAction {
    private OurGame ourGame;

    public FireInputAction(OurGame ourGame) {
        this.ourGame = ourGame;
    }

    @Override
    public void performAction(float time, Event event) {
        ourGame.fire();
    }
}