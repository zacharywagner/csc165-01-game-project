package ourGame;

import org.joml.Vector3f;

import tage.*;
import tage.ai.behaviortrees.BTCondition;

public class ShotgunBehaviour extends BTCondition {
    private BossController bossController;
    public ShotgunBehaviour(BossController bossController, boolean toNegate){
        super(toNegate);
        this.bossController = bossController;
    }

    protected boolean check(){
        System.out.println("Shotgun!? Run away!");
        bossController.getBoss().becomeShotgun();
        return true;
    }
}