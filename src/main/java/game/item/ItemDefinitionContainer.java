package game.item;

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

            new ItemDefinition(material + "pick", "textures/tools/" + material + "pick.png", null, toolLevel, 0, 0, 0);

            new ItemDefinition(material + "shovel", "textures/tools/" + material + "shovel.png", null, 0, toolLevel, 0, 0);

            new ItemDefinition(material + "axe", "textures/tools/" + material + "axe.png", null, 0, 0, toolLevel, 0);

            if (!material.equals("wood") && !material.equals("stone")) {
                new ItemDefinition(material, "textures/items/" + material + ".png", null);
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

        new ItemDefinition("door", "textures/door.png", test);

        new ItemDefinition("boat", "textures/boatitem.png", null);

        new ItemDefinition("stick", "textures/items/stick.png", null);

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

        new ItemDefinition("torchItem", "textures/torch.png", torchPlace);
    }
}
