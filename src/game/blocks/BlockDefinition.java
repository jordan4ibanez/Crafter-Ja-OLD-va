package game.blocks;

import org.joml.Vector3d;
import org.joml.Vector3f;

import static engine.sound.SoundAPI.playSound;
import static game.chunk.Chunk.*;
import static game.crafting.InventoryLogic.openCraftingInventory;
import static game.falling.FallingEntity.addFallingEntity;
import static game.item.ItemDefinition.registerItem;
import static game.item.ItemEntity.createItem;
import static game.light.Light.torchFloodFill;
import static game.tnt.TNTEntity.createTNT;

public class BlockDefinition {

    //holds BlockDefinition data
    private final static BlockDefinition[] blockIDs = new BlockDefinition[(byte)30];

    //holds the blockshape data
    private final static BlockShape[] blockShapeMap = new BlockShape[(byte)9];

    //fixed fields for the class
    private static final byte atlasSizeX = 32;
    private static final byte atlasSizeY = 32;

    public static byte getAtlasSizeX(){
        return atlasSizeX;
    }

    public static byte getAtlasSizeY(){
        return atlasSizeY;
    }

    //actual block object fields
    public byte     ID;
    public String  name;
    public boolean dropsItem;
    public float[] frontTexture;  //front
    public float[] backTexture;   //back
    public float[] rightTexture;  //right
    public float[] leftTexture;   //left
    public float[] topTexture;    //top
    public float[] bottomTexture; //bottom
    public boolean walkable;
    public boolean steppable;
    public boolean isLiquid;
    public byte drawType;
    public String placeSound;
    public String digSound;
    public BlockModifier blockModifier;
    public boolean isRightClickable;
    public boolean isOnPlaced;
    public float viscosity;
    public boolean pointable;
    public float stoneHardness;
    public float dirtHardness;
    public float woodHardness;
    public float leafHardness;
    public String droppedItem;

    public BlockDefinition(
            byte ID,
            float stoneHardness,
            float dirtHardness,
            float woodHardness,
            float leafHardness,
            String name,
            boolean dropsItem,
            byte[] front,
            byte[] back,
            byte[] right,
            byte[] left,
            byte[] top,
            byte[] bottom,
            byte drawType,
            boolean walkable,
            boolean steppable,
            boolean isLiquid,
            BlockModifier blockModifier,
            String placeSound,
            String digSound,
            boolean isRightClickable,
            boolean isOnPlaced,
            float viscosity,
            boolean pointable,
            String droppedItem

    ){

        this.ID   = ID;
        this.stoneHardness = stoneHardness;
        this.dirtHardness = dirtHardness;
        this.woodHardness = woodHardness;
        this.leafHardness = leafHardness;
        this.name = name;
        this.dropsItem = dropsItem;
        this.frontTexture  = calculateTexture(  front[0],  front[1] );
        this.backTexture   = calculateTexture(   back[0],   back[1] );
        this.rightTexture  = calculateTexture(  right[0],  right[1] );
        this.leftTexture   = calculateTexture(   left[0],   left[1] );
        this.topTexture    = calculateTexture(    top[0],    top[1] );
        this.bottomTexture = calculateTexture( bottom[0], bottom[1] );
        this.drawType = drawType;
        this.walkable = walkable;
        this.steppable = steppable;
        this.isLiquid = isLiquid;
        this.blockModifier = blockModifier;
        this.placeSound = placeSound;
        this.digSound = digSound;
        this.isRightClickable = isRightClickable;
        this.isOnPlaced = isOnPlaced;
        this.viscosity = viscosity;
        this.pointable = pointable;
        this.droppedItem = droppedItem;
        blockIDs[ID] = this;

        registerItem(name, ID);
    }

