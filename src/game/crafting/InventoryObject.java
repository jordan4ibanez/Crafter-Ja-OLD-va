package game.crafting;

import game.item.Item;

public class InventoryObject {
    Item[][] inventory;

    public InventoryObject(int sizeX, int sizeY){
        this.inventory = new Item[sizeY][sizeX];
    }

    public void set(int x, int y, Item newItem){
        inventory[y][x] = newItem;
    }

    public Item get(int x, int y){
        return inventory[y][x];
    }
}
