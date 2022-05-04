package ourGame.spaceships.controllers;

import org.joml.Vector3f;

import ourGame.spaceships.*;
import tage.*;
import tage.ai.behaviortrees.BTCondition;

public class Ram extends BTCondition {
    public Ram(boolean toNegate){
        super(toNegate);
    }

    protected boolean check(){
        System.out.println("Ram!");
        return true;
    }
}
