package engine;

import engine.graph.*;
import game.chunk.ChunkObject;
import game.falling.FallingEntityObject;
import game.item.Item;
import game.mob.MobObject;
import game.particle.ParticleObject;
import org.joml.*;

import java.lang.Math;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

import static engine.FancyMath.getDistance;
import static engine.MouseInput.getMousePos;
import static engine.Timer.getFpsCounted;
import static engine.graph.Camera.*;
import static engine.graph.Transformation.*;
import static engine.graph.Transformation.buildOrthoProjModelMatrix;
import static engine.Hud.*;
import static game.Crafter.getDebugInfo;
import static game.falling.FallingEntity.getFallingEntities;
import static game.item.ItemEntity.*;
import static game.mob.Mob.getAllMobs;
import static game.particle.Particle.getAllParticles;
import static game.tnt.TNTEntity.*;
import static engine.Window.*;
import static game.chunk.Chunk.*;
import static game.player.Inventory.*;
import static game.player.Player.*;
import static org.lwjgl.opengl.GL11.*;

public class Renderer {

    private static float FOV = (float) Math.toRadians(72.0f); //todo: make this a calculator method ala calculateFOV(float);

    private static final float Z_NEAR = 0.1f;
    private static final float Z_FAR = 1120.f;

    private static float itemRotation = 0f;

    private static float windowScale = 0f;

    private static Vector2d windowSize = new Vector2d();

    private static ShaderProgram shaderProgram;

    private static ShaderProgram hudShaderProgram;

    public static Vector2d getWindowSize(){
        return windowSize;
    }

    private static void resetWindowScale(){
        if (windowSize.x <= windowSize.y){
            windowScale = (float)windowSize.x;
        } else {
            windowScale = (float) windowSize.y;
        }
        System.out.println("Window scale is now: " + windowScale);
    }

    public static float getWindowScale(){
        return windowScale;
    }

    public static void initRenderer() throws Exception{
        shaderProgram = new ShaderProgram();
        shaderProgram.createVertexShader(Utils.loadResource("/resources/vertex.vs"));
        shaderProgram.createFragmentShader(Utils.loadResource("/resources/fragment.fs"));
        shaderProgram.link();

        //create uniforms for world and projection matrices
        shaderProgram.createUniform("projectionMatrix");
        //create uniforms for model view matrix
        shaderProgram.createUniform("modelViewMatrix");
        //create uniforms for texture sampler
        shaderProgram.createUniform("texture_sampler");

        //ortholinear hud
        hudShaderProgram = new ShaderProgram();
        hudShaderProgram.createVertexShader(Utils.loadResource("/resources/hud_vertex.vs"));
        hudShaderProgram.createFragmentShader(Utils.loadResource("/resources/hud_fragment.fs"));
        hudShaderProgram.link();

        //create uniforms for model view matrix
        hudShaderProgram.createUniform("modelViewMatrix");
        //create uniforms for texture sampler
        hudShaderProgram.createUniform("texture_sampler");


        setWindowClearColor(0.53f,0.81f,0.92f,0.f);

        windowSize.x = getWindowWidth();
        windowSize.y = getWindowHeight();

        resetWindowScale();
    }

    public static void clearScreen(){
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }



