package game.entity;

import java.util.ArrayList;
import java.util.List;

public class EntityContainer {

    private final List<Entity> entities = new ArrayList<>();

    public void remove(Entity entity){
        entities.remove(entity);
    }

    public void add(Entity entity){
        entities.add(entity);
    }

    public List<Entity> getAll(){
        return entities;
    }

}
