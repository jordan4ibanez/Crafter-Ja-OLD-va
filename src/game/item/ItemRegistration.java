package game.item;

import org.joml.Vector3f;

import static engine.sound.SoundAPI.playSound;
import static game.blocks.BlockDefinition.isWalkable;
import static game.chunk.Chunk.getBlock;
import static game.chunk.Chunk.setBlock;
import static game.item.ItemDefinition.registerItem;
import static game.player.Inventory.removeItemFromInventory;
import static game.player.Player.getCurrentInventorySelection;
import static game.player.Player.getPlayerDir;

public class ItemRegistration {
    public static void registerTools(){

        registerItem("stone_pickaxe", "textures/stone_pickaxe.png", null);


        ItemModifier test = new ItemModifier() {
            @Override
            public void onPlace(Vector3f pos) {
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
    }
}
