package game.entity.item;

import engine.graphics.Mesh;
import game.blocks.BlockDefinitionContainer;
import org.joml.Vector3i;

import java.util.HashMap;

import static engine.sound.SoundAPI.playSound;
import static game.chunk.Chunk.*;
import static game.chunk.Chunk.placeBlock;
import static game.crafting.InventoryObject.removeItemFromInventory;
import static game.player.Player.getCurrentInventorySelection;
import static game.player.Player.getPlayerDir;

public class ItemDefinitionContainer {
    private final HashMap<String, ItemDefinition> definitions = new HashMap<>();

    public Mesh getMesh(String name){
        return definitions.get(name).getMesh();
    }

    public ItemModifier getModifier(String name){
        return definitions.get(name).getModifier();
    }

    public boolean isItem(String name){
        return definitions.get(name).isItem();
    }

    public float getStoneMiningLevel(String name){
        return definitions.get(name).getStoneMiningLevel();
    }

    public float getDirtMiningLevel(String name){
        return definitions.get(name).getDirtMiningLevel();
    }

    public float getWoodMiningLevel(String name){
        return definitions.get(name).getWoodMiningLevel();
    }

    public float getLeafMiningLevel(String name){
        return definitions.get(name).getLeafMiningLevel();
    }

    public int blockID(String name){
        return definitions.get(name).blockID();
    }

    public boolean isRightClickable(String name){
        return definitions.get(name).isRightClickable();
    }

    public boolean isOnPlaced(String name){
        return definitions.get(name).isOnPlaced();
    }

    public boolean isTool(String name){
        return definitions.get(name).isTool();
    }


    public ItemDefinitionContainer(){
        final String[] materials = new String[]{
                "wood",
                "coal",
                "stone",
                "iron",
                "gold",
                "lapis",
                "diamond",
                "emerald",
                "sapphire",
                "ruby",
        };

        int toolLevel = 2;

        for (String material : materials) {

            definitions.put( material + "pick", new ItemDefinition(material + "pick", "textures/tools/" + material + "pick.png", null, toolLevel, 0, 0, 0));

            definitions.put( material + "shovel", new ItemDefinition(material + "shovel", "textures/tools/" + material + "shovel.png", null, 0, toolLevel, 0, 0));

            definitions.put( material + "axe", new ItemDefinition(material + "axe", "textures/tools/" + material + "axe.png", null, 0, 0, toolLevel, 0));

            if (!material.equals("wood") && !material.equals("stone")) {
                definitions.put( material, new ItemDefinition(material, "textures/items/" + material + ".png", null));
            }

            toolLevel++;
        }


        ItemModifier test = new ItemModifier() {
            @Override
            public void onPlace(Vector3i pos, Vector3i above, BlockDefinitionContainer definitionContainer) {
                if (definitionContainer.getWalkable(getBlock(pos.x, pos.y - 1, pos.z))) {
                    byte rot = getPlayerDir();
                    setBlock(pos.x, pos.y + 1, pos.z, (byte) 23, rot);
                    setBlock(pos.x, pos.y, pos.z, (byte) 24, rot);
                    playSound("wood_1");

                    removeItemFromInventory("main", getCurrentInventorySelection(), 0);
                }
            }
        };

        definitions.put( "door", new ItemDefinition("door", "textures/door.png", test));

        definitions.put( "boat", new ItemDefinition("boat", "textures/boatitem.png", null));

        definitions.put( "stick", new ItemDefinition("stick", "textures/items/stick.png", null));

        ItemModifier torchPlace = new ItemModifier() {
            @Override
            public void onPlace(Vector3i pos, Vector3i above, BlockDefinitionContainer blockDefinitionContainer) {
                int pointingX = pos.x - above.x;
                int pointingY = pos.y - above.y;
                int pointingZ = pos.z - above.z;


                if (pointingX > 0) {
                    placeBlock(above.x, above.y, above.z, (byte) 29, (byte) 0);
                    System.out.println("torch: 0");
                } else if (pointingX < 0) {
                    placeBlock(above.x, above.y, above.z, (byte) 29, (byte) 1);
                    System.out.println("torch: 1");
                } else if (pointingZ > 0) {
                    placeBlock(above.x, above.y, above.z, (byte) 29, (byte) 2);
                    System.out.println("torch: 2");
                } else if (pointingZ < 0) {
                    placeBlock(above.x, above.y, above.z, (byte) 29, (byte) 3);
                    System.out.println("torch: 3");
                } else if (pointingY < 0) {
                    placeBlock(above.x, above.y, above.z, (byte) 29, (byte) 4);
                    System.out.println("torch: 4");
                }


            }
        };

        definitions.put( "torchItem", new ItemDefinition("torchItem", "textures/torch.png", torchPlace));
    }
}
