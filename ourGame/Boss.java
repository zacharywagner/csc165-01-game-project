package ourGame;

import java.util.ArrayList;
import java.util.Random;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import tage.GameObject;
import tage.shapes.AnimatedShape;
import tage.shapes.AnimatedShape.EndType;


public class Boss extends Spaceship{
    private BossController bossController;
    private boolean isSpawning = false;
    private State state = State.NONE;
    private float timer = 0f;
    private float shotTimer = 0f;
    private Random random;
    
    private enum State{
        NONE,
        SHOTGUN,
        SNIPER    
    }

    public Boss(OurGame ourGame){
        super(GameObject.root(), ourGame.getBossAnimatedShape(), ourGame, new float[]{24f, 32f, 12f}, ourGame.getBossTexture(), false);
        ourGame.getBossAnimatedShape().playAnimation("KICK", 0.5f, EndType.LOOP, 0);
        setLocalLocation(new Vector3f(0f, -10f, -28f));
        setLocalScale(new Matrix4f().scale(3f));
        setLocalRotation(new Matrix4f().rotate((float)Math.toRadians(270d), 0f, 1f, 0f));
        bossController = new BossController(this, ourGame);
        bossController.start();
        setHealth(5000);
        random = new Random();
    }

    public void shotgun(float offset){
        for(int i = -6; i < 39; i++){
            Vector3f direction = new Vector3f(-1f, 0f, 0f);
            float rad = (float)(Math.PI / 32d);
            direction.rotateY((i + offset) * rad);
            getOurGame().getOrCreateProjectile(direction, false, getWorldLocation(), 16f);
        }
    }

    public void fireAtPlayers(){
        ArrayList<Spaceship> spaceships = new ArrayList<Spaceship>();
        getOurGame().getSpaceships().forEach((key, value) -> {
            if(value.getIsFriend()){
                spaceships.add(value);
            }
        });
        spaceships.forEach((spaceship) -> {
            Vector3f direction = spaceship.getWorldLocation().sub(getWorldLocation());
            direction.normalize();
            getOurGame().getOrCreateProjectile(direction, false, getWorldLocation(), 64f);
        });
    }

    public void updateBoss(){
        switch(state){
            case NONE:{
                break;
            }
            case SHOTGUN:{
                timer += getOurGame().getDeltaTime();
                shotTimer += getOurGame().getDeltaTime();
                System.out.println(timer);
                if(shotTimer >= .35f && timer < 5.5f){
                    shotgun(random.nextFloat());
                    shotTimer = 0f;
                }
                break;
            }
            case SNIPER:{
                timer += getOurGame().getDeltaTime();
                shotTimer += getOurGame().getDeltaTime();
                if(shotTimer >= .03f && timer < 5.5f){
                    fireAtPlayers();
                    shotTimer = 0f;
                }
                break;
            }
        }
    }

    public BossController getBossController(){
        return bossController;
    }

    public void becomeSniper(){
        state = State.SNIPER;
        timer = 0f;
    }

    public void becomeShotgun(){
        state = State.SHOTGUN;
        timer = 0f;
        shotTimer = 0f;
        shotgun(0);
    }

    public void becomeSpawner(){
        isSpawning = true;
    }
}
