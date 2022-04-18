package ourGame;

import tage.*;
import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;
import org.joml.*;

public class RightAction extends AbstractInputAction {
    private OurGame game;
    private GameObject avatar;
    private ProtocolClient protocolClient;
    private Vector3f oldPos, newPos;
    private Vector4f rightDirectionAvatar;

    public RightAction(OurGame g, ProtocolClient p) {
        game = g;
        protocolClient = p;
    }

    @Override
    public void performAction(float time, Event e) {
        float keyValue = e.getValue();
        if (keyValue > -0.2 && keyValue < 0.2) {
            return;
        }

        avatar = game.getAvatar();
        oldPos = avatar.getWorldLocation();
        rightDirectionAvatar = new Vector4f(1f,0f,0f,1f);
        rightDirectionAvatar.mul(avatar.getWorldRotation());

        if(keyValue > 0.2) {
            rightDirectionAvatar.mul(-0.005f * time);
        }
        newPos = oldPos.add(rightDirectionAvatar.x(), rightDirectionAvatar.y(), rightDirectionAvatar.z());
        avatar.setLocalLocation(newPos);
        if(protocolClient != null) {
            protocolClient.sendMoveMessage(avatar.getWorldLocation());
        }
    }
}