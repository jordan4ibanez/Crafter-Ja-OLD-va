package game.mob;

public interface MobInterface {
    //what a mob does each tick
    default void onTick(MobObject thisMob){
    }
    //what happens when a mob is spawned
    default boolean onSpawn(){
        return false;
    }

    //what the mob does when right clicked
    default void onRightClick(MobObject thisMob){

    }

    //what happens when the mob dies
    default void onDeath(MobObject thisMob){

    }

    //what the mob does when punched
    default void onPunch(MobObject thisMob){

    }
}
