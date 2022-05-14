package ourGame;

import java.util.ArrayList;
import java.util.Random;
import java.util.Vector;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import tage.GameObject;
import tage.shapes.AnimatedShape;
import tage.shapes.AnimatedShape.EndType;

/*
███████▓█████▓▓╬╬╬╬╬╬╬╬▓███▓╬╬╬╬╬╬╬▓╬╬▓█ 
████▓▓▓▓╬╬▓█████╬╬╬╬╬╬███▓╬╬╬╬╬╬╬╬╬╬╬╬╬█ 
███▓▓▓▓╬╬╬╬╬╬▓██╬╬╬╬╬╬▓▓╬╬╬╬╬╬╬╬╬╬╬╬╬╬▓█ 
████▓▓▓╬╬╬╬╬╬╬▓█▓╬╬╬╬╬╬╬╬╬╬╬╬╬╬╬╬╬╬╬╬╬▓█ 
███▓█▓███████▓▓███▓╬╬╬╬╬╬▓███████▓╬╬╬╬▓█ 
████████████████▓█▓╬╬╬╬╬▓▓▓▓▓▓▓▓╬╬╬╬╬╬╬█ 
███▓▓▓▓▓▓▓╬╬▓▓▓▓▓█▓╬╬╬╬╬╬╬╬╬╬╬╬╬╬╬╬╬╬╬▓█ 
████▓▓▓╬╬╬╬▓▓▓▓▓▓█▓╬╬╬╬╬╬╬╬╬╬╬╬╬╬╬╬╬╬╬▓█ 
███▓█▓▓▓▓▓▓▓▓▓▓▓▓▓▓╬╬╬╬╬╬╬╬╬╬╬╬╬╬╬╬╬╬╬▓█ 
█████▓▓▓▓▓▓▓▓█▓▓▓█▓╬╬╬╬╬╬╬╬╬╬╬╬╬╬╬╬╬╬╬▓█ 
█████▓▓▓▓▓▓▓██▓▓▓█▓╬╬╬╬╬╬╬╬╬╬╬╬╬╬╬╬╬╬╬██ 
█████▓▓▓▓▓████▓▓▓█▓╬╬╬╬╬╬╬╬╬╬╬╬╬╬╬╬╬╬╬██ 
████▓█▓▓▓▓██▓▓▓▓██╬╬╬╬╬╬╬╬╬╬╬╬╬╬╬╬╬╬╬╬██ 
████▓▓███▓▓▓▓▓▓▓██▓╬╬╬╬╬╬╬╬╬╬╬╬█▓╬▓╬╬▓██ 
█████▓███▓▓▓▓▓▓▓▓████▓▓╬╬╬╬╬╬╬█▓╬╬╬╬╬▓██ 
█████▓▓█▓███▓▓▓████╬▓█▓▓╬╬╬▓▓█▓╬╬╬╬╬╬███ 
██████▓██▓███████▓╬╬╬▓▓╬▓▓██▓╬╬╬╬╬╬╬▓███ 
███████▓██▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓╬╬╬╬╬╬╬╬╬╬╬████ 
███████▓▓██▓▓▓▓▓╬╬╬╬╬╬╬╬╬╬╬╬╬╬╬╬╬╬╬▓████ 
████████▓▓▓█████▓▓╬╬╬╬╬╬╬╬╬╬╬╬╬╬╬╬▓█████ 
█████████▓▓▓█▓▓▓▓▓███▓╬╬╬╬╬╬╬╬╬╬╬▓██████ 
██████████▓▓▓█▓▓▓╬▓██╬╬╬╬╬╬╬╬╬╬╬▓███████ 
███████████▓▓█▓▓▓▓███▓╬╬╬╬╬╬╬╬╬▓████████ 
██████████████▓▓▓███▓▓╬╬╬╬╬╬╬╬██████████ 
███████████████▓▓▓██▓▓╬╬╬╬╬╬▓███████████
*/

