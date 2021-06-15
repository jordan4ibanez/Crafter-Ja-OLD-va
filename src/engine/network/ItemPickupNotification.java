package engine.network;

public class ItemPickupNotification {
    public String name;
    public int stack;

    public ItemPickupNotification(){
    }

    public ItemPickupNotification(String name, int stack){
        this.name = name;
        this.stack = stack;
    }
}
