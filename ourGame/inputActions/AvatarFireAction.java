package ourGame.inputActions;

import org.joml.Vector3f;

import net.java.games.input.Event;
import ourGame.*;
import tage.input.action.AbstractInputAction;

public class AvatarFireAction extends AbstractInputAction{

    private OurGame ourGame;
    private ProtocolClient protocolClient;
    private long lastActionTime;

    public AvatarFireAction(OurGame ourGame, ProtocolClient protocolClient){
        this.ourGame = ourGame;
        this.protocolClient = protocolClient;
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
        Vector3f direction = new Vector3f(0f, 0f, -1f);
        boolean isPlayers = true;
        float speed = 64f;
        ourGame.getOrCreateProjectile(direction, isPlayers, position, speed);
        if(!ourGame.getIsSinglePlayer()){
            protocolClient.sendSendShotMessage(direction, isPlayers, position, speed);
        }
    }
}
