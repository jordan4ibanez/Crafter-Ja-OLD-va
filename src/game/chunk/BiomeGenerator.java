package game.chunk;

import engine.FastNoise;
import org.joml.Vector2i;
import org.joml.Vector3i;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedList;

import static engine.Window.windowShouldClose;
import static game.chunk.Chunk.*;
import static game.chunk.ChunkMath.posToIndex;
import static game.chunk.ChunkUpdateHandler.chunkUpdate;

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


    private static void runBiomeGeneration(){

        if (!queue.isEmpty()) {

            Vector2i newData = queue.pop();

            int chunkX = newData.x;
            int chunkZ = newData.y;

            ChunkObject thisChunk = getChunk(chunkX, chunkZ);

            if (thisChunk == null) {
                thisChunk = new ChunkObject(chunkX, chunkZ);
            } else {
                return;
            }

            thisChunk.modified = true;
            //biome max 128 trees
            LinkedList<Vector3i> treePosArray = new LinkedList<>();
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
                        byte currBlock = thisChunk.block[posToIndex(generationX, generationY, generationZ)];

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
                                treePosArray.add(new Vector3i(generationX, generationY, generationZ));
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

                        thisChunk.block[posToIndex(generationX, generationY, generationZ)] = currBlock;

                        if (height >= waterHeight) {
                            thisChunk.heightMap[generationX][generationZ] = height;
                        } else {
                            thisChunk.heightMap[generationX][generationZ] = waterHeight;
                        }

                        if (gennedSand || gennedGrass) {
                            thisChunk.light[posToIndex(generationX, generationY, generationZ)] = 0;
                        } else {
                            thisChunk.light[posToIndex(generationX, generationY, generationZ)] = setByteNaturalLight((byte)0,(byte)15);
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
                                treePosArray.add(new Vector3i(generationX, height, generationZ));
                            }

                        }
                    }
                }
            }

            //generate tree cores
            for (Vector3i basePos : treePosArray) {
                //generate stumps
                for (int y = 0; y < 4; y++) {
                    //stay within borders
                    if (y + basePos.y < 127 && basePos.x >= 0 && basePos.x <= 15 && basePos.z >= 0 && basePos.z <= 15) {
                        thisChunk.block[posToIndex(basePos.x, basePos.y + y, basePos.z)] = 25;
                    }
                }
            }

            //generate tree leaves
            for (Vector3i basePos : treePosArray) {
                byte treeWidth = 0;
                for (int y = 5; y > 1; y--) {
                    for (int x = -treeWidth; x <= treeWidth; x++) {
                        for (int z = -treeWidth; z <= treeWidth; z++) {

                            if (basePos.x + x >= 0 && basePos.x + x <= 15 &&
                                    basePos.y + y >= 0 && basePos.y + y <= 127 &&
                                    basePos.z + z >= 0 && basePos.z + z <= 15) {

                                int index = posToIndex(basePos.x + x, basePos.y + y, basePos.z + z);

                                if (thisChunk.block[index] == 0) {
                                    thisChunk.block[index] = 26;
                                }
                            }
                        }
                    }
                    if (treeWidth < 2) {
                        treeWidth++;
                    }
                }

            }

            setChunk(thisChunk);

            //dump everything into the chunk updater
            for (int i = 0; i < 8; i++) {
                //generateChunkMesh(thisChunk.x, thisChunk.z, i); //instant
                chunkUpdate(thisChunk.x, thisChunk.z, i); //delayed
            }

            //instantSave(thisChunk);
        }
    }
}
