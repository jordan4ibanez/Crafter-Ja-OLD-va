package game.light;

import org.joml.Vector3i;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

public class Light implements Runnable {

    //internal pointer to self reference
    private Light thisObject;

    //thread safe containers for light updates
    private final ConcurrentLinkedDeque<Vector3i> lightQueue = new ConcurrentLinkedDeque<>();
    private final ConcurrentLinkedDeque<Vector3i> torchQueue = new ConcurrentLinkedDeque<>();

    //point internal pointer to reference call, only one object shall exist
    public Light(){
        thisObject = this;
    }

    //external call to internal object
    public void lightFloodFill(int posX, int posY, int posZ){
        thisObject.lightQueue.add(new Vector3i(posX, posY, posZ));
    }

    //external call to internal object
    public void torchFloodFill(int posX, int posY, int posZ){
        thisObject.torchQueue.add(new Vector3i(posX, posY, posZ));
    }

    //finalized constants
    private final byte maxLightLevel = 15;
    private final byte blockIndicator = 127;
    private final byte lightDistance = 15;
    private final byte max = (lightDistance * 2) + 1;

    private byte currentLightLevel = 15;

    public byte getCurrentGlobalLightLevel(){
        return currentLightLevel;
    }

    public void setCurrentLightLevel(byte newLightLevel) {
        currentLightLevel = newLightLevel;
        setChunkThreadCurrentGlobalLightLevel(currentLightLevel);
        updateChunksWithNewLightLevel();
    }

    private void updateChunksWithNewLightLevel(){
        floodChunksWithNewLight();
    }

    public byte getImmediateLight(int x, int y, int z){
        int theBlock = getBlock(x, y, z);

        if (theBlock == 0 && underSunLight(x, y, z)){
            return thisObject.maxLightLevel;
        }

        byte maxLight = 0;

        if (getBlock(x + 1, y, z) == 0) {
            byte gottenLight = getNaturalLight(x + 1, y, z);
            if (gottenLight > maxLight + 1){
                maxLight = (byte)(gottenLight - 1);
            }
        }
        if (getBlock(x - 1, y, z) == 0) {
            byte gottenLight = getNaturalLight(x - 1, y, z);
            if (gottenLight > maxLight + 1){
                maxLight = (byte)(gottenLight - 1);
            }
        }
        if (getBlock(x, y + 1, z) == 0) {
            byte gottenLight = getNaturalLight(x, y + 1, z);
            if (gottenLight > maxLight + 1){
                maxLight = (byte)(gottenLight - 1);
            }
        }
        if (getBlock(x, y - 1, z) == 0) {
            byte gottenLight = getNaturalLight(x, y - 1, z);
            if (gottenLight > maxLight + 1){
                maxLight = (byte)(gottenLight - 1);
            }
        }
        if (getBlock(x, y, z + 1) == 0) {
            byte gottenLight = getNaturalLight(x, y, z + 1);
            if (gottenLight > maxLight + 1){
                maxLight = (byte)(gottenLight - 1);
            }
        }
        if (getBlock(x, y, z - 1) == 0) {
            byte gottenLight = getNaturalLight(x, y, z - 1);
            if (gottenLight > maxLight + 1){
                maxLight = (byte)(gottenLight - 1);
            }
        }

        return maxLight;
    }

