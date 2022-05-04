package ourGame.spaceships.controllers;

import org.joml.Vector3f;

import ourGame.spaceships.*;
import tage.*;
import tage.ai.behaviortrees.BTCondition;

public class AvatarNear extends BTCondition {
    private Red red;
    private RedController redController;

    public AvatarNear(Red red, RedController redController, boolean toNegate){
        super(toNegate);
        this.red = red;
        this.redController = redController;
    }

    protected boolean check(){
        GameObject avatar = red.getOurGame().getAvatar();
        Vector3f worldLocation = red.getGameObject().getWorldLocation();
        float distance = avatar.getWorldLocation().z - worldLocation.z;
        //System.out.println(distance);
        distance = Math.abs(distance);
        if(distance < 20f) return true;
        return false;
    }
}
