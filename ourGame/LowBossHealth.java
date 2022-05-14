
package ourGame;

import org.joml.Vector3f;

import tage.*;
import tage.ai.behaviortrees.BTCondition;

public class LowBossHealth extends BTCondition {
    private Boss boss;
    private BossController bossController;

    public LowBossHealth(Boss boss, BossController bossController, boolean toNegate){
        super(toNegate);
        this.boss = boss;
        this.bossController = bossController;
    }

    protected boolean check(){
        System.out.println("Low health?");
        System.out.println(boss.getHealth());
        if(boss.getHealth() < 200 && !boss.getIsSpawning()) return true;
        return false;
    }
}