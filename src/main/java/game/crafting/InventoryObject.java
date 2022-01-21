package game.crafting;

import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import engine.network.Networking.getIfMultiplayer;

final public class InventoryObject {

    private final Object2ObjectOpenHashMap<String, String[][]> inventory = new Object2ObjectOpenHashMap<>();
    private final Object2ObjectOpenHashMap<String, int[][]> count = new Object2ObjectOpenHashMap<>();

    //selection is initially off the grid
    private final Object2IntOpenHashMap<String> selectionX = new Object2IntOpenHashMap<>();
    private final Object2IntOpenHashMap<String> selectionY = new Object2IntOpenHashMap<>();

    private final Object2DoubleOpenHashMap<String> posX = new Object2DoubleOpenHashMap<>();
    private final Object2DoubleOpenHashMap<String> posY = new Object2DoubleOpenHashMap<>();

    private final Object2IntOpenHashMap<String> sizeX = new Object2IntOpenHashMap<>();
    private final Object2IntOpenHashMap<String> sizeY = new Object2IntOpenHashMap<>();

    //private final boolean mainInventory;

    public void createInventory(String newName, int newSizeX, int newSizeY, double newPosX, double newPosY, boolean isMainInventory){
        inventory.put(newName, new String[newSizeY][newSizeX]);
        count.put(newName, new int[newSizeY][newSizeX]);

        selectionX.put(newName, -1);
        selectionY.put(newName, -1);

        sizeX.put(newName, newSizeX);
        sizeY.put(newName, newSizeY);

        posX.put(newName, newPosX);
        posY.put(newName, newPosY);
    }


    //external modification
    public void setInventoryItem(String name, int x, int y, String newItem, int newCount){
        inventory.get(name)[y][x] = newItem;
        count.get(name)[y][x] = newCount;
    }

    //mutable
    public String getItemInInventory(String name, int x, int y){
        return inventory.get(name)[y][x];
    }

    //immutable
    public int getInventoryCount(String name, int x, int y){
        return count.get(name)[y][x];
    }

    //external modification
    public void setInventoryCount(String name, int x, int y, int newCount){
        count.get(name)[y][x] = newCount;
    }

    //mutable
    public String[][] getInventoryAsArray(String name){
        return inventory.get(name);
    }

    //mutable
    public int[][] getInventoryCountAsArray(String name){
        return count.get(name);
    }

    //external modification
    public void deleteInventoryItem(String name, int x, int y){
        inventory.get(name)[y][x] = null;
        count.get(name)[y][x] = 0;
    }

    //external modification
    public void clearOutInventory(String name){
        String[][] thisInventory = inventory.get(name);
        int[][] thisCount = count.get(name);
        for (int y = 0; y < thisInventory.length; y++) {
            for (int x = 0; x < thisInventory[0].length; x++) {
                thisInventory[y][x] = null;
                thisCount[y][x] = 0;
            }
        }
    }

    //external modification
    public void setInventorySelection(String name, int x, int y){
        selectionX.put(name, x);
        selectionY.put(name, y);
    }

    //immutable
    public int getInventorySelectionX(String name){
        return selectionX.getInt(name);
    }

    //immutable
    public int getInventorySelectionY(String name){
        return selectionY.getInt(name);
    }

    //immutable
    public double getInventoryPosX(String name){
        return posX.getDouble(name);
    }

    //immutable
    public double getInventoryPosY(String name){
        return posY.getDouble(name);
    }

    //immutable
    public int getInventorySizeX(String name){
        return sizeX.getInt(name);
    }
    //immutable
    public int getInventorySizeY(String name){
        return sizeY.getInt(name);
    }

    //a simple public tool to add items to an inventory
    public boolean addToInventory(String name, String newItemName){
        String[][] thisInventory = inventory.get(name);
        int[][] thisCount = count.get(name);

        //check whole inventory
        for (int y = 0; y < thisInventory.length; y++) {
            for (int x = 0; x < thisInventory[0].length; x++) {
                if (thisInventory[y][x] != null && thisInventory[y][x].equals(newItemName) && thisCount[y][x] < 64){
                    tickUpInventoryStack(name, x,y);
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
                if (thisInventory[y][x] == null){
                    thisInventory[y][x] = newItemName;
                    thisCount[y][x] = 1;
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

    //external modification
    public void removeItemFromInventory(String name, int x, int y){
        String[][] thisInventory = inventory.get(name);
        int[][] thisCount = count.get(name);
        if (thisCount[y][x] > 0) {
            thisCount[y][x]--;
            if (thisCount[y][x] <= 0) {
                thisInventory[y][x] = null;
                thisCount[y][x] = 0;
            }
        } else {
            thisInventory[y][x] = null;
        }
    }

    //internal
    private void tickUpInventoryStack(String name, int x, int y){
        count.get(name)[y][x]++;
    }

}
