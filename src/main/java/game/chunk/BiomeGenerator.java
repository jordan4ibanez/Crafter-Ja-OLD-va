package game.chunk;

import engine.FastNoise;
import org.joml.Math;
import org.joml.Vector2i;
import org.joml.Vector3i;

import java.util.ArrayDeque;
import java.util.Deque;

import static engine.Window.windowShouldClose;
import static game.chunk.Chunk.*;
import static game.chunk.ChunkUpdateHandler.chunkUpdate;

//this class is entirely run on it's own thread, be careful with OpenGL context
public class BiomeGenerator implements Runnable {

    //static reference to self object
    private static BiomeGenerator thisThing = null;

    private final ArrayDeque<Vector2i> queue = new ArrayDeque<>();
    private int seed = 532_444_432;
    private final FastNoise noise = new FastNoise();

    //self starting thread
    public BiomeGenerator(){
        thisThing = this;
    }

    @Override
    public void run() {
        noise.SetSeed(seed);
        while (!windowShouldClose()) {
            boolean needsToSleep = runBiomeGeneration();
            if (needsToSleep){
                try {
                    //System.out.println("sleeping");
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } //else {
            //System.out.println("I'm awake");
            //}
        }
    }

    public void internalAddChunkToBiomeGenerator(int x, int z){
        queue.add(new Vector2i(x,z));
    }

    public static void addChunkToBiomeGeneration(int x, int z){
        thisThing.internalAddChunkToBiomeGenerator(x, z);
    }

    private final Vector2i newData = new Vector2i();
    private final Deque<Vector3i> treePosQueue = new ArrayDeque<>();

    private boolean runBiomeGeneration(){

        if (queue.isEmpty()) {
            return true;
        }

        newData.set(queue.pop());

        int chunkX = newData.x;
        int chunkZ = newData.y;


        //don't regen existing chunks
        if (getChunkKey(newData.x, newData.y) != null) {
            return false;
        }

        byte[] blockData = new byte[32768];
        byte[] rotationData = new byte[32768];
        byte[] lightData = new byte[32768];
        byte[] heightMapData = new byte[256];

        //standard generation
        byte generationX;
        byte generationZ;
        byte height;
        int noiseMultiplier = 50;
        byte waterHeight = 50;
        double heightAdder = 70;
        for (generationX = 0; generationX < 16; generationX++) {
            for (generationZ = 0; generationZ < 16; generationZ++) {
                boolean gennedSand = false;
                boolean gennedWater = false;
                boolean gennedGrass = false;
                double dirtHeightRandom = Math.floor(Math.random() * 2d);

                float realPosX = (float) ((chunkX * 16d) + generationX);
                float realPosZ = (float) ((chunkZ * 16d) + generationZ);

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
                    byte dirtHeight = 4;
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
                            System.out.println(noise.GetSimplex(generationX, generationY, generationZ));
                            if (noise.GetSimplex(generationX, generationY, generationZ) > 0.25) {
                                currBlock = 20;
                            } else {
                                currBlock = 35;
                            }
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
                        heightMapData[posToIndex2D(generationX,generationZ)] = height;
                    } else {
                        heightMapData[posToIndex2D(generationX,generationZ)] = waterHeight;
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

                    float realPosX = (float) ((chunkX * 16d) + generationX);
                    float realPosZ = (float) ((chunkZ * 16d) + generationZ);

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
                    if (heightMapData[posToIndex2D(basePos.x,basePos.z)] < y + basePos.y){
                        heightMapData[posToIndex2D(basePos.x,basePos.z)] = (byte)(y + basePos.y);
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
                            if (heightMapData[posToIndex2D(basePos.x + x,basePos.z + z)] < y + basePos.y) {
                                heightMapData[posToIndex2D(basePos.x + x,basePos.z + z)] = (byte) (y + basePos.y);
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
        
        return false;
    }

    //only use the current thread's core/thread to calculate
    private int posToIndex( int x, int y, int z ) {
        return (z * 2048) + (y * 16) + x;
    }

    //make the inverse of this eventually
    private int posToIndex2D(int x, int z){
        return (z * 16) + x;
    }

    //Thanks a lot Lars!!
    private byte getByteTorchLight(byte input){
        return (byte) (input & ((1 << 4) - 1));
    }
    private byte getByteNaturalLight(byte input){
        return (byte) (((1 << 4) - 1) & input >> 4);
    }

    private byte setByteTorchLight(byte input, byte newValue){
        byte naturalLight = getByteNaturalLight(input);
        return (byte) (naturalLight << 4 | newValue);
    }

    private byte setByteNaturalLight(byte input, byte newValue){
        byte torchLight = getByteTorchLight(input);
        return (byte) (newValue << 4 | torchLight);
    }

    public void setSeed(int newSeed){
        seed = newSeed;
    }
}
