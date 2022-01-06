package game.crafting;

import static engine.network.Networking.getIfMultiplayer;

public class InventoryObject {
    private final String[][] inventory;
    private final int[][] count;

    private final String name;
    //selection is initially off the grid
    private int selectionX = -1;
    private int selectionY = -1;

    private final double posX;
    private final double posY;

    private final int sizeX;
    private final int sizeY;
    private final boolean mainInventory;

    public InventoryObject(String newName, int sizeX, int sizeY, double posX, double posY, boolean isMainInventory){
        this.name = newName;
        this.inventory = new String[sizeY][sizeX];
        this.count = new int[sizeY][sizeX];
        this.sizeX = sizeX;
        this.sizeY = sizeY;

        this.posX = posX;
        this.posY = posY;
        this.mainInventory = isMainInventory;
    }


    //immutable
    public void set(int x, int y, String newItem, int newCount){
        this.inventory[y][x] = newItem;
        this.count[y][x] = newCount;
    }

    //mutable
    public String getItem(int x, int y){
        return this.inventory[y][x];
    }

    //immutable
    public int getCount(int x, int y){
        return this.count[y][x];
    }

    public void setCount(int x, int y, int newCount){
        this.count[y][x] = newCount;
    }

    //immutable
    public void delete(int x, int y){
        this.inventory[y][x] = null;
        this.count[y][x] = 0;
    }
    //immutable
    public void clear(){
        for (int y = 0; y < this.inventory.length; y++) {
            for (int x = 0; x < this.inventory[0].length; x++) {
                inventory[y][x] = null;
                count[y][x] = 0;
            }
        }
    }

    //internal modification
    public void setSelection(int x, int y){
        this.selectionX = x;
        this.selectionY = y;
    }

    //mutable
    public String getName(){
        return this.name;
    }

    //immutable
    public int getSelectionX(){
        return this.selectionX;
    }
    //immutable
    public int getSelectionY(){
        return this.selectionY;
    }

    //immutable
    public double getPosX(){
        return this.posX;
    }

    //immutable
    public double getPosY(){
        return this.posY;
    }

    //immutable
    public int getSizeX(){
        return this.sizeX;
    }
    //immutable
    public int getSizeY(){
        return this.sizeY;
    }

    //immutable
    public boolean isMainInventory(){
        return this.mainInventory;
    }

    //a simple internal tool to add items to an inventory
    public boolean addToInventory(String name){
        //check whole inventory
        for (int y = 0; y < this.inventory.length; y++) {
            for (int x = 0; x < this.inventory[0].length; x++) {
                if (this.inventory[y][x] != null && this.inventory[y][x].equals(name) && this.count[y][x] < 64){
                    tickUpStack(x,y);
                    if (getIfMultiplayer()){
                        System.out.println("oh nuuuu gotta fix this");
                        //sendServerUpdatedInventory();
                    }
                    return true;
                }
            }
        }
        //failed to find one, create new stack
        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 9; x++) {
                if (inventory[y][x] == null){
                    inventory[y][x] = name;
                    count[y][x] = 1;
                    if (getIfMultiplayer()){
                        System.out.println("oh nuuuu gotta fix this");
                        //sendServerUpdatedInventory();
                    }
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
        this.count[y][x]++;
    }


}
