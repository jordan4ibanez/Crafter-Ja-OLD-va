package game.blocks;

import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;

import static engine.sound.SoundAPI.playSound;
import static game.chunk.Chunk.*;
import static game.chunk.ChunkMeshGenerator.passChunkMeshThreadData;
import static game.crafting.InventoryLogic.openCraftingInventory;
import static game.falling.FallingEntity.addFallingEntity;
import static game.item.ItemDefinition.registerItem;
import static game.item.ItemEntity.createItem;
import static game.light.Light.torchFloodFill;
import static game.tnt.TNTEntity.createTNT;


public class BlockDefinition {
    //holds BlockDefinition data
    private static final byte maxIDs = 30;

    //fixed fields for the class
    private static final byte atlasSizeX = 32;
    private static final byte atlasSizeY = 32;

    //holds the blockshape data
    private final static float[][][] blockShapeMap = new float[(byte)9][0][0];

    //actual block object fields
    private static final String[] names = new String[maxIDs];
    private static final boolean[] dropsItems = new boolean[maxIDs];
    private static final byte[] drawTypes = new byte[maxIDs];
    private static final float[][] frontTextures = new float[maxIDs][0];  //front
    private static final float[][] backTextures = new float[maxIDs][0];   //back
    private static final float[][] rightTextures = new float[maxIDs][0];  //right
    private static final float[][] leftTextures = new float[maxIDs][0];   //left
    private static final float[][] topTextures = new float[maxIDs][0];    //top
    private static final float[][] bottomTextures = new float[maxIDs][0]; //bottom

    private static final boolean[] walkables = new boolean[maxIDs];
    private static final boolean[] steppables = new boolean[maxIDs];
    private static final boolean[] isLiquids = new boolean[maxIDs];
    private static final String[] placeSounds = new String[maxIDs];
    private static final String[] digSounds = new String[maxIDs];
    private static final BlockModifier[] blockModifiers = new BlockModifier[maxIDs];
    private static final boolean[] isRightClickables = new boolean[maxIDs];
    private static final boolean[] isOnPlaceds = new boolean[maxIDs];
    private static final float[] viscositys = new float[maxIDs];
    private static final boolean[] pointables = new boolean[maxIDs];
    private static final float[] stoneHardnesses = new float[maxIDs];
    private static final float[] dirtHardnesses = new float[maxIDs];
    private static final float[] woodHardnesses = new float[maxIDs];
    private static final float[] leafHardnesses = new float[maxIDs];
    private static final String[] droppedItems = new String[maxIDs];

    private static void registerBlock(
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
        stoneHardnesses[ID] = stoneHardness;
        dirtHardnesses[ID] = dirtHardness;
        woodHardnesses[ID] = woodHardness;
        leafHardnesses[ID] = leafHardness;
        names[ID] = name;
        dropsItems[ID] = dropsItem;
        frontTextures[ID]  = calculateTexture(  front[0],  front[1] );
        backTextures[ID]   = calculateTexture(   back[0],   back[1] );
        rightTextures[ID]  = calculateTexture(  right[0],  right[1] );
        leftTextures[ID]   = calculateTexture(   left[0],   left[1] );
        topTextures[ID]    = calculateTexture(    top[0],    top[1] );
        bottomTextures[ID] = calculateTexture( bottom[0], bottom[1] );
        drawTypes[ID] = drawType;
        walkables[ID] = walkable;
        steppables[ID] = steppable;
        isLiquids[ID] = isLiquid;
        blockModifiers[ID] = blockModifier;
        placeSounds[ID] = placeSound;
        digSounds[ID] = digSound;
        isRightClickables[ID] = isRightClickable;
        isOnPlaceds[ID] = isOnPlaced;
        viscositys[ID] = viscosity;
        pointables[ID] = pointable;
        droppedItems[ID] = droppedItem;

        registerItem(name, ID);
    }

