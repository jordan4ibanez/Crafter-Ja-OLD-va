package game.entity;

import engine.time.Delta;
import game.chunk.Chunk;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.UUID;

import static org.joml.Math.floor;

public abstract class Entity {

    private final EntityContainer entityContainer;

    private final Vector3d pos = new Vector3d();
    private final Vector3i flooredPos = new Vector3i();
    private final Vector3i oldFlooredPos = new Vector3i();

    private final Vector3f inertia = new Vector3f();

    private final UUID uuid = UUID.randomUUID();

    private float timer = 0;
    private byte light = 15;
    private float lightUpdateTimer = 0f;

    private final float width;
    private final float height;

    private final boolean item;
    private final boolean mob;
    private final boolean particle;

    public Entity(EntityContainer entityContainer, Vector3d pos, Vector3f inertia, float width, float height, boolean item, boolean mob, boolean particle){

        this.width = width;
        this.height = height;

        this.item = item;
        this.mob = mob;
        this.particle = particle;

        this.entityContainer = entityContainer;
        this.entityContainer.add(this);

        this.pos.set(pos);
        this.inertia.set(inertia);
        this.flooredPos.set((int) floor(pos.x), (int) floor(pos.y), (int) floor(pos.z));
        this.oldFlooredPos.set((int) floor(pos.x), (int) floor(pos.y), (int) floor(pos.z));
    }

    public void onTick(Chunk chunk, Delta delta) {

        lightUpdateTimer += delta.getDelta();

        flooredPos.set((int) floor(getPos().x), (int) floor(getPos().y), (int) floor(getPos().z));

        //poll local light every quarter second
        if (lightUpdateTimer >= 0.25f || !flooredPos.equals(oldFlooredPos)){

            light = chunk.getLight(flooredPos.x, flooredPos.y, flooredPos.z);

            lightUpdateTimer = 0f;
        }

        oldFlooredPos.set(flooredPos);
    }

    public byte getLight() {
        return light;
    }

    public float getWidth(){
        return width;
    }

    public float getHeight(){
        return height;
    }

    public void delete(){
        entityContainer.remove(this);
    }

    public UUID getUuid(){
        return uuid;
    }

    public boolean isItem(){
        return item;
    }

    public boolean isMob(){
        return mob;
    }

    public boolean isParticle() {
        return particle;
    }

    public float getTimer(){
        return timer;
    }

    public void setTimer(float timer){
        this.timer = timer;
    }

    public Vector3d getPos(){
        return pos;
    }

    public void setPos(Vector3d pos){
        this.pos.set(pos);
    }

    public Vector3f getInertia(){
        return inertia;
    }

    public void setInertia(Vector3f inertia){
        this.inertia.set(inertia);
    }
}
