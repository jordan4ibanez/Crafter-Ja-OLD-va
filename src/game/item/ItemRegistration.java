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

    public static void registerTools(){

        for (String material : materials) {
            registerItem(material + "pickaxe", "textures/tools/" + material + "pick.png", null);
            registerItem(material + "shovel", "textures/tools/" + material + "shovel.png", null);
            registerItem(material + "axe", "textures/tools/" + material + "axe.png", null);

            if (!material.equals("wood") && !material.equals("stone")){
                registerItem(material, "textures/items/" + material + ".png", null);
            }


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

        registerItem("Door", "textures/door.png", test);

        registerItem("Boat", "textures/boatitem.png", null);
    }
}
