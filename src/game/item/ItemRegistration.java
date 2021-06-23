package game.item;

import org.joml.Vector3d;
import org.joml.Vector3i;

import static engine.sound.SoundAPI.playSound;
import static game.blocks.BlockDefinition.isWalkable;
import static game.chunk.Chunk.*;
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
            public void onPlace(Vector3i pos, Vector3i ignore) {
                if (isWalkable(getBlock(pos.x, pos.y - 1, pos.z))) {
                    byte rot = getPlayerDir();
                    setBlock(pos.x, pos.y + 1, pos.z, (byte) 23, rot);
                    setBlock(pos.x, pos.y, pos.z, (byte) 24, rot);
                    playSound("wood_1");

                    removeItemFromInventory(getCurrentInventorySelection(), 0);
                }
            }
        };

        registerItem("door", "textures/door.png", test);

        registerItem("boat", "textures/boatitem.png", null);

        registerItem("stick", "textures/items/stick.png", null);

        ItemModifier torchPlace = new ItemModifier() {
            @Override
            public void onPlace(Vector3i pos, Vector3i pointedAbove) {
                Vector3i pointing = pos.sub(pointedAbove);

                if (pointing.x > 0){
                    placeBlock(pointedAbove.x, pointedAbove.y, pointedAbove.z, (byte)29, (byte) 0);
                    System.out.println("torch: 0");
                } else if (pointing.x < 0) {
                    placeBlock(pointedAbove.x, pointedAbove.y, pointedAbove.z, (byte) 29, (byte) 1);
                    System.out.println("torch: 1");
                } else if (pointing.z > 0) {
                    placeBlock(pointedAbove.x, pointedAbove.y, pointedAbove.z, (byte) 29, (byte) 2);
                    System.out.println("torch: 2");
                } else if (pointing.z < 0) {
                    placeBlock(pointedAbove.x, pointedAbove.y, pointedAbove.z, (byte) 29, (byte) 3);
                    System.out.println("torch: 3");
                } else if (pointing.y < 0) {
                    placeBlock(pointedAbove.x, pointedAbove.y, pointedAbove.z, (byte) 29, (byte) 4);
                    System.out.println("torch: 4");
                }


            }
        };

        registerItem("torchItem", "textures/torch.png", torchPlace);
    }
}
