package game.item;

import engine.graph.Mesh;
import org.joml.Vector3f;

import static engine.FancyMath.randomForceValue;
import static game.item.ItemDefinition.getItemDefinition;

public class Item {
    private static int currentID = 0;

    public String name;
    public int stack;
    public ItemDefinition definition;
    public Vector3f pos;
    public float scale;
    public float timer;
    public float hover;
    public boolean floatUp;
    public boolean exists;
    public boolean collecting;
    public Vector3f rotation;
    public Vector3f inertia;
    public int ID;
    public final Mesh mesh;

    //inventory item
    public Item(String name, int stack){
        this.name = name;
        this.definition = getItemDefinition(name);
        this.stack = stack;
        this.mesh = getItemDefinition(name).mesh;
    }

    //item being mined
    public Item(String name, Vector3f pos, int stack) {
        this.name = name;
        this.pos = pos;
        this.definition = getItemDefinition(name);
        this.stack = stack;
        this.inertia = new Vector3f(randomForceValue(9f), (float) Math.random() * 10f, randomForceValue(9f));
        this.rotation = new Vector3f(0, 0, 0);
        this.hover = 0f;
        this.floatUp = true;
        this.exists = true;
        this.collecting = false;
        this.scale = 1f;
        this.timer = 0f;
        this.ID = currentID;
        this.mesh = getItemDefinition(name).mesh;
        currentID++;
    }

    //item with inertia vector when spawned (mined, blown up, etc)
    public Item(String name, Vector3f pos, Vector3f inertia, int stack) {
        this.name = name;
        this.pos = pos;
        this.definition = getItemDefinition(name);
        this.stack = stack;
        this.inertia = inertia;
        this.rotation = new Vector3f(0, 0, 0);
        this.hover = 0f;
        this.floatUp = true;
        this.exists = true;
        this.collecting = false;
        this.scale = 1f;
        this.timer = 0f;
        this.ID = currentID;
        this.mesh = getItemDefinition(name).mesh;
        currentID++;
    }

    //this is for the ItemEntity class to create
    //a new itemID in it's map without overwriting.
    //works in conjunction with this class.
    public static int getCurrentID(){
        return currentID;
    }
}
