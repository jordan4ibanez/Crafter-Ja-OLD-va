package game.mob;

public interface MobInterface {
    //what a mob does each tick
    default void onTick(int thisMob){

    }
    //what happens when a mob is spawned
    default void onSpawn(){

    }

    //what the mob does when right clicked
    default void onRightClick(int thisMob){

    }

    //what happens when the mob dies
    default void onDeath(int thisMob){

    }

    //what the mob does when punched
    default void onPunch(int thisMob){

    }
}
