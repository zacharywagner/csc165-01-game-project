
package ourGame;

import org.joml.Vector3f;

import tage.*;
import tage.ai.behaviortrees.BTCondition;

public class AvatarNear extends BTCondition {
    private Boss boss;
    private BossController bossController;

    public AvatarNear(Boss boss, BossController bossController, boolean toNegate){
        super(toNegate);
        this.boss = boss;
        this.bossController = bossController;
    }

    protected boolean check(){
        //System.out.println("Check!");
        GameObject avatar = boss.getOurGame().getAvatar();
        Vector3f worldLocation = boss.getWorldLocation();
        float distance = avatar.getWorldLocation().z - worldLocation.z;
        System.out.println(distance);
        distance = Math.abs(distance);
        if(distance < 30f) return true;
        return false;
    }
}