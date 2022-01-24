package game.entity;

public interface EntityInterface {
    default void onSpawn(Entity entity){
        System.out.println("Welp I'm here now!");
    }

    default void onTick(Entity entity){
        System.out.println("tick tock!");
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
