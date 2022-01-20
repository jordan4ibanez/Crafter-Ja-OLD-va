package game.blocks;

import java.util.ArrayList;

import static engine.sound.SoundAPI.playSound;
import static game.chunk.Chunk.*;
import static game.chunk.Chunk.placeBlock;
import static game.crafting.InventoryLogic.openCraftingInventory;
import static game.falling.FallingEntity.createFallingEntity;
import static game.item.ItemEntity.throwItem;
import static game.light.Light.torchFloodFill;
import static game.tnt.TNTEntity.createTNT;

public class BlockDefinitionContainer {

    private final ArrayList<BlockDefinition> blocks = new ArrayList<>();

    public float getStoneHardness(int ID){
        return blocks.get(ID).getStoneHardness();
    }

    public float getDirtHardness(int ID){
        return blocks.get(ID).getDirtHardness();
    }

    public float getWoodHardness(int ID){
        return blocks.get(ID).getWoodHardness();
    }

    public float getLeafHardness(int ID){
        return blocks.get(ID).getLeafHardness();
    }

    public String getName(int ID){
        return blocks.get(ID).getBlockName();
    }

    public boolean dropsItem(int ID){
        return blocks.get(ID).getDropsItem();
    }

    public float[] getFrontTexturePoints(int ID, byte rot){
        return blocks.get(ID).getFrontTexturePoints(rot);
    }

    public float[] getBackTexturePoints(int ID, byte rot){
        return blocks.get(ID).getBackTexturePoints(rot);
    }

    public float[] getRightTexturePoints(int ID, byte rot){
        return blocks.get(ID).getRightTexturePoints(rot);
    }

    public float[] getLeftTexturePoints(int ID, byte rot){
        return blocks.get(ID).getLeftTexturePoints(rot);
    }

    public float[] getTopTexturePoints(int ID){
        return blocks.get(ID).getTopTexturePoints();
    }

    public float[] getBottomTexturePoints(int ID){
        return blocks.get(ID).getBottomTexturePoints();
    }

    public float[][] getShape(int ID, byte rot){
        return blocks.get(ID).getBlockShape(rot);
    }

    public byte getDrawType(int ID){
        return blocks.get(ID).getDrawType();
    }

    public boolean getWalkable(int ID){
        return blocks.get(ID).isBlockWalkable();
    }

    public boolean getSteppable(int ID){
        return blocks.get(ID).isSteppable();
    }

    public boolean isLiquid(int ID){
        return blocks.get(ID).isBlockLiquid();
    }

    public String getPlaceSound(int ID){
        return blocks.get(ID).getPlaceSound();
    }

    public String getDigSound(int ID){
        return blocks.get(ID).getDigSound();
    }

    public boolean isRightClickable(int ID){
        return blocks.get(ID).getRightClickable();
    }

    public boolean isOnPlaced(int ID){
        return blocks.get(ID).getIsOnPlaced();
    }

    public float getViscosity(int ID){
        return blocks.get(ID).getBlockViscosity();
    }

    public boolean isPointable(int ID){
        return blocks.get(ID).isBlockPointable();
    }

    public String getDroppedItem(int ID){
        return blocks.get(ID).getDroppedItem();
    }

    public BlockModifier getBlockModifier(int ID){
        return blocks.get(ID).getBlockModifier();
    }

