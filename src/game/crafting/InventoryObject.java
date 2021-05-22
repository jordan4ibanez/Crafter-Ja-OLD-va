package game.crafting;

import game.item.Item;
import org.joml.Vector2d;
import org.joml.Vector2i;

public class InventoryObject {
    Item[][] inventory;

    String name;
    //selection is initially off the grid
    Vector2i selection = new Vector2i(-1,-1);
    Vector2d position;
    Vector2i size;
    boolean mainInventory;

    public InventoryObject(String newName, int sizeX, int sizeY, Vector2d position, boolean isMainInventory){
        this.name = newName;
        this.inventory = new Item[sizeY][sizeX];
        this.size = new Vector2i(sizeX, sizeY);
        this.position = position;
        this.mainInventory = isMainInventory;
    }

    public void set(int x, int y, Item newItem){
        inventory[y][x] = newItem;
    }
    public Item get(int x, int y){
        //leak memory to allow modification
        return inventory[y][x];
    }


    public void setSelection(int x, int y){
        selection.x = x;
        selection.y = y;
    }
    public Vector2i getSelection(){
        //don't leak memory
        return new Vector2i(selection);
    }

    public Vector2d getPosition(){
        //don't leak memory
        return new Vector2d(position);
    }

    public Vector2i getSize(){
        //don't leak memory
        return new Vector2i(size);
    }

    public boolean isMainInventory(){
        return mainInventory;
    }
}
