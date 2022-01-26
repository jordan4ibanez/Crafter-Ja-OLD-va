package engine.render;

import engine.Utils;
import engine.Window;
import engine.graphics.Camera;
import engine.graphics.Mesh;
import engine.graphics.ShaderProgram;
import engine.graphics.Transformation;
import engine.gui.GUI;
import engine.gui.GUIObject;
import engine.settings.Settings;
import engine.time.Delta;
import engine.time.TimeOfDay;
import game.chunk.Chunk;
import game.chunk.ChunkObject;
import game.clouds.Cloud;
import game.crafting.InventoryLogic;
import game.light.Light;
import game.player.Player;
import org.joml.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

import static org.joml.Math.toRadians;
import static org.lwjgl.opengl.GL11C.*;

public class GameRenderer {

    private Camera camera;
    private Chunk chunk;
    private Window window;
    private Delta delta;
    private Settings settings;
    private Player player;
    private Transformation transformation;
    private TimeOfDay timeOfDay;
    private GUI gui;
    private Cloud cloud;
    private Light light;
    private InventoryLogic inventoryLogic;

    public void setCamera(Camera camera){
        if (this.camera == null){
            this.camera = camera;
        }
    }

    public void setChunk(Chunk chunk){
        if (this.chunk == null){
            this.chunk = chunk;
        }
    }
    public void setWindow(Window window){
        if (this.window == null){
            this.window = window;
            this.transformation = new Transformation(this.camera, this.window);
        }
    }
    public void setDelta(Delta delta){
        if (this.delta == null){
            this.delta = delta;
        }
    }
    public void setSettings(Settings settings){
        if (this.settings == null){
            this.settings = settings;
        }
    }
    public void setPlayer(Player player){
        if (this.player == null){
            this.player = player;
        }
    }

    public void setTimeOfDay(TimeOfDay timeOfDay){
        if (this.timeOfDay == null){
            this.timeOfDay = timeOfDay;
        }
    }
    public void setGui(GUI gui){
        if (this.gui == null){
            this.gui = gui;
        }
    }
    public void setCloud(Cloud cloud){
        if (this.cloud == null){
            this.cloud = cloud;
        }
    }
    public void setLight(Light light){
        if (this.light == null){
            this.light = light;
        }
    }
    public void setInventoryLogic(InventoryLogic inventoryLogic){
        if (this.inventoryLogic == null){
            this.inventoryLogic = inventoryLogic;
        }
    }

    private final float FOV = toRadians(72.0f);
    private final float Z_NEAR = 0.1f;
    private final ShaderProgram shaderProgram;
    private final ShaderProgram hudShaderProgram;
    private final ShaderProgram glassLikeShaderProgram;
    private final ShaderProgram entityShaderProgram;
    private final HashMap<Double, Mesh[]> normalDrawTypeHash  = new HashMap<>();
    private final HashMap<Double, Mesh[]> liquidDrawTypeHash  = new HashMap<>();
    private final HashMap<Double, Mesh[]> allFaceDrawTypeHash = new HashMap<>();
    private final HashMap<Double, Vector2i> chunkHashKeys    = new HashMap<>();

    public GameRenderer(){
        //normal shader program
        Utils utils = new Utils();
        shaderProgram = new ShaderProgram(utils.loadResource("resources/vertex.vs"), utils.loadResource("resources/fragment.fs"));

        //create uniforms for world and projection matrices
        shaderProgram.createUniform("projectionMatrix");
        //create uniforms for model view matrix
        shaderProgram.createUniform("modelViewMatrix");
        //create uniforms for texture sampler
        shaderProgram.createUniform("texture_sampler");

        //ortholinear hud shader program
        hudShaderProgram = new ShaderProgram(utils.loadResource("resources/hud_vertex.vs"), utils.loadResource("resources/hud_fragment.fs"));

        //create uniforms for model view matrix
        hudShaderProgram.createUniform("modelViewMatrix");
        //create uniforms for texture sampler
        hudShaderProgram.createUniform("texture_sampler");

        //glassLike shader program
        glassLikeShaderProgram = new ShaderProgram(utils.loadResource("resources/glasslike_vertex.vs"), utils.loadResource("resources/glasslike_fragment.fs"));

        //create uniforms for world and projection matrices
        glassLikeShaderProgram.createUniform("projectionMatrix");
        //create uniforms for model view matrix
        glassLikeShaderProgram.createUniform("modelViewMatrix");
        //create uniforms for texture sampler
        glassLikeShaderProgram.createUniform("texture_sampler");
        //glassLike shader program
        entityShaderProgram = new ShaderProgram(utils.loadResource("resources/entity_vertex.vs"), utils.loadResource("resources/entity_fragment.fs"));

        //create uniforms for world and projection matrices
        entityShaderProgram.createUniform("projectionMatrix");
        //create uniforms for model view matrix
        entityShaderProgram.createUniform("modelViewMatrix");
        //create uniforms for texture sampler
        entityShaderProgram.createUniform("texture_sampler");
        //create uniform for light value
        entityShaderProgram.createUniform("light");

        //window.setWindowClearColor(0.53f,0.81f,0.92f,0.f);

        //window.updateScale();
    }

    public float getzNear(){
        return Z_NEAR;
    }

    public ShaderProgram getShaderProgram(){
        return shaderProgram;
    }

    public ShaderProgram getHudShaderProgram(){
        return hudShaderProgram;
    }

    public void clearScreen(){
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }


