package game.item;

import engine.graphics.Mesh;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.Arrays;

import static engine.FancyMath.randomForceValue;
import static game.chunk.ChunkMesh.convertLight;
import static game.item.ItemDefinition.getItemDefinition;

public class Item {

    //KEEP THIS IN THIS CLASS
    //IT'S TOO COMPLEX TO REMOVE THIS OUT OF THIS CLASS!
    private static int currentID = 0;

    public String name;
    public int stack;
    public ItemDefinition definition;
    public Vector3d pos;
    public float scale;
    public float timer;
    public float hover;
    public boolean floatUp;
    public boolean exists;
    public boolean collecting;
    public float collectionTimer = 0;
    public boolean deletionOkay = false;
    public Vector3f rotation;
    public Vector3f inertia;
    public int ID;

    public Mesh mesh;
    public byte light = 15;
    public float lightUpdateTimer = 1f;
    public Vector3i oldFlooredPos = new Vector3i(0,0,0);


    //yes this is ridiculous, but it is also safe
    //internal integer overflow to 0
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
        rebuildLightMesh(this);
        tickUpCurrentID();
    }

    //item being mined
    public Item(String name, Vector3d pos, int stack) {
        this.name = name;
        this.pos = pos;
        this.definition = getItemDefinition(name);
        this.stack = stack;
        this.inertia = new Vector3f(randomForceValue(2f), (float) Math.random() * 4f, randomForceValue(2f));
        this.rotation = new Vector3f(0, 0, 0);
        this.hover = 0f;
        this.floatUp = true;
        this.exists = true;
        this.collecting = false;
        this.scale = 1f;
        this.timer = 0f;
        this.ID = currentID;
        rebuildLightMesh(this);
        tickUpCurrentID();
    }

    //item being mined with life
    public Item(String name, Vector3d pos, int stack, float life) {
        this.name = name;
        this.pos = pos;
        this.definition = getItemDefinition(name);
        this.stack = stack;
        this.inertia = new Vector3f(randomForceValue(2f), (float) Math.random() * 4f, randomForceValue(2f));
        this.rotation = new Vector3f(0, 0, 0);
        this.hover = 0f;
        this.floatUp = true;
        this.exists = true;
        this.collecting = false;
        this.scale = 1f;
        this.timer = life;
        this.ID = currentID;
        rebuildLightMesh(this);
        tickUpCurrentID();
    }

    //item with inertia vector when spawned (mined, blown up, etc)
    public Item(String name, Vector3d pos, Vector3f inertia, int stack) {
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
        rebuildLightMesh(this);
        tickUpCurrentID();
    }

    //item with inertia vector when spawned (mined, blown up, etc)
    public Item(String name, Vector3d pos, Vector3f inertia, int stack, float life) {
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
        this.timer = life;
        this.ID = currentID;
        rebuildLightMesh(this);
        tickUpCurrentID();
    }

    //clone item
    public Item(Item thisItem) {
        this.name = thisItem.name;
        if (thisItem.pos == null){
            this.pos = new Vector3d();
        } else {
            this.pos = new Vector3d(thisItem.pos);
        }
        this.definition = getItemDefinition(name);
        this.stack = thisItem.stack;
        if (thisItem.inertia == null){
            this.inertia = new Vector3f();
        } else {
            this.inertia = new Vector3f(thisItem.inertia);
        }
        this.rotation = new Vector3f(0, 0, 0);
        this.hover = 0f;
        this.floatUp = true;
        this.exists = true;
        this.collecting = false;
        this.scale = 1f;
        this.timer = 0f;
        this.ID = currentID;
        rebuildLightMesh(this);
        tickUpCurrentID();
    }


    //rebuild the items mesh
    public void rebuildLightMesh(Item self) {
        ItemDefinition temp = getItemDefinition(self.name);

        //clone the light array
        float[] newLightArray = new float[temp.lightArray.length];

        //convert the 0-15 light value to 0.0-1.0
        float floatedLightValue = convertLight((float)self.light/15f);

        Arrays.fill(newLightArray, floatedLightValue);

        if (self.mesh != null){
            self.mesh.cleanUp(false);
        }

        self.mesh = new Mesh(temp.positionsArray, newLightArray, temp.indicesArray, temp.textureCoordArray, temp.texture);
    }
}