public class Boss extends Spaceship{
    private BossController bossController;
    private boolean isSpawning = false;
    private State state = State.NONE;
    private float timer = 0f;
    private float shotTimer = 0f;
    private Random random;
    private Enemy enemy1, enemy2;
    private float spawnTimer = 0f;

    private enum State{
        NONE,
        SHOTGUN,
        SNIPER    
    }

    public Boss(OurGame ourGame, int health){
        super(GameObject.root(), ourGame.getBossAnimatedShape(), ourGame, new float[]{24f, 32f, 12f}, ourGame.getBossTexture(), false);
        ourGame.getBossAnimatedShape().playAnimation("KICK", 0.75f, EndType.LOOP, 0);
        setLocalLocation(new Vector3f(0f, -10f, -28f));
        setLocalScale(new Matrix4f().scale(3f));
        setLocalRotation(new Matrix4f().rotate((float)Math.toRadians(270d), 0f, 1f, 0f));
        bossController = new BossController(this, ourGame);
        bossController.start();
        random = new Random();
        setHealth(health);
        if(getOurGame().getIsSinglePlayer() || (getOurGame().isConnected() && getOurGame().getIsHost())){
            enemy1 = getOurGame().instantiateEnemy(new Vector3f(20f, 0f, 0f), this);
            enemy1.propagateRotation(false);
            enemy1.setLocalRotation(new Matrix4f().rotate((float)Math.toRadians(90d), 0f, 1f, 0f));
            enemy1.setLocalScale(new Matrix4f().scale(1f/3f));
            enemy2 = getOurGame().instantiateEnemy(new Vector3f(-20f, 0f, 0f), this);
            enemy2.propagateRotation(false);
            enemy2.setLocalRotation(new Matrix4f().rotate((float)Math.toRadians(90d), 0f, 1f, 0f));
            enemy2.setLocalScale(new Matrix4f().scale(1f/3f));
        }
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
        if(timer > 5.5f && getHealth() > 0){
            if(!getOurGame().getFloatController().isEnabled()){
                getOurGame().getBossAnimatedShape().playAnimation("KICK", 0.75f, EndType.LOOP, 0);
                getOurGame().getFloatController().enable();
            }
        }
        switch(state){
            case NONE:{
                break;
            }
            case SHOTGUN:{
                timer += getOurGame().getDeltaTime();
                shotTimer += getOurGame().getDeltaTime();
                //System.out.println(timer);
                if(shotTimer >= .35f && timer < 5.5f && !getIsDead()){
                    shotgun(random.nextFloat());
                    shotTimer = 0f;
                }
                break;
            }
            case SNIPER:{
                timer += getOurGame().getDeltaTime();
                shotTimer += getOurGame().getDeltaTime();
                if(shotTimer >= .03f && timer < 5.5f && !getIsDead()){
                    fireAtPlayers();
                    shotTimer = 0f;
                }
                break;
            }
        }
        spawnTimer += getOurGame().getDeltaTime();
        if(isSpawning && spawnTimer >= 10f){
                spawnTimer = 0f;
                //getOurGame().instantiateEnemy(location);
        }
    }

    public BossController getBossController(){
        return bossController;
    }

    public void becomeSniper(){
        getOurGame().getFloatController().disable();
        state = State.SNIPER;
        timer = 0f;
        getOurGame().getBossAnimatedShape().playAnimation("SUMMON", 0.75f, EndType.LOOP, 0);

    }

    public void becomeShotgun(){
        getOurGame().getFloatController().disable();
        state = State.SHOTGUN;
        timer = 0f;
        shotTimer = 0f;
        shotgun(0);
        getOurGame().getBossAnimatedShape().playAnimation("SUMMON", 0.75f, EndType.LOOP, 0);

    }

    public void becomeSpawner(){
        isSpawning = true;
        spawnTimer = 10f;
    }

    @Override
    public void onDeath(){
        super.onDeath();
        getOurGame().getBossAnimatedShape().playAnimation("DEAD", 0.5f, EndType.PAUSE, 0);
        getOurGame().getFloatController().disable();
    }
}
