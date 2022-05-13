package ourGame;

import java.util.ArrayList;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import tage.GameObject;
import tage.shapes.AnimatedShape;
import tage.shapes.AnimatedShape.EndType;

public class Boss extends Spaceship{
    public Boss(OurGame ourGame){
        super(GameObject.root(), ourGame.getBossAnimatedShape(), ourGame, new float[]{24f, 32f, 12f}, ourGame.getBossTexture(), false);
        ourGame.getBossAnimatedShape().playAnimation("KICK", 0.5f, EndType.LOOP, 0);
        setLocalLocation(new Vector3f(10f, -10f, 10f));
        setLocalScale(new Matrix4f().scale(3f));
        setLocalRotation(new Matrix4f().rotate((float)Math.toRadians(270d), 0f, 1f, 0f));
    }

    public void shotgun(int offset){
        for(int i = 0; i < 33; i++){
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
}
