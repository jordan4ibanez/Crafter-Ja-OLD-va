package game.item;

import org.joml.Vector3d;

import static engine.sound.SoundAPI.playSound;
import static game.blocks.BlockDefinition.isWalkable;
import static game.chunk.Chunk.getBlock;
import static game.chunk.Chunk.setBlock;
import static game.item.ItemDefinition.registerItem;
import static game.crafting.Inventory.removeItemFromInventory;
import static game.player.Player.getCurrentInventorySelection;
import static game.player.Player.getPlayerDir;

public class ItemRegistration {

    private final static String[] materials = new String[]{
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

    public static void registerItems(){

        int toolLevel = 2;
        for (String material : materials) {
            registerItem(material + "pick", "textures/tools/" + material + "pick.png", null, toolLevel,0,0,0);
            registerItem(material + "shovel", "textures/tools/" + material + "shovel.png", null,0,toolLevel,0,0);
            registerItem(material + "axe", "textures/tools/" + material + "axe.png", null,0,0,toolLevel,0);

            if (!material.equals("wood") && !material.equals("stone")){
                registerItem(material, "textures/items/" + material + ".png", null);
            }

            toolLevel++;
        }


        ItemModifier test = new ItemModifier() {
            @Override
            public void onPlace(Vector3d pos) {
                if (isWalkable(getBlock((int)pos.x,(int)pos.y - 1, (int) pos.z))) {
                    byte rot = getPlayerDir();
                    setBlock((int) pos.x, (int) pos.y + 1, (int) pos.z, 23, rot);
                    setBlock((int) pos.x, (int) pos.y, (int) pos.z, 24, rot);
                    playSound("wood_1");

                    removeItemFromInventory(getCurrentInventorySelection(), 0);
                }
            }
        };

        registerItem("door", "textures/door.png", test);

        registerItem("boat", "textures/boatitem.png", null);

        registerItem("stick", "textures/items/stick.png", null);
    }
}
