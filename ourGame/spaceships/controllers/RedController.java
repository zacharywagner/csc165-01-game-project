package ourGame.spaceships.controllers;

import ourGame.*;
import ourGame.spaceships.*;
import tage.*;
import tage.ai.behaviortrees.*;

public class RedController {
    private BehaviorTree behaviorTree;
    private Red red;
    private float timer = 0f;

    public RedController(Red red){
        this.red = red;
    }

    public void start(){
        setupBehaviourTree();
        timer = 0f;
    }

    public void update(float deltaTime){
        if(timer >= 1f){
            behaviorTree.update(timer);
            timer = 0f;
        }
        else{
            timer += deltaTime;
        }
    }

    private void setupBehaviourTree(){
        behaviorTree = new BehaviorTree(BTCompositeType.SELECTOR);
        behaviorTree.insertAtRoot(new BTSequence(10));
        behaviorTree.insertAtRoot(new BTSequence(20));
        behaviorTree.insert(10, new AvatarNear(red, this, true));
        behaviorTree.insert(10, new Fire(false));
        behaviorTree.insert(20, new AvatarNear(red, this, false));
        behaviorTree.insert(20, new Ram(false));
    }
}