    public static void renderGame(){
        Mesh workerMesh;
        clearScreen();

        if (isWindowResized()){
            windowSize.x = getWindowWidth();
            windowSize.y = getWindowHeight();
            glViewport(0,0, getWindowWidth(), getWindowHeight());
            setWindowResized(false);
            resetWindowScale();
        }


        //todo: BEGIN WORLD SHADER PROGRAM!
        shaderProgram.bind();

        //update projection matrix
        Matrix4d projectionMatrix = getProjectionMatrix(FOV + getRunningFOVAdder(), getWindowWidth(), getWindowHeight(), Z_NEAR, Z_FAR);

        shaderProgram.setUniform("projectionMatrix", projectionMatrix);

        //update the view matrix
        Matrix4d viewMatrix = getViewMatrix();

        shaderProgram.setUniform("texture_sampler", 0);

        Matrix4d modelViewMatrix;

//render each chunk column -- THIS NEEDS TO BE LAST
        Vector3d camPos = getCameraPosition();

        HashMap<Double, ChunkObject> chunkHash = new HashMap<Double, ChunkObject>();

        //get all distances
        for (ChunkObject thisChunk : getMap()){
            double currentDistance = getDistance((thisChunk.x * 16d) + 8d, 0,(thisChunk.z * 16d) + 8d, camPos.x, 0, camPos.z);
            chunkHash.put(currentDistance, thisChunk);
        }

        ChunkObject[] chunkArraySorted = new ChunkObject[chunkHash.size()];

        int arrayIndex = 0;

        //sort all distances
        while (!chunkHash.isEmpty()){

            //System.out.println(chunkHash.size());

            AtomicReference<Double> maxDistance = new AtomicReference<>(0d);

            chunkHash.forEach((distancer,y) ->{
                if(maxDistance.get() <= distancer) {
                    maxDistance.set(distancer);
                }
            });

            double maxDistancePrimitive = maxDistance.get();

            //link
            chunkArraySorted[arrayIndex] = chunkHash.get(maxDistancePrimitive);

            arrayIndex++;

            //remove
            chunkHash.remove(maxDistancePrimitive);
        }



        //render normal chunk meshes
        for (int i = 0; i < arrayIndex; i++) {

            ChunkObject thisChunk = chunkArraySorted[i];

            if (thisChunk == null) {
                continue;
            }

            //normal
            if (thisChunk.normalMesh != null) {
                for (Mesh thisMesh : thisChunk.normalMesh) {
                    if (thisMesh != null) {
                        modelViewMatrix = updateModelViewMatrix(new Vector3d(thisChunk.x * 16d, 0, thisChunk.z * 16d), new Vector3f(0, 0, 0), viewMatrix);
                        shaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                        thisMesh.render();
                    }
                }
            }
        }


        //render liquid chunk meshes
        for (int i = 0; i < arrayIndex; i++) {

            ChunkObject thisChunk = chunkArraySorted[i];

            if (thisChunk == null) {
                continue;
            }
            //liquid
            if (thisChunk.liquidMesh != null) {
                for (Mesh thisMesh : thisChunk.liquidMesh) {
                    if (thisMesh != null) {
                        modelViewMatrix = updateModelViewMatrix(new Vector3d(thisChunk.x * 16d, 0, thisChunk.z * 16d), new Vector3f(0, 0, 0), viewMatrix);
                        shaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                        thisMesh.render();
                    }
                }
            }
        }


        //render allFaces chunk meshes
        for (int i = 0; i < arrayIndex; i++) {

            ChunkObject thisChunk = chunkArraySorted[i];

            if (thisChunk == null) {
                continue;
            }

            //allFaces
            if (thisChunk.allFacesMesh != null) {
                for (Mesh thisMesh : thisChunk.allFacesMesh) {
                    if (thisMesh != null) {
                        modelViewMatrix = updateModelViewMatrix(new Vector3d(thisChunk.x * 16d, 0, thisChunk.z * 16d), new Vector3f(0, 0, 0), viewMatrix);
                        shaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                        thisMesh.render();
                    }
                }
            }
        }

        //        render each item entity
        for (Item thisItem : getAllItems()){
            modelViewMatrix = updateModelViewMatrix(new Vector3d(thisItem.pos).add(0,thisItem.hover,0), thisItem.rotation, viewMatrix);
            shaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
            thisItem.mesh.render();
        }

        //render each TNT entity
        Mesh tntMesh = getTNTMesh();
        for (int i = 0; i < getTotalTNT(); i++){
            if (!tntExists(i)){
                continue;
            }
            modelViewMatrix = getTNTModelViewMatrix(i, viewMatrix);
            shaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
            tntMesh.render();
        }

        //render falling entities
        for (FallingEntityObject thisObject : getFallingEntities()){
            modelViewMatrix = getGenericMatrixWithPosRotationScale(thisObject.pos, new Vector3f(0,0,0), new Vector3d(2.5d,2.5d,2.5d), viewMatrix);
            shaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
            thisObject.mesh.render();
        }


        //render mobs
        for (MobObject thisMob : getAllMobs()){
            if (thisMob == null){
                continue;
            }
            int offsetIndex = 0;

            for (Mesh thisMesh : thisMob.meshes) {
                modelViewMatrix = getMobMatrix(new Vector3d(thisMob.pos), thisMob.bodyOffsets[offsetIndex], new Vector3f(0, thisMob.smoothRotation, thisMob.deathRotation), new Vector3f(thisMob.bodyRotations[offsetIndex]), new Vector3d(1f, 1f, 1f), viewMatrix);
                shaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                thisMesh.render();
                offsetIndex++;
            }
        }


        //render particles
        for (ParticleObject thisParticle : getAllParticles()){
            Mesh thisMesh = thisParticle.mesh;

            modelViewMatrix = updateParticleViewMatrix(thisParticle.pos, new Vector3f(getCameraRotation()), viewMatrix);
            shaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
            thisMesh.render();
        }

        //render rain drops
        /*
        Mesh rainDrop = getRainDropMesh();
        for (RainDropEntity thisRainDrop : getRainDrops()){
            modelViewMatrix = updateParticleViewMatrix(thisRainDrop.pos, new Vector3f(0,getCameraRotation().y,0), viewMatrix);
            shaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
            rainDrop.render();
        }
         */

        //render world selection mesh
        if (getPlayerWorldSelectionPos() != null){

            Vector3i tempPos = getPlayerWorldSelectionPos();

            Mesh selectionMesh = getWorldSelectionMesh();

            Vector3d actualPos = new Vector3d(tempPos.x, tempPos.y, tempPos.z);

            modelViewMatrix = updateModelViewMatrix(actualPos, new Vector3f(0, 0, 0), viewMatrix);
            shaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
            selectionMesh.render();

            if (getDiggingFrame() >= 0) {
                Mesh crackMesh = getMiningCrackMesh();
                modelViewMatrix = updateModelViewMatrix(actualPos, new Vector3f(0, 0, 0), viewMatrix);
                shaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                crackMesh.render();
            }
        }

        //BEGIN HUD

        glClear(GL_DEPTH_BUFFER_BIT);

        projectionMatrix = getProjectionMatrix(FOV, getWindowWidth(), getWindowHeight(), Z_NEAR, Z_FAR);
        shaderProgram.setUniform("projectionMatrix", projectionMatrix);

        //draw wield hand or item
        {
            //wield hand
            if (getItemInInventorySlot(getPlayerInventorySelection(),0) == null){
                Mesh thisMesh = getWieldHandMesh();
                modelViewMatrix = getGenericMatrixWithPosRotationScale(getWieldHandAnimationPos(), getWieldHandAnimationRot(), new Vector3d(5d, 5d, 5d), new Matrix4d());
                hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                thisMesh.render();
            //block/item
            } else if (getWieldInventory() != null){
                Mesh thisMesh = getWieldInventory().mesh;
                modelViewMatrix = getGenericMatrixWithPosRotationScale(getWieldHandAnimationPos(), getWieldHandAnimationRot(), new Vector3d(20d, 20d, 20d), new Matrix4d());
                hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                thisMesh.render();
            }
        }

        shaderProgram.unbind();
        glClear(GL_DEPTH_BUFFER_BIT);

        //TODO: BEGIN HUD SHADER PROGRAM!
        hudShaderProgram.bind();
        hudShaderProgram.setUniform("texture_sampler", 0);
        resetOrthoProjectionMatrix(); // needed to get current screen size

        //render water effect
        if (isCameraSubmerged()) {
            modelViewMatrix = buildOrthoProjModelMatrix(new Vector3d(0,0,0),new Vector3f(0,0,0), new Vector3d(windowScale * 2,windowScale,windowScale));
            hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
            getGlobalWaterEffectMesh().render();
        }

        glClear(GL_DEPTH_BUFFER_BIT);

        {
            //render inverted crosshair
            glBlendFunc(GL_ONE_MINUS_DST_COLOR, GL_ONE_MINUS_SRC_COLOR);
            modelViewMatrix = buildOrthoProjModelMatrix(new Vector3d(0,0,0),new Vector3f(0,0,0), new Vector3d(windowScale/20f,windowScale/20f,windowScale/20f));
            hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
            getCrossHairMesh().render();
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        }






        glClear(GL_DEPTH_BUFFER_BIT);

        if (!isPaused()) {
            if (isPlayerInventoryOpen()) {
                {
                    modelViewMatrix = buildOrthoProjModelMatrix(new Vector3d(0, 0, 0), new Vector3f(0, 0, 0), new Vector3d(windowScale, windowScale, windowScale));
                    hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                    getInventoryMesh().render();
                }

                glClear(GL_DEPTH_BUFFER_BIT);

                {
                    modelViewMatrix = buildOrthoProjModelMatrix(new Vector3d(-(windowScale / 3.75d), (windowScale / 2.8d), 0), getPlayerHudRotation(), new Vector3d((windowScale / 18d), (windowScale / 18d), (windowScale / 18d)));
                    hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                    getPlayerMesh().render();
                }

                glClear(GL_DEPTH_BUFFER_BIT);

                {
                    //render the actual inventory
                    for (int x = 1; x <= 9; x++) {
                        for (int y = -2; y > -5; y--) {
                            modelViewMatrix = buildOrthoProjModelMatrix(new Vector3d((x - 5) * (windowScale / 9.5d), (y + 0.3d) * (windowScale / 9.5d), 0), new Vector3f(0, 0, 0), new Vector3d(windowScale / 10.5f, windowScale / 10.5d, windowScale / 10.5d));
                            hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);

                            if (getInvSelection() != null && (x - 1) == getInvSelection()[0] && ((y * -1) - 1) == getInvSelection()[1]) {
                                getInventorySlotSelectedMesh().render();
                            } else {
                                getInventorySlotMesh().render();
                            }

                        }
                    }

                    //render the inventory hotbar (top row)
                    for (int x = 1; x <= 9; x++) {
                        modelViewMatrix = buildOrthoProjModelMatrix(new Vector3d((x - 5) * (windowScale / 9.5d), -0.5d * (windowScale / 9.5d), 0), new Vector3f(0, 0, 0), new Vector3d(windowScale / 10.5d, windowScale / 10.5d, windowScale / 10.5d));
                        hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);

                        if (getInvSelection() != null && (x - 1) == getInvSelection()[0] && 0 == getInvSelection()[1]) {
                            getInventorySlotSelectedMesh().render();
                        } else {
                            getInventorySlotMesh().render();
                        }
                    }
                }


                glClear(GL_DEPTH_BUFFER_BIT);


                boolean itemSelected = false;

                //render items in inventory
                for (int x = 1; x <= 9; x++) {
                    for (int y = -2; y > -5; y--) {
                        glClear(GL_DEPTH_BUFFER_BIT);
                        if (getItemInInventorySlot(x - 1, ((y * -1) - 1)) != null) {
                            if (getInvSelection() != null && (x - 1) == getInvSelection()[0] && ((y * -1) - 1) == getInvSelection()[1]) {
                                itemSelected = true;
                                modelViewMatrix = buildOrthoProjModelMatrix(new Vector3d((x - 5) * (windowScale / 9.5d), ((y + 0.3d) * (windowScale / 9.5d)) - (windowScale / 55d), 0), new Vector3f(45, 45 + itemRotation, 0), new Vector3d(windowScale / 8d, windowScale / 8d, windowScale / 8d));
                            } else {
                                modelViewMatrix = buildOrthoProjModelMatrix(new Vector3d((x - 5) * (windowScale / 9.5d), ((y + 0.3d) * (windowScale / 9.5d)) - (windowScale / 55d), 0), new Vector3f(45, 45, 0), new Vector3d(windowScale / 8d, windowScale / 8d, windowScale / 8d));
                            }
                            hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                            getItemInInventorySlot(x - 1, ((y * -1) - 1)).mesh.render();


                            if (getItemInInventorySlot(x - 1, ((y * -1) - 1)).stack > 1) {
                                //draw stack numbers
                                glClear(GL_DEPTH_BUFFER_BIT);
                                modelViewMatrix = buildOrthoProjModelMatrix(new Vector3d((x - 4.98d) * (windowScale / 9.5d), ((y + 0.28d) * (windowScale / 9.5d)) - (windowScale / 55d), 0), new Vector3f(0, 0, 0), new Vector3d(windowScale / 48, windowScale / 48, windowScale / 48));
                                hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                                workerMesh = createCustomHudText(Integer.toString(getItemInInventorySlot(x - 1, ((y * -1) - 1)).stack), 0f, 0f, 0f);
                                workerMesh.render();
                                workerMesh.cleanUp(false);

                                glClear(GL_DEPTH_BUFFER_BIT);
                                modelViewMatrix = buildOrthoProjModelMatrix(new Vector3d((x - 5) * (windowScale / 9.5d), ((y + 0.3d) * (windowScale / 9.5d)) - (windowScale / 55d), 0), new Vector3f(0, 0, 0), new Vector3d(windowScale / 48, windowScale / 48, windowScale / 48));
                                hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                                workerMesh = createCustomHudText(Integer.toString(getItemInInventorySlot(x - 1, ((y * -1) - 1)).stack), 1f, 1f, 1f);
                                workerMesh.render();
                                workerMesh.cleanUp(false);
                            }
                        }
                    }
                }

                //render items in inventory hotbar (upper part)

                for (int x = 1; x <= 9; x++) {
                    if (getItemInInventorySlot(x - 1, 0) != null) {
                        glClear(GL_DEPTH_BUFFER_BIT);
                        if (getInvSelection() != null && (x - 1) == getInvSelection()[0] && 0 == getInvSelection()[1]) {
                            itemSelected = true;
                            modelViewMatrix = buildOrthoProjModelMatrix(new Vector3d((x - 5) * (windowScale / 9.5d), (-0.5d * (windowScale / 9.5d)) - (windowScale / 55d), 0), new Vector3f(45, 45 + itemRotation, 0), new Vector3d(windowScale / 8d, windowScale / 8d, windowScale / 8d));
                        } else {
                            modelViewMatrix = buildOrthoProjModelMatrix(new Vector3d((x - 5) * (windowScale / 9.5d), (-0.5d * (windowScale / 9.5d)) - (windowScale / 55d), 0), new Vector3f(45, 45, 0), new Vector3d(windowScale / 8d, windowScale / 8d, windowScale / 8d));
                        }

                        hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                        getItemInInventorySlot(x - 1, 0).mesh.render();

                        glClear(GL_DEPTH_BUFFER_BIT);

                        if (getItemInInventorySlot(x - 1, 0).stack > 1) {
                            //render item stack numbers
                            modelViewMatrix = buildOrthoProjModelMatrix(new Vector3d((x - 4.98d) * (windowScale / 9.5d), ((-0.52d) * (windowScale / 9.5d)) - (windowScale / 55d), 0), new Vector3f(0, 0, 0), new Vector3d(windowScale / 48, windowScale / 48, windowScale / 48));
                            hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                            workerMesh = createCustomHudText(Integer.toString(getItemInInventorySlot(x - 1, 0).stack), 0f, 0f, 0f);
                            workerMesh.render();
                            workerMesh.cleanUp(false);

                            glClear(GL_DEPTH_BUFFER_BIT);
                            modelViewMatrix = buildOrthoProjModelMatrix(new Vector3d((x - 5) * (windowScale / 9.5d), ((-0.5d) * (windowScale / 9.5d)) - (windowScale / 55d), 0), new Vector3f(0, 0, 0), new Vector3d(windowScale / 48, windowScale / 48, windowScale / 48));
                            hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                            workerMesh = createCustomHudText(Integer.toString(getItemInInventorySlot(x - 1, 0).stack), 1f, 1f, 1f);
                            workerMesh.render();
                            workerMesh.cleanUp(false);
                        }
                    }
                }

                //this is used for an effect of
                //rotating the selected item in HUD
                if (!itemSelected) {
                    disableItemRotationInHud();
                } else {
                    enableItemRotationInHud();
                }
                itemRotation = getItemRotationInHud();


                //debug testing for rendered item
                {
                    if (getMouseInventory() != null) {
                        glClear(GL_DEPTH_BUFFER_BIT);
                        //need to create new object or the mouse position gets messed up
                        Vector2d mousePos = new Vector2d(getMousePos());

                        //work from the center
                        mousePos.x -= (getWindowSize().x / 2f);
                        mousePos.y -= (getWindowSize().y / 2f);
                        mousePos.y *= -1f;

                        modelViewMatrix = buildOrthoProjModelMatrix(new Vector3d((float) mousePos.x, (float) mousePos.y - (windowScale / 55d), 0), new Vector3f(45, 45, 0), new Vector3d(windowScale / 8d, windowScale / 8d, windowScale / 8d));
                        hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);

                        getMouseInventory().mesh.render();

                        glClear(GL_DEPTH_BUFFER_BIT);

                        //stack numbers
                        if(getMouseInventory().stack > 1) {
                            modelViewMatrix = buildOrthoProjModelMatrix(new Vector3d( mousePos.x + (windowScale / 400d),  mousePos.y - (windowScale / 55d) - (windowScale / 400d), 0), new Vector3f(0, 0, 0), new Vector3d(windowScale / 48, windowScale / 48, windowScale / 48));
                            hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                            workerMesh = createCustomHudText(Integer.toString(getMouseInventory().stack), 0f, 0f, 0f);
                            workerMesh.render();
                            workerMesh.cleanUp(false);

                            glClear(GL_DEPTH_BUFFER_BIT);
                            modelViewMatrix = buildOrthoProjModelMatrix(new Vector3d( mousePos.x,  mousePos.y - (windowScale / 55f), 0), new Vector3f(0, 0, 0), new Vector3d(windowScale / 48, windowScale / 48, windowScale / 48));
                            hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                            workerMesh = createCustomHudText(Integer.toString(getMouseInventory().stack), 1f, 1f, 1f);
                            workerMesh.render();
                            workerMesh.cleanUp(false);
                        }
                    }
                }
            } else {


                //healthbar
                {
                    byte[] healthArray = getHealthHudArray();
                    float[] healthJiggleArray = getHealthHudFloatArray();

                    for (byte i = 0; i < healthArray.length; i++) {

                        float jiggle = healthJiggleArray[i];

                        if (getPlayerHealth() > 6){
                            jiggle = 0f;
                        }

                        modelViewMatrix = buildOrthoProjModelMatrix(new Vector3d(-windowScale/2.105f + (i * (windowScale/19.5d)), (-windowSize.y / 2d) + (windowScale / 6.8d) + jiggle, 0), new Vector3f(0, 0, 0), new Vector3d((windowScale/20f), (windowScale/20f), (windowScale/20f)));
                        hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);

                        //save cpu render calls
                        if (healthArray[i] == 2) {
                            getHeartHudMesh().render();
                        } else if (healthArray[i] == 1){
                            getHeartShadowHudMesh().render();
                            glClear(GL_DEPTH_BUFFER_BIT);
                            getHalfHeartHudMesh().render();
                        } else {
                            getHeartShadowHudMesh().render();
                        }
                    }
                }

                glClear(GL_DEPTH_BUFFER_BIT);

                //hotbar
                {
                    modelViewMatrix = buildOrthoProjModelMatrix(new Vector3d(0,  (-windowSize.y / 2d) + (windowScale / 16.5d), 0), new Vector3f(0, 0, 0), new Vector3d((windowScale), (windowScale), (windowScale)));
                    hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                    getHotBarMesh().render();
                }

                glClear(GL_DEPTH_BUFFER_BIT);

                //selection bar (in the hotbar)
                {
                    modelViewMatrix = buildOrthoProjModelMatrix(new Vector3d((getPlayerInventorySelection() - 4) * (windowScale / 9.1d),  (-windowSize.y / 2f) + ((windowScale / 8.25f) / 2f), 0), new Vector3f(0, 0, 0), new Vector3d(windowScale / 8.25f, windowScale / 8.25f, windowScale / 8.25f));
                    hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                    getSelectionMesh().render();
                }

                //THESE GO LAST!
                
                //version info
                {
                    modelViewMatrix = buildOrthoProjModelMatrix(new Vector3d(((-windowSize.x / 2d) + (windowSize.x / 600d)), ((windowSize.y / 2.d) - (windowSize.y / 600d)), 0), new Vector3f(0, 0, 0), new Vector3d(windowScale / 30d, windowScale / 30d, windowScale / 30d));
                    hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                    getVersionInfoTextShadow().render();
                }

                glClear(GL_DEPTH_BUFFER_BIT);

                {
                    modelViewMatrix = buildOrthoProjModelMatrix(new Vector3d(-windowSize.x / 2d, (windowSize.y / 2.d), 0), new Vector3f(0, 0, 0), new Vector3d(windowScale / 30d, windowScale / 30d, windowScale / 30d));
                    hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                    getVersionInfoText().render();
                }


                if(getDebugInfo()) {

                    //x info
                    {
                        glClear(GL_DEPTH_BUFFER_BIT);

                        modelViewMatrix = buildOrthoProjModelMatrix(new Vector3d(((-windowSize.x / 2d) + (windowSize.x / 600d)), ((windowSize.y / 2.3d) - (windowSize.y / 600d)), 0), new Vector3f(0, 0, 0), new Vector3d(windowScale / 30d, windowScale / 30d, windowScale / 30d));
                        hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                        workerMesh = createCustomHudText("X:" + getPlayerPos().x, 0f, 0f, 0f);
                        workerMesh.render();
                        workerMesh.cleanUp(false);

                        glClear(GL_DEPTH_BUFFER_BIT);
                        modelViewMatrix = buildOrthoProjModelMatrix(new Vector3d((-windowSize.x / 2d), (windowSize.y / 2.3d), 0), new Vector3f(0, 0, 0), new Vector3d(windowScale / 30d, windowScale / 30d, windowScale / 30d));
                        hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                        workerMesh = createCustomHudText("X:" + getPlayerPos().x, 1f, 1f, 1f);
                        workerMesh.render();
                        workerMesh.cleanUp(false);
                        workerMesh = null;
                    }

                    //y info
                    {
                        glClear(GL_DEPTH_BUFFER_BIT);
                        modelViewMatrix = buildOrthoProjModelMatrix(new Vector3d(((-windowSize.x / 2d) + (windowSize.x / 600d)), ((windowSize.y / 2.6d) - (windowSize.y / 600d)), 0), new Vector3f(0, 0, 0), new Vector3d(windowScale / 30d, windowScale / 30d, windowScale / 30d));
                        hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                        workerMesh = createCustomHudText("Y:" + getPlayerPos().y, 0f, 0f, 0f);
                        workerMesh.render();
                        workerMesh.cleanUp(false);

                        glClear(GL_DEPTH_BUFFER_BIT);
                        modelViewMatrix = buildOrthoProjModelMatrix(new Vector3d((-windowSize.x / 2d), (windowSize.y / 2.6d), 0), new Vector3f(0, 0, 0), new Vector3d(windowScale / 30d, windowScale / 30d, windowScale / 30d));
                        hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                        workerMesh = createCustomHudText("Y:" + getPlayerPos().y, 1f, 1f, 1f);
                        workerMesh.render();
                        workerMesh.cleanUp(false);
                        workerMesh = null;
                    }

                    //z info
                    {
                        glClear(GL_DEPTH_BUFFER_BIT);
                        modelViewMatrix = buildOrthoProjModelMatrix(new Vector3d(((-windowSize.x / 2d) + (windowSize.x / 600d)), ((windowSize.y / 2.9d) - (windowSize.y / 600d)), 0), new Vector3f(0, 0, 0), new Vector3d(windowScale / 30d, windowScale / 30d, windowScale / 30d));
                        hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                        workerMesh = createCustomHudText("Z:" + getPlayerPos().z, 0f, 0f, 0f);
                        workerMesh.render();
                        workerMesh.cleanUp(false);

                        glClear(GL_DEPTH_BUFFER_BIT);
                        modelViewMatrix = buildOrthoProjModelMatrix(new Vector3d((-windowSize.x / 2d), (float) (windowSize.y / 2.9d), 0), new Vector3f(0, 0, 0), new Vector3d(windowScale / 30d, windowScale / 30d, windowScale / 30d));
                        hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                        workerMesh = createCustomHudText("Z:" + getPlayerPos().z, 1f, 1f, 1f);
                        workerMesh.render();
                        workerMesh.cleanUp(false);
                        workerMesh = null;
                    }

                    //render fps
                    {
                        glClear(GL_DEPTH_BUFFER_BIT);
                        modelViewMatrix = buildOrthoProjModelMatrix(new Vector3d(((-windowSize.x / 2d) + (windowSize.x / 600d)), ((windowSize.y / 3.3d) - (windowSize.y / 600d)), 0), new Vector3f(0, 0, 0), new Vector3d(windowScale / 30d, windowScale / 30d, windowScale / 30d));
                        hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                        workerMesh = createCustomHudText("FPS:" + getFpsCounted(), 0f, 0f, 0f);
                        workerMesh.render();
                        workerMesh.cleanUp(false);

                        glClear(GL_DEPTH_BUFFER_BIT);
                        modelViewMatrix = buildOrthoProjModelMatrix(new Vector3d((-windowSize.x / 2d), (windowSize.y / 3.3d), 0), new Vector3f(0, 0, 0), new Vector3d(windowScale / 30d, windowScale / 30d, windowScale / 30d));
                        hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                        workerMesh = createCustomHudText("FPS:" + getFpsCounted(), 1f, 1f, 1f);
                        workerMesh.render();
                        workerMesh.cleanUp(false);
                        workerMesh = null;
                    }
                }

                //render items in hotbar
                for (int x = 1; x <= 9; x++) {

                    if (getItemInInventorySlot(x - 1, 0) != null) {

                        glClear(GL_DEPTH_BUFFER_BIT);

                        modelViewMatrix = buildOrthoProjModelMatrix(new Vector3d(((x - 5d) * (windowScale / 9.1d)),  (-windowSize.y / 2d) + (windowScale / 24d), 0), new Vector3f(45, 45, 0), new Vector3d(windowScale / 8d, windowScale / 8d, windowScale / 8d));
                        hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                        getItemInInventorySlot(x - 1, 0).mesh.render();


                        glClear(GL_DEPTH_BUFFER_BIT);

//                        render hotbar counts if greater than 1

                        if (getItemInInventorySlot(x - 1, 0).stack > 1) {

                            modelViewMatrix = buildOrthoProjModelMatrix(new Vector3d(((x - 4.98d) * (windowScale / 9.1d)),  (-windowSize.y / 2d) + (windowScale / 26d), 0), new Vector3f(0, 0, 0), new Vector3d(windowScale / 48, windowScale / 48, windowScale / 48));
                            hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                            workerMesh = createCustomHudText(Integer.toString(getItemInInventorySlot(x - 1, 0).stack), 0f, 0f, 0f);
                            workerMesh.render();
                            workerMesh.cleanUp(false);

                            glClear(GL_DEPTH_BUFFER_BIT);
                            modelViewMatrix = buildOrthoProjModelMatrix(new Vector3d(((x - 5d) * (windowScale / 9.1d)),  (-windowSize.y / 2d) + (windowScale / 24d), 0), new Vector3f(0, 0, 0), new Vector3d(windowScale / 48, windowScale / 48, windowScale / 48));
                            hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                            workerMesh = createCustomHudText(Integer.toString(getItemInInventorySlot(x - 1, 0).stack), 1f, 1f, 1f);
                            workerMesh.render();
                            workerMesh.cleanUp(false);
                        }
                    }
                }


            }
        } else {
            {
                modelViewMatrix = buildOrthoProjModelMatrix(new Vector3d(0, 0, 0), new Vector3f(0, 0, 0), new Vector3d(windowSize.x, windowSize.y, windowScale));
                hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                getMenuBgMesh().render();
            }

            glClear(GL_DEPTH_BUFFER_BIT);

            {
                for (int y = 0; y > -3; y --){
                    modelViewMatrix = buildOrthoProjModelMatrix(new Vector3d(0, (y+1) * windowScale/3d, 0), new Vector3f(0, 0, 0), new Vector3d(windowScale/2d, windowScale/2d, windowScale/2d));
                    hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);


                    if (getPauseButtonSelection() == y * -1){
                        if (getIfClicking()){
                            getButtonPushedMesh().render();
                        } else {
                            getButtonSelectedMesh().render();
                        }
                    } else {
                        getButtonMesh().render();
                    }
                }
            }

            glClear(GL_DEPTH_BUFFER_BIT);
            {
                modelViewMatrix = buildOrthoProjModelMatrix(new Vector3d(-windowScale / 5.25d, (1) * windowScale / 2.8d, 0), new Vector3f(0, 0, 0), new Vector3d(windowScale / 20d, windowScale / 20d, windowScale / 20d));
                hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                getContinueMesh().render();
            }
            glClear(GL_DEPTH_BUFFER_BIT);

            {
                modelViewMatrix = buildOrthoProjModelMatrix(new Vector3d(-windowScale / 4.5d, windowScale / 50d, 0), new Vector3f(0, 0, 0), new Vector3d(windowScale / 20d, windowScale / 20d, windowScale / 20d));
                hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                getToggleVsyncMesh().render();
            }

            glClear(GL_DEPTH_BUFFER_BIT);

            {
                modelViewMatrix = buildOrthoProjModelMatrix(new Vector3d(-windowScale / 12d, -windowScale / 3.25d, 0), new Vector3f(0, 0, 0), new Vector3d(windowScale / 20d, windowScale / 20d, windowScale / 20d));
                hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                getQuitGameMesh().render();
            }
        }



        hudShaderProgram.unbind();

        workerMesh = null;
    }

    public static void cleanupRenderer(){
        if (shaderProgram != null){
            shaderProgram.cleanup();
        }

        if (hudShaderProgram != null){
            hudShaderProgram.cleanup();
        }
    }
}