    public static void onDigCall(int ID, Vector3d pos) {
        BlockDefinition blockDefinition = blockIDs[ID];
        if(blockDefinition != null){
            if(blockDefinition.dropsItem){
                //dropped defined item
                if (blockDefinition.droppedItem != null){
                    createItem(blockDefinition.droppedItem, pos.add(0.5d, 0.5d, 0.5d), 1, 2.5f);
                }
                //drop self
                else {
                    createItem(blockDefinition.name, pos.add(0.5d, 0.5d, 0.5d), 1, 2.5f);
                }
            }
            if(blockDefinition.blockModifier != null){
                try {
                    blockDefinition.blockModifier.onDig(pos);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (!blockDefinition.digSound.equals("")) {
                playSound(blockDefinition.digSound);
            }
        }
    }

    public static void onPlaceCall(int ID, Vector3d pos) {
        BlockDefinition blockDefinition = blockIDs[ID];
        if (blockDefinition != null) {
            if (blockDefinition.blockModifier != null){
                try {
                    blockDefinition.blockModifier.onPlace(pos);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (!blockDefinition.placeSound.equals("")) {
                playSound(blockDefinition.placeSound);
            }
        }
    }

    private static float[] calculateTexture(byte x, byte y){
        float[] texturePoints = new float[4];
        texturePoints[0] = (float)x/(float)atlasSizeX;     //min x (-)
        texturePoints[1] = (float)(x+1)/(float)atlasSizeX; //max x (+)

        texturePoints[2] = (float)y/(float)atlasSizeY;     //min y (-)
        texturePoints[3] = (float)(y+1)/(float)atlasSizeY; //max y (+)
        return texturePoints;
    }

    public static String getBlockName(byte ID){
        return blockIDs[ID].name;
    }

    public static boolean getRightClickable(byte ID){
        return(blockIDs[ID].isRightClickable);
    }

    public static boolean getIsOnPlaced(byte ID){
        return(blockIDs[ID].isOnPlaced);
    }

    public static byte getBlockDrawType(byte ID){
        if (ID < 0){
            return 0;
        }
        return blockIDs[ID].drawType;
    }

    public static boolean getIfLiquid(int ID){
        return blockIDs[ID].isLiquid;
    }

    public static double[][] getBlockShape(int ID, byte rot){

        double[][] newBoxes = new double[blockShapeMap[blockIDs[ID].drawType].getBoxes().length][6];


        int index = 0;

        //automated as base, since it's the same
        switch (rot) {
            case 0 -> {
                for (double[] thisShape : blockShapeMap[blockIDs[ID].drawType].getBoxes()) {
                    System.arraycopy(thisShape, 0, newBoxes[index], 0, 6);
                    index++;
                }
            }
            case 1 -> {
                for (double[] thisShape : blockShapeMap[blockIDs[ID].drawType].getBoxes()) {

                    double blockDiffZ = 1d - thisShape[5];
                    double widthZ = thisShape[5] - thisShape[2];

                    newBoxes[index][0] = blockDiffZ;
                    newBoxes[index][1] = thisShape[1];//-y
                    newBoxes[index][2] = thisShape[0]; // -z

                    newBoxes[index][3] = blockDiffZ + widthZ;
                    newBoxes[index][4] = thisShape[4];//+y
                    newBoxes[index][5] = thisShape[3]; //+z
                    index++;
                }
            }
            case 2 -> {
                for (double[] thisShape : blockShapeMap[blockIDs[ID].drawType].getBoxes()) {

                    double blockDiffZ = 1d - thisShape[5];
                    double widthZ = thisShape[5] - thisShape[2];

                    double blockDiffX = 1d - thisShape[3];
                    double widthX = thisShape[3] - thisShape[0];

                    newBoxes[index][0] = blockDiffX;
                    newBoxes[index][1] = thisShape[1];//-y
                    newBoxes[index][2] = blockDiffZ; // -z

                    newBoxes[index][3] = blockDiffX + widthX;
                    newBoxes[index][4] = thisShape[4];//+y
                    newBoxes[index][5] = blockDiffZ + widthZ; //+z
                    index++;
                }
            }
            case 3 -> {
                for (double[] thisShape : blockShapeMap[blockIDs[ID].drawType].getBoxes()) {
                    double blockDiffX = 1d - thisShape[3];
                    double widthX = thisShape[3] - thisShape[0];

                    newBoxes[index][0] = thisShape[2];
                    newBoxes[index][1] = thisShape[1];//-y
                    newBoxes[index][2] = blockDiffX; // -z

                    newBoxes[index][3] = thisShape[5];
                    newBoxes[index][4] = thisShape[4];//+y
                    newBoxes[index][5] = blockDiffX + widthX; //+z
                    index++;
                }
            }
        }
        return newBoxes;
    }

    public static boolean isWalkable(int ID){
        return blockIDs[ID].walkable;
    }

    public static boolean isSteppable(int ID){
        return blockIDs[ID].steppable;
    }

    public static void initializeBlocks() {

        //air
        blockShapeMap[0] = new BlockShape(new double[][]{{0f,0f,0f,1f,1f,1f}});

        //normal
        blockShapeMap[1] = new BlockShape(new double[][]{{0f,0f,0f,1f,1f,1f}});

        //stair
        blockShapeMap[2] =
                new BlockShape(new double[][]{
                                {0f,0f,0f,1f,0.5f,1f},
                                {0f,0f,0f,1f,1f,0.5f}
                        });

        //slab
        blockShapeMap[3] =
                new BlockShape(new double[][]{
                                {0f,0f,0f,1f,0.5f,1f}
                        });

        //allfaces
        blockShapeMap[4] =
                new BlockShape(new double[][]{
                                {0f,0f,0f,1f,1f,1f}
                        });

        //door open
        blockShapeMap[5] =
                new BlockShape(
                        new double[][]{
                                {0f,0f,0f,2f/16f,1f,1f}
                        }
                );

        //door closed
        blockShapeMap[6] =
                new BlockShape(
                        new double[][]{
                                {0f,0f,14f/16f,1f,1f,1f}
                        }
                );

        //torch
        blockShapeMap[7] = new BlockShape(new double[][]{{0f,0f,0f,1f,1f,1f}});

        //liquid source
        blockShapeMap[8] = new BlockShape(new double[][]{{0f,0f,0f,1f,1f,1f}});

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
                (byte) 0,
                false,
                false,
                false,
                null,
                "",
                "",
                false,
                false,
                0,
                false,
                null
        );

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
                (byte) 1,
                true,
                false,
                false,
                null,
                "dirt_1",
                "dirt_2",
                false,
                false,
                0,
                true,
                null
        );

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
                (byte) 1,
                true,
                false,
                false,
                null,
                "dirt_1",
                "dirt_2",
                false,
                false,
                0,
                true,
                "dirt"
        );

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
                (byte) 1,
                true,
                false,
                false,
                null,
                "stone_1",
                "stone_2",
                false,
                false,
                0,
                true,
                "cobblestone"
        );

        new BlockDefinition(
                (byte) 4,
                1.5f,
                0,
                0,
                0,
                "cobblestone",
                true,
                new byte[]{2,0}, //front
                new byte[]{2,0}, //back
                new byte[]{2,0}, //right
                new byte[]{2,0}, //left
                new byte[]{2,0}, //top
                new byte[]{2,0},  //bottom
                (byte) 1,
                true,
                false,
                false,
                null,
                "stone_3",
                "stone_2",
                false,
                false,
                0,
                true,
                null
        );

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
                (byte) 1,
                true,
                false,
                false,
                null,
                "stone_1",
                "stone_1",
                false,
                false,
                0,
                true,
                null
        );


