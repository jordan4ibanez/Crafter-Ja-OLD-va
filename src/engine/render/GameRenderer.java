package engine.render;

import engine.Utils;
import engine.graphics.Mesh;
import engine.graphics.ShaderProgram;
import engine.gui.GUIObject;
import game.crafting.InventoryObject;
import game.falling.FallingEntityObject;
import game.item.Item;
import game.mob.MobObject;
import game.particle.ParticleObject;
import org.joml.*;

import java.lang.Math;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.HashMap;

import static engine.FancyMath.getDistance;
import static engine.MouseInput.getMousePos;
import static engine.Window.*;
import static engine.debug.CheckRuntimeInfo.getRuntimeInfoText;
import static engine.graphics.Camera.*;
import static engine.graphics.Transformation.*;
import static engine.gui.GUI.*;
import static engine.gui.GUILogic.*;
import static engine.gui.TextHandling.createTextWithShadow;
import static engine.settings.Settings.*;
import static engine.time.TimeOfDay.getTimeOfDayLinear;
import static game.chat.Chat.getCurrentMessageMesh;
import static game.chat.Chat.getViewableChatMessages;
import static game.chunk.Chunk.*;
import static game.clouds.Cloud.*;
import static game.crafting.Inventory.*;
import static game.crafting.InventoryLogic.getPlayerHudRotation;
import static game.falling.FallingEntity.getFallingEntities;
import static game.item.ItemDefinition.getItemDefinition;
import static game.item.ItemDefinition.getItemMesh;
import static game.item.ItemEntity.getAllItems;
import static game.light.Light.getCurrentGlobalLightLevel;
import static game.mob.Mob.*;
import static game.particle.Particle.getAllParticles;
import static game.player.Player.*;
import static game.player.PlayerMesh.*;
import static game.player.WieldHand.getWieldHandAnimationPos;
import static game.player.WieldHand.getWieldHandAnimationRot;
import static game.tnt.TNTEntity.*;
import static org.lwjgl.opengl.GL44.*;
import static org.lwjgl.opengl.GL44C.GL_BLEND;
import static org.lwjgl.opengl.GL44C.glDisable;

//import static game.mob.Human.getHumanBodyOffsets;
//import static game.mob.Human.getHumanMeshes;

public class GameRenderer {

    private static final float FOV = (float) Math.toRadians(72.0f); //todo: make this a calculator method ala calculateFOV(float);

    private static final float Z_NEAR = 0.1f;

    private static float windowScale = 0f;

    private static final Vector2d windowSize = new Vector2d();

    private static ShaderProgram shaderProgram;
    private static ShaderProgram hudShaderProgram;
    private static ShaderProgram glassLikeShaderProgram;
    private static ShaderProgram entityShaderProgram;

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

    public static float getzNear(){
        return Z_NEAR;
    }

    public static ShaderProgram getShaderProgram(){
        return shaderProgram;
    }

    public static ShaderProgram getHudShaderProgram(){
        return hudShaderProgram;
    }

    public static float getWindowScale(){
        return windowScale;
    }

    public static void initRenderer() throws Exception{
        //normal shader program
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

        //ortholinear hud shader program
        hudShaderProgram = new ShaderProgram();
        hudShaderProgram.createVertexShader(Utils.loadResource("/resources/hud_vertex.vs"));
        hudShaderProgram.createFragmentShader(Utils.loadResource("/resources/hud_fragment.fs"));
        hudShaderProgram.link();

        //create uniforms for model view matrix
        hudShaderProgram.createUniform("modelViewMatrix");
        //create uniforms for texture sampler
        hudShaderProgram.createUniform("texture_sampler");


        //glassLike shader program
        glassLikeShaderProgram = new ShaderProgram();
        glassLikeShaderProgram.createVertexShader(Utils.loadResource("/resources/glasslike_vertex.vs"));
        glassLikeShaderProgram.createFragmentShader(Utils.loadResource("/resources/glasslike_fragment.fs"));
        glassLikeShaderProgram.link();

        //create uniforms for world and projection matrices
        glassLikeShaderProgram.createUniform("projectionMatrix");
        //create uniforms for model view matrix
        glassLikeShaderProgram.createUniform("modelViewMatrix");
        //create uniforms for texture sampler
        glassLikeShaderProgram.createUniform("texture_sampler");

        //glassLike shader program
        entityShaderProgram = new ShaderProgram();
        entityShaderProgram.createVertexShader(Utils.loadResource("/resources/entity_vertex.vs"));
        entityShaderProgram.createFragmentShader(Utils.loadResource("/resources/entity_fragment.fs"));
        entityShaderProgram.link();

        //create uniforms for world and projection matrices
        entityShaderProgram.createUniform("projectionMatrix");
        //create uniforms for model view matrix
        entityShaderProgram.createUniform("modelViewMatrix");
        //create uniforms for texture sampler
        entityShaderProgram.createUniform("texture_sampler");
        //create uniform for light value
        entityShaderProgram.createUniform("light");


        //setWindowClearColor(0.f,0.f,0.f,0.f);
        setWindowClearColor(0.53f,0.81f,0.92f,0.f);

        windowSize.x = getWindowWidth();
        windowSize.y = getWindowHeight();

        resetWindowScale();
    }

    public static void clearScreen(){
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }


    public static void rescaleWindow(){
        if (isWindowResized()){
            windowSize.x = getWindowWidth();
            windowSize.y = getWindowHeight();
            glViewport(0,0, getWindowWidth(), getWindowHeight());
            setWindowResized(false);
            resetWindowScale();
        }
    }

    private static final Matrix4d projectionMatrix = new Matrix4d();
    private static final Matrix4d viewMatrix = new Matrix4d();
    private static final Matrix4d modelViewMatrix = new Matrix4d();

    private static final Vector3d camPos = new Vector3d();

    //private static final Vector3d workerVec3D = new Vector3d();
    //private static final Vector3d workerVec3D2 = new Vector3d();
    //private static final Vector3f workerVec3F = new Vector3f();
    //private static final Vector3f workerVec3F2 = new Vector3f();

    //private static final Vector2d workerVec2D = new Vector2d();