    public static void onDigCall(byte ID, int posX, int posY, int posZ) {
        if(dropsItems[ID]){
            //dropped defined item
            if (droppedItems[ID] != null){
                createItem(droppedItems[ID], posX + 0.5d,posY + 0.5d, posZ + 0.5d, 1, 2.5f);
            }
            //drop self
            else {
                createItem(names[ID], posX + 0.5d, posY + 0.5d, posZ + 0.5d, 1, 2.5f);
            }
        }
        if(blockModifiers[ID] != null){
            try {
                blockModifiers[ID].onDig(posX, posY, posZ);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (!digSounds[ID].equals("")) {
            playSound(digSounds[ID]);
        }
    }

    public static void onPlaceCall(byte ID, int posX, int posY, int posZ) {

        if (blockModifiers[ID] != null){
            try {
                blockModifiers[ID].onPlace(posX,posY,posZ);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (!placeSounds[ID].equals("")) {
            playSound(placeSounds[ID]);
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
        return names[ID];
    }

    public static boolean getRightClickable(byte ID){
        return(isRightClickables[ID]);
    }

    public static boolean getIsOnPlaced(byte ID){
        return(isOnPlaceds[ID]);
    }

    public static byte getBlockDrawType(byte ID){
        if (ID < 0){
            return 0;
        }
        return drawTypes[ID];
    }

    public static boolean getIfLiquid(byte ID){
        return isLiquids[ID];
    }

    public static float[][] getBlockShape(byte ID, byte rot){

        float[][] newBoxes = new float[blockShapeMap[drawTypes[ID]].length][6];

        byte index = 0;

        //automated as base, since it's the same
        switch (rot) {
            case 0 -> {
                for (float[] thisShape : blockShapeMap[drawTypes[ID]]) {
                    System.arraycopy(thisShape, 0, newBoxes[index], 0, 6);
                    index++;
                }
            }
            case 1 -> {
                for (float[] thisShape : blockShapeMap[drawTypes[ID]]) {

                    float blockDiffZ = 1f - thisShape[5];
                    float widthZ = thisShape[5] - thisShape[2];

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
                for (float[] thisShape : blockShapeMap[drawTypes[ID]]) {

                    float blockDiffZ = 1f - thisShape[5];
                    float widthZ = thisShape[5] - thisShape[2];

                    float blockDiffX = 1f - thisShape[3];
                    float widthX = thisShape[3] - thisShape[0];

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
                for (float[] thisShape : blockShapeMap[drawTypes[ID]]) {
                    float blockDiffX = 1f - thisShape[3];
                    float widthX = thisShape[3] - thisShape[0];

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

    public static boolean isBlockWalkable(byte ID){
        return walkables[ID];
    }

    public static boolean isSteppable(byte ID){
        return steppables[ID];
    }

    public static void initializeBlocks() {

        //air
        blockShapeMap[0] = new float[][]{{0f,0f,0f,1f,1f,1f}};

        //normal
        blockShapeMap[1] = new float[][]{{0f,0f,0f,1f,1f,1f}};

        //stair
        blockShapeMap[2] = new float[][]{
                                {0f,0f,0f,1f,0.5f,1f},
                                {0f,0f,0f,1f,1f,0.5f}
                        };

        //slab
        blockShapeMap[3] = new float[][]{{0f,0f,0f,1f,0.5f,1f}};

        //allfaces
        blockShapeMap[4] = new float[][]{{0f,0f,0f,1f,1f,1f}};

        //door open
        blockShapeMap[5] = new float[][]{{0f,0f,0f,2f/16f,1f,1f}};

        //door closed
        blockShapeMap[6] = new float[][]{{0f,0f,14f/16f,1f,1f,1f}};

        //torch
        blockShapeMap[7] = new float[][]{{0f,0f,0f,1f,1f,1f}};

        //liquid source
        blockShapeMap[8] = new float[][]{{0f,0f,0f,1f,1f,1f}};

        registerBlock(
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

        registerBlock(
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

        registerBlock(
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

        registerBlock(
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

        registerBlock(
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

        registerBlock(
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

        registerBlock(
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

        registerBlock(
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

        registerBlock(
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

        registerBlock(
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

        registerBlock(
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

        registerBlock(
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

        registerBlock(
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

        registerBlock(
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

        registerBlock(
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

        registerBlock(
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

        registerBlock(
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


        registerBlock(
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
        registerBlock(
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
        registerBlock(
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
            public void onPlace(Vector3i pos) {
                if (getBlock(pos.x, pos.y - 1, pos.z) == 0) {
                    digBlock(pos.x, pos.y, pos.z);
                    addFallingEntity(new Vector3d(pos.x + 0.5d, pos.y, pos.z + 0.5d), new Vector3f(0, 0, 0), (byte)20);
                }
            }
        };
        registerBlock(
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

        registerBlock(
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
                    public void onRightClick(Vector3i pos) {
                        if (getBlock(pos.x, pos.y - 1, pos.z) == 22) {
                            byte rot = getBlockRotation(pos.x, pos.y, pos.z);
                            setBlock(pos.x, pos.y, pos.z, (byte) 23,rot);
                            setBlock(pos.x, pos.y - 1, pos.z, (byte) 24,rot);
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

        registerBlock(
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
                    public void onRightClick(Vector3i pos) {
                        if (getBlock(pos.x, pos.y + 1, pos.z) == 21) {
                            byte rot = getBlockRotation(pos.x, pos.y, pos.z);
                            setBlock(pos.x, pos.y + 1, pos.z, (byte) 23,rot);
                            setBlock(pos.x, pos.y, pos.z, (byte) 24,rot);
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

        registerBlock(
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
                    public void onRightClick(Vector3i pos) {
                        if (getBlock(pos.x, pos.y - 1, pos.z) == 24) {
                            byte rot = getBlockRotation(pos.x, pos.y, pos.z);
                            setBlock(pos.x, pos.y, pos.z, (byte) 21,rot);
                            setBlock(pos.x, pos.y - 1, pos.z, (byte) 22,rot);
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

        registerBlock(
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
                    public void onRightClick(Vector3i pos) {
                        if (getBlock(pos.x, pos.y + 1, pos.z) == 23) {
                            byte rot = getBlockRotation(pos.x, pos.y, pos.z);
                            setBlock(pos.x, pos.y + 1, pos.z, (byte) 21,rot);
                            setBlock(pos.x, pos.y, pos.z, (byte) 22,rot);
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

        registerBlock(
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

        registerBlock(
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
                "leaves_1",
                "leaves_2",
                false,
                false,
                0,
                true,
                null
        );

        registerBlock(
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
            public void onRightClick(Vector3i pos) {
                //BlockModifier.super.onRightClick(pos);
                openCraftingInventory(true);
            }
        };

        registerBlock(
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
            public void onPlace(Vector3i pos) {
                torchFloodFill(pos.x, pos.y, pos.z);
            }

            @Override
            public void onDig(Vector3d pos){
                torchFloodFill((int)pos.x, (int)pos.y, (int)pos.z);
            }
        };

        registerBlock(
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

        //this passes all block data to the chunk mesh generator thread
        passChunkMeshThreadData(drawTypes,frontTextures,backTextures,rightTextures,leftTextures,topTextures,bottomTextures,isLiquids);
    }

    public static boolean blockHasOnRightClickCall(byte ID){
        return(isRightClickables[ID] && blockModifiers[ID] != null);
    }

    public static float[] getFrontTexturePoints(byte ID, byte rotation){
        return switch (rotation) {
            case 1 -> rightTextures[ID];
            case 2 -> backTextures[ID];
            case 3 -> leftTextures[ID];
            default -> frontTextures[ID];
        };
    }
    public static float[] getBackTexturePoints(byte ID, byte rotation){
        return switch (rotation) {
            case 1 -> leftTextures[ID];
            case 2 -> frontTextures[ID];
            case 3 -> rightTextures[ID];
            default -> backTextures[ID];
        };

    }
    public static float[] getRightTexturePoints(byte ID, byte rotation){
        return switch (rotation) {
            case 1 -> backTextures[ID];
            case 2 -> leftTextures[ID];
            case 3 -> frontTextures[ID];
            default -> rightTextures[ID];
        };
    }
    public static float[] getLeftTexturePoints(byte ID, byte rotation){
        return switch (rotation) {
            case 1 -> frontTextures[ID];
            case 2 -> rightTextures[ID];
            case 3 -> backTextures[ID];
            default -> leftTextures[ID];
        };
    }

    public static BlockModifier getBlockModifier(byte ID){
        return blockModifiers[ID];
    }

    public static float getStoneHardness(byte ID){
        return stoneHardnesses[ID];
    }

    public static float getDirtHardness(byte ID){
        return dirtHardnesses[ID];
    }

    public static float getWoodHardness(byte ID){
        return woodHardnesses[ID];
    }

    public static float getLeafHardness(byte ID){
        return leafHardnesses[ID];
    }

    public static boolean isBlockLiquid(byte ID){
        return isLiquids[ID];
    }

    public static float getBlockViscosity(byte ID){
        return viscositys[ID];
    }
    public static float[] getTopTexturePoints(byte ID){
        return topTextures[ID];
    }
    public static float[] getBottomTexturePoints(byte ID){
        return bottomTextures[ID];
    }

    public static boolean isBlockPointable(byte ID){
        return pointables[ID];
    }

    public static String getDigSound(byte ID){
        return digSounds[ID];
    }

    //these two methods are specifically designed for the ChunkMeshGenerator
    public static byte getBlockIDsSize(){
        return maxIDs;
    }
    public static byte getBlockShapeMapSize(){
        return (byte)blockShapeMap.length;
    }
}
