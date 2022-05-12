package ourGame;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import tage.GameObject;
import tage.shapes.AnimatedShape;
import tage.shapes.AnimatedShape.EndType;

public class Boss extends Spaceship{
    public Boss(OurGame ourGame){
        super(GameObject.root(), ourGame.getBossAnimatedShape(), ourGame, new float[]{24f, 32f, 12f}, ourGame.getBossTexture(), false);
        ourGame.getBossAnimatedShape().playAnimation("KICK", 0.5f, EndType.LOOP, 0);
        setLocalLocation(new Vector3f(10f, 0f, 10f));
        setLocalScale(new Matrix4f().scale(3f));
        setLocalRotation(new Matrix4f().rotate((float)Math.toRadians(270d), 0f, 1f, 0f));
    }
}