    private static final HashMap<Double, Mesh[]> normalDrawTypeHash = new HashMap<>();
    private static final HashMap<Double, Mesh[]> liquidDrawTypeHash = new HashMap<>();
    private static final HashMap<Double, Mesh[]> allFaceDrawTypeHash = new HashMap<>();
    private static final HashMap<Double, Vector2i> chunkHashKeys = new HashMap<>();


    public static void renderGame(){
        processClearColorInterpolation();
        clearScreen();
        rescaleWindow();




        int renderDistance = getRenderDistance();

        //update projection matrix
        projectionMatrix.set(getProjectionMatrix(FOV + getRunningFOVAdder(), getWindowWidth(), getWindowHeight(), Z_NEAR, (renderDistance * 2) * 16f));
        //update the view matrix
        viewMatrix.set(getViewMatrix());

        camPos.set(getCameraPosition());

        //todo BEGIN chunk sorting ---------------------------------------------------------------------------------------------


        AbstractMap<Vector2i,Mesh[]> normalChunkMeshes = getNormalMeshes();
        AbstractMap<Vector2i,Mesh[]> liquidChunkMeshes = getLiquidMeshes();
        AbstractMap<Vector2i,Mesh[]> allFaceChunkMeshes = getAllFaceMeshes();


        int meshCount = 0;

        double flickerFixer = 0d;

        //get all distances
        for (Vector2i key : getChunkKeys()){
            double currentDistance = camPos.distance((key.x * 16d) + 8d, 0,(key.y * 16d) + 8d);

            if (normalDrawTypeHash.get(currentDistance) != null){
                currentDistance += flickerFixer;
                flickerFixer += 0.00000000001d;
            }

            if (getChunkDistanceFromPlayer(key.x, key.y) <= renderDistance) {
                normalDrawTypeHash.put(currentDistance, normalChunkMeshes.get(key));
                liquidDrawTypeHash.put(currentDistance, liquidChunkMeshes.get(key));
                allFaceDrawTypeHash.put(currentDistance, allFaceChunkMeshes.get(key));
                chunkHashKeys.put(currentDistance, key);
                meshCount++;
            }
        }

        double[]keySort = new double[meshCount];
        int index = 0;
        //sort all distances
        for (double thisKey : chunkHashKeys.keySet()){
            keySort[index] = thisKey;
            index++;
        }

        Arrays.sort(keySort);


        Mesh[][] normalDrawTypeArray = new Mesh[meshCount][8];
        Mesh[][] liquidDrawTypeArray = new Mesh[meshCount][8];
        Mesh[][] allFaceDrawTypeArray = new Mesh[meshCount][8];
        Vector2i[] chunkArrayKeys = new Vector2i[meshCount];

        int arrayIndex = 0;

        //render outwards in
        for (int i = meshCount - 1; i >= 0; i--){

            double key = keySort[i];

            //link
            normalDrawTypeArray[arrayIndex] = normalDrawTypeHash.get(key);
            liquidDrawTypeArray[arrayIndex] = liquidDrawTypeHash.get(key);
            allFaceDrawTypeArray[arrayIndex] = allFaceDrawTypeHash.get(key);
            chunkArrayKeys[arrayIndex] = chunkHashKeys.get(key);

            arrayIndex++;

            //remove
            normalDrawTypeHash.remove(key);
            liquidDrawTypeHash.remove(key);
            allFaceDrawTypeHash.remove(key);
            chunkHashKeys.remove(key);
        }



        for (double value : normalDrawTypeHash.keySet()){
            normalDrawTypeHash.replace(value, null);
        }

        for (double value : liquidDrawTypeHash.keySet()){
            liquidDrawTypeHash.replace(value, null);
        }
        for (double value : allFaceDrawTypeHash.keySet()){
            allFaceDrawTypeHash.replace(value, null);
        }

        for (double value : chunkHashKeys.keySet()){
            chunkHashKeys.replace(value, null);
        }

        normalDrawTypeHash.clear();
        liquidDrawTypeHash.clear();
        allFaceDrawTypeHash.clear();
        chunkHashKeys.clear();

        //todo END chunk sorting ---------------------------------------------------------------------------------------------


        //get fast or fancy
        boolean graphicsMode = getGraphicsMode();

        if (graphicsMode) {
            glassLikeShaderProgram.bind();
            glassLikeShaderProgram.setUniform("projectionMatrix", projectionMatrix);
            glassLikeShaderProgram.setUniform("texture_sampler", 0);
        } else {
            shaderProgram.bind();
            shaderProgram.setUniform("projectionMatrix", projectionMatrix);
            shaderProgram.setUniform("texture_sampler", 0);
        }


        //render the sun and moon
        //glDisable(GL_CULL_FACE);
        {

            double timeOfDayLinear = getTimeOfDayLinear();

            //daytime sky
            if (timeOfDayLinear <= 0.85 && timeOfDayLinear >= 0.15) {
                modelViewMatrix.set(updateSunMatrix());
                shaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                getSunMesh().render();

            }
            //nighttime sky
            if (timeOfDayLinear > 0.65 || timeOfDayLinear < 0.35){
                modelViewMatrix.set(updateMoonMatrix());
                shaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                getMoonMesh().render();
            }
        }
        //glEnable(GL_CULL_FACE);//debugging



        glClear(GL_DEPTH_BUFFER_BIT);

        if (graphicsMode) {
            glassLikeShaderProgram.unbind();
        } else {
            shaderProgram.unbind();
        }



        entityShaderProgram.bind();
        entityShaderProgram.setUniform("projectionMatrix", projectionMatrix);
        entityShaderProgram.setUniform("texture_sampler", 0);

        //debug render cloud
        {
            boolean[][] cloudData = getCloudData();
            float cloudScale = getCloudScale();
            Vector2i cloudPos = getCloudPos();
            float cloudScroll = getCloudScroll();
            if (graphicsMode) {
                for (byte x = 0; x < 16; x++) {
                    for (byte z = 0; z < 16; z++) {
                        if (cloudData[x][z]) {
                            modelViewMatrix.set(updateModelViewMatrix(workerVec3D.set((x * cloudScale) + ((cloudPos.x - 8) * 16d), 130, (z * cloudScale) + ((cloudPos.y - 8) * 16d) + cloudScroll), workerVec3F.set(0, 0, 0)));
                            entityShaderProgram.setLightUniform("light", getCurrentGlobalLightLevel());
                            entityShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                            //getCloud2DMesh().render();
                            getCloud3DMesh().render();
                        }
                    }
                }
            } else {
                for (byte x = 0; x < 16; x++) {
                    for (byte z = 0; z < 16; z++) {
                        if (cloudData[x][z]) {
                            modelViewMatrix.set(updateModelViewMatrix(workerVec3D.set((x * cloudScale) + ((cloudPos.x - 8) * 16d), 130, (z * cloudScale) + ((cloudPos.y - 8) * 16d) + cloudScroll), workerVec3F.set(0, 0, 0)));
                            entityShaderProgram.setLightUniform("light", getCurrentGlobalLightLevel());
                            entityShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                            getCloud2DMesh().render();
                        }
                    }
                }
            }
        }

        entityShaderProgram.unbind();

        if (graphicsMode) {
            glassLikeShaderProgram.bind();
        } else {
            shaderProgram.bind();
        }

        glDisable(GL_BLEND);

        //render normal chunk meshes
        for (int i = 0; i < arrayIndex; i++) {

            Mesh[] thisChunk = normalDrawTypeArray[i];

            if (thisChunk == null) {
                continue;
            }

            Vector2i thisPos = chunkArrayKeys[i];

            //normal
            for (Mesh thisMesh : thisChunk) {
                if (thisMesh != null) {
                    modelViewMatrix.set(updateModelViewMatrix(workerVec3D.set(thisPos.x * 16d, 0, thisPos.y * 16d), workerVec3F.set(0, 0, 0)));
                    if (graphicsMode) {
                        glassLikeShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                    } else {
                        shaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                    }
                    thisMesh.render();
                }
            }
        }


        //render allFaces chunk meshes
        for (int i = 0; i < arrayIndex; i++) {

            Mesh[] thisChunk = allFaceDrawTypeArray[i];

            if (thisChunk == null) {
                continue;
            }

            Vector2i thisPos = chunkArrayKeys[i];

            //allFaces
            for (Mesh thisMesh : thisChunk) {
                if (thisMesh != null) {
                    modelViewMatrix.set(updateModelViewMatrix(workerVec3D.set(thisPos.x * 16d, 0, thisPos.y * 16d), workerVec3F.set(0, 0, 0)));
                    if (graphicsMode) {
                        glassLikeShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                    } else {
                        shaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                    }
                    thisMesh.render();
                }
            }
        }

        if (graphicsMode) {
            glassLikeShaderProgram.unbind();
        } else {
            shaderProgram.unbind();
        }


        //begin game entity rendering
        entityShaderProgram.bind();

        //        render each item entity
        for (Object thisObject : getAllItems()){
            Item thisItem = (Item) thisObject;
            modelViewMatrix.set(updateModelViewMatrix(workerVec3D.set(thisItem.pos).add(0,thisItem.hover,0), thisItem.rotation));
            entityShaderProgram.setLightUniform("light", thisItem.light);
            entityShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
            getItemMesh(thisItem.name).render();
        }


        //render each TNT entity
        Mesh tntMesh = getTNTMesh();
        for (int i = 0; i < getTotalTNT(); i++){
            if (!tntExists(i)){
                continue;
            }
            entityShaderProgram.setLightUniform("light", 15); //todo make this work
            modelViewMatrix.set(getTNTModelViewMatrix(i));
            entityShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
            tntMesh.render();
        }

        //render falling entities
        for (FallingEntityObject thisObject : getFallingEntities()){
            entityShaderProgram.setLightUniform("light", 15); //todo make this work
            modelViewMatrix.set(getGenericMatrixWithPosRotationScale(thisObject.pos, workerVec3F.set(0,0,0), workerVec3D.set(2.5d,2.5d,2.5d)));
            entityShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
            thisObject.mesh.render();
        }


        //render mobs
        for (MobObject thisMob : getAllMobs()){
            if (thisMob == null){
                continue;
            }
            int offsetIndex = 0;

            boolean backFaceCulling = getMobBackFaceCulling(thisMob.ID);

            if (!backFaceCulling){
                glDisable(GL_CULL_FACE);
            }

            entityShaderProgram.setLightUniform("light", thisMob.light + thisMob.hurtAdder); //hurt adder adds 15 to the value so it turns red

            for (Mesh thisMesh : getMobMesh(thisMob.ID)) {
                modelViewMatrix.set(getMobMatrix(thisMob.pos, thisMob.bodyOffsets[offsetIndex], workerVec3F.set(0, thisMob.smoothRotation, thisMob.deathRotation), workerVec3F2.set(thisMob.bodyRotations[offsetIndex]), workerVec3D2.set(1f, 1f, 1f)));
                entityShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                thisMesh.render();
                offsetIndex++;
            }

            if (!backFaceCulling){
                glEnable(GL_CULL_FACE);
            }
        }

        //render other players
        /*
        for (Object thisObject : getOtherPlayers()){

            entityShaderProgram.setLightUniform("light", 15); //todo make this work

            PlayerObject thisPlayer = (PlayerObject) thisObject;

            if (thisPlayer == null){
                continue;
            }
            int offsetIndex = 0;

            Mesh[] playerMeshes = getHumanMeshes();
            Vector3f[] playerBodyOffsets = getHumanBodyOffsets();
            Vector3f[] playerBodyRotation = getPlayerBodyRotations();



            for (Mesh thisMesh : playerMeshes) {
                if (offsetIndex == 0) {
                    modelViewMatrix = getMobMatrix(thisPlayer.pos, playerBodyOffsets[offsetIndex], workerVec3F.set(0, thisPlayer.camRot.y, 0), workerVec3F2.set(thisPlayer.camRot.x + playerBodyRotation[offsetIndex].x, playerBodyRotation[offsetIndex].y, playerBodyRotation[offsetIndex].z), workerVec3D2.set(1f, 1f, 1f), viewMatrix);
                } else {
                    modelViewMatrix = getMobMatrix(thisPlayer.pos, playerBodyOffsets[offsetIndex], workerVec3F.set(0, thisPlayer.camRot.y, 0), workerVec3F2.set(playerBodyRotation[offsetIndex]), workerVec3D2.set(1f, 1f, 1f), viewMatrix);
                }
                entityShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                thisMesh.render();
                offsetIndex++;
            }

            //finally render their name
            //this is a temporary hack to see what other people are playing
            modelViewMatrix = updateTextIn3DSpaceViewMatrix(workerVec3D.set(thisPlayer.pos).add(0,2.05d,0), workerVec3F.set(getCameraRotation()), workerVec3D2.set(0.25d,0.25d,0.25d), viewMatrix);

            entityShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
            workerMesh = createTextCentered(thisPlayer.name, 1f, 1f, 1f);
            workerMesh.render();
            workerMesh.cleanUp(false);
        }

        */


        //render player in third person mode
        if (getCameraPerspective() > 0){
            Mesh[] playerMeshes = getPlayerMeshes();
            Vector3f[] playerBodyOffsets = getHumanBodyOffsets();
            Vector3f[] playerBodyRotation = getPlayerBodyRotations();
            Vector3d pos = getPlayerPos();

            entityShaderProgram.setLightUniform("light", 15); //todo make this work

            int offsetIndex = 0;
            for (Mesh thisMesh : playerMeshes) {
                float headRot = 0; //pitch
                if (getCameraPerspective() == 1) {
                    if (offsetIndex == 0){
                        headRot = getCameraRotation().x;
                    }
                    modelViewMatrix.set(getMobMatrix(pos, playerBodyOffsets[offsetIndex], workerVec3F.set(0, getCameraRotation().y, 0), workerVec3F2.set(headRot + playerBodyRotation[offsetIndex].x,playerBodyRotation[offsetIndex].y,playerBodyRotation[offsetIndex].z), workerVec3D2.set(1f, 1f, 1f)));
                } else {
                    if (offsetIndex == 0){
                        headRot = getCameraRotation().x * -1f;
                    }
                    modelViewMatrix.set(getMobMatrix(pos, playerBodyOffsets[offsetIndex], workerVec3F.set(0, getCameraRotation().y + 180f, 0), workerVec3F2.set(headRot + playerBodyRotation[offsetIndex].x,playerBodyRotation[offsetIndex].y,playerBodyRotation[offsetIndex].z), workerVec3D2.set(1f, 1f, 1f)));
                }
                entityShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                thisMesh.render();
                offsetIndex++;
            }

            //finally render their name
            //this is a temporary hack to see what other people are playing
            //modelViewMatrix = updateTextIn3DSpaceViewMatrix(workerVec3D.set(pos).add(0,2.05d,0), workerVec3F.set(getCameraRotation()), workerVec3D2.set(0.25d,0.25d,0.25d), viewMatrix);

            //entityShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
            //workerMesh = createTextCentered(getPlayerName(), 1f, 1f, 1f);
            //workerMesh.render();
            //workerMesh.cleanUp(false);
        }

        //render particles
        for (Object loadedObject : getAllParticles()){

            ParticleObject thisParticle = (ParticleObject) loadedObject;

            entityShaderProgram.setLightUniform("light", thisParticle.light); //todo make this work

            Mesh thisMesh = thisParticle.mesh;

            modelViewMatrix.set(updateParticleViewMatrix(thisParticle.pos, workerVec3F.set(getCameraRotation())));
            entityShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
            thisMesh.render();
        }


        //render world selection mesh
        if (!getPlayerWorldSelectionPos().equals(0,-555,0)){

            entityShaderProgram.setLightUniform("light", 15); //todo make this work

            workerVec3D.set(getPlayerWorldSelectionPos());
            modelViewMatrix.set(updateModelViewMatrix(workerVec3D, workerVec3F.set(0,0,0)));
            entityShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
            getWorldSelectionMesh().render();

            if (getDiggingFrame() >= 0) {
                modelViewMatrix.set(updateModelViewMatrix(workerVec3D, workerVec3F.set(0,0,0)));
                entityShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                getMiningCrackMesh(getDiggingFrame()).render();
            }
        }

        entityShaderProgram.unbind();


        if (graphicsMode) {
            glEnable(GL_BLEND);
        }

        //do standard blending
        shaderProgram.bind();
        shaderProgram.setUniform("projectionMatrix", projectionMatrix);
        shaderProgram.setUniform("texture_sampler", 0);


        //render liquid chunk meshes
        if (graphicsMode) {
            glDisable(GL_CULL_FACE);
        }

        for (int i = 0; i < arrayIndex; i++) {

            Mesh[] thisChunk = liquidDrawTypeArray[i];

            if (thisChunk == null) {
                continue;
            }

            Vector2i thisPos = chunkArrayKeys[i];

            //liquid

            for (Mesh thisMesh : thisChunk) {
                if (thisMesh != null) {
                    modelViewMatrix.set(updateModelViewMatrix(workerVec3D.set(thisPos.x * 16d, 0, thisPos.y * 16d), workerVec3F.set(0, 0, 0)));
                    shaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                    thisMesh.render();
                }
            }
        }

        Arrays.fill(normalDrawTypeArray, null);
        Arrays.fill(liquidDrawTypeArray, null);
        Arrays.fill(allFaceDrawTypeArray, null);
        Arrays.fill(chunkArrayKeys, null);

        if (graphicsMode) {
            glEnable(GL_CULL_FACE);
        }

        //finished with standard shader
        shaderProgram.unbind();

        entityShaderProgram.bind();

        //BEGIN HUD (3d parts) - just wield hand for now

        glClear(GL_DEPTH_BUFFER_BIT);

        projectionMatrix.set(getProjectionMatrix(FOV, getWindowWidth(), getWindowHeight(), Z_NEAR, 100));


        //draw wield hand or item
        if (getCameraPerspective() == 0) {

            entityShaderProgram.setUniform("projectionMatrix", projectionMatrix);
            entityShaderProgram.setLightUniform("light", getPlayerLightLevel());

            //wield hand
            if (getItemInInventorySlot(getPlayerInventorySelection(),0) == null){
                modelViewMatrix.set(getWieldHandMatrix(getCameraPosition(),getWieldHandAnimationPos(),getCameraRotation(), getWieldHandAnimationRot(), workerVec3D.set(0.35d), workerVec3D2.set(0.05d)));
                entityShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                getWieldHandMesh().render();
            //block/item
            } else if (getWieldInventory() != null){
                modelViewMatrix.set(getWieldHandMatrix(getCameraPosition(),getWieldHandAnimationPos(),getCameraRotation(), getWieldHandAnimationRot(), workerVec3D.set(1d, 1d, 1d), workerVec3D2.set(0.05d)));
                entityShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                getItemMesh(getWieldInventory().name).render();
            }

            entityShaderProgram.unbind();
        }

        //finished with 3d


        glEnable(GL_BLEND);

        //BEGIN HUD 2D
        glClear(GL_DEPTH_BUFFER_BIT);

        //TODO: BEGIN HUD SHADER PROGRAM!
        hudShaderProgram.bind();
        hudShaderProgram.setUniform("texture_sampler", 0);
        updateOrthoProjectionMatrix(); // needed to get current screen size

        //render water effect
        if (isCameraSubmerged()) {
            modelViewMatrix.set(updateOrthoModelMatrix(workerVec3D.set(0,0,0),workerVec3F.set(0,0,0), workerVec3D2.set(windowScale * 2,windowScale,windowScale)));
            hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
            getGlobalWaterEffectMesh().render();
        }

        glClear(GL_DEPTH_BUFFER_BIT);

        //render inverted crosshair
        if (getCameraPerspective() == 0){
            glBlendFunc(GL_ONE_MINUS_DST_COLOR, GL_ONE_MINUS_SRC_COLOR);
            modelViewMatrix.set(updateOrthoModelMatrix(workerVec3D.set(0,0,0),workerVec3F.set(0,0,0), workerVec3D2.set(windowScale/20f,windowScale/20f,windowScale/20f)));
            hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
            getCrossHairMesh().render();
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        }

        glClear(GL_DEPTH_BUFFER_BIT);


        if (!isPaused()) {

            if (isPlayerInventoryOpen()) {

                //inventory backdrop
                {
                    modelViewMatrix.set(updateOrthoModelMatrix(workerVec3D.set(0, 0, 0), workerVec3F.set(0, 0, 0), workerVec3D2.set(windowScale, windowScale, windowScale)));
                    hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                    getInventoryBackdropMesh().render();
                }

                glClear(GL_DEPTH_BUFFER_BIT);

                //player inside box
                {
                    modelViewMatrix.set(updateOrthoModelMatrix(workerVec3D.set(-(windowScale / 3.75d), (windowScale / 2.8d), 0), getPlayerHudRotation(), workerVec3D2.set((windowScale / 18d), (windowScale / 18d), (windowScale / 18d))));
                    hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                    getPlayerMesh().render();
                }

                glClear(GL_DEPTH_BUFFER_BIT);

                //inventory foreground
                {
                    modelViewMatrix.set(updateOrthoModelMatrix(workerVec3D.set(0, 0, 0), workerVec3F.set(0, 0, 0), workerVec3D2.set(windowScale, windowScale, windowScale)));
                    hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                    getInventoryMesh().render();
                }


                glClear(GL_DEPTH_BUFFER_BIT);

                renderInventoryGUI(getMainInventory());

                if (isAtCraftingBench()){
                    renderInventoryGUI(getBigCraftInventory());
                } else {
                    renderInventoryGUI(getSmallCraftInventory());
                }


                renderInventoryGUI(getOutputInventory());
                renderInventoryGUI(getArmorInventory());



                //render mouse item
                if (getMouseInventory() != null) {
                    glClear(GL_DEPTH_BUFFER_BIT);
                    //need to create new object or the mouse position gets messed up
                    workerVec2D.set(getMousePos());

                    //work from the center
                    workerVec2D.x -= (getWindowSize().x / 2f);
                    workerVec2D.y -= (getWindowSize().y / 2f);
                    workerVec2D.y *= -1f;
                    if (getItemDefinition(getMouseInventory().name).isItem) {
                        modelViewMatrix.set(updateOrthoModelMatrix(workerVec3D.set((float) workerVec2D.x, (float) workerVec2D.y - (windowScale / 27d), 0), workerVec3F.set(0, 0, 0), workerVec3D2.set(windowScale / 5d, windowScale / 5d, windowScale / 5d)));
                    } else {
                        modelViewMatrix.set(updateOrthoModelMatrix(workerVec3D.set((float) workerVec2D.x, (float) workerVec2D.y - (windowScale / 55d), 0), workerVec3F.set(45, 45, 0), workerVec3D2.set(windowScale / 8d, windowScale / 8d, windowScale / 8d)));
                    }

                    hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);

                    getItemMesh(getMouseInventory().name).render();

                    glClear(GL_DEPTH_BUFFER_BIT);

                    //stack numbers
                    if(getMouseInventory().stack > 1) {
                        glClear(GL_DEPTH_BUFFER_BIT);
                        modelViewMatrix.set(updateOrthoModelMatrix(workerVec3D.set( workerVec2D.x + (windowScale/47d),  workerVec2D.y - (windowScale / 35f), 0), workerVec3F.set(0, 0, 0), workerVec3D2.set(windowScale / 48, windowScale / 48, windowScale / 48)));
                        hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                        //workerMesh = createTextCenteredWithShadow(Integer.toString(getMouseInventory().stack), 1f, 1f, 1f);
                        //workerMesh.render();
                        //workerMesh.cleanUp(false);
                    }
                }
            } else {

                //health bar
                {
                    byte[] healthArray = getHealthHudArray();
                    float[] healthJiggleArray = getHealthHudFloatArray();

                    for (byte i = 0; i < healthArray.length; i++) {

                        float jiggle = healthJiggleArray[i];

                        if (getPlayerHealth() > 6) {
                            jiggle = 0f;
                        }

                        modelViewMatrix.set(updateOrthoModelMatrix(workerVec3D.set(-windowScale / 2.105f + (i * (windowScale / 19.5d)), (-windowSize.y / 2d) + (windowScale / 6.8d) + jiggle, 0), workerVec3F.set(0, 0, 0), workerVec3D2.set((windowScale / 20f), (windowScale / 20f), (windowScale / 20f))));
                        hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);

                        //save cpu render calls
                        if (healthArray[i] == 2) {
                            getHeartHudMesh().render();
                        } else if (healthArray[i] == 1) {
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
                    modelViewMatrix.set(updateOrthoModelMatrix(workerVec3D.set(0,  (-windowSize.y / 2d) + (windowScale / 16.5d), 0), workerVec3F.set(0, 0, 0), workerVec3D2.set((windowScale), (windowScale), (windowScale))));
                    hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                    getHotBarMesh().render();
                }

                glClear(GL_DEPTH_BUFFER_BIT);

                //selection bar (in the hotbar)
                {
                    modelViewMatrix.set(updateOrthoModelMatrix(workerVec3D.set((getPlayerInventorySelection() - 4) * (windowScale / 9.1d),  (-windowSize.y / 2f) + ((windowScale / 8.25f) / 2f), 0), workerVec3F.set(0, 0, 0), workerVec3D2.set(windowScale / 8.25f, windowScale / 8.25f, windowScale / 8.25f)));
                    hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                    getHotBarSelectionMesh().render();
                }

                //THESE GO LAST!

                glClear(GL_DEPTH_BUFFER_BIT);

                //version info
                {
                    modelViewMatrix.set(updateOrthoModelMatrix(workerVec3D.set(-windowSize.x / 2d, (windowSize.y / 2.1d), 0), workerVec3F.set(0, 0, 0), workerVec3D2.set(windowScale / 30d, windowScale / 30d, windowScale / 30d)));
                    hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                    getVersionInfoText().render();
                }

                if(getDebugInfo()) {

                    //x info
                    {

                        glClear(GL_DEPTH_BUFFER_BIT);
                        modelViewMatrix.set(updateOrthoModelMatrix(workerVec3D.set((-windowSize.x / 2d), (windowSize.y / 2.3d), 0), workerVec3F.set(0, 0, 0), workerVec3D2.set(windowScale / 30d, windowScale / 30d, windowScale / 30d)));
                        hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                        Mesh workerMesh = createTextWithShadow("X:" + getPlayerPos().x, 1f, 1f, 1f);
                        workerMesh.render();
                        workerMesh.cleanUp(false);
                    }

                    //y info
                    {

                        glClear(GL_DEPTH_BUFFER_BIT);
                        modelViewMatrix.set(updateOrthoModelMatrix(workerVec3D.set((-windowSize.x / 2d), (windowSize.y / 2.6d), 0), workerVec3F.set(0, 0, 0), workerVec3D2.set(windowScale / 30d, windowScale / 30d, windowScale / 30d)));
                        hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                        Mesh workerMesh = createTextWithShadow("Y:" + getPlayerPos().y, 1f, 1f, 1f);
                        workerMesh.render();
                        workerMesh.cleanUp(false);
                    }

                    //z info
                    {
                        glClear(GL_DEPTH_BUFFER_BIT);
                        modelViewMatrix.set(updateOrthoModelMatrix(workerVec3D.set((-windowSize.x / 2d), (float) (windowSize.y / 3d), 0), workerVec3F.set(0, 0, 0), workerVec3D2.set(windowScale / 30d, windowScale / 30d, windowScale / 30d)));
                        hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                        Mesh workerMesh = createTextWithShadow("Z:" + getPlayerPos().z, 1f, 1f, 1f);
                        workerMesh.render();
                        workerMesh.cleanUp(false);
                    }


                    Mesh[] runtimeInfo = getRuntimeInfoText();
                    for (int i = 0; i < runtimeInfo.length; i++){
                        if (runtimeInfo[i] != null){
                            glClear(GL_DEPTH_BUFFER_BIT);
                            modelViewMatrix.set(updateOrthoModelMatrix(workerVec3D.set((-windowSize.x / 2d), (float) (windowSize.y / 3d) + ((-i - 1) * (windowSize.y/20d)), 0), workerVec3F.set(0, 0, 0), workerVec3D2.set(windowScale / 30d, windowScale / 30d, windowScale / 30d)));
                            hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                            runtimeInfo[i].render();
                        }
                    }


                    //render fps
                    {
                        glClear(GL_DEPTH_BUFFER_BIT);
                        modelViewMatrix.set(updateOrthoModelMatrix(workerVec3D.set((-windowSize.x / 2d), (windowSize.y / 3d + (-7 * (windowSize.y/20d))), 0), workerVec3F.set(0, 0, 0), workerVec3D2.set(windowScale / 30d, windowScale / 30d, windowScale / 30d)));
                        hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                        getFPSMesh().render();
                    }
                } else {
                    //only show FPS
                    {
                        glClear(GL_DEPTH_BUFFER_BIT);
                        modelViewMatrix.set(updateOrthoModelMatrix(workerVec3D.set((-windowSize.x / 2d), (windowSize.y / 2.3d), 0), workerVec3F.set(0, 0, 0), workerVec3D2.set(windowScale / 30d, windowScale / 30d, windowScale / 30d)));
                        hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                        getFPSMesh().render();
                    }
                }

                //render items in hotbar
                for (byte x = 1; x <= 9; x++) {

                    Item thisItem = getItemInInventorySlot(x - 1, 0);
                    if (thisItem != null) {

                        glClear(GL_DEPTH_BUFFER_BIT);

                        if (getItemDefinition(thisItem.name).isItem) {
                            modelViewMatrix.set(updateOrthoModelMatrix(workerVec3D.set(((x - 5d) * (windowScale / 9.1d)), (-windowSize.y / 2d) + (windowScale / 48d), 0), workerVec3F.set(0, 0, 0), workerVec3D2.set(windowScale / 5d, windowScale / 5d, windowScale / 5d)));
                        } else {
                            modelViewMatrix.set(updateOrthoModelMatrix(workerVec3D.set(((x - 5d) * (windowScale / 9.1d)), (-windowSize.y / 2d) + (windowScale / 24d), 0), workerVec3F.set(45, 45, 0), workerVec3D2.set(windowScale / 8.01d, windowScale / 8.01d, windowScale / 8.01d)));
                        }
                        hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                        getItemMesh(getItemInInventorySlot(x - 1, 0).name).render();


                        glClear(GL_DEPTH_BUFFER_BIT);

                        //render hotbar counts if greater than 1

                        if (getItemInInventorySlot(x - 1, 0).stack > 1) {
                            modelViewMatrix.set(updateOrthoModelMatrix(workerVec3D.set(((x - 4.8d) * (windowScale / 9.1d)),  (-windowSize.y / 2d) + (windowScale / 32d), 0), workerVec3F.set(0, 0, 0), workerVec3D2.set(windowScale / 48, windowScale / 48, windowScale / 48)));
                            hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                            //workerMesh = createTextCenteredWithShadow(Integer.toString(getItemInInventorySlot(x - 1, 0).stack), 1f, 1f, 1f);
                            //workerMesh.render();
                            //workerMesh.cleanUp(false);
                        }
                    }
                }


            }
        } else {
            //render inventory base
            {
                modelViewMatrix.set(updateOrthoModelMatrix(workerVec3D.set(0, 0, 0), workerVec3F.set(0, 0, 0), workerVec3D2.set(windowSize.x, windowSize.y, windowScale)));
                hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                getMenuBgMesh().render();
            }

            glClear(GL_DEPTH_BUFFER_BIT);

            renderGameGUI();
        }

        //render chat bar
        if (isChatOpen()){

            //render background
            glClear(GL_DEPTH_BUFFER_BIT);
            modelViewMatrix.set(updateOrthoModelMatrix(workerVec3D.set((-windowSize.x / 2d), (-windowSize.y / 2.9d), 0), workerVec3F.set(0, 0, 0), workerVec3D2.set(windowSize.x / 1.5d, windowScale / 15d, windowScale / 5d)));
            hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
            getChatBoxMesh().render();

            //render typing text
            glClear(GL_DEPTH_BUFFER_BIT);
            modelViewMatrix.set(updateOrthoModelMatrix(workerVec3D.set((-windowSize.x / 2d), (-windowSize.y / 2.9d), 0), workerVec3F.set(0, 0, 0), workerVec3D2.set(windowScale / 30d, windowScale / 30d, windowScale / 30d)));
            hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
            getCurrentMessageMesh().render();
        }

        //render chat messages
        {
            byte i = 1;
            for (Mesh mesh : getViewableChatMessages()){
                if (mesh != null){
                    //render background
                    glClear(GL_DEPTH_BUFFER_BIT);
                    modelViewMatrix.set(updateOrthoModelMatrix(workerVec3D.set((-windowSize.x / 2d), (-windowSize.y / 2.9d) + ((windowScale/15d) * i), 0), workerVec3F.set(0, 0, 0), workerVec3D2.set(windowSize.x / 1.5d, windowScale / 15d, windowScale / 5d)));
                    hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                    getChatBoxMesh().render();

                    //render chat mesh
                    glClear(GL_DEPTH_BUFFER_BIT);
                    modelViewMatrix.set(updateOrthoModelMatrix(workerVec3D.set((-windowSize.x / 2d), (-windowSize.y / 2.9d)+ ((windowScale/15d) * i), 0), workerVec3F.set(0, 0, 0), workerVec3D2.set(windowScale / 30d, windowScale / 30d, windowScale / 30d)));
                    hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                    mesh.render();
                    i++;
                }
            }
        }

        hudShaderProgram.unbind();
    }

