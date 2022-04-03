package ourGame;

import tage.*;
import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;
import org.joml.*;

public class BackNForthAction extends AbstractInputAction {
    private OurGame game;
    private GameObject avatar;
    private Camera camera;
    private Vector3f oldPos, newPos;
    private Vector4f fwdDirectionAvatar;

    public BackNForthAction(OurGame g) {
        game = g;
    }

    @Override
    public void performAction(float time, Event e) {
        float keyValue = e.getValue();
        if (keyValue > -0.2 && keyValue < 0.2) {
            return;
        }

        avatar = game.getAvatar();
        oldPos = avatar.getWorldLocation();
        fwdDirectionAvatar = new Vector4f(0f,0f,1f,1f);
        fwdDirectionAvatar.mul(avatar.getWorldRotation());

        if(keyValue < -0.2) {
            fwdDirectionAvatar.mul(0.005f * time);
        }
        else {
            fwdDirectionAvatar.mul(-0.005f * time);
        }
        newPos = oldPos.add(fwdDirectionAvatar.x(), fwdDirectionAvatar.y(), fwdDirectionAvatar.z());
        avatar.setLocalLocation(newPos);
    }
}
