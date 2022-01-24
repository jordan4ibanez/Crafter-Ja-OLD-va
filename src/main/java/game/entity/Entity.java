package game.entity;

import engine.time.Delta;
import org.joml.Vector3d;
import org.joml.Vector3f;

import java.util.UUID;

public abstract class Entity implements EntityInterface{

    //master pointer
    private final EntityContainer entityContainer;

    private final UUID uuid = UUID.randomUUID();
    private final Vector3d pos = new Vector3d();
    private final Vector3f inertia = new Vector3f();
    private float timer = 0;
    private final boolean item;
    private final boolean mob;

    public Entity(EntityContainer entityContainer, Vector3d pos, Vector3f inertia, boolean item, boolean mob){
        this.entityContainer = entityContainer;
        this.entityContainer.add(this);

        this.pos.set(pos);
        this.inertia.set(inertia);

        this.item = item;
        this.mob = mob;
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


    public abstract void onTick(Entity entity, Delta delta);
}
