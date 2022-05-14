package ourGame;

import tage.ai.behaviortrees.*;

public class BossController {
    private BehaviorTree behaviorTree;
    private Boss boss;
    private OurGame ourGame;
    private float timer;

    public BossController(Boss boss, OurGame ourGame){
        this.boss = boss;
        this.ourGame = ourGame;
    }

    public void start(){
        setupBehaviourTree();
        timer = 0f;
    }

    public void update(){
        if(timer >= 12f && boss.getHealth() > 0){
            behaviorTree.update(timer);
            timer = 0f;
        }
        else{
            timer += (float)ourGame.getDeltaTime();
        }
    }

    private void setupBehaviourTree(){
    behaviorTree = new BehaviorTree(BTCompositeType.SELECTOR);
        behaviorTree.insertAtRoot(new BTSequence(10));
        behaviorTree.insertAtRoot(new BTSequence(20));
        behaviorTree.insertAtRoot(new BTSequence(30));
        behaviorTree.insert(10, new LowBossHealth(boss, this, false));
        behaviorTree.insert(10, new SummonBehaviour(this, false));
        behaviorTree.insert(20, new AvatarNear(boss, this, true));
        behaviorTree.insert(20, new SniperBehaviour(this, false));
        behaviorTree.insert(30, new AvatarNear(boss, this, false));
        behaviorTree.insert(30, new ShotgunBehaviour(this, false));
    }

    public Boss getBoss(){
        return boss;
    }
}
