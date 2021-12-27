package game.chunk;

import engine.FastNoise;
import org.joml.Math;
import org.joml.Vector2i;
import org.joml.Vector3i;

import java.util.*;

import static engine.Window.windowShouldClose;
import static game.chunk.Chunk.getChunkKey;
import static game.chunk.Chunk.setChunk;
import static game.chunk.ChunkUpdateHandler.chunkUpdate;

//this class is entirely run on it's own thread, be careful with OpenGL context
public class BiomeGenerator implements Runnable{

    private final static Deque<Vector2i> queue = new ArrayDeque<>();
    private final static int seed = 532_444_432;
    private final static FastNoise noise = new FastNoise();

    public void run() {
        noise.SetSeed(seed);
        while (!windowShouldClose()) {
            runBiomeGeneration();
        }
    }

    public static void addChunkToBiomeGeneration(int x, int z){
        queue.add(new Vector2i(x,z));
    }

    private static final double heightAdder = 70;
    private static final byte dirtHeight = 4;
    private static final byte waterHeight = 50;
    private static final int noiseMultiplier = 50;
    private static final Vector2i newData = new Vector2i();
    private static final Deque<Vector3i> treePosQueue = new ArrayDeque<>();

    private static void runBiomeGeneration(){

        if (!queue.isEmpty()) {

            newData.set(queue.pop());

            int chunkX = newData.x;
            int chunkZ = newData.y;


            //don't regen existing chunks
            if (getChunkKey(newData) != null) {
                return;
            }

            byte[] blockData = new byte[32768];
            byte[] rotationData = new byte[32768];
            byte[] lightData = new byte[32768];
            byte[][] heightMapData = new byte[16][16];

            //standard generation
            byte generationX;
            byte generationZ;
            byte height;
            for (generationX = 0; generationX < 16; generationX++) {
                for (generationZ = 0; generationZ < 16; generationZ++) {
                    boolean gennedSand = false;
                    boolean gennedWater = false;
                    boolean gennedGrass = false;
                    double dirtHeightRandom = Math.floor(Math.random() * 2d);

                    float realPosX = (float) ((chunkX * 16d) + (double) generationX);
                    float realPosZ = (float) ((chunkZ * 16d) + (double) generationZ);

                    height = (byte) (Math.abs(noise.GetPerlin(realPosX, realPosZ) * noiseMultiplier + heightAdder));

                    //catch ultra deep oceans
                    if (height < 6) {
                        height = 6;
                    }

                    //y column
                    byte generationY;
                    for (generationY = 127; generationY >= 0; generationY--) {

                        //don't overwrite
                        byte currBlock = blockData[posToIndex(generationX, generationY, generationZ)];

                        //bedrock
                        if (generationY <= 0 + dirtHeightRandom) {
                            currBlock = 5;
                            //grass gen
                        } else if (generationY == height && generationY >= waterHeight) {

                            if (generationY <= waterHeight + 1) {
                                currBlock = 20;
                                gennedSand = true;
                            } else {
                                currBlock = 2;
                                gennedGrass = true;
                            }
                            //tree gen
                        } else if (generationY == height + 1 && generationY > waterHeight + 1) {

                            float noiseTest2 = Math.abs(noise.GetWhiteNoise(realPosX, generationY, realPosZ));

                            //add tree to queue
                            if (noiseTest2 > 0.98f) {
                                treePosQueue.add(new Vector3i(generationX, generationY, generationZ));
                            }
                            //dirt/sand gen
                        } else if (generationY < height && generationY >= height - dirtHeight - dirtHeightRandom) {
                            if (gennedSand || gennedWater) {
                                gennedSand = true;
                                currBlock = 20;
                            } else {
                                currBlock = 1;
                            }

                            //stone gen
                        } else if (generationY < height - dirtHeight) {
                            if (generationY <= 30 && generationY > 0) {
                                if (Math.random() > 0.95) {
                                    currBlock = (byte) Math.floor(8 + (Math.random() * 8));
                                } else {
                                    currBlock = 3;
                                }
                            } else {
                                currBlock = 3;
                            }
                            //water gen
                        } else {
                            if (generationY <= waterHeight) {
                                currBlock = 7;
                                gennedWater = true;
                            }
                        }

                        blockData[posToIndex(generationX, generationY, generationZ)] = currBlock;

                        if (height >= waterHeight) {
                            heightMapData[generationX][generationZ] = height;
                        } else {
                            heightMapData[generationX][generationZ] = waterHeight;
                        }

                        if (gennedSand || gennedGrass) {
                            lightData[posToIndex(generationX, generationY, generationZ)] = 0;
                        } else {
                            lightData[posToIndex(generationX, generationY, generationZ)] = setByteNaturalLight((byte)0,(byte)15);
                        }
                    }
                }
            }

            //check for trees outside chunk borders (simulated chunk generation)
            for (generationX = -3; generationX < 16 + 3; generationX++) {
                for (generationZ = -3; generationZ < 16 + 3; generationZ++) {

                    //only check outside
                    if (generationX < 0 || generationX > 15 || generationZ < 0 || generationZ > 15) {

                        float realPosX = (float) ((chunkX * 16d) + (double) generationX);
                        float realPosZ = (float) ((chunkZ * 16d) + (double) generationZ);

                        height = (byte) (Math.abs(noise.GetPerlin(realPosX, realPosZ) * noiseMultiplier + heightAdder) + (byte) 1);

                        if (height > waterHeight + 1) {

                            float noiseTest2 = Math.abs(noise.GetWhiteNoise(realPosX, height, realPosZ));

                            //add tree to queue
                            if (noiseTest2 > 0.98f) {
                                treePosQueue.add(new Vector3i(generationX, height, generationZ));
                            }

                        }
                    }
                }
            }

            //generate tree cores
            for (Vector3i basePos : treePosQueue) {
                //generate stumps
                for (byte y = 0; y < 4; y++) {
                    //stay within borders
                    if (y + basePos.y < 127 && basePos.x >= 0 && basePos.x <= 15 && basePos.z >= 0 && basePos.z <= 15) {
                        blockData[posToIndex(basePos.x, basePos.y + y, basePos.z)] = 25;
                        //updates heightmap
                        if (heightMapData[basePos.x][basePos.z] < y + basePos.y){
                            heightMapData[basePos.x][basePos.z] = (byte)(y + basePos.y);
                        }
                    }
                }
            }

            //generate tree leaves
            for (Vector3i basePos : treePosQueue) {
                byte treeWidth = 0;
                for (byte y = 5; y > 1; y--) {
                    for (byte x = (byte) -treeWidth; x <= treeWidth; x++) {
                        for (byte z = (byte) -treeWidth; z <= treeWidth; z++) {


                            if (basePos.x + x >= 0 && basePos.x + x <= 15 && basePos.y + y >= 0 && basePos.y + y <= 127 && basePos.z + z >= 0 && basePos.z + z <= 15) {

                                int index = posToIndex(basePos.x + x, basePos.y + y, basePos.z + z);
                                if (blockData[index] == 0) {
                                    blockData[index] = 26;
                                }

                                //updates heightmap
                                if (heightMapData[basePos.x + x][basePos.z + z] < y + basePos.y) {
                                    heightMapData[basePos.x + x][basePos.z + z] = (byte) (y + basePos.y);
                                }
                            }
                        }
                    }
                    if (treeWidth < 2){
                        treeWidth++;
                    }
                }

                for (byte x = (byte) -2; x <= 2; x++) {
                    for (byte z = (byte) -2; z <= 2; z++) {

                        //generates shadows under the trees
                        boolean solved = false;
                        byte y = (byte) (basePos.y + 1);
                        if (Math.abs(x) == 2 || Math.abs(z) == 2) {
                            while (!solved) {
                                if (basePos.x + x >= 0 && basePos.x + x <= 15 && y >= 0 && basePos.z + z >= 0 && basePos.z + z <= 15) {
                                    if (blockData[posToIndex(basePos.x + x, y, basePos.z + z)] == 0) {
                                        lightData[posToIndex(basePos.x + x, y, basePos.z + z)] = setByteNaturalLight((byte) 0, (byte) 14);
                                    } else {
                                        solved = true;
                                    }
                                    y--;
                                } else {
                                    solved = true;
                                }
                            }
                        } else if (Math.abs(x) == 1 || Math.abs(z) == 1){
                            while (!solved) {
                                if (basePos.x + x >= 0 && basePos.x + x <= 15 && y >= 0 && basePos.z + z >= 0 && basePos.z + z <= 15) {
                                    if (blockData[posToIndex(basePos.x + x, y, basePos.z + z)] == 0) {
                                        lightData[posToIndex(basePos.x + x, y, basePos.z + z)] = setByteNaturalLight((byte) 0, (byte) 13);
                                    } else {
                                        solved = true;
                                    }
                                    y--;
                                } else {
                                    solved = true;
                                }
                            }
                        }
                    }
                }
            }

            setChunk(chunkX,chunkZ,blockData,rotationData,lightData,heightMapData);

            //dump everything into the chunk updater
            for (int i = 0; i < 8; i++) {
                chunkUpdate(chunkX, chunkZ, i);
            }

            treePosQueue.clear();

            //instantSave(thisChunk);
        }
    }

    //only use the current thread's core/thread to calculate
    public static int posToIndex( int x, int y, int z ) {
        return (z * 2048) + (y * 16) + x;
    }

    //Thanks a lot Lars!!
    public static byte getByteTorchLight(byte input){
        return (byte) (input & ((1 << 4) - 1));
    }
    public static byte getByteNaturalLight(byte input){
        return (byte) (((1 << 4) - 1) & input >> 4);
    }

    public static byte setByteTorchLight(byte input, byte newValue){
        byte naturalLight = getByteNaturalLight(input);
        return (byte) (naturalLight << 4 | newValue);
    }

    public static byte setByteNaturalLight(byte input, byte newValue){
        byte torchLight = getByteTorchLight(input);
        return (byte) (newValue << 4 | torchLight);
    }
}
