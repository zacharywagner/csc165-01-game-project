package ourGame.spaceships.controllers;

import org.joml.Vector3f;

import ourGame.spaceships.*;
import tage.*;
import tage.ai.behaviortrees.BTCondition;

public class Fire extends BTCondition {
    private RedController redController;
    public Fire(RedController redController, boolean toNegate){
        super(toNegate);
        this.redController = redController;
    }

    protected boolean check(){
        System.out.println("Fire!");
        redController.getRed().fire();
        return true;
    }
}