    public void renderGame() {
        window.processClearColorInterpolation();
        clearScreen();

        int renderDistance = settings.getRenderDistance();

        //update projection matrix
        transformation.resetProjectionMatrix(FOV + player.getRunningFOVAdder(), window.getWidth(), window.getHeight(), Z_NEAR, (renderDistance * 2) * 16f);

        //todo BEGIN chunk sorting ---------------------------------------------------------------------------------------------

        Collection<ChunkObject> map = chunk.getAllChunks();

        /*
        int meshCount = 0;

        double flickerFixer = 0d;

        //get all distances
        for (ChunkObject chunk : chunk.getAllChunks()){
            Vector2i key = chunk.getPos();
            double currentDistance = getCameraPosition().distance((key.x * 16d) + 8d, 0,(key.y * 16d) + 8d);

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


        Mesh[][] normalDrawTypeArray  = new Mesh[meshCount][8];
        Mesh[][] liquidDrawTypeArray  = new Mesh[meshCount][8];
        Mesh[][] allFaceDrawTypeArray = new Mesh[meshCount][8];
        Vector2i[] chunkArrayKeys    = new Vector2i[meshCount];

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
         */
        //todo END chunk sorting ---------------------------------------------------------------------------------------------


        //get fast or fancy
        boolean graphicsMode = settings.getGraphicsMode();

        if (graphicsMode) {
            glassLikeShaderProgram.bind();
            glassLikeShaderProgram.setUniform("projectionMatrix", transformation.getProjectionMatrix());
            glassLikeShaderProgram.setUniform("texture_sampler", 0);
        } else {
            shaderProgram.bind();
            shaderProgram.setUniform("projectionMatrix", transformation.getProjectionMatrix());
            shaderProgram.setUniform("texture_sampler", 0);
        }


        //render the sun and moon
        //glDisable(GL_CULL_FACE);
        {

            double timeOfDayLinear = timeOfDay.getTimeOfDayLinear();

            //daytime sky
            if (timeOfDayLinear <= 0.85 && timeOfDayLinear >= 0.15) {
                transformation.updateCelestialMatrix(timeOfDayLinear - 0.5d);
                shaderProgram.setUniform("modelViewMatrix", transformation.getModelMatrix());
                gui.getSunMesh().render();

            }
            //nighttime sky
            if (timeOfDayLinear > 0.65 || timeOfDayLinear < 0.35) {
                transformation.updateCelestialMatrix(timeOfDayLinear);
                shaderProgram.setUniform("modelViewMatrix", transformation.getModelMatrix());
                gui.getMoonMesh().render();
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
        entityShaderProgram.setUniform("projectionMatrix", transformation.getProjectionMatrix());
        entityShaderProgram.setUniform("texture_sampler", 0);

        //Render clouds
        {
            boolean[][] cloudData = cloud.getCloudData();
            float cloudScale = cloud.getCloudScale();
            Vector2i cloudPos = cloud.getCloudPos();
            float cloudScroll = cloud.getCloudScroll();
            if (graphicsMode) {
                for (byte x = 0; x < 16; x++) {
                    for (byte z = 0; z < 16; z++) {
                        if (cloudData[x][z]) {
                            transformation.updateViewMatrix((x * cloudScale) + ((cloudPos.x - 8) * 16d), 130, (z * cloudScale) + ((cloudPos.y - 8) * 16d) + cloudScroll, 0, 0, 0);
                            entityShaderProgram.setLightUniform("light", light.getCurrentGlobalLightLevel());
                            entityShaderProgram.setUniform("modelViewMatrix", transformation.getModelMatrix());
                            cloud.getCloud3DMesh().render();
                        }
                    }
                }
            } else {
                for (byte x = 0; x < 16; x++) {
                    for (byte z = 0; z < 16; z++) {
                        if (cloudData[x][z]) {
                            transformation.updateViewMatrix((x * cloudScale) + ((cloudPos.x - 8) * 16d), 130, (z * cloudScale) + ((cloudPos.y - 8) * 16d) + cloudScroll, 0, 0, 0);
                            entityShaderProgram.setLightUniform("light", light.getCurrentGlobalLightLevel());
                            entityShaderProgram.setUniform("modelViewMatrix", transformation.getModelMatrix());
                            cloud.getCloud2DMesh().render();
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
        for (ChunkObject aChunk : map) {

            Mesh[] thisChunk = aChunk.getNormalMeshArray();

            if (thisChunk == null) {
                continue;
            }

            Vector2i thisPos = aChunk.getPos();
            float thisHover = aChunk.getHover();

            //normal
            for (Mesh thisMesh : thisChunk) {
                if (thisMesh != null) {
                    transformation.updateViewMatrix(thisPos.x * 16d, 0, thisPos.y * 16d, 0, 0, 0);
                    if (graphicsMode) {
                        glassLikeShaderProgram.setUniform("modelViewMatrix", transformation.getModelMatrix());
                    } else {
                        shaderProgram.setUniform("modelViewMatrix", transformation.getModelMatrix());
                    }
                    thisMesh.render();
                }
            }
        }


        //render allFaces chunk meshes

        for (ChunkObject aChunk : map) {

            Mesh[] thisChunk = aChunk.getAllFaceMeshArray();

            if (thisChunk == null) {
                continue;
            }

            Vector2i thisPos = aChunk.getPos();
            //float thisHover = getChunkHover(thisPos);

            //allFaces
            for (Mesh thisMesh : thisChunk) {
                if (thisMesh != null) {
                    transformation.updateViewMatrix(thisPos.x * 16d, 0, thisPos.y * 16d, 0, 0, 0);
                    if (graphicsMode) {
                        glassLikeShaderProgram.setUniform("modelViewMatrix", transformation.getModelMatrix());
                    } else {
                        shaderProgram.setUniform("modelViewMatrix", transformation.getModelMatrix());
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
        /*
        for (int thisItem : getAllItems()){
            updateItemViewMatrix(getItemPosX(thisItem),getItemPosYWithHover(thisItem),getItemPosZ(thisItem), 0, getItemRotation(thisItem), 0);
            entityShaderProgram.setLightUniform("light", getItemLight(thisItem));
            entityShaderProgram.setUniform("modelViewMatrix", getModelMatrix());
            getItemMesh(getItemName(thisItem)).render();
        }
         */


        //render each TNT entity
        /*
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
         */

        //render falling entities

        /*
        index = 0;
        for (boolean thisExists : getFallingEntities()){
            if (!thisExists) {
                index++;
                continue;
            }
            entityShaderProgram.setLightUniform("light", getFallingEntityLight(index));
            Vector3d thisPos = getFallingEntityPos(index);
            updateViewMatrixWithPosRotationScale(thisPos.x, thisPos.y, thisPos.z, 0,0,0, 2.5f, 2.5f, 2.5f);

            entityShaderProgram.setUniform("modelViewMatrix", getModelMatrix());

            getItemMesh(getBlockName(getFallingEntityBlockID(index))).render();
            index++;
        }
         */

        //render mobs

        /*
        for (int thisMob : getMobKeys()){

            int offsetIndex = 0;

            //primitive
            int thisMobDefinitionID = getMobID(thisMob);
            boolean backFaceCulling = getMobBackFaceCulling(thisMobDefinitionID);
            float thisMobSmoothRotation = getMobSmoothRotation(thisMob);
            float thisMobDeathRotation = getMobDeathRotation(thisMob);

            //pointer
            Vector3d thisMobPos = getMobPos(thisMob);
            Vector3f[] thisMobBodyOffsets = getMobDefinitionBodyOffsets(thisMobDefinitionID);
            Vector3f[] thisMobBodyRotations = getMobBodyRotations(thisMob);

            if (!backFaceCulling){
                glDisable(GL_CULL_FACE);
            }

            entityShaderProgram.setLightUniform("light", getMobLight(thisMob) + getMobHurtAdder(thisMob)); //hurt adder adds 15 to the value so it turns red

            for (Mesh thisMesh : getMobDefinitionBodyMeshes(thisMobDefinitionID)) {
                updateMobMatrix(
                        thisMobPos.x, thisMobPos.y, thisMobPos.z,
                        thisMobBodyOffsets[offsetIndex].x, thisMobBodyOffsets[offsetIndex].y, thisMobBodyOffsets[offsetIndex].z,
                        0, thisMobSmoothRotation, thisMobDeathRotation,
                        thisMobBodyRotations[offsetIndex].x,thisMobBodyRotations[offsetIndex].y, thisMobBodyRotations[offsetIndex].z,
                        1f, 1f, 1f);
                entityShaderProgram.setUniform("modelViewMatrix", getModelMatrix());
                thisMesh.render();
                offsetIndex++;
            }

            if (!backFaceCulling){
                glEnable(GL_CULL_FACE);
            }
        }

         */
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

        /*
        if (camera.getCameraPerspective() > 0){
            Mesh[] playerMeshes = player.getPlayerMeshes();
            Vector3f[] playerBodyOffsets = getPlayerBodyOffsets();
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
                    updateMobMatrix(pos.x, pos.y, pos.z, playerBodyOffsets[offsetIndex].x, playerBodyOffsets[offsetIndex].y, playerBodyOffsets[offsetIndex].z, 0, getCameraRotation().y, 0, headRot + playerBodyRotation[offsetIndex].x,playerBodyRotation[offsetIndex].y,playerBodyRotation[offsetIndex].z, 1f, 1f, 1f);
                } else {
                    if (offsetIndex == 0){
                        headRot = getCameraRotation().x * -1f;
                    }
                    updateMobMatrix(pos.x, pos.y, pos.z, playerBodyOffsets[offsetIndex].x,playerBodyOffsets[offsetIndex].y,playerBodyOffsets[offsetIndex].z, 0, getCameraRotation().y + 180f, 0, headRot + playerBodyRotation[offsetIndex].x,playerBodyRotation[offsetIndex].y,playerBodyRotation[offsetIndex].z, 1f, 1f, 1f);
                }
                entityShaderProgram.setUniform("modelViewMatrix", getModelMatrix());
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
         */
        //render particles

        /*
        int particleIndex = 0;
        for (boolean particleExists : getParticleExistence()) {
            if (!particleExists){
                particleIndex++;
                continue;
            }
            entityShaderProgram.setLightUniform("light", getParticleLight(particleIndex));
            updateParticleViewMatrix(getParticlePosX(particleIndex), getParticlePosY(particleIndex), getParticlePosZ(particleIndex), getCameraRotationX(), getCameraRotationY(), getCameraRotationZ());
            entityShaderProgram.setUniform("modelViewMatrix", getModelMatrix());
            getParticleMesh(particleIndex).render();
            particleIndex++;
        }
         */


        //render world selection mesh

        //fixme implement null check instead
        if (!player.getPlayerWorldSelectionPos().equals(0, -555, 0)) {

            entityShaderProgram.setLightUniform("light", 15); //todo make this work

            transformation.updateViewMatrix(player.getPlayerWorldSelectionPos().x, player.getPlayerWorldSelectionPos().y, player.getPlayerWorldSelectionPos().z, 0, 0, 0);
            entityShaderProgram.setUniform("modelViewMatrix", transformation.getModelMatrix());
            gui.getWorldSelectionMesh().render();

            if (player.getDiggingFrame() >= 0) {
                transformation.updateViewMatrix(player.getPlayerWorldSelectionPos().x, player.getPlayerWorldSelectionPos().y, player.getPlayerWorldSelectionPos().z, 0, 0, 0);
                entityShaderProgram.setUniform("modelViewMatrix", transformation.getModelMatrix());
                gui.getMiningCrackMesh(player.getDiggingFrame()).render();
            }
        }

        entityShaderProgram.unbind();


        if (graphicsMode) {
            glEnable(GL_BLEND);
        }

        //do standard blending
        shaderProgram.bind();
        shaderProgram.setUniform("projectionMatrix", transformation.getProjectionMatrix());
        shaderProgram.setUniform("texture_sampler", 0);


        //render liquid chunk meshes
        if (graphicsMode) {
            glDisable(GL_CULL_FACE);
        }


        for (ChunkObject aChunk : map) {

            Mesh[] thisChunk = aChunk.getLiquidMeshArray();

            if (thisChunk == null) {
                continue;
            }

            Vector2i thisPos = aChunk.getPos();
            //float thisHover = getChunkHover(thisPos);

            //liquid

            for (Mesh thisMesh : thisChunk) {
                if (thisMesh != null) {
                    transformation.updateViewMatrix(thisPos.x * 16d, 0, thisPos.y * 16d, 0, 0, 0);
                    shaderProgram.setUniform("modelViewMatrix", transformation.getModelMatrix());
                    thisMesh.render();
                }
            }
        }

        /*
        Arrays.fill(normalDrawTypeArray, null);
        Arrays.fill(liquidDrawTypeArray, null);
        Arrays.fill(allFaceDrawTypeArray, null);
        Arrays.fill(chunkArrayKeys, null);
         */

        if (graphicsMode) {
            glEnable(GL_CULL_FACE);
        }

        //finished with standard shader
        shaderProgram.unbind();


        //BEGIN HUD (3d parts) - just wield hand for now

        glClear(GL_DEPTH_BUFFER_BIT);

        //resetting the rendering position here for wield hand
        //the HUD is ortholinear, it is not affected by FOV
        transformation.resetProjectionMatrix(FOV, window.getWidth(), window.getHeight(), Z_NEAR, 100);


        //draw wield hand or item

        if (camera.getCameraPerspective() == 0) {
            entityShaderProgram.bind();

            entityShaderProgram.setUniform("projectionMatrix", transformation.getProjectionMatrix());
            entityShaderProgram.setLightUniform("light", player.getPlayerLightLevel());

            //wield hand
            if (inventoryLogic.getInventory().getMain().getItem(player.getPlayerInventorySelection(),0) == null){
                transformation.setWieldHandMatrix(
                        camera.getCameraPosition().x,camera.getCameraPosition().y, camera.getCameraPosition().z,
                        player.getWieldHand().getWieldHandAnimationPosX(), player.getWieldHand().getWieldHandAnimationPosY(), player.getWieldHand().getWieldHandAnimationPosZ(),
                        camera.getCameraRotation().x,camera.getCameraRotation().y, camera.getCameraRotation().z,
                        player.getWieldHand().getWieldHandAnimationRot().x,player.getWieldHand().getWieldHandAnimationRot().y,player.getWieldHand().getWieldHandAnimationRot().z,
                        0.35d,0.35d,0.35d,
                        0.05d,0.05d,0.05d);
                entityShaderProgram.setUniform("modelViewMatrix", transformation.getModelMatrix());
                getWieldHandMesh().render();
            //block/item
            } else if (inventoryLogic.getInventory().getMain().getItem(player.getPlayerInventorySelection(),0) != null){
                transformation.setWieldHandMatrix(
                        camera.getCameraPosition().x, camera.getCameraPosition().y, camera.getCameraPosition().z,
                        player.getWieldHand().getWieldHandAnimationPosX(), player.getWieldHand().getWieldHandAnimationPosY(), player.getWieldHand().getWieldHandAnimationPosZ(),
                        camera.getCameraRotation().x, camera.getCameraRotation().y, camera.getCameraRotation().z,
                        player.getWieldHand().getWieldHandAnimationRot().x, player.getWieldHand().getWieldHandAnimationRot().y, player.getWieldHand().getWieldHandAnimationRot().z,
                        1d, 1d, 1d,
                        0.05d,0.05d,0.05d);
                entityShaderProgram.setUniform("modelViewMatrix", getModelMatrix());
                inventoryLogic.getWieldInventory().render();
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
        transformation.resetOrthoProjectionMatrix(); // needed to get current screen size

        //render water effect

        /*
        if (camera.isSubmerged()) {
            transformation.updateOrthoModelMatrix(0,0,0,0,0,0, window.getScale() * 2, window.getScale(),window.getScale());
            hudShaderProgram.setUniform("modelViewMatrix", transformation.getOrthoModelMatrix());
            getGlobalWaterEffectMesh().render();
        }
         */

        glClear(GL_DEPTH_BUFFER_BIT);

        //render inverted crosshair
        if (camera.getCameraPerspective() == 0) {
            glBlendFunc(GL_ONE_MINUS_DST_COLOR, GL_ONE_MINUS_SRC_COLOR);
            transformation.updateOrthoModelMatrix(0, 0, 0, 0, 0, 0, window.getScale() / 20f, window.getScale() / 20f, window.getScale() / 20f);
            hudShaderProgram.setUniform("modelViewMatrix", transformation.getOrthoModelMatrix());
            gui.getCrossHairMesh().render();
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        }

        glClear(GL_DEPTH_BUFFER_BIT);

    }
        /*
        if (!isPaused()) {

            if (isPlayerInventoryOpen()) {

                //inventory backdrop
                {
                    updateOrthoModelMatrix(0, 0, 0, 0, 0, 0, windowScale, windowScale, windowScale);
                    hudShaderProgram.setUniform("modelViewMatrix", getOrthoModelMatrix());
                    getInventoryBackdropMesh().render();
                }

                glClear(GL_DEPTH_BUFFER_BIT);

                //player inside box
                {
                    updateOrthoModelMatrix(-(windowScale / 3.75d), (windowScale / 2.8d), 0, getPlayerHudRotationX(),getPlayerHudRotationY(),getPlayerHudRotationZ(), (windowScale / 18d), (windowScale / 18d), (windowScale / 18d));
                    hudShaderProgram.setUniform("modelViewMatrix", getOrthoModelMatrix());
                    getPlayerMesh().render();
                }

                glClear(GL_DEPTH_BUFFER_BIT);

                //inventory foreground
                {
                    updateOrthoModelMatrix(0, 0, 0, 0, 0, 0, windowScale, windowScale, windowScale);
                    hudShaderProgram.setUniform("modelViewMatrix", getOrthoModelMatrix());
                    getInventoryMesh().render();
                }


                glClear(GL_DEPTH_BUFFER_BIT);

                renderInventoryGUI("main");

                if (isAtCraftingBench()){
                    renderInventoryGUI("bigCraft");
                } else {
                    renderInventoryGUI("smallCraft");
                }


                renderInventoryGUI("output");
                renderInventoryGUI("armor");



                //render mouse item
                if (getMouseInventory() != null) {
                    glClear(GL_DEPTH_BUFFER_BIT);

                    if (getIfItem(getMouseInventory())) {
                        updateOrthoModelMatrix( getMousePosCenteredX(),  getMousePosCenteredY() - (windowScale / 27d), 0, 0, 0, 0, windowScale / 5d, windowScale / 5d, windowScale / 5d);
                    } else {
                        updateOrthoModelMatrix( getMousePosCenteredX(), getMousePosCenteredY() - (windowScale / 55d), 0, 45, 45, 0, windowScale / 8d, windowScale / 8d, windowScale / 8d);
                    }

                    hudShaderProgram.setUniform("modelViewMatrix", getOrthoModelMatrix());

                    getItemMesh(getMouseInventory()).render();

                    glClear(GL_DEPTH_BUFFER_BIT);

                    //stack numbers
                    if(getMouseInventoryCount() > 1) {
                        glClear(GL_DEPTH_BUFFER_BIT);
                        updateOrthoModelMatrix( getMousePosCenteredX() + (windowScale/47d),  getMousePosCenteredY() - (windowScale / 35f), 0, 0, 0, 0, windowScale / 48, windowScale / 48, windowScale / 48);
                        hudShaderProgram.setUniform("modelViewMatrix", getOrthoModelMatrix());
                        Mesh workerMesh = createTextCenteredWithShadow(Integer.toString(getMouseInventoryCount()), 1f, 1f, 1f);
                        workerMesh.render();
                        workerMesh.cleanUp(false);
                    }
                }
            } else {

                //health bar
                {
                    //everybody likes to jiggle...right?
                    byte[] healthArray = getHealthHudArray();
                    float[] healthJiggleArray = getHealthHudFloatArray();

                    for (byte i = 0; i < healthArray.length; i++) {

                        float jiggle = healthJiggleArray[i];

                        if (getPlayerHealth() > 6) {
                            jiggle = 0f;
                        }

                        updateOrthoModelMatrix(-windowScale / 2.105f + (i * (windowScale / 19.5d)), (-windowSizeY / 2d) + (windowScale / 6.8d) + jiggle, 0, 0, 0, 0, (windowScale / 20f), (windowScale / 20f), (windowScale / 20f));
                        hudShaderProgram.setUniform("modelViewMatrix", getOrthoModelMatrix());

                        //save cpu render calls
                        switch (healthArray[i]) {
                            case 2 -> getHeartHudMesh().render();
                            case 1 -> {
                                getHeartShadowHudMesh().render();
                                glClear(GL_DEPTH_BUFFER_BIT);
                                getHalfHeartHudMesh().render();
                            }
                            default -> getHeartShadowHudMesh().render();
                        }
                    }
                }
                glClear(GL_DEPTH_BUFFER_BIT);

                //hotbar
                {
                    updateOrthoModelMatrix(0,  (-windowSizeY / 2d) + (windowScale / 16.5d), 0, 0, 0, 0, (windowScale), (windowScale), (windowScale));
                    hudShaderProgram.setUniform("modelViewMatrix", getOrthoModelMatrix());
                    getHotBarMesh().render();
                }

                glClear(GL_DEPTH_BUFFER_BIT);

                //selection bar (in the hotbar)
                {
                    updateOrthoModelMatrix((getPlayerInventorySelection() - 4) * (windowScale / 9.1d),  (-windowSizeY / 2f) + ((windowScale / 8.25f) / 2f), 0, 0, 0, 0, windowScale / 8.25f, windowScale / 8.25f, windowScale / 8.25f);
                    hudShaderProgram.setUniform("modelViewMatrix", getOrthoModelMatrix());
                    getHotBarSelectionMesh().render();
                }

                //THESE GO LAST!

                glClear(GL_DEPTH_BUFFER_BIT);

                //version info
                {
                    updateOrthoModelMatrix(-windowSizeX / 2d, (windowSizeY / 2.1d), 0, 0, 0, 0, windowScale / 30d, windowScale / 30d, windowScale / 30d);
                    hudShaderProgram.setUniform("modelViewMatrix", getOrthoModelMatrix());
                    getVersionInfoText().render();
                }

                if(getDebugInfo()) {

                    //x info
                    {

                        glClear(GL_DEPTH_BUFFER_BIT);
                        updateOrthoModelMatrix((-windowSizeX / 2d), (windowSizeY / 2.3d), 0, 0, 0, 0, windowScale / 30d, windowScale / 30d, windowScale / 30d);
                        hudShaderProgram.setUniform("modelViewMatrix", getOrthoModelMatrix());
                        //this creates a new object every frame >:(
                        Mesh workerMesh = createTextWithShadow("X:" + getPlayerPos().x, 1f, 1f, 1f);
                        workerMesh.render();
                        workerMesh.cleanUp(false);
                    }

                    //y info
                    {

                        glClear(GL_DEPTH_BUFFER_BIT);
                        updateOrthoModelMatrix((-windowSizeX / 2d), (windowSizeY / 2.6d), 0, 0, 0, 0, windowScale / 30d, windowScale / 30d, windowScale / 30d);
                        hudShaderProgram.setUniform("modelViewMatrix", getOrthoModelMatrix());
                        //this creates a new object every frame >:(
                        Mesh workerMesh = createTextWithShadow("Y:" + getPlayerPos().y, 1f, 1f, 1f);
                        workerMesh.render();
                        workerMesh.cleanUp(false);
                    }

                    //z info
                    {
                        glClear(GL_DEPTH_BUFFER_BIT);
                        updateOrthoModelMatrix((-windowSizeX / 2d), (float) (windowSizeY / 3d), 0, 0, 0, 0, windowScale / 30d, windowScale / 30d, windowScale / 30d);
                        hudShaderProgram.setUniform("modelViewMatrix", getOrthoModelMatrix());
                        //this creates a new object every frame >:(
                        Mesh workerMesh = createTextWithShadow("Z:" + getPlayerPos().z, 1f, 1f, 1f);
                        workerMesh.render();
                        workerMesh.cleanUp(false);
                    }


                    Mesh[] runtimeInfo = getRuntimeInfoText();

                    for (int i = 0; i < runtimeInfo.length; i++){
                        glClear(GL_DEPTH_BUFFER_BIT);
                        updateOrthoModelMatrix((-windowSizeX / 2d), (float) (windowSizeY / 3d) + ((-i - 1) * (windowSizeY/20d)), 0, 0, 0, 0, windowScale / 30d, windowScale / 30d, windowScale / 30d);
                        hudShaderProgram.setUniform("modelViewMatrix", getOrthoModelMatrix());
                        if (runtimeInfo[i] != null) {
                            runtimeInfo[i].render();
                        }
                    }


                    //render fps
                    {
                        glClear(GL_DEPTH_BUFFER_BIT);
                        updateOrthoModelMatrix((-windowSizeX / 2d), (windowSizeY / 3d + (-7 * (windowSizeY/20d))), 0, 0, 0, 0, windowScale / 30d, windowScale / 30d, windowScale / 30d);
                        hudShaderProgram.setUniform("modelViewMatrix", getOrthoModelMatrix());
                        getFPSMesh().render();
                    }
                } else {
                    //only show FPS
                    {
                        glClear(GL_DEPTH_BUFFER_BIT);
                        updateOrthoModelMatrix((-windowSizeX / 2d), (windowSizeY / 2.3d), 0, 0, 0, 0, windowScale / 30d, windowScale / 30d, windowScale / 30d);
                        hudShaderProgram.setUniform("modelViewMatrix", getOrthoModelMatrix());
                        getFPSMesh().render();
                    }
                }

                //render items in hotbar
                for (byte x = 1; x <= 9; x++) {

                    String thisItem = getItemInInventory("main", x - 1, 0);

                    if (thisItem != null) {

                        glClear(GL_DEPTH_BUFFER_BIT);

                        if (getIfItem(thisItem)) {
                            updateOrthoModelMatrix(((x - 5d) * (windowScale / 9.1d)), (-windowSizeY / 2d) + (windowScale / 48d), 0, 0, 0, 0, windowScale / 5d, windowScale / 5d, windowScale / 5d);
                        } else {
                            updateOrthoModelMatrix(((x - 5d) * (windowScale / 9.1d)), (-windowSizeY / 2d) + (windowScale / 24d), 0, 45, 45, 0, windowScale / 8.01d, windowScale / 8.01d, windowScale / 8.01d);
                        }
                        hudShaderProgram.setUniform("modelViewMatrix", getOrthoModelMatrix());
                        getItemMesh(thisItem).render();


                        glClear(GL_DEPTH_BUFFER_BIT);

                        //render hotbar counts if greater than 1

                        int count = getInventoryCount("main", x - 1 , 0);

                        if (count > 1) {
                            updateOrthoModelMatrix(((x - 4.8d) * (windowScale / 9.1d)),  (-windowSizeY / 2d) + (windowScale / 32d), 0, 0, 0, 0, windowScale / 48, windowScale / 48, windowScale / 48);
                            hudShaderProgram.setUniform("modelViewMatrix", getOrthoModelMatrix());
                            //
                            Mesh workerMesh = createTextCenteredWithShadow(Integer.toString(count), 1f, 1f, 1f);
                            workerMesh.render();
                            workerMesh.cleanUp(false);
                        }
                    }
                }
            }
        } else {
            //render inventory base
            {
                updateOrthoModelMatrix(0, 0, 0, 0, 0, 0, windowSizeX, windowSizeY, windowScale);
                hudShaderProgram.setUniform("modelViewMatrix", getOrthoModelMatrix());
                getMenuBgMesh().render();
            }

            glClear(GL_DEPTH_BUFFER_BIT);

            renderGameGUI();
        }

        //render chat bar
        if (isChatOpen()){

            //render background
            glClear(GL_DEPTH_BUFFER_BIT);
            updateOrthoModelMatrix((-windowSizeX / 2d), (-windowSizeY / 2.9d), 0, 0, 0, 0, windowSizeX / 1.5d, windowScale / 15d, windowScale / 5d);
            hudShaderProgram.setUniform("modelViewMatrix", getOrthoModelMatrix());
            getChatBoxMesh().render();

            //render typing text
            glClear(GL_DEPTH_BUFFER_BIT);
            updateOrthoModelMatrix((-windowSizeX / 2d), (-windowSizeY / 2.9d), 0, 0, 0, 0, windowScale / 30d, windowScale / 30d, windowScale / 30d);
            hudShaderProgram.setUniform("modelViewMatrix", getOrthoModelMatrix());
            getCurrentMessageMesh().render();
        }


        //render chat messages

        {
            //needs to be fixed
            byte i = 1;
            for (Mesh mesh : getViewableChatMessages()){
                if (mesh != null) {
                    //render background
                    glClear(GL_DEPTH_BUFFER_BIT);
                    updateOrthoModelMatrix((-windowSizeX / 2d), (-windowSizeY / 2.9d) + ((windowScale / 15d) * i), 0, 0, 0, 0, windowSizeX / 1.5d, windowScale / 15d, windowScale / 5d);
                    hudShaderProgram.setUniform("modelViewMatrix", getOrthoModelMatrix());
                    getChatBoxMesh().render();

                    //render chat mesh
                    glClear(GL_DEPTH_BUFFER_BIT);
                    updateOrthoModelMatrix((-windowSizeX / 2d), (-windowSizeY / 2.9d) + ((windowScale / 15d) * i), 0, 0, 0, 0, windowScale / 30d, windowScale / 30d, windowScale / 30d);
                    hudShaderProgram.setUniform("modelViewMatrix", getOrthoModelMatrix());
                    mesh.render();
                }
                i++;
            }
        }
        hudShaderProgram.unbind();
    }


    private void renderInventoryGUI(String inventoryName){

        double startingPointX = getInventoryPosX(inventoryName);
        double startingPointY = getInventoryPosY(inventoryName);

        //this is the size of the actual slots
        //it also makes the default spacing of (0)
        //they bunch up right next to each other with 0
        double scale = windowScale/10.5d;
        double blockScale = windowScale / 8d;
        double itemScale = windowScale / 5d;
        double textScale = windowScale / 48;

        //this is the spacing between the slots
        double spacing = windowScale / 75d;

        int sizeX = getInventorySizeX(inventoryName);
        int sizeY = getInventorySizeY(inventoryName);

        double inventoryHalfSizeX = sizeX/2d;
        double inventoryHalfSizeY = sizeY/2d;

        int selectionX = getInventorySelectionX(inventoryName);
        int selectionY = getInventorySelectionY(inventoryName);

        String[][] thisInventory = getInventoryAsArray(inventoryName);
        int[][] thisCount = getInventoryCountAsArray(inventoryName);

        double yProgram;
        if (inventoryName.equals("main")) {
            for (byte x = 0; x < sizeX; x++) {
                for (byte y = 0; y < sizeY; y++) {

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

                    updateOrthoModelMatrix((x + 0.5d - inventoryHalfSizeX + startingPointX) * (scale + spacing), ((y * -1d) - 0.5d + startingPointY + inventoryHalfSizeY + yProgram) * (scale + spacing), 0, 0, 0, 0, scale, scale, scale);
                    hudShaderProgram.setUniform("modelViewMatrix", getOrthoModelMatrix());

                    if (selectionX == x && selectionY == y){
                        getInventorySlotSelectedMesh().render();
                    } else {
                        getInventorySlotMesh().render();
                    }

                    String thisItem = thisInventory[y][x];

                    //only attempt if an actual item and not empty slot
                    if (thisItem != null) {

                        //render item
                        glClear(GL_DEPTH_BUFFER_BIT);
                        if (getIfItem(thisItem)) {
                            updateOrthoModelMatrix((x + 0.5d - inventoryHalfSizeX + startingPointX) * (scale + spacing), ((y * -1d) - 0.5d + startingPointY + inventoryHalfSizeY + yProgram) * (scale + spacing) - (blockScale / 3.25d), 0, 0, 0, 0, itemScale, itemScale, itemScale);
                        } else {
                            updateOrthoModelMatrix((x + 0.5d - inventoryHalfSizeX + startingPointX) * (scale + spacing), ((y * -1d) - 0.5d + startingPointY + inventoryHalfSizeY + yProgram) * (scale + spacing) - (blockScale / 7d), 0, 45, 45, 0, blockScale, blockScale, blockScale);
                        }
                        hudShaderProgram.setUniform("modelViewMatrix", getOrthoModelMatrix());
                        getItemMesh(thisItem).render();

                        int count = thisCount[y][x];

                        //render item stack number
                        if (count > 1) {
                            glClear(GL_DEPTH_BUFFER_BIT);
                            updateOrthoModelMatrix((x + 0.7d - inventoryHalfSizeX + startingPointX) * (scale + spacing), ((y * -1d) - 0.6d + startingPointY + inventoryHalfSizeY + yProgram) * (scale + spacing) - (blockScale / 7d), 0, 0, 0, 0, textScale, textScale, textScale);
                            hudShaderProgram.setUniform("modelViewMatrix", getOrthoModelMatrix());
                            //this creates a new object in memory >:(
                            Mesh itemStackLabel = createTextCenteredWithShadow(Integer.toString(count), 1, 1, 1);
                            itemStackLabel.render();
                            itemStackLabel.cleanUp(false);
                        }
                    }

                }
            }
        } else {
            for (int x = 0; x < sizeX; x++) {
                for (int y = 0; y < sizeY; y++) {

                    //background of the slot
                    glClear(GL_DEPTH_BUFFER_BIT);

                    updateOrthoModelMatrix((x + 0.5d - inventoryHalfSizeX + startingPointX) * (scale + spacing), ((y * -1d) - 0.5d + startingPointY + inventoryHalfSizeY) * (scale + spacing), 0, 0, 0, 0, scale, scale, scale);
                    hudShaderProgram.setUniform("modelViewMatrix", getOrthoModelMatrix());

                    if (selectionX == x && selectionY == y){
                        getInventorySlotSelectedMesh().render();
                    } else {
                        getInventorySlotMesh().render();
                    }


                    String thisItem = thisInventory[y][x];

                    //only attempt if an actual item and not empty slot
                    if (thisItem != null) {

                        //render item
                        glClear(GL_DEPTH_BUFFER_BIT);
                        if (getIfItem(thisItem)) {
                            updateOrthoModelMatrix((x + 0.5d - inventoryHalfSizeX + startingPointX) * (scale + spacing), ((y * -1d) - 0.5d + startingPointY + inventoryHalfSizeY) * (scale + spacing) - (blockScale / 3.25d), 0, 0, 0, 0, itemScale, itemScale, itemScale);
                        } else {
                            updateOrthoModelMatrix((x + 0.5d - inventoryHalfSizeX + startingPointX) * (scale + spacing), ((y * -1d) - 0.5d + startingPointY + inventoryHalfSizeY) * (scale + spacing) - (blockScale / 7d), 0, 45, 45, 0, blockScale, blockScale, blockScale);
                        }
                        hudShaderProgram.setUniform("modelViewMatrix", getOrthoModelMatrix());
                        getItemMesh(thisItem).render();

                        int count = thisCount[y][x];

                        //render item stack number
                        if (count > 1) {
                            glClear(GL_DEPTH_BUFFER_BIT);
                            updateOrthoModelMatrix((x + 0.7d - inventoryHalfSizeX + startingPointX) * (scale + spacing), ((y * -1d) - 0.6d + startingPointY + inventoryHalfSizeY) * (scale + spacing) - (blockScale / 7d), 0, 0, 0, 0, textScale, textScale, textScale);
                            hudShaderProgram.setUniform("modelViewMatrix", getOrthoModelMatrix());
                            //this creates a new object in memory >:(
                            Mesh itemStackLabel = createTextCenteredWithShadow(Integer.toString(count), 1, 1, 1);
                            itemStackLabel.render();
                            itemStackLabel.cleanUp(false);
                        }
                    }
                }
            }
        }

    }

    private void renderGameGUI(){
        for (GUIObject thisButton : getGamePauseMenuGUI()) {
            
            double xPos = thisButton.pos.x * (windowScale / 100d);
            double yPos = thisButton.pos.y * (windowScale / 100d);


            updateOrthoModelMatrix(xPos, yPos, 0, 0, 0, 0, windowScale / 20d, windowScale / 20d, windowScale / 20d);
            hudShaderProgram.setUniform("modelViewMatrix", getOrthoModelMatrix());
            thisButton.textMesh.render();

            float xAdder = 20 / thisButton.buttonScale.x;
            float yAdder = 20 / thisButton.buttonScale.y;

            updateOrthoModelMatrix(xPos, yPos, 0, 0, 0, 0, windowScale / xAdder, windowScale / yAdder, windowScale / 20d);
            hudShaderProgram.setUniform("modelViewMatrix", getOrthoModelMatrix());
            if (thisButton.selected){
                getButtonSelectedMesh().render();
            } else {
                getButtonMesh().render();
            }
        }
    }
    public void cleanupRenderer(){
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

    private double getChunkDistanceFromPlayer(int x, int z){
        Vector3i currentChunk = getPlayerCurrentChunk();
        return max(getDistance(0,0,currentChunk.z, 0, 0, z), getDistance(currentChunk.x,0,0, x, 0, 0));
    }
   */
}
