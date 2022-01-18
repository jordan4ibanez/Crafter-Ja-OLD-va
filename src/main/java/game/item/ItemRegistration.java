package game.item;

import static engine.sound.SoundAPI.playSound;
import static game.blocks.BlockDefinition.isBlockWalkable;
import static game.chunk.Chunk.*;
import static game.crafting.InventoryObject.removeItemFromInventory;
import static game.item.ItemDefinition.*;
import static game.player.Player.getCurrentInventorySelection;
import static game.player.Player.getPlayerDir;

final public class ItemRegistration {

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
            registerToolDefinition(material + "pick", "textures/tools/" + material + "pick.png", null, toolLevel,0,0,0);
            registerToolDefinition(material + "shovel", "textures/tools/" + material + "shovel.png", null,0,toolLevel,0,0);
            registerToolDefinition(material + "axe", "textures/tools/" + material + "axe.png", null,0,0,toolLevel,0);

            if (!material.equals("wood") && !material.equals("stone")){
                registerItemDefinition(material, "textures/items/" + material + ".png", null);
            }

            toolLevel++;
        }


        ItemModifier test = new ItemModifier() {
            @Override
            public void onPlace(int posX, int posY, int posZ, int abovePosX, int abovePosY, int abovePosZ) {
                if (isBlockWalkable(getBlock(posX, posY - 1, posZ))) {
                    byte rot = getPlayerDir();
                    setBlock(posX,posY + 1, posZ, (byte) 23, rot);
                    setBlock(posX, posY, posZ, (byte) 24, rot);
                    playSound("wood_1");

                    removeItemFromInventory("main", getCurrentInventorySelection(), 0);
                }
            }
        };

        registerItemDefinition("door", "textures/door.png", test);

        registerItemDefinition("boat", "textures/boatitem.png", null);

        registerItemDefinition("stick", "textures/items/stick.png", null);

        ItemModifier torchPlace = new ItemModifier() {
            @Override
            public void onPlace(int posX, int posY, int posZ, int abovePosX, int abovePosY, int abovePosZ) {
                int pointingX = posX - abovePosX;
                int pointingY = posY - abovePosY;
                int pointingZ = posZ - abovePosZ;


                if (pointingX > 0){
                    placeBlock(abovePosX, abovePosY, abovePosZ, (byte)29, (byte) 0);
                    System.out.println("torch: 0");
                } else if (pointingX < 0) {
                    placeBlock(abovePosX, abovePosY, abovePosZ, (byte) 29, (byte) 1);
                    System.out.println("torch: 1");
                } else if (pointingZ > 0) {
                    placeBlock(abovePosX, abovePosY, abovePosZ, (byte) 29, (byte) 2);
                    System.out.println("torch: 2");
                } else if (pointingZ < 0) {
                    placeBlock(abovePosX, abovePosY, abovePosZ, (byte) 29, (byte) 3);
                    System.out.println("torch: 3");
                } else if (pointingY < 0) {
                    placeBlock(abovePosX, abovePosY, abovePosZ, (byte) 29, (byte) 4);
                    System.out.println("torch: 4");
                }


            }
        };

        registerItemDefinition("torchItem", "textures/torch.png", torchPlace);
    }
}
