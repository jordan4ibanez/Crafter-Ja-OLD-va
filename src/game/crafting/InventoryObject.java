package game.crafting;

import game.item.Item;
import org.joml.Vector2d;

public class InventoryObject {
    Item[][] inventory;
    Vector2d position;

    public InventoryObject(int sizeX, int sizeY, Vector2d position){
        this.inventory = new Item[sizeY][sizeX];
        this.position = position;
    }

    public void set(int x, int y, Item newItem){
        inventory[y][x] = newItem;
    }

    public Item get(int x, int y){
        return inventory[y][x];
    }
}