    private static void renderInventoryGUI(InventoryObject inventory){

        Vector2d startingPoint = inventory.getPosition();

        //this is the size of the actual slots
        //it also makes the default spacing of (0)
        //they bunch up right next to each other with 0
        double scale = windowScale/10.5d;
        double blockScale = windowScale / 8d;
        double itemScale = windowScale / 5d;
        double textScale = windowScale / 48;

        //this is the spacing between the slots
        double spacing = windowScale / 75d;

        workerVec2D.set((double)inventory.getSize().x/2d,(double)inventory.getSize().y/2d);

        double yProgram;
        if (inventory.isMainInventory()) {
            for (byte x = 0; x < inventory.getSize().x; x++) {
                for (byte y = 0; y < inventory.getSize().y; y++) {

                    //this is a quick and dirty hack to implement
                    //the space between the hotbar and rest of inventory
                    //on the main inventory
                    if (y == 0){
                        yProgram = 0.2d;
                    } else {
                        yProgram = 0;
                    }

                    //background of the slot
                    glClear(GL_DEPTH_BUFFER_BIT);

                    modelViewMatrix.set(updateOrthoModelMatrix(workerVec3D.set(((double) x + 0.5d - workerVec2D.x + startingPoint.x) * (scale + spacing), ((y * -1d) - 0.5d + startingPoint.y + workerVec2D.y + yProgram) * (scale + spacing), 0), workerVec3F.set(0, 0, 0), workerVec3D2.set(scale, scale, scale)));
                    hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);

                    if (inventory.getSelection().x == x && inventory.getSelection().y == y){
                        getInventorySlotSelectedMesh().render();
                    } else {
                        getInventorySlotMesh().render();
                    }

                    Item thisItem = inventory.get(x,y);

                    //only attempt if an actual item and not empty slot
                    if (thisItem != null) {

                        //render item
                        glClear(GL_DEPTH_BUFFER_BIT);
                        if (getItemDefinition(thisItem.name).isItem) {
                            modelViewMatrix.set(updateOrthoModelMatrix(workerVec3D.set(((double) x + 0.5d - workerVec2D.x + startingPoint.x) * (scale + spacing), ((y * -1d) - 0.5d + startingPoint.y + workerVec2D.y + yProgram) * (scale + spacing) - (blockScale / 3.25d), 0), workerVec3F.set(0, 0, 0), workerVec3D2.set(itemScale, itemScale, itemScale)));
                        } else {
                            modelViewMatrix.set(updateOrthoModelMatrix(workerVec3D.set(((double) x + 0.5d - workerVec2D.x + startingPoint.x) * (scale + spacing), ((y * -1d) - 0.5d + startingPoint.y + workerVec2D.y + yProgram) * (scale + spacing) - (blockScale / 7d), 0), workerVec3F.set(45, 45, 0), workerVec3D2.set(blockScale, blockScale, blockScale)));
                        }
                        hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                        getItemMesh(thisItem.name).render();

                        //render item stack number
                        if (thisItem.stack > 1) {
                            glClear(GL_DEPTH_BUFFER_BIT);
                            modelViewMatrix.set(updateOrthoModelMatrix(workerVec3D.set(((double) x + 0.7d - workerVec2D.x + startingPoint.x) * (scale + spacing), ((y * -1d) - 0.6d + startingPoint.y + workerVec2D.y + yProgram) * (scale + spacing) - (blockScale / 7d), 0), workerVec3F.set(0, 0, 0), workerVec3D2.set(textScale, textScale, textScale)));
                            hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                            //Mesh itemStackLabel = createTextCenteredWithShadow(Integer.toString(thisItem.stack), 1, 1, 1);
                            //itemStackLabel.render();
                            //itemStackLabel.cleanUp(false);
                        }
                    }

                }
            }
        } else {
            for (int x = 0; x < inventory.getSize().x; x++) {
                for (int y = 0; y < inventory.getSize().y; y++) {

                    //background of the slot
                    glClear(GL_DEPTH_BUFFER_BIT);

                    modelViewMatrix.set(updateOrthoModelMatrix(workerVec3D.set(((double) x + 0.5d - workerVec2D.x + startingPoint.x) * (scale + spacing), ((y * -1d) - 0.5d + startingPoint.y + workerVec2D.y) * (scale + spacing), 0), workerVec3F.set(0, 0, 0), workerVec3D2.set(scale, scale, scale)));
                    hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);

                    if (inventory.getSelection().x == x && inventory.getSelection().y == y){
                        getInventorySlotSelectedMesh().render();
                    } else {
                        getInventorySlotMesh().render();
                    }


                    Item thisItem = inventory.get(x,y);

                    //only attempt if an actual item and not empty slot
                    if (thisItem != null) {

                        //render item
                        glClear(GL_DEPTH_BUFFER_BIT);
                        if (getItemDefinition(thisItem.name).isItem) {
                            modelViewMatrix.set(updateOrthoModelMatrix(workerVec3D.set(((double) x + 0.5d - workerVec2D.x + startingPoint.x) * (scale + spacing), ((y * -1d) - 0.5d + startingPoint.y + workerVec2D.y) * (scale + spacing) - (blockScale / 3.25d), 0), workerVec3F.set(0, 0, 0), workerVec3D2.set(itemScale, itemScale, itemScale)));
                        } else {
                            modelViewMatrix.set(updateOrthoModelMatrix(workerVec3D.set(((double) x + 0.5d - workerVec2D.x + startingPoint.x) * (scale + spacing), ((y * -1d) - 0.5d + startingPoint.y + workerVec2D.y) * (scale + spacing) - (blockScale / 7d), 0), workerVec3F.set(45, 45, 0), workerVec3D2.set(blockScale, blockScale, blockScale)));
                        }
                        hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                        getItemMesh(thisItem.name).render();

                        //render item stack number
                        if (thisItem.stack > 1) {
                            glClear(GL_DEPTH_BUFFER_BIT);
                            modelViewMatrix.set(updateOrthoModelMatrix(workerVec3D.set(((double) x + 0.7d - workerVec2D.x + startingPoint.x) * (scale + spacing), ((y * -1d) - 0.6d + startingPoint.y + workerVec2D.y) * (scale + spacing) - (blockScale / 7d), 0), workerVec3F.set(0, 0, 0), workerVec3D2.set(textScale, textScale, textScale)));
                            hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                            //Mesh itemStackLabel = createTextCenteredWithShadow(Integer.toString(thisItem.stack), 1, 1, 1);
                            // itemStackLabel.render();
                            //itemStackLabel.cleanUp(false);
                        }
                    }
                }
            }
        }

    }

    private static void renderGameGUI(){
        for (GUIObject thisButton : getGamePauseMenuGUI()) {
            ShaderProgram hudShaderProgram = getHudShaderProgram();

            float windowScale = getWindowScale();

            double xPos = thisButton.pos.x * (windowScale / 100d);
            double yPos = thisButton.pos.y * (windowScale / 100d);


            Matrix4d modelViewMatrix = updateOrthoModelMatrix(workerVec3D.set(xPos, yPos, 0), workerVec3F.set(0, 0, 0), workerVec3D2.set(windowScale / 20d, windowScale / 20d, windowScale / 20d));
            hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
            thisButton.textMesh.render();

            float xAdder = 20 / thisButton.buttonScale.x;
            float yAdder = 20 / thisButton.buttonScale.y;

            modelViewMatrix = updateOrthoModelMatrix(workerVec3D.set(xPos, yPos, 0), workerVec3F.set(0, 0, 0), workerVec3D2.set(windowScale / xAdder, windowScale / yAdder, windowScale / 20d));
            hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
            if (thisButton.selected){
                getButtonSelectedMesh().render();
            } else {
                getButtonMesh().render();
            }
        }
    }

    public static void cleanupRenderer(){
        if (shaderProgram != null){
            shaderProgram.cleanup();
        }

        if (hudShaderProgram != null){
            hudShaderProgram.cleanup();
        }

        if (glassLikeShaderProgram != null){
            glassLikeShaderProgram.cleanup();
        }


        if (entityShaderProgram != null){
            entityShaderProgram.cleanup();
        }
    }

    private static double getChunkDistanceFromPlayer(int x, int z){
        Vector3i currentChunk = getPlayerCurrentChunk();
        return Math.max(getDistance(0,0,currentChunk.z, 0, 0, z), getDistance(currentChunk.x,0,0, x, 0, 0));
    }
}
