package game.crafting;

import org.joml.Vector2d;
import org.joml.Vector2i;

final public class InventoryObject {

    private final String name;

    private final String[][] inventory;
    private final int[][] count;

    private final Vector2i selection = new Vector2i(-1,-1);
    private final Vector2d pos;
    private final Vector2i size;

    private final boolean mainInventory;

    public InventoryObject(String name, Vector2i size, Vector2d pos, boolean mainInventory){
        this.name = name;
        this.inventory = new String[size.y][size.x];
        this.count =  new int[size.y][size.x];
        this.size = new Vector2i();
        this.pos = new Vector2d(pos);
        this.mainInventory = mainInventory;
    }

    public void setItem(int x, int y, String newItem, int newCount){
        inventory[y][x] = newItem;
        count[y][x] = newCount;
    }

    public String getItem(int x, int y){
        return inventory[y][x];
    }

    public int getCount(int x, int y){
        return count[y][x];
    }

    public void setCount(int x, int y, int newCount){
        count[y][x] = newCount;
    }

    public String[][] getInventoryAsArray(){
        return inventory;
    }

    public int[][] getCountAsArray(){
        return count;
    }

    public void deleteItem(int x, int y){
        inventory[y][x] = null;
        count[y][x] = 0;
    }

    public void clear(){
        for (int y = 0; y < size.y; y++) {
            for (int x = 0; x < size.x; x++) {
                inventory[y][x] = null;
                count[y][x] = 0;
            }
        }
    }

    public void setSelection(int x, int y){
        selection.set(x,y);
    }


    public Vector2d getPos(){
        return pos;
    }

    public Vector2i getSize(){
        return size;
    }

    public String getName(){
        return name;
    }

    public boolean addItem(String item){
        //check whole inventory
        for (int y = 0; y < size.y; y++) {
            for (int x = 0; x < size.x; x++) {
                if (inventory[y][x] != null && inventory[y][x].equals(item) && count[y][x] < 64){
                    tickUpStack(x,y);
                    /*
                    if (getIfMultiplayer()){
                        System.out.println("oh nuuuu gotta fix this");
                        //sendServerUpdatedInventory();
                    }
                     */
                    return true;
                }
            }
        }
        //failed to find one, create new stack
        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 9; x++) {
                if (inventory[y][x] == null){
                    inventory[y][x] = item;
                    count[y][x] = 1;
                    /*
                    if (getIfMultiplayer()){
                        System.out.println("oh nuuuu gotta fix this");
                        //sendServerUpdatedInventory();
                    }
                     */
                    return true;
                }
            }
        }
        return false;
    }

    public void removeItem(int x, int y){
        if (count[y][x] > 0) {
            count[y][x]--;
            if (count[y][x] <= 0) {
                inventory[y][x] = null;
                count[y][x] = 0;
            }
        } else {
            inventory[y][x] = null;
        }
    }

    //internal
    private void tickUpStack(int x, int y){
        count[y][x]++;
    }

}
