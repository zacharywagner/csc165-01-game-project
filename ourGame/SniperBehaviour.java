package ourGame;

import org.joml.Vector3f;

import tage.*;
import tage.ai.behaviortrees.BTCondition;

public class SniperBehaviour extends BTCondition {
    private BossController bossController;
    public SniperBehaviour(BossController bossController, boolean toNegate){
        super(toNegate);
        this.bossController = bossController;
    }

    protected boolean check(){
        System.out.println("Sniper, get down!");
        bossController.getBoss().becomeSniper();
        return true;
    }
}