package game.light;

import org.joml.Vector3f;

import java.util.*;

import static game.chunk.Chunk.*;

public class Light {
    private static final Deque<Vector3f> queue = new ArrayDeque<>();
    private static final Deque<LightUpdate> lightSources = new ArrayDeque<>();;

    private static final byte maxLightLevel = 15;
    private static final byte blockIndicator = 127;
    private static final byte lightDistance = 15;
    private static final byte max = (lightDistance * 2) + 1;
    private static final byte[][][] memoryMap = new byte[(lightDistance * 2) + 1][(lightDistance * 2) + 1][(lightDistance * 2) + 1];

    public static void lightFloodFill(int posX, int posY, int posZ) {
        queue.add(new Vector3f(posX, posY, posZ));
    }

    public static void indexLight(){
//        new Thread(() -> {
            if (!queue.isEmpty()) {
                Vector3f tempObject = queue.pop();
                int posX = (int) tempObject.x;
                int posY = (int) tempObject.y;
                int posZ = (int) tempObject.z;

                for (int x = posX - lightDistance; x <= posX + lightDistance; x++) {
                    for (int y = posY - lightDistance; y <= posY + lightDistance; y++) {
                        for (int z = posZ - lightDistance; z <= posZ + lightDistance; z++) {
                            int theBlock = getBlock(x, y, z);
                            if (theBlock == 0 && underSunLight(x, y, z)) {
                                int skipCheck = 0;
                                if (getBlock(x + 1, y, z) == 0 && underSunLight(x + 1, y, z) && getLight(x + 1, y, z) == maxLightLevel) {
                                    skipCheck++;
                                }
                                if (getBlock(x - 1, y, z) == 0 && underSunLight(x - 1, y, z) && getLight(x - 1, y, z) == maxLightLevel) {
                                    skipCheck++;
                                }
                                if (getBlock(x, y + 1, z) == 0 && underSunLight(x, y + 1, z) && getLight(x, y + 1, z) == maxLightLevel) {
                                    skipCheck++;
                                }
                                if (getBlock(x, y - 1, z) == 0 && underSunLight(x, y - 1, z) && getLight(x, y - 1, z) == maxLightLevel) {
                                    skipCheck++;
                                }
                                if (getBlock(x, y, z + 1) == 0 && underSunLight(x, y, z + 1) && getLight(x, y, z + 1) == maxLightLevel) {
                                    skipCheck++;
                                }
                                if (getBlock(x, y, z - 1) == 0 && underSunLight(x, y, z - 1) && getLight(x, y, z - 1) == maxLightLevel) {
                                    skipCheck++;
                                }
                                if (skipCheck < 6) {
                                    lightSources.add(new LightUpdate(x - posX + lightDistance, y - posY + lightDistance, z - posZ + lightDistance));
                                }
                                memoryMap[x - posX + lightDistance][y - posY + lightDistance][z - posZ + lightDistance] = maxLightLevel;
                            } else if (theBlock == 0) {
                                memoryMap[x - posX + lightDistance][y - posY + lightDistance][z - posZ + lightDistance] = 0;
                            } else {
                                memoryMap[x - posX + lightDistance][y - posY + lightDistance][z - posZ + lightDistance] = blockIndicator;
                            }
                        }
                    }
                }

                while (!lightSources.isEmpty()) {
                    LightUpdate thisUpdate = lightSources.pop();

                    Deque<LightUpdate> lightSteps = new ArrayDeque<>();

                    int[] crawlerPos;

                    lightSteps.push(new LightUpdate(thisUpdate.x, thisUpdate.y, thisUpdate.z, maxLightLevel));

                    while (!lightSteps.isEmpty()) {
                        LightUpdate newUpdate = lightSteps.pop();

                        if (newUpdate.level <= 1) {
                            continue;
                        }
                        if (newUpdate.x < 0 || newUpdate.x > max || newUpdate.y < 0 || newUpdate.y > max || newUpdate.z < 0 || newUpdate.z > max) {
                            continue;
                        }

                        crawlerPos = new int[]{newUpdate.x, newUpdate.y, newUpdate.z};

                        //+x
                        {
                            if (crawlerPos[0] + 1 < max && memoryMap[crawlerPos[0] + 1][crawlerPos[1]][crawlerPos[2]] < newUpdate.level - 1) {
                                memoryMap[crawlerPos[0] + 1][crawlerPos[1]][crawlerPos[2]] = (byte) (newUpdate.level - 1);
                                lightSteps.add(new LightUpdate(crawlerPos[0] + 1, crawlerPos[1], crawlerPos[2], (byte) (newUpdate.level - 1)));
                            }
                        }

                        //-x
                        {
                            if (crawlerPos[0] - 1 >= 0 && memoryMap[crawlerPos[0] - 1][crawlerPos[1]][crawlerPos[2]] < newUpdate.level - 1) {
                                memoryMap[crawlerPos[0] - 1][crawlerPos[1]][crawlerPos[2]] = (byte) (newUpdate.level - 1);
                                lightSteps.add(new LightUpdate(crawlerPos[0] - 1, crawlerPos[1], crawlerPos[2], (byte) (newUpdate.level - 1)));
                            }
                        }

                        //+z
                        {
                            if (crawlerPos[2] + 1 < max && memoryMap[crawlerPos[0]][crawlerPos[1]][crawlerPos[2] + 1] < newUpdate.level - 1) {
                                memoryMap[crawlerPos[0]][crawlerPos[1]][crawlerPos[2] + 1] = (byte) (newUpdate.level - 1);
                                lightSteps.add(new LightUpdate(crawlerPos[0], crawlerPos[1], crawlerPos[2] + 1, (byte) (newUpdate.level - 1)));
                            }
                        }

                        //-z
                        {
                            if (crawlerPos[2] - 1 >= 0 && memoryMap[crawlerPos[0]][crawlerPos[1]][crawlerPos[2] - 1] < newUpdate.level - 1) {
                                memoryMap[crawlerPos[0]][crawlerPos[1]][crawlerPos[2] - 1] = (byte) (newUpdate.level - 1);
                                lightSteps.add(new LightUpdate(crawlerPos[0], crawlerPos[1], crawlerPos[2] - 1, (byte) (newUpdate.level - 1)));
                            }
                        }

                        //+y
                        {
                            if (crawlerPos[1] + 1 < max && memoryMap[crawlerPos[0]][crawlerPos[1] + 1][crawlerPos[2]] < newUpdate.level - 1) {
                                memoryMap[crawlerPos[0]][crawlerPos[1] + 1][crawlerPos[2]] = (byte) (newUpdate.level - 1);
                                lightSteps.add(new LightUpdate(crawlerPos[0], crawlerPos[1] + 1, crawlerPos[2], (byte) (newUpdate.level - 1)));
                            }
                        }

                        //-y
                        {
                            if (crawlerPos[1] - 1 >= 0 && memoryMap[crawlerPos[0]][crawlerPos[1] - 1][crawlerPos[2]] < newUpdate.level - 1) {
                                memoryMap[crawlerPos[0]][crawlerPos[1] - 1][crawlerPos[2]] = (byte) (newUpdate.level - 1);
                                lightSteps.add(new LightUpdate(crawlerPos[0], crawlerPos[1] - 1, crawlerPos[2], (byte) (newUpdate.level - 1)));
                            }
                        }
                    }
                }

                for (int x = posX - lightDistance; x <= posX + lightDistance; x++) {
                    for (int y = posY - lightDistance; y <= posY + lightDistance; y++) {
                        for (int z = posZ - lightDistance; z <= posZ + lightDistance; z++) {
                            if (memoryMap[x - posX + lightDistance][y - posY + lightDistance][z - posZ + lightDistance] != blockIndicator) {
                                setLight(x, y, z, memoryMap[x - posX + lightDistance][y - posY + lightDistance][z - posZ + lightDistance]);
                            }
                        }
                    }
                }
                lightSources.clear();
            }
    }
}