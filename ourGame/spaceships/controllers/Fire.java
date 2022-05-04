package ourGame.spaceships.controllers;

import org.joml.Vector3f;

import ourGame.spaceships.*;
import tage.*;
import tage.ai.behaviortrees.BTCondition;

public class Fire extends BTCondition {
    public Fire(boolean toNegate){
        super(toNegate);
    }

    protected boolean check(){
        System.out.println("Fire!");
        return true;
    }
}
