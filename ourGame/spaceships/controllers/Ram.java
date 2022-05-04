package ourGame.spaceships.controllers;

import org.joml.Vector3f;

import ourGame.spaceships.*;
import tage.*;
import tage.ai.behaviortrees.BTCondition;

public class Ram extends BTCondition {
    private RedController redController;
    public Ram(RedController redController, boolean toNegate){
        super(toNegate);
        this.redController = redController;
    }

    protected boolean check(){
        System.out.println("Ram!");
        redController.getRed().ram();
        return true;
    }
}