    public BlockDefinitionContainer() {
        //stair
        float[][] stairShape = new float[][]{
                {0f,0f,0f,1f,0.5f,1f},
                {0f,0f,0f,1f,1f,0.5f}
        };

        //slab
        float[][] slabShape = new float[][]{
                {0f,0f,0f,1f,0.5f,1f}
        };

        //door open
        float[][] doorOpen = new float[][]{
                {0f,0f,0f,2f/16f,1f,1f}
        };

        //door closed
        float[][] doorClosed = new float[][]{
                {0f,0f,14f/16f,1f,1f,1f}
        };

        //vertical slab
        float[][] verticalSlab = new float[][]{
                {0f,0f,0f,1f,1f,0.5f}
        };

        blocks.add(
        new BlockDefinition(
                (byte) 0,
                -1f,
                -1f,
                -1f,
                -1f,
                "air",
                false,
                new byte[]{-1,-1}, //front
                new byte[]{-1,-1}, //back
                new byte[]{-1,-1}, //right
                new byte[]{-1,-1}, //left
                new byte[]{-1,-1}, //top
                new byte[]{-1,-1},  //bottom
                null,
                (byte) 0,
                false,
                false,
                false,
                "",
                "",
                false,
                false,
                0,
                false,
                null,
                null
        )
        );

        blocks.add(
        new BlockDefinition(
                (byte) 1,
                0f,
                1f,
                0f,
                0f,
                "dirt",
                true,
                new byte[]{0,0}, //front
                new byte[]{0,0}, //back
                new byte[]{0,0}, //right
                new byte[]{0,0}, //left
                new byte[]{0,0}, //top
                new byte[]{0,0},  //bottom
                null,
                (byte) 1,
                true,
                false,
                false,
                "dirt_1",
                "dirt_2",
                false,
                false,
                0,
                true,
                null,
                null
        )
        );

        blocks.add(
        new BlockDefinition(
                (byte) 2,
                0,
                2f,
                0,
                0,
                "grass",
                true,
                new byte[]{5,0}, //front
                new byte[]{5,0}, //back
                new byte[]{5,0}, //right
                new byte[]{5,0}, //left
                new byte[]{4,0}, //top
                new byte[]{0,0},  //bottom
                null,
                (byte) 1,
                true,
                false,
                false,
                "dirt_1",
                "dirt_2",
                false,
                false,
                0,
                true,
                "dirt",
                null
        )
        );

        blocks.add(
        new BlockDefinition(
                (byte) 3,
                1,
                0,
                0,
                0,
                "stone",
                true,
                new byte[]{1,0}, //front
                new byte[]{1,0}, //back
                new byte[]{1,0}, //right
                new byte[]{1,0}, //left
                new byte[]{1,0}, //top
                new byte[]{1,0},  //bottom
                null,
                (byte) 1,
                true,
                false,
                false,
                "stone_1",
                "stone_2",
                false,
                false,
                0,
                true,
                "cobble",
                null
        )
        );

        blocks.add(
        new BlockDefinition(
                (byte) 4,
                1.5f,
                0,
                0,
                0,
                "cobble",
                true,
                new byte[]{2,0}, //front
                new byte[]{2,0}, //back
                new byte[]{2,0}, //right
                new byte[]{2,0}, //left
                new byte[]{2,0}, //top
                new byte[]{2,0},  //bottom
                null,
                (byte) 1,
                true,
                false,
                false,
                "stone_3",
                "stone_2",
                false,
                false,
                0,
                true,
                null,
                null
        )
        );

        blocks.add(
        new BlockDefinition(
                (byte) 5,
                -1,
                -1,
                -1,
                -1,
                "bedrock",
                false,
                new byte[]{6,0}, //front
                new byte[]{6,0}, //back
                new byte[]{6,0}, //right
                new byte[]{6,0}, //left
                new byte[]{6,0}, //top
                new byte[]{6,0},  //bottom
                null,
                (byte) 1,
                true,
                false,
                false,
                "stone_1",
                "stone_1",
                false,
                false,
                0,
                true,
                null,
                null
        )
        );


        //tnt explosion
        BlockModifier kaboom = new BlockModifier() {
            @Override
            public void onDig(double posX, double posY, double posZ) {
                createTNT(posX, posY, posZ, 0, true);
            }
        };

        blocks.add(
        new BlockDefinition(
                (byte) 6,
                0,
                0,
                2,
                0,
                "tnt",
                false,
                new byte[]{7,0}, //front
                new byte[]{7,0}, //back
                new byte[]{7,0}, //right
                new byte[]{7,0}, //left
                new byte[]{8,0}, //top
                new byte[]{9,0},  //bottom
                null,
                (byte) 1,
                true,
                false,
                false,
                "dirt_1",
                "wood_2",
                false,
                false,
                0,
                true,
                null,
                kaboom
        )
        );

        blocks.add(
        new BlockDefinition(
                (byte) 7,
                -1,
                -1,
                -1,
                -1,
                "water",
                false,
                new byte[]{10,0}, //front
                new byte[]{10,0}, //back
                new byte[]{10,0}, //right
                new byte[]{10,0}, //left
                new byte[]{10,0}, //top
                new byte[]{10,0},  //bottom
                null,
                (byte) 8, //liquid source
                false,
                false,
                true,
                "",
                "",
                false,
                false,
                40,
                false,
                null,
                null
        )
        );

        blocks.add(
        new BlockDefinition(
                (byte) 8,
                4,
                0,
                0,
                0,
                "coal ore",
                true,
                new byte[]{11,0}, //front
                new byte[]{11,0}, //back
                new byte[]{11,0}, //right
                new byte[]{11,0}, //left
                new byte[]{11,0}, //top
                new byte[]{11,0},  //bottom
                null,
                (byte) 1,
                true,
                false,
                false,
                "stone_1",
                "stone_2",
                false,
                false,
                0,
                true,
                "coal",
                null
        )
        );

        blocks.add(
        new BlockDefinition(
                (byte) 9,
                6,
                0,
                0,
                0,
                "iron ore",
                true,
                new byte[]{12,0}, //front
                new byte[]{12,0}, //back
                new byte[]{12,0}, //right
                new byte[]{12,0}, //left
                new byte[]{12,0}, //top
                new byte[]{12,0},  //bottom
                null,
                (byte) 1,
                true,
                false,
                false,
                "stone_1",
                "stone_2",
                false,
                false,
                0,
                true,
                null,
                null
        )
        );

        blocks.add(
        new BlockDefinition(
                (byte) 10,
                8,
                0,
                0,
                0,
                "gold ore",
                true,
                new byte[]{13,0}, //front
                new byte[]{13,0}, //back
                new byte[]{13,0}, //right
                new byte[]{13,0}, //left
                new byte[]{13,0}, //top
                new byte[]{13,0},  //bottom
                null,
                (byte) 1,
                true,
                false,
                false,
                "stone_1",
                "stone_2",
                false,
                false,
                0,
                true,
                null,
                null
        )
        );

        blocks.add(
        new BlockDefinition(
                (byte) 11,
                10,
                0,
                0,
                0,
                "diamond ore",
                true,
                new byte[]{14,0}, //front
                new byte[]{14,0}, //back
                new byte[]{14,0}, //right
                new byte[]{14,0}, //left
                new byte[]{14,0}, //top
                new byte[]{14,0},  //bottom
                null,
                (byte)1,
                true,
                false,
                false,
                "stone_1",
                "stone_2",
                false,
                false,
                0,
                true,
                "diamond",
                null
        )
        );

        blocks.add(
        new BlockDefinition(
                (byte) 12,
                12,
                0,
                0,
                0,
                "emerald ore",
                true,
                new byte[]{15,0}, //front
                new byte[]{15,0}, //back
                new byte[]{15,0}, //right
                new byte[]{15,0}, //left
                new byte[]{15,0}, //top
                new byte[]{15,0},  //bottom
                null,
                (byte) 1,
                true,
                false,
                false,
                "stone_1",
                "stone_2",
                false,
                false,
                0,
                true,
                "emerald",
                null
        )
        );

        blocks.add(
        new BlockDefinition(
                (byte) 13,
                10,
                0,
                0,
                0,
                "lapis ore",
                true,
                new byte[]{16,0}, //front
                new byte[]{16,0}, //back
                new byte[]{16,0}, //right
                new byte[]{16,0}, //left
                new byte[]{16,0}, //top
                new byte[]{16,0},  //bottom
                null,
                (byte) 1,
                true,
                false,
                false,
                "stone_1",
                "stone_2",
                false,
                false,
                0,
                true,
                "lapis",
                null
        )
        );

        blocks.add(
        new BlockDefinition(
                (byte) 14,
                14,
                0,
                0,
                0,
                "sapphire ore",
                true,
                new byte[]{17,0}, //front
                new byte[]{17,0}, //back
                new byte[]{17,0}, //right
                new byte[]{17,0}, //left
                new byte[]{17,0}, //top
                new byte[]{17,0},  //bottom
                null,
                (byte) 1,
                true,
                false,
                false,
                "stone_1",
                "stone_2",
                false,
                false,
                0,
                true,
                "sapphire",
                null
        )
        );

        blocks.add(
        new BlockDefinition(
                (byte) 15,
                16,
                0,
                0,
                0,
                "ruby ore",
                true,
                new byte[]{18,0}, //front
                new byte[]{18,0}, //back
                new byte[]{18,0}, //right
                new byte[]{18,0}, //left
                new byte[]{18,0}, //top
                new byte[]{18,0},  //bottom
                null,
                (byte) 1,
                true,
                false,
                false,
                "stone_1",
                "stone_2",
                false,
                false,
                0,
                true,
                "ruby",
                null
        )
        );

        blocks.add(
        new BlockDefinition(
                (byte) 16,
                2,
                0,
                0,
                0,
                "cobble stair",
                true,
                new byte[]{2,0}, //front
                new byte[]{2,0}, //back
                new byte[]{2,0}, //right
                new byte[]{2,0}, //left
                new byte[]{2,0}, //top
                new byte[]{2,0},  //bottom
                stairShape,
                (byte) 2,
                true,
                true,
                false,
                "stone_3",
                "stone_2",
                false,
                false,
                0,
                true,
                null,
                null
        )
        );


        blocks.add(
        new BlockDefinition(
                (byte) 17,
                0,
                0,
                1,
                0,
                "pumpkin",
                true,
                new byte[]{19,0}, //front
                new byte[]{19,0}, //back
                new byte[]{19,0}, //right
                new byte[]{19,0}, //left
                new byte[]{20,0}, //top
                new byte[]{20,0},  //bottom
                null,
                (byte) 1,
                true,
                false,
                false,
                "wood_1",
                "wood_2",
                false,
                false,
                0,
                true,
                null,
                null
        )
        );

        blocks.add(
        new BlockDefinition(
                (byte) 18,
                0,
                0,
                1,
                0,
                "jack 'o lantern unlit",
                true,
                new byte[]{21,0}, //front
                new byte[]{19,0}, //back
                new byte[]{19,0}, //right
                new byte[]{19,0}, //left
                new byte[]{20,0}, //top
                new byte[]{20,0},  //bottom
                null,
                (byte) 1,
                true,
                false,
                false,
                "wood_1",
                "wood_2",
                false,
                false,
                0,
                true,
                null,
                null
        )
        );

        blocks.add(
        new BlockDefinition(
                (byte)19,
                0,
                0,
                1,
                0,
                "jack 'o lantern lit",
                true,
                new byte[]{22,0}, //front
                new byte[]{19,0}, //back
                new byte[]{19,0}, //right
                new byte[]{19,0}, //left
                new byte[]{20,0}, //top
                new byte[]{20,0},  //bottom
                null,
                (byte) 1,
                true,
                false,
                false,
                "wood_1",
                "wood_2",
                false,
                false,
                0,
                true,
                null,
                null
        )
        );

        //falling sand
        BlockModifier fallSand = new BlockModifier() {
            @Override
            public void onPlace(int posX, int posY, int posZ) {
                if (getBlock(posX, posY - 1, posZ) == 0) {
                    //a beautiful hack - places air
                    placeBlock(posX, posY, posZ, (byte) 0, (byte) 0);
                    createFallingEntity(posX + 0.5d, posY, posZ + 0.5d,0, 0, 0, (byte)20);
                }
            }
        };

        blocks.add(
        new BlockDefinition(
                (byte) 20,
                0,
                1,
                0,
                0,
                "sand",
                true,
                new byte[]{23,0}, //front
                new byte[]{23,0}, //back
                new byte[]{23,0}, //right
                new byte[]{23,0}, //left
                new byte[]{23,0}, //top
                new byte[]{23,0},  //bottom
                null,
                (byte) 1,
                true,
                false,
                false,
                "sand_1",
                "sand_2",
                false,
                false,
                0,
                true,
                null,
                fallSand
        )
        );

        blocks.add(
        new BlockDefinition(
                (byte) 21,
                0,
                0,
                1,
                0,
                "doorOpenTop",
                false,
                new byte[]{24,0}, //front
                new byte[]{24,0}, //back
                new byte[]{24,0}, //right
                new byte[]{24,0}, //left
                new byte[]{24,0}, //top
                new byte[]{24,0},  //bottom
                doorOpen,
                (byte) 5,
                true,
                false,
                false,
                "wood_1",
                "wood_1",
                true,
                false,
                0,
                true,
                null,
                new BlockModifier() {
                    @Override
                    public void onDig(double posX, double posY, double posZ) {
                        if (getBlock((int)posX, (int)posY - 1, (int)posZ) == 22) {
                            setBlock((int)posX, (int)posY - 1, (int)posZ, (byte) 0, (byte) 0);
                            throwItem("door", posX + 0.5d, posY + 0.5d, posZ + 0.5d,1, 0);
                        }
                    }

                    @Override
                    public void onRightClick(int posX, int posY, int posZ) {
                        if (getBlock(posX, posY - 1, posZ) == 22) {
                            byte rot = getBlockRotation(posX, posY, posZ);
                            setBlock(posX, posY, posZ, (byte) 23,rot);
                            setBlock(posX, posY - 1, posZ, (byte) 24,rot);
                            playSound("door_close", posX + 0.5d, posX + 0.5d, posZ + 0.5d, false);
                        }
                    }
                }
        )
        );

        blocks.add(
        new BlockDefinition(
                (byte) 22,
                0,
                0,
                1,
                0,
                "doorOpenBottom",
                false,
                new byte[]{25,0}, //front
                new byte[]{25,0}, //back
                new byte[]{25,0}, //right
                new byte[]{25,0}, //left
                new byte[]{25,0}, //top
                new byte[]{25,0},  //bottom
                doorOpen,
                (byte) 5,
                true,
                false,
                false,
                "wood_1",
                "wood_1",
                true,
                false,
                0,
                true,
                null,
                new BlockModifier() {

                    @Override
                    public void onDig(double posX, double posY, double posZ) {
                        if (getBlock((int)posX, (int)posY + 1, (int)posZ) == 21) {
                            setBlock((int)posX, (int)posY + 1, (int)posZ, (byte) 0, (byte) 0);
                            throwItem("door", posX + 0.5d, posY + 0.5d, posZ + 0.5d, 1, 0);
                        }
                    }

                    @Override
                    public void onRightClick(int posX, int posY, int posZ) {
                        if (getBlock(posX, posY + 1, posZ) == 21) {
                            byte rot = getBlockRotation(posX, posY, posZ);
                            setBlock(posX, posY + 1, posZ, (byte) 23,rot);
                            setBlock(posX, posY, posZ, (byte) 24,rot);
                            playSound("door_close", posX + 0.5d, posY + 0.5d, posZ + 0.5d, true);
                        }
                    }
                }
        )
        );

        blocks.add(
        new BlockDefinition(
                (byte) 23,
                0,
                0,
                1,
                0,
                "doorClosedTop",
                false,
                new byte[]{24,0}, //front
                new byte[]{24,0}, //back
                new byte[]{24,0}, //right
                new byte[]{24,0}, //left
                new byte[]{24,0}, //top
                new byte[]{24,0},  //bottom
                doorClosed,
                (byte)6,
                true,
                false,
                false,
                "wood_1",
                "wood_1",
                true,
                false,
                0,
                true,
                null,
                new BlockModifier() {

                    @Override
                    public void onDig(double posX, double posY, double posZ) {
                        if (getBlock((int)posX, (int)posY - 1, (int)posZ) == 24) {
                            setBlock((int)posX, (int)posY - 1, (int)posZ, (byte) 0, (byte) 0);
                            throwItem("door", posX + 0.5d, posY + 0.5d, posZ + 0.5d, 1, 0);
                        }
                    }

                    @Override
                    public void onRightClick(int posX, int posY, int posZ) {
                        if (getBlock(posX, posY - 1, posZ) == 24) {
                            byte rot = getBlockRotation(posX, posY, posZ);
                            setBlock(posX, posY, posZ, (byte) 21,rot);
                            setBlock(posX, posY - 1, posZ, (byte) 22,rot);
                            playSound("door_open", posX + 0.5d, posY + 0.5d, posZ + 0.5d, true);
                        }
                    }
                }
        )
        );

        blocks.add(
        new BlockDefinition(
                (byte) 24,
                0,
                0,
                1,
                0,
                "doorClosedBottom",
                false,
                new byte[]{25,0}, //front
                new byte[]{25,0}, //back
                new byte[]{25,0}, //right
                new byte[]{25,0}, //left
                new byte[]{25,0}, //top
                new byte[]{25,0},  //bottom
                doorClosed,
                (byte) 6,
                true,
                false,
                false,
                "wood_1",
                "wood_1",
                true,
                false,
                0,
                true,
                null,
                new BlockModifier() {

                    @Override
                    public void onDig(double posX, double posY, double posZ) {
                        if (getBlock((int)posX, (int)posY + 1, (int)posZ) == 23) {
                            setBlock((int)posX, (int)posY + 1, (int)posZ, (byte) 0, (byte) 0);
                            throwItem("door", posX + 0.5d, posY + 0.5d, posZ + 0.5d,1,0);
                        }
                    }

                    @Override
                    public void onRightClick(int posX, int posY, int posZ) {
                        if (getBlock(posX, posY + 1, posZ) == 23) {
                            byte rot = getBlockRotation(posX, posY, posZ);
                            setBlock(posX, posY + 1, posZ, (byte) 21,rot);
                            setBlock(posX, posY, posZ, (byte) 22,rot);
                            playSound("door_open", posX + 0.5d, posY + 0.5d, posZ + 0.5d, true);
                        }
                    }
                }
        )
        );

        blocks.add(
        new BlockDefinition(
                (byte) 25,
                0,
                0,
                3,
                0,
                "tree",
                true,
                new byte[]{26,0}, //front
                new byte[]{26,0}, //back
                new byte[]{26,0}, //right
                new byte[]{26,0}, //left
                new byte[]{27,0}, //top
                new byte[]{27,0},  //bottom
                null,
                (byte) 1,
                true,
                false,
                false,
                "wood_1",
                "wood_2",
                false,
                false,
                0,
                true,
                null,
                null
        )
        );

        blocks.add(
        new BlockDefinition(
                (byte) 26,
                0,
                0,
                0,
                0.1f,
                "leaves",
                false,
                new byte[]{28,0}, //front
                new byte[]{28,0}, //back
                new byte[]{28,0}, //right
                new byte[]{28,0}, //left
                new byte[]{28,0}, //top
                new byte[]{28,0},  //bottom
                null,
                (byte) 4, //allfaces
                true,
                false,
                false,
                "leaves_1",
                "leaves_2",
                false,
                false,
                0,
                true,
                null,
                null
        )
        );

        blocks.add(
        new BlockDefinition(
                (byte) 27,
                0,
                0,
                2,
                0,
                "wood",
                true,
                new byte[]{29,0}, //front
                new byte[]{29,0}, //back
                new byte[]{29,0}, //right
                new byte[]{29,0}, //left
                new byte[]{29,0}, //top
                new byte[]{29,0},  //bottom
                null,
                (byte) 1, //regular
                true,
                false,
                false,
                "wood_1",
                "wood_2",
                false,
                false,
                0,
                true,
                null,
                null
        )
        );

        BlockModifier workBench = new BlockModifier() {
            @Override
            public void onRightClick(int posX, int posY, int posZ) {
                //BlockModifier.super.onRightClick(pos);
                openCraftingInventory(true);
            }
        };

        blocks.add(
        new BlockDefinition(
                (byte) 28,
                0,
                0,
                2,
                0,
                "workbench",
                true,
                new byte[]{31,0}, //front
                new byte[]{31,0}, //back
                new byte[]{31,0}, //right
                new byte[]{31,0}, //left
                new byte[]{30,0}, //top
                new byte[]{31,0},  //bottom
                null,
                (byte) 1, //regular
                true,
                false,
                false,
                "wood_1",
                "wood_2",
                true,
                false,
                0,
                true,
                null,
                workBench
        )
        );


        BlockModifier torchPlace = new BlockModifier() {
            @Override
            public void onPlace(int posX, int posY, int posZ) {
                torchFloodFill(posX, posY, posZ);
            }

            @Override
            public void onDig(double posX, double posY, double posZ){
                torchFloodFill((int)posX, (int)posY, (int)posZ);
            }
        };

        blocks.add(
        new BlockDefinition(
                (byte) 29,
                0,
                0,
                0,
                0.0001f,
                "torch",
                true,
                new byte[]{0,1}, //front
                new byte[]{0,1}, //back
                new byte[]{0,1}, //right
                new byte[]{0,1}, //left
                new byte[]{0,1}, //top
                new byte[]{0,1},  //bottom
                null,
                (byte) 7, //torch like
                false,
                false,
                false,
                "wood_1",
                "wood_2",
                false,
                true,
                0,
                true,
                "torchItem",
                torchPlace
        )
        );

        blocks.add(
        new BlockDefinition(
                (byte) 30,
                0,
                2,
                0,
                0,
                "wood stair",
                true,
                new byte[]{29,0}, //front
                new byte[]{29,0}, //back
                new byte[]{29,0}, //right
                new byte[]{29,0}, //left
                new byte[]{29,0}, //top
                new byte[]{29,0},  //bottom
                stairShape,
                (byte) 2,
                true,
                true,
                false,
                "wood_1",
                "wood_2",
                false,
                false,
                0,
                true,
                null,
                null
        )
        );

        blocks.add(
        new BlockDefinition(
                (byte) 31,
                1.5f,
                0,
                0,
                0,
                "cobble slab",
                true,
                new byte[]{2,0}, //front
                new byte[]{2,0}, //back
                new byte[]{2,0}, //right
                new byte[]{2,0}, //left
                new byte[]{2,0}, //top
                new byte[]{2,0},  //bottom
                slabShape,
                (byte) 3,
                true,
                true,
                false,
                "stone_3",
                "stone_2",
                false,
                false,
                0,
                true,
                null,
                null
        )
        );

        blocks.add(
        new BlockDefinition(
                (byte) 32,
                0,
                2,
                0,
                0,
                "wood slab",
                true,
                new byte[]{29,0}, //front
                new byte[]{29,0}, //back
                new byte[]{29,0}, //right
                new byte[]{29,0}, //left
                new byte[]{29,0}, //top
                new byte[]{29,0},  //bottom
                slabShape,
                (byte) 3,
                true,
                true,
                false,
                "wood_1",
                "wood_2",
                false,
                false,
                0,
                true,
                null,
                null
        )
        );

        blocks.add(
        new BlockDefinition(
                (byte) 33,
                1.5f,
                0,
                0,
                0,
                "cobble vertical slab",
                true,
                new byte[]{2,0}, //front
                new byte[]{2,0}, //back
                new byte[]{2,0}, //right
                new byte[]{2,0}, //left
                new byte[]{2,0}, //top
                new byte[]{2,0},  //bottom
                verticalSlab,
                (byte) 9,
                true,
                true,
                false,
                "stone_3",
                "stone_2",
                false,
                false,
                0,
                true,
                null,
                null
        )
        );

        blocks.add(
        new BlockDefinition(
                (byte) 34,
                2,
                0,
                0,
                0,
                "wood vertical slab",
                true,
                new byte[]{29,0}, //front
                new byte[]{29,0}, //back
                new byte[]{29,0}, //right
                new byte[]{29,0}, //left
                new byte[]{29,0}, //top
                new byte[]{29,0},  //bottom
                verticalSlab,
                (byte) 9,
                true,
                true,
                false,
                "wood_1",
                "stone_2",
                false,
                false,
                0,
                true,
                null,
                null
        )
        );

        //falling sand
        BlockModifier fallGravel = new BlockModifier() {
            @Override
            public void onPlace(int posX, int posY, int posZ) {
                if (getBlock(posX, posY - 1, posZ) == 0) {
                    //a beautiful hack - places air
                    placeBlock(posX, posY, posZ, (byte) 0, (byte) 0);
                    createFallingEntity(posX + 0.5d, posY, posZ + 0.5d,0, 0, 0, (byte)35);
                }
            }
        };

        blocks.add(
        new BlockDefinition(
                (byte) 35,
                0,
                1,
                0,
                0,
                "gravel",
                true,
                new byte[]{1,1}, //front
                new byte[]{1,1}, //back
                new byte[]{1,1}, //right
                new byte[]{1,1}, //left
                new byte[]{1,1}, //top
                new byte[]{1,1},  //bottom
                null,
                (byte) 1,
                true,
                false,
                false,
                "sand_1",
                "sand_2",
                false,
                false,
                0,
                true,
                null,
                fallGravel
        )
        );
    }
}
