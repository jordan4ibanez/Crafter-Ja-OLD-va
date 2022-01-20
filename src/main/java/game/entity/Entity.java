package game.entity;

public class Entity implements EntityInterface{
    public void sayHi(){
        System.out.println("hi there");
        this.sayBye();
    }
}
