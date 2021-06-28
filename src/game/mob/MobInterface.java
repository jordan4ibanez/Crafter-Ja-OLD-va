package game.mob;

public interface MobInterface {
    //what a mob does each tick
    default void onTick(MobObject thisMob){
    }
    //thrown into the mob spawning algorithm
    default boolean spawnCalculation(){
        return false;
    }
}
