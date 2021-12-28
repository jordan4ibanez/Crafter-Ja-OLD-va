package game.item;

import engine.graphics.Mesh;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.Arrays;

import static engine.FancyMath.convertLight;
import static engine.FancyMath.randomForceValue;
import static game.item.ItemDefinition.getItemDefinition;

public class Item {

    //KEEP THIS IN THIS CLASS
    //IT'S TOO COMPLEX TO REMOVE THIS OUT OF THIS CLASS!
    private static int currentID = 0;

    public String name;
    public int stack;
    public ItemDefinition definition;
    public final Vector3d pos = new Vector3d();
    public final Vector3d goalPos = new Vector3d();
    public float scale;
    public float timer;
    public float hover;
    public boolean floatUp;
    public boolean exists;
    public boolean collecting;
    public float collectionTimer = 0;
    public boolean deletionOkay = false;
    public final Vector3f rotation = new Vector3f();
    public final Vector3f inertia = new Vector3f();
    public int ID;

    public byte light = 15;
    public float lightUpdateTimer = 1f;
    public Vector3i oldFlooredPos = new Vector3i(0,0,0);


    //yes this is ridiculous, but it is also safe
    //internal automatic integer overflow to 0
    private static void tickUpCurrentID(){
        currentID++;
        if (currentID == 2147483647){
            currentID = 0;
        }
    }

    public static int getCurrentID(){
        return currentID;
    }

    //inventory item
    public Item(String name, int stack){
        this.name = name;
        this.definition = getItemDefinition(name);
        this.stack = stack;
        this.ID = currentID;
        tickUpCurrentID();
    }

    //item being mined
    public Item(String name, double posX, double posY, double posZ, float inertiaX, float inertiaY, float inertiaZ, int stack, float timer) {
        this.name = name;
        this.pos.set(posX, posY, posZ);
        this.definition = getItemDefinition(name);
        this.stack = stack;
        this.inertia.set(inertiaX, inertiaY, inertiaZ);
        this.rotation.set(0);
        this.hover = 0f;
        this.floatUp = true;
        this.exists = true;
        this.collecting = false;
        this.scale = 1f;
        this.timer = timer;
        this.ID = currentID;
        tickUpCurrentID();
    }

    //clone item
    public Item(Item itemBeingCloned) {
        this.name = itemBeingCloned.name;
        this.pos.set(itemBeingCloned.pos);
        this.definition = getItemDefinition(name);
        this.stack = itemBeingCloned.stack;
        this.inertia.set(itemBeingCloned.inertia);
        this.rotation.set(0);
        this.hover = 0f;
        this.floatUp = true;
        this.exists = true;
        this.collecting = false;
        this.scale = 1f;
        this.timer = 0f;
        this.ID = currentID;
        tickUpCurrentID();
    }
}
