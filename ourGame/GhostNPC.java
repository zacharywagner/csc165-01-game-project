package ourGame;

import tage.*;
import org.joml.*;

public class GhostNPC extends GameObject {
    int uid;

    public GhostNPC(int uid, ObjShape s, TextureImage t, Vector3f p) {
        super(GameObject.root(), s, t);
        this.uid = uid;
        setPosition(p);
    }

    public int getID() {
        return uid;
    }

    public void setPosition(Vector3f position) {
        setLocalLocation(position);
    }

    public Vector3f getPosition() {
        return getWorldLocation();
    }
}
