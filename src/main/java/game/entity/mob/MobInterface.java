package game.entity.mob;

import engine.time.Delta;

//this is an Object which holds methods, amazing
public interface MobInterface {
    //what a mob does each tick
    default void onTick(Mob thisMob, Delta delta){

    }
    //what happens when a mob is spawned
    default void onSpawn(Mob mob, Delta delta){

    }

    //what the mob does when right-clicked
    default void onRightClick(Mob thisMob, Delta delta){

    }

    //what happens when the mob dies
    default void onDeath(Mob thisMob, Delta delta){

    }

    //what the mob does when punched
    default void onPunch(Mob thisMob, Delta delta){

    }
}
