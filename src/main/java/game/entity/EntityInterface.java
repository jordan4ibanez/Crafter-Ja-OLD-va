package game.entity;

public interface EntityInterface {

    default void onSpawn(){
        System.out.println("Welp I'm here now!");
    }

    default void onTick(){
        System.out.println("tick tock!");
    }

    default void hurt(int damage){
        System.out.println("That hurt " + damage + " amount!");
    }

    default void onHeal(){
        System.out.println("I'm feeling a bit better!");
    }

    default void onHurt(){
        System.out.println("Ouch!");
    }

    default void onDeath(){
        System.out.println("Oof I am le dead!");
    }

    default void sayHi(){
        System.out.println("hello hello hello hello");
    }

    default void sayBye(){
        System.out.println("Welp see you later!");
    }
}