    //internal
    //this needs to calculate the edges of the light as light sources which might be EXTREMELY heavy duty
    private boolean internalLightFloodFill() {
        if (lightQueue.isEmpty()){
            return true;
        }

        Vector3i thisUpdatePos = lightQueue.pop();
        final Deque<LightUpdate> lightSources = new ArrayDeque<>();
        final byte[][][] memoryMap = new byte[(lightDistance * 2) + 1][(lightDistance * 2) + 1][(lightDistance * 2) + 1];
        for (int x = thisUpdatePos.x - lightDistance; x <= thisUpdatePos.x + lightDistance; x++) {
            for (int y = thisUpdatePos.y - lightDistance; y <= thisUpdatePos.y + lightDistance; y++) {
                for (int z = thisUpdatePos.z - lightDistance; z <= thisUpdatePos.z + lightDistance; z++) {
                    int theBlock = getBlock(x, y, z);
                    if (theBlock == 0 && underSunLight(x, y, z)) {
                        int skipCheck = 0;
                        if (getBlock(x + 1, y, z) == 0 && underSunLight(x + 1, y, z) && getNaturalLight(x + 1, y, z) == currentLightLevel) {
                            skipCheck++;
                        }
                        if (getBlock(x - 1, y, z) == 0 && underSunLight(x - 1, y, z) && getNaturalLight(x - 1, y, z) == currentLightLevel) {
                            skipCheck++;
                        }
                        if (getBlock(x, y + 1, z) == 0 && underSunLight(x, y + 1, z) && getNaturalLight(x, y + 1, z) == currentLightLevel) {
                            skipCheck++;
                        }
                        if (getBlock(x, y - 1, z) == 0 && underSunLight(x, y - 1, z) && getNaturalLight(x, y - 1, z) == currentLightLevel) {
                            skipCheck++;
                        }
                        if (getBlock(x, y, z + 1) == 0 && underSunLight(x, y, z + 1) && getNaturalLight(x, y, z + 1) == currentLightLevel) {
                            skipCheck++;
                        }
                        if (getBlock(x, y, z - 1) == 0 && underSunLight(x, y, z - 1) && getNaturalLight(x, y, z - 1) == currentLightLevel) {
                            skipCheck++;
                        }
                        if (skipCheck < 6) {
                            lightSources.add(new LightUpdate(x - thisUpdatePos.x + lightDistance, y - thisUpdatePos.y + lightDistance, z - thisUpdatePos.z + lightDistance));
                        }
                        memoryMap[x - thisUpdatePos.x + lightDistance][y - thisUpdatePos.y + lightDistance][z - thisUpdatePos.z + lightDistance] = currentLightLevel;
                    } else if (theBlock == 0) {
                        memoryMap[x - thisUpdatePos.x + lightDistance][y - thisUpdatePos.y + lightDistance][z - thisUpdatePos.z + lightDistance] = 0;
                    } else {
                        memoryMap[x - thisUpdatePos.x + lightDistance][y - thisUpdatePos.y + lightDistance][z - thisUpdatePos.z + lightDistance] = blockIndicator;
                    }
                }
            }
        }

        int[] crawlerPos;

        while (!lightSources.isEmpty()) {
            LightUpdate thisUpdate = lightSources.pop();

            Deque<LightUpdate> lightSteps = new ArrayDeque<>();

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

        for (int x = thisUpdatePos.x - lightDistance; x <= thisUpdatePos.x + lightDistance; x++) {
            for (int y = thisUpdatePos.y - lightDistance; y <= thisUpdatePos.y + lightDistance; y++) {
                for (int z = thisUpdatePos.z - lightDistance; z <= thisUpdatePos.z + lightDistance; z++) {
                    if (memoryMap[x - thisUpdatePos.x + lightDistance][y - thisUpdatePos.y + lightDistance][z - thisUpdatePos.z + lightDistance] != blockIndicator) {
                        setNaturalLight(x, y, z, memoryMap[x - thisUpdatePos.x + lightDistance][y - thisUpdatePos.y + lightDistance][z - thisUpdatePos.z + lightDistance]);
                    }
                }
            }
        }
        lightSources.clear();

        return false;
    }



    //this needs to calculate the edges of the light as light sources which might be EXTREMELY heavy duty
    private boolean internalTorchFloodFill() {

        if (torchQueue.isEmpty()){
            return true;
        }

        Vector3i thisTorchUpdate = torchQueue.pop();

        final Deque<LightUpdate> lightSources = new ArrayDeque<>();
        final byte[][][] memoryMap = new byte[(lightDistance * 2) + 1][(lightDistance * 2) + 1][(lightDistance * 2) + 1];

        //lightSources.add(new LightUpdate(lightDistance, lightDistance, lightDistance, getTorchLight(posX,posY,posZ)));

        int minX = thisTorchUpdate.x - lightDistance;
        int maxX = thisTorchUpdate.x + lightDistance;
        int minY = thisTorchUpdate.y - lightDistance;
        int maxY = thisTorchUpdate.y + lightDistance;
        int minZ = thisTorchUpdate.z - lightDistance;
        int maxZ = thisTorchUpdate.z + lightDistance;

        for (int x = thisTorchUpdate.x - lightDistance; x <= thisTorchUpdate.x + lightDistance; x++) {
            for (int y = thisTorchUpdate.y - lightDistance; y <= thisTorchUpdate.y + lightDistance; y++) {
                for (int z = thisTorchUpdate.z - lightDistance; z <= thisTorchUpdate.z + lightDistance; z++) {
                    int theBlock = getBlock(x, y, z);
                    if (theBlock == 29){
                        byte maxTorchLightLevel = 12;
                        lightSources.add(new LightUpdate( x - thisTorchUpdate.x + lightDistance, y - thisTorchUpdate.y + lightDistance, z - thisTorchUpdate.z + lightDistance, maxTorchLightLevel));
                    } else if (theBlock == 0 && (x == minX || x == maxX || y == minY || y == maxY || z == minZ || z == maxZ)) {
                        memoryMap[x - thisTorchUpdate.x + lightDistance][y - thisTorchUpdate.y + lightDistance][z - thisTorchUpdate.z + lightDistance] = getTorchLight(x, y, z);
                    } else if (theBlock != 0){
                        memoryMap[x - thisTorchUpdate.x + lightDistance][y - thisTorchUpdate.y + lightDistance][z - thisTorchUpdate.z + lightDistance] = blockIndicator;
                    } else { //everything else is zeroed out
                        memoryMap[x - thisTorchUpdate.x + lightDistance][y - thisTorchUpdate.y + lightDistance][z - thisTorchUpdate.z + lightDistance] = 0;
                    }
                }
            }
        }

        int[] crawlerPos;

        while (!lightSources.isEmpty()) {
            LightUpdate thisUpdate = lightSources.pop();

            Deque<LightUpdate> lightSteps = new ArrayDeque<>();

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

        for (int x = thisTorchUpdate.x - lightDistance; x <= thisTorchUpdate.x + lightDistance; x++) {
            for (int y = thisTorchUpdate.y - lightDistance; y <= thisTorchUpdate.y + lightDistance; y++) {
                for (int z = thisTorchUpdate.z - lightDistance; z <= thisTorchUpdate.z + lightDistance; z++) {
                    if (memoryMap[x - thisTorchUpdate.x + lightDistance][y - thisTorchUpdate.y + lightDistance][z - thisTorchUpdate.z + lightDistance] != blockIndicator) {
                        setTorchLight(x, y, z, memoryMap[x - thisTorchUpdate.x + lightDistance][y - thisTorchUpdate.y + lightDistance][z - thisTorchUpdate.z + lightDistance]);
                    }
                }
            }
        }
        return false;
    }


    private boolean sleepLock(boolean current, boolean input){
        if (!current){
            return false;
        }

        return input;
    }

    @Override
    public void run() {
        while (!windowShouldClose()) {
            boolean needsToSleep = true;

            needsToSleep = sleepLock(needsToSleep, internalLightFloodFill());
            needsToSleep = sleepLock(needsToSleep, internalTorchFloodFill());

            if (needsToSleep){
                try {
                    //System.out.println("sleeping");
                    Thread.sleep(30);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } //else {
                //System.out.println("I'm AWAKE! ");
            //}
        }
    }

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

}