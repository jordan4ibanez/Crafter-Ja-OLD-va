package game.item;

import engine.graph.Mesh;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.Arrays;

import static engine.FancyMath.randomForceValue;
import static game.chunk.ChunkMesh.getTextureAtlas;
import static game.item.ItemDefinition.getItemDefinition;

public class Item {
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
    public Vector3f rotation;
    public Vector3f inertia;
    public int ID;

    public Mesh mesh;
    public byte light = 15;
    public float lightUpdateTimer = 1f;
    public Vector3i oldFlooredPos = new Vector3i(0,0,0);


    //inventory item
    public Item(String name, int stack){
        this.name = name;
        this.definition = getItemDefinition(name);
        this.stack = stack;

        rebuildLightMesh(this);
    }

    //item being mined
    public Item(String name, Vector3d pos, int stack) {
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

        rebuildLightMesh(this);

        currentID++;
    }

    //item being mined with life
    public Item(String name, Vector3d pos, int stack, float life) {
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
        this.timer = life;
        this.ID = currentID;

        rebuildLightMesh(this);
        currentID++;
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
        currentID++;
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
        currentID++;
    }


    public void rebuildLightMesh(Item self) {
        ItemDefinition temp = getItemDefinition(self.name);

        //clean up old mesh - this causes a texture null pointer
        /*
        if (self.mesh != null){
            //don't delete the world texture
            boolean cleanUpTexture = (temp.texture != getTextureAtlas());
            System.out.println(cleanUpTexture);
            self.mesh.cleanUp(cleanUpTexture);
        }
         */

        //clone the light array
        float[] newLightArray = new float[temp.lightArray.length];

        //convert the 0-15 light value to 0.0-1.0
        float floatedLightValue = (float)self.light/15f;
        //System.out.println(self.light);

        Arrays.fill(newLightArray, floatedLightValue);

        self.mesh = new Mesh(temp.positionsArray, newLightArray, temp.indicesArray, temp.textureCoordArray, temp.texture);
    }

    //this is for the ItemEntity class to create
    //a new itemID in it's map without overwriting.
    //works in conjunction with this class.
    public static int getCurrentID(){
        return currentID;
    }
}
