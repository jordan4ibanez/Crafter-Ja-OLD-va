package game.entity;

import engine.sound.SoundAPI;
import engine.time.Delta;
import game.chunk.Chunk;
import game.crafting.InventoryLogic;
import game.entity.collision.Collision;
import game.player.Player;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.UUID;

import static org.joml.Math.floor;

public abstract class Entity implements EntityInterface{

    //master pointers
    private final EntityContainer entityContainer;
    private final Chunk chunk;

    private final UUID uuid = UUID.randomUUID();
    private final Vector3d pos = new Vector3d();
    private final Vector3f inertia = new Vector3f();
    private float timer = 0;
    private final boolean item;
    private final boolean mob;
    private byte light = 15;
    private float lightUpdateTimer = 0f;
    private final Vector3i flooredPos;
    private final Vector3i oldFlooredPos;


    public Entity(Chunk chunk, EntityContainer entityContainer, Vector3d pos, Vector3f inertia, boolean item, boolean mob){
        this.chunk = chunk;
        this.entityContainer = entityContainer;
        this.entityContainer.add(this);

        this.pos.set(pos);
        this.inertia.set(inertia);

        this.item = item;
        this.mob = mob;

        this.flooredPos = new Vector3i((int) floor(pos.x), (int) floor(pos.y), (int) floor(pos.z));
        this.oldFlooredPos = new Vector3i((int) floor(pos.x), (int) floor(pos.y), (int) floor(pos.z));
    }

    public boolean isItem(){
        return this.item;
    }

    public boolean isMob(){
        return this.mob;
    }

    public float getTimer(){
        return this.timer;
    }

    public void setTimer(float timer){
        this.timer = timer;
    }

    public Vector3d getPos(){
        return this.pos;
    }

    public void setPos(Vector3d pos){
        this.pos.set(pos);
    }

    public Vector3f getInertia(){
        return this.inertia;
    }

    public void setInertia(Vector3f inertia){
        this.inertia.set(inertia);
    }

    public void delete(){
        entityContainer.remove(this);
    }


    public void sayHi(){
        System.out.println("hi there");
        this.sayBye(this);
    }


    @Override
    public void onTick(Entity entity, Delta delta, Player player) {
        EntityInterface.super.onTick(entity, delta, player);

        lightUpdateTimer += delta.getDelta();

        flooredPos.set((int) floor(getPos().x), (int) floor(getPos().y), (int) floor(getPos().z));

        //poll local light every quarter second
        if (lightUpdateTimer >= 0.25f || !flooredPos.equals(oldFlooredPos)){

            light = chunk.getLight(flooredPos.x, flooredPos.y, flooredPos.z);

            lightUpdateTimer = 0f;
        }

        oldFlooredPos.set(flooredPos);
    };

    public abstract void onTick(Entity entity, Player player, Delta delta);

    public abstract void onTick(Entity entity, InventoryLogic inventoryLogic, Player player, Delta delta);

    public abstract void onTick(Entity entity, SoundAPI soundAPI, InventoryLogic inventoryLogic, Player player, Delta delta);

    public abstract void onTick(Collision collision, Entity entity, SoundAPI soundAPI, InventoryLogic inventoryLogic, Player player, Delta delta);
}
