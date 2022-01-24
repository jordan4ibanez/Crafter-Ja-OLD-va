package game.entity;

import engine.time.Delta;
import game.player.Player;

public interface EntityInterface {
    default void onSpawn(Entity entity){
        System.out.println("Welp I'm here now!");
    }

    default void onTick(Entity entity, Delta delta, Player player){
        System.out.println("tick tock!");
    }

    default void hurt(Entity entity, int damage){
        System.out.println("That hurt " + damage + " amount!");
    }

    default void onHeal(Entity entity){
        System.out.println("I'm feeling a bit better!");
    }

    default void onHurt(Entity entity){
        System.out.println("Ouch!");
    }

    default void onDeath(Entity entity){
        System.out.println("Oof I am le dead!");
    }

    default void sayBye(Entity entity){
        System.out.println("Welp see you later!");
    }
}