        //tnt explosion
        BlockModifier kaboom = new BlockModifier() {
            @Override
            public void onDig(Vector3d pos) {
                createTNT(pos, 0, true);
            }
        };

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
                (byte) 1,
                true,
                false,
                false,
                kaboom,
                "dirt_1",
                "wood_2",
                false,
                false,
                0,
                true,
                null
        );

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
                (byte) 8, //liquid source
                false,
                false,
                true,
                null,
                "",
                "",
                false,
                false,
                40,
                false,
                null
        );

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
                (byte) 1,
                true,
                false,
                false,
                null,
                "stone_1",
                "stone_2",
                false,
                false,
                0,
                true,
                "coal"
        );

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
                (byte) 1,
                true,
                false,
                false,
                null,
                "stone_1",
                "stone_2",
                false,
                false,
                0,
                true,
                null
        );

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
                (byte) 1,
                true,
                false,
                false,
                null,
                "stone_1",
                "stone_2",
                false,
                false,
                0,
                true,
                null
        );

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
                (byte)1,
                true,
                false,
                false,
                null,
                "stone_1",
                "stone_2",
                false,
                false,
                0,
                true,
                "diamond"
        );

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
                (byte) 1,
                true,
                false,
                false,
                null,
                "stone_1",
                "stone_2",
                false,
                false,
                0,
                true,
                "emerald"
        );

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
                (byte) 1,
                true,
                false,
                false,
                null,
                "stone_1",
                "stone_2",
                false,
                false,
                0,
                true,
                "lapis"
        );

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
                (byte) 1,
                true,
                false,
                false,
                null,
                "stone_1",
                "stone_2",
                false,
                false,
                0,
                true,
                "sapphire"
        );

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
                (byte) 1,
                true,
                false,
                false,
                null,
                "stone_1",
                "stone_2",
                false,
                false,
                0,
                true,
                "ruby"
        );

        new BlockDefinition(
                (byte) 16,
                2,
                0,
                0,
                0,
                "cobblestone stair",
                true,
                new byte[]{2,0}, //front
                new byte[]{2,0}, //back
                new byte[]{2,0}, //right
                new byte[]{2,0}, //left
                new byte[]{2,0}, //top
                new byte[]{2,0},  //bottom
                (byte) 2,
                true,
                true,
                false,
                null,
                "stone_3",
                "stone_2",
                false,
                false,
                0,
                true,
                null
        );


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
                (byte) 1,
                true,
                false,
                false,
                null,
                "wood_1",
                "wood_2",
                false,
                false,
                0,
                true,
                null
        );
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
                (byte) 1,
                true,
                false,
                false,
                null,
                "wood_1",
                "wood_2",
                false,
                false,
                0,
                true,
                null
        );
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
                (byte) 1,
                true,
                false,
                false,
                null,
                "wood_1",
                "wood_2",
                false,
                false,
                0,
                true,
                null
        );

        //falling sand
        BlockModifier fallSand = new BlockModifier() {
            @Override
            public void onPlace(Vector3d pos) {
                if (getBlock((int)pos.x, (int)pos.y - 1, (int)pos.z) == 0) {
                    digBlock((int) pos.x, (int) pos.y, (int) pos.z);
                    addFallingEntity(new Vector3d(pos.x + 0.5d, pos.y, pos.z + 0.5d), new Vector3f(0, 0, 0), (byte)20);
                }
            }
        };
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
                (byte) 1,
                true,
                false,
                false,
                fallSand,
                "sand_1",
                "sand_2",
                false,
                false,
                0,
                true,
                null
        );

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
                (byte) 5,
                true,
                false,
                false,
                new BlockModifier() {
                    @Override
                    public void onDig(Vector3d pos) {
                        if (getBlock((int)pos.x, (int)pos.y - 1, (int)pos.z) == 22) {
                            setBlock((int)pos.x, (int)pos.y - 1, (int)pos.z, (byte) 0, (byte) 0);
                            createItem("door", pos.add(0.5d,0.5d,0.5d), 1);
                        }
                    }

                    @Override
                    public void onRightClick(Vector3d pos) {
                        if (getBlock((int)pos.x, (int)pos.y - 1, (int)pos.z) == 22) {
                            byte rot = getBlockRotation((int)pos.x, (int)pos.y, (int)pos.z);
                            setBlock((int)pos.x, (int)pos.y, (int)pos.z, (byte) 23,rot);
                            setBlock((int)pos.x, (int)pos.y - 1, (int)pos.z, (byte) 24,rot);
                            playSound("door_close", new Vector3d(pos.x + 0.5d, pos.y + 0.5d, pos.z + 0.5d));
                        }
                    }
                },
                "wood_1",
                "wood_1",
                true,
                false,
                0,
                true,
                null
        );

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
                (byte) 5,
                true,
                false,
                false,
                new BlockModifier() {

                    @Override
                    public void onDig(Vector3d pos) {
                        if (getBlock((int)pos.x, (int)pos.y + 1, (int)pos.z) == 21) {
                            setBlock((int)pos.x, (int)pos.y + 1, (int)pos.z, (byte) 0, (byte) 0);
                            createItem("door", pos.add(0.5d,0.5d,0.5d), 1);
                        }
                    }

                    @Override
                    public void onRightClick(Vector3d pos) {
                        if (getBlock((int)pos.x, (int)pos.y + 1, (int)pos.z) == 21) {
                            byte rot = getBlockRotation((int)pos.x, (int)pos.y, (int)pos.z);
                            setBlock((int)pos.x, (int)pos.y + 1, (int)pos.z, (byte) 23,rot);
                            setBlock((int)pos.x, (int)pos.y, (int)pos.z, (byte) 24,rot);
                            playSound("door_close", new Vector3d(pos.x + 0.5d, pos.y + 0.5d, pos.z + 0.5d));
                        }
                    }
                },
                "wood_1",
                "wood_1",
                true,
                false,
                0,
                true,
                null
        );

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
                (byte)6,
                true,
                false,
                false,
                new BlockModifier() {

                    @Override
                    public void onDig(Vector3d pos) {
                        if (getBlock((int)pos.x, (int)pos.y - 1, (int)pos.z) == 24) {
                            setBlock((int)pos.x, (int)pos.y - 1, (int)pos.z, (byte) 0, (byte) 0);
                            createItem("door", pos.add(0.5d,0.5d,0.5d), 1);
                        }
                    }

                    @Override
                    public void onRightClick(Vector3d pos) {
                        if (getBlock((int)pos.x, (int)pos.y - 1, (int)pos.z) == 24) {
                            byte rot = getBlockRotation((int)pos.x, (int)pos.y, (int)pos.z);
                            setBlock((int)pos.x, (int)pos.y, (int)pos.z, (byte) 21,rot);
                            setBlock((int)pos.x, (int)pos.y - 1, (int)pos.z, (byte) 22,rot);
                            playSound("door_open", new Vector3d(pos.x + 0.5d, pos.y + 0.5d, pos.z + 0.5d));
                        }
                    }
                },
                "wood_1",
                "wood_1",
                true,
                false,
                0,
                true,
                null
        );

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
                (byte) 6,
                true,
                false,
                false,
                new BlockModifier() {

                    @Override
                    public void onDig(Vector3d pos) {
                        if (getBlock((int)pos.x, (int)pos.y + 1, (int)pos.z) == 23) {
                            setBlock((int)pos.x, (int)pos.y + 1, (int)pos.z, (byte) 0, (byte) 0);
                            createItem("door", pos.add(0.5d,0.5d,0.5d), 1);
                        }
                    }

                    @Override
                    public void onRightClick(Vector3d pos) {
                        if (getBlock((int)pos.x, (int)pos.y + 1, (int)pos.z) == 23) {
                            byte rot = getBlockRotation((int)pos.x, (int)pos.y, (int)pos.z);
                            setBlock((int)pos.x, (int)pos.y + 1, (int)pos.z, (byte) 21,rot);
                            setBlock((int)pos.x, (int)pos.y, (int)pos.z, (byte) 22,rot);
                            playSound("door_open", new Vector3d(pos.x + 0.5d, pos.y + 0.5d, pos.z + 0.5d));
                        }
                    }
                },
                "wood_1",
                "wood_1",
                true,
                false,
                0,
                true,
                null
        );

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
                (byte) 1,
                true,
                false,
                false,
                null,
                "wood_1",
                "wood_2",
                false,
                false,
                0,
                true,
                null
        );

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
                (byte) 4, //allfaces
                true,
                false,
                false,
                null,
                "wood_1",
                "wood_2",
                false,
                false,
                0,
                true,
                null
        );

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
                (byte) 1, //regular
                true,
                false,
                false,
                null,
                "wood_1",
                "wood_2",
                false,
                false,
                0,
                true,
                null
        );

        BlockModifier workBench = new BlockModifier() {
            @Override
            public void onRightClick(Vector3d pos) {
                //BlockModifier.super.onRightClick(pos);
                openCraftingInventory(true);
            }
        };

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
                (byte) 1, //regular
                true,
                false,
                false,
                workBench,
                "wood_1",
                "wood_2",
                true,
                false,
                0,
                true,
                null
        );


        BlockModifier torchPlace = new BlockModifier() {
            @Override
            public void onPlace(Vector3d pos) {
                torchFloodFill((int)pos.x, (int)pos.y, (int)pos.z);
            }

            @Override
            public void onDig(Vector3d pos){
                torchFloodFill((int)pos.x, (int)pos.y, (int)pos.z);
            }
        };

        new BlockDefinition(
                (byte) 29,
                0,
                0,
                0,
                0.25f,
                "torch",
                true,
                new byte[]{0,1}, //front
                new byte[]{0,1}, //back
                new byte[]{0,1}, //right
                new byte[]{0,1}, //left
                new byte[]{0,1}, //top
                new byte[]{0,1},  //bottom
                (byte) 7, //torch like
                false,
                false,
                false,
                torchPlace,
                "wood_1",
                "wood_2",
                false,
                true,
                0,
                true,
                "torchItem"
        );
    }

    public static BlockDefinition getBlockDefinition(int ID){
        return blockIDs[ID];
    }

    public static BlockDefinition getBlockDefinition(String name){
        for(BlockDefinition thisBlockDefinition : blockIDs){
            if (thisBlockDefinition.name.equals(name)){
                return thisBlockDefinition;
            }
        }
        return null;
    }

    public static boolean blockHasOnRightClickCall(int ID){
        return(blockIDs[ID].isRightClickable && blockIDs[ID].blockModifier != null);
    }

    public static float[] getFrontTexturePoints(int ID, byte rotation){
        return switch (rotation) {
            case 1 -> blockIDs[ID].rightTexture;
            case 2 -> blockIDs[ID].backTexture;
            case 3 -> blockIDs[ID].leftTexture;
            default -> blockIDs[ID].frontTexture;
        };
    }
    public static float[] getBackTexturePoints(int ID, byte rotation){
        return switch (rotation) {
            case 1 -> blockIDs[ID].leftTexture;
            case 2 -> blockIDs[ID].frontTexture;
            case 3 -> blockIDs[ID].rightTexture;
            default -> blockIDs[ID].backTexture;
        };

    }
    public static float[] getRightTexturePoints(int ID, byte rotation){
        return switch (rotation) {
            case 1 -> blockIDs[ID].backTexture;
            case 2 -> blockIDs[ID].leftTexture;
            case 3 -> blockIDs[ID].frontTexture;
            default -> blockIDs[ID].rightTexture;
        };
    }
    public static float[] getLeftTexturePoints(int ID, byte rotation){
        return switch (rotation) {
            case 1 -> blockIDs[ID].frontTexture;
            case 2 -> blockIDs[ID].rightTexture;
            case 3 -> blockIDs[ID].backTexture;
            default -> blockIDs[ID].leftTexture;
        };
    }

    public static boolean isBlockLiquid(int ID){
        return blockIDs[ID].isLiquid;
    }

    public static float getBlockViscosity(int ID){
        return blockIDs[ID].viscosity;
    }
    public static float[] getTopTexturePoints(int ID){
        return blockIDs[ID].topTexture;
    }
    public static float[] getBottomTexturePoints(int ID){
        return blockIDs[ID].bottomTexture;
    }

    public static boolean isBlockPointable(int ID){
        return blockIDs[ID].pointable;
    }
}
