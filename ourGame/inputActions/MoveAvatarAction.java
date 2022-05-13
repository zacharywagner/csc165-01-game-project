package ourGame.inputActions;

import org.joml.Vector3f;

import net.java.games.input.Event;
import net.java.games.input.Component.Identifier;
import ourGame.*;
import tage.input.action.AbstractInputAction;

public class MoveAvatarAction extends AbstractInputAction{

    private OurGame ourGame;
    private ProtocolClient protocolClient;

    public MoveAvatarAction(OurGame ourGame, ProtocolClient protocolClient){
        this.ourGame = ourGame;
        this.protocolClient = protocolClient;
    }

    public void performAction(float time, Event event){
        Identifier identifier = event.getComponent().getIdentifier();
        Player avatar = ourGame.getAvatar();
        Vector3f avatarLocation = avatar.getLocalLocation();
        if(identifier.equals(net.java.games.input.Component.Identifier.Key.W)){
            //System.out.println("Moving the avatar forward!");
            avatarLocation.z -= avatar.getSpeed() * time;
        }
        else if(identifier.equals(net.java.games.input.Component.Identifier.Key.A)){
            //System.out.println("Moving the avatar left!");
            avatarLocation.x -= avatar.getSpeed() * time;
        }
        else if(identifier.equals(net.java.games.input.Component.Identifier.Key.S)){
            //System.out.println("Moving the avatar backward!");
            avatarLocation.z += avatar.getSpeed() * time;
        }
        else if(identifier.equals(net.java.games.input.Component.Identifier.Key.D)){
            //System.out.println("Moving the avatar right!");
            avatarLocation.x += avatar.getSpeed() * time;
        }
        else if(identifier.equals(net.java.games.input.Component.Identifier.Axis.X)){
            float value = event.getValue();
            if(Math.abs(value) > 0.1f){
                avatarLocation.x += avatar.getSpeed() * time * value;
            }
        }
        else if(identifier.equals(net.java.games.input.Component.Identifier.Axis.Y)){
            float value = event.getValue();
            if(Math.abs(value) > 0.1f){
                avatarLocation.z += avatar.getSpeed() * time * value;
            }
        }
        avatar.setLocalLocation(avatarLocation);
        if (protocolClient != null) {
            protocolClient.sendMoveMessage(avatarLocation);
        }
    }
}
