package ourGame;

import java.util.ArrayList;
import java.util.Random;
import java.util.Vector;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import tage.GameObject;
import tage.audio.*;
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
    private Sound initSound, shotgunSound, sniperSound;

    public boolean getIsSpawning(){
        return isSpawning;
    }

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
            enemy1 = getOurGame().instantiateEnemy(new Vector3f(20f, 10f, 0f), this);
            //enemy1.propagateRotation(false);
            enemy1.setLocalRotation(new Matrix4f().rotate((float)Math.toRadians(180d), 0f, 1f, 0f));
            enemy1.setLocalScale(new Matrix4f().scale(1f/3f));
            enemy2 = getOurGame().instantiateEnemy(new Vector3f(-20f, 10f, 0f), this);
            //enemy2.propagateRotation(false);
            enemy2.setLocalRotation(new Matrix4f().rotate((float)Math.toRadians(180d), 0f, 1f, 0f));
            enemy2.setLocalScale(new Matrix4f().scale(1f/3f));
        }
    }

    public void initb(){
        OurGame ourGame = getOurGame();
        initSound = ourGame.createSound(ourGame.createAudioResource("Initializing", AudioResourceType.AUDIO_SAMPLE), SoundType.SOUND_EFFECT, 100, false);
        initSound.setRollOff(0.1f);
        shotgunSound = ourGame.createSound(ourGame.createAudioResource("TargetFound", AudioResourceType.AUDIO_SAMPLE), SoundType.SOUND_EFFECT, 100, false);
        shotgunSound.setRollOff(0.1f);
        sniperSound = ourGame.createSound(ourGame.createAudioResource("TargetAcquired", AudioResourceType.AUDIO_SAMPLE), SoundType.SOUND_EFFECT, 100, false);
        sniperSound.setRollOff(0.1f);
    }

    public void shotgun(float offset){
        for(int i = -6; i < 39; i++){
            Vector3f direction = new Vector3f(-1f, 0f, 0f);
            float rad = (float)(Math.PI / 32d);
            direction.rotateY((i + offset) * rad);
            getOurGame().getOrCreateProjectile(direction, false, getWorldLocation(), 16f);
        }
    }


    public void playInitSound(){
        initSound.setLocation(getWorldLocation());
        initSound.play();
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
                timer += getOurGame().getDeltaTime();
                shotTimer += getOurGame().getDeltaTime();
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
    }

    public BossController getBossController(){
        return bossController;
    }

    public void becomeSniper(){
        getOurGame().getFloatController().disable();
        state = State.SNIPER;
        timer = 0f;
        getOurGame().getBossAnimatedShape().playAnimation("SUMMON", 0.75f, EndType.LOOP, 0);
        sniperSound.setLocation(getWorldLocation());
        sniperSound.play();
    }

    public void becomeShotgun(){
        getOurGame().getFloatController().disable();
        state = State.SHOTGUN;
        timer = 0f;
        shotTimer = 0f;
        shotgun(0);
        getOurGame().getBossAnimatedShape().playAnimation("SUMMON", 0.75f, EndType.LOOP, 0);
        shotgunSound.setLocation(getWorldLocation());
        shotgunSound.play();
    }

    public void becomeSpawner(){
        getOurGame().getBossAnimatedShape().playAnimation("SUMMON", 0.75f, EndType.LOOP, 0);
        state = State.NONE;
        playInitSound();
        enemy1.setIsDead(false);
        enemy1.getRenderStates().enableRendering();
        enemy2.setIsDead(false);
        enemy2.getRenderStates().enableRendering();
        isSpawning = true;
    }

    @Override
    public void onDeath(){
        super.onDeath();
        getOurGame().getBossAnimatedShape().playAnimation("DEAD", 0.5f, EndType.PAUSE, 0);
        getOurGame().getFloatController().disable();
    }
}
