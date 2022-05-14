package ourGame;

import org.joml.Vector3f;

import tage.*;
import tage.ai.behaviortrees.BTCondition;

public class SummonBehaviour extends BTCondition {
    private BossController bossController;
    public SummonBehaviour(BossController bossController, boolean toNegate){
        super(toNegate);
        this.bossController = bossController;
    }

    protected boolean check(){
        System.out.println("Spawn 'em!");
        bossController.getBoss().becomeSpawner();
        return true;
    }
}