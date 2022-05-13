package ourGame;

import tage.ai.behaviortrees.BehaviorTree;

public class BossController {
    private BehaviorTree behaviorTree;
    private Boss boss;
    private OurGame ourGame;
    private float timer;

    public BossController(Boss boss, OurGame ourGame){
        this.ourGame = ourGame;
    }

    public void start(){
        setupBehaviourTree();
        timer = 0f;
    }

    public void update(){
        if(timer >= 7f && boss.getHealth() > 0){
            behaviorTree.update(timer);
            timer = 0f;
        }
        else{
            timer += (float)ourGame.getDeltaTime();
        }
    }

    private void setupBehaviourTree(){

    }

    public Boss getBoss(){
        return boss;
    }
}
