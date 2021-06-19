package engine.network;

public class NetworkInventory {
    public String name;
    public String[][] inventory;

    public NetworkInventory(int x, int y){
        this.inventory = new String[x][y];
    }
}
