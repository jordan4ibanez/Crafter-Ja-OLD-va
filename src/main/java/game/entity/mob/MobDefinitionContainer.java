package game.entity.mob;

import java.util.HashMap;

public class MobDefinitionContainer {
    private final HashMap<String, MobDefinition> definition = new HashMap<>();

    public void add(MobDefinition mobDefinition){
        definition.put(mobDefinition.getName(), mobDefinition);
    }
}
