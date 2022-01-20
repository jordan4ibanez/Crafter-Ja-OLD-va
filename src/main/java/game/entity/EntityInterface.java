package game.entity;

public interface EntityInterface {
    default void sayBye(){
        System.out.println("Welp see you later");
    }
}
