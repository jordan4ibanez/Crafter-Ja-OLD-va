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
    public Item(String name, Vector3d pos, int stack) {
        this.name = name;
        this.pos.set(pos);
        this.definition = getItemDefinition(name);
        this.stack = stack;
        this.inertia.set(randomForceValue(2f), (float) Math.random() * 4f, randomForceValue(2f));
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

    //item being mined with interpolation
    public Item(String name, Vector3d pos, Vector3d goalPos, int stack) {
        this.name = name;
        this.pos.set(pos);
        this.goalPos.set(goalPos);
        this.definition = getItemDefinition(name);
        this.stack = stack;
        this.inertia.set(randomForceValue(2f), (float) Math.random() * 4f, randomForceValue(2f));
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

    //item being mined with life
    public Item(String name, Vector3d pos, int stack, float life) {
        this.name = name;
        this.pos.set(pos);
        this.definition = getItemDefinition(name);
        this.stack = stack;
        this.inertia.set(randomForceValue(2f), (float) Math.random() * 4f, randomForceValue(2f));
        this.rotation.set(0);
        this.hover = 0f;
        this.floatUp = true;
        this.exists = true;
        this.collecting = false;
        this.scale = 1f;
        this.timer = life;
        this.ID = currentID;
        tickUpCurrentID();
    }

    //item with inertia vector when spawned (mined, blown up, etc)
    public Item(String name, Vector3d pos, Vector3f inertia, int stack) {
        this.name = name;
        this.pos.set(pos);
        this.definition = getItemDefinition(name);
        this.stack = stack;
        this.inertia.set(inertia);
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

    //item with inertia vector when spawned (mined, blown up, etc)
    public Item(String name, Vector3d pos, Vector3f inertia, int stack, float life) {
        this.name = name;
        this.pos.set(pos);
        this.definition = getItemDefinition(name);
        this.stack = stack;
        this.inertia.set(inertia);
        this.rotation.set(0);
        this.hover = 0f;
        this.floatUp = true;
        this.exists = true;
        this.collecting = false;
        this.scale = 1f;
        this.timer = life;
        this.ID = currentID;
        tickUpCurrentID();
    }

    //clone item
    public Item(Item thisItem) {
        this.name = thisItem.name;
        if (thisItem.pos != null){
            this.pos.set(thisItem.pos);
        } else {
            this.pos.set(0);
        }
        this.definition = getItemDefinition(name);
        this.stack = thisItem.stack;
        if (thisItem.inertia != null){
            this.inertia.set(thisItem.inertia);
        } else {
            this.inertia.set(0);
        }
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
