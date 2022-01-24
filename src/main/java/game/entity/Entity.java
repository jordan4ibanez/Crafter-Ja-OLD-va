package game.entity;

import org.joml.Vector3d;
import org.joml.Vector3f;

import java.util.UUID;

public class Entity implements EntityInterface{

    //master pointer
    private final EntityContainer entityContainer;

    private final UUID uuid = UUID.randomUUID();
    private final Vector3d pos = new Vector3d();
    private final Vector3f inertia = new Vector3f();

    public Entity(EntityContainer entityContainer, Vector3d pos, Vector3f inertia){
        this.entityContainer = entityContainer;
        this.entityContainer.add(this);

        this.pos.set(pos);
        this.inertia.set(inertia);
    }

    public Vector3d getPos(){
        return this.pos;
    }

    public Vector3f getInertia(){
        return this.inertia;
    }

    public void delete(){
        entityContainer.remove(this);
    }


    public void sayHi(){
        System.out.println("hi there");
        this.sayBye(this);
    }


}
