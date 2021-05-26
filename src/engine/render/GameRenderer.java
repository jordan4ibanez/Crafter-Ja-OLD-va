package engine.render;

import engine.Utils;
import engine.graphics.*;
import engine.gui.GUIObject;
import game.chunk.ChunkObject;
import game.crafting.InventoryObject;
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
import static engine.debug.CheckRuntimeInfo.getRuntimeInfoText;
import static engine.graphics.Camera.*;
import static engine.graphics.Transformation.*;
import static engine.graphics.Transformation.buildOrthoProjModelMatrix;
import static engine.gui.GUI.*;
import static engine.gui.GUILogic.*;
import static engine.gui.TextHandling.*;
import static engine.settings.Settings.*;
import static game.crafting.InventoryLogic.getPlayerHudRotation;
import static game.falling.FallingEntity.getFallingEntities;
import static game.item.ItemDefinition.getItemDefinition;
import static game.item.ItemEntity.*;
import static game.mob.Human.getHumanBodyOffsets;
import static game.mob.Human.getHumanMeshes;
import static game.mob.Mob.getAllMobs;
import static game.particle.Particle.getAllParticles;
import static game.tnt.TNTEntity.*;
import static engine.Window.*;
import static game.chunk.Chunk.*;
import static game.crafting.Inventory.*;
import static game.player.Player.*;
import static org.lwjgl.opengl.GL44.*;
import static org.lwjgl.opengl.GL44C.GL_BLEND;
import static org.lwjgl.opengl.GL44C.glDisable;

public class GameRenderer {

    private static final float FOV = (float) Math.toRadians(72.0f); //todo: make this a calculator method ala calculateFOV(float);

    private static final float Z_NEAR = 0.1f;

    private static float windowScale = 0f;

    private static final Vector2d windowSize = new Vector2d();

    private static ShaderProgram shaderProgram;
    private static ShaderProgram hudShaderProgram;
    private static ShaderProgram glassLikeShaderProgram;

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

    public static float getFOV(){
        return FOV;
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


    private static Mesh workerMesh;

    public static void renderGame(){
        clearScreen();

        rescaleWindow();

        int renderDistance = getRenderDistance();

        //update projection matrix
        Matrix4d projectionMatrix = getProjectionMatrix(FOV + getRunningFOVAdder(), getWindowWidth(), getWindowHeight(), Z_NEAR, (renderDistance + 1) * 16f);
        //update the view matrix
        Matrix4d viewMatrix = getViewMatrix();



        Matrix4d modelViewMatrix;

        //todo chunk sorting ---------------------------------------------------------------------------------------------

        Vector3d camPos = getCameraPosition();

        HashMap<Double, ChunkObject> chunkHash = new HashMap<>();


        //get all distances
        for (ChunkObject thisChunk : getMap()){
            double currentDistance = getDistance((thisChunk.x * 16d) + 8d, 0,(thisChunk.z * 16d) + 8d, camPos.x, 0, camPos.z);

            //this doesn't fix anything, todo: fix flickering chunks
            if (chunkHash.get(currentDistance) != null){
                currentDistance += 0.000000001;
            }


            if (getChunkDistanceFromPlayer(thisChunk.x, thisChunk.z) <= renderDistance) {
                chunkHash.put(currentDistance, thisChunk);
            }
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

        //todo end chunk sorting ---------------------------------------------------------------------------------------------

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

        glDisable(GL_BLEND);

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
                        if (graphicsMode) {
                            glassLikeShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                        } else {
                            shaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                        }
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
                        if (graphicsMode) {
                            glassLikeShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                        } else {
                            shaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                        }
                        thisMesh.render();
                    }
                }
            }
        }

        //        render each item entity
        for (Item thisItem : getAllItems()){
            modelViewMatrix = updateModelViewMatrix(new Vector3d(thisItem.pos).add(0,thisItem.hover,0), thisItem.rotation, viewMatrix);
            if (graphicsMode) {
                glassLikeShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
            } else {
                shaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
            }
            thisItem.mesh.render();
        }

        //render each TNT entity
        Mesh tntMesh = getTNTMesh();
        for (int i = 0; i < getTotalTNT(); i++){
            if (!tntExists(i)){
                continue;
            }
            modelViewMatrix = getTNTModelViewMatrix(i, viewMatrix);
            if (graphicsMode) {
                glassLikeShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
            } else {
                shaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
            }
            tntMesh.render();
        }

        //render falling entities
        for (FallingEntityObject thisObject : getFallingEntities()){
            modelViewMatrix = getGenericMatrixWithPosRotationScale(thisObject.pos, new Vector3f(0,0,0), new Vector3d(2.5d,2.5d,2.5d), viewMatrix);
            if (graphicsMode) {
                glassLikeShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
            } else {
                hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
            }
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
                if (graphicsMode) {
                    glassLikeShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                } else {
                    hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                }
                thisMesh.render();
                offsetIndex++;
            }
        }

        //todo: remove dependency of Human mob
        if (getCameraPerspective() > 0){
            Mesh[] playerMeshes = getHumanMeshes();
            Vector3f[] playerBodyOffsets = getHumanBodyOffsets();
            Vector3f[] playerBodyRotation = getPlayerBodyRotations();
            Vector3d pos = getPlayerPos();
            int offsetIndex = 0;
            for (Mesh thisMesh : playerMeshes) {
                if (getCameraPerspective() == 1) {
                    float headRot = 0; //pitch
                    if (offsetIndex == 0){
                        headRot = getCameraRotation().x;
                    }
                    modelViewMatrix = getMobMatrix(new Vector3d(pos), playerBodyOffsets[offsetIndex], new Vector3f(0, getCameraRotation().y, 0), new Vector3f(headRot + playerBodyRotation[offsetIndex].x,playerBodyRotation[offsetIndex].y,playerBodyRotation[offsetIndex].z), new Vector3d(1f, 1f, 1f), viewMatrix);
                } else {
                    float headRot = 0; //pitch
                    if (offsetIndex == 0){
                        headRot = getCameraRotation().x * -1f;
                    }
                    modelViewMatrix = getMobMatrix(new Vector3d(pos), playerBodyOffsets[offsetIndex], new Vector3f(0, getCameraRotation().y + 180f, 0), new Vector3f(headRot + playerBodyRotation[offsetIndex].x,playerBodyRotation[offsetIndex].y,playerBodyRotation[offsetIndex].z), new Vector3d(1f, 1f, 1f), viewMatrix);
                }
                if (graphicsMode) {
                    glassLikeShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                } else {
                    hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                }
                thisMesh.render();
                offsetIndex++;
            }
        }


        //render particles
        for (ParticleObject thisParticle : getAllParticles()){
            Mesh thisMesh = thisParticle.mesh;

            modelViewMatrix = updateParticleViewMatrix(thisParticle.pos, new Vector3f(getCameraRotation()), viewMatrix);
            if (graphicsMode) {
                glassLikeShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
            } else {
                hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
            }
            thisMesh.render();
        }

        //render rain drops
        /*
        Mesh rainDrop = getRainDropMesh();
        for (RainDropEntity thisRainDrop : getRainDrops()){
            modelViewMatrix = updateParticleViewMatrix(thisRainDrop.pos, new Vector3f(0,getCameraRotation().y,0), viewMatrix);
            if (graphicsMode) {
                glassLikeShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
            } else {
                hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
            }
            rainDrop.render();
        }
         */

        //we must unbind then rebind
        if (!graphicsMode){
            shaderProgram.unbind();

            glassLikeShaderProgram.bind();
            glassLikeShaderProgram.setUniform("projectionMatrix", projectionMatrix);
            glassLikeShaderProgram.setUniform("texture_sampler", 0);
        }

        //render world selection mesh
        if (getPlayerWorldSelectionPos() != null){

            Vector3i tempPos = getPlayerWorldSelectionPos();

            Mesh selectionMesh = getWorldSelectionMesh();

            Vector3d actualPos = new Vector3d(tempPos.x, tempPos.y, tempPos.z);

            modelViewMatrix = updateModelViewMatrix(actualPos, new Vector3f(0, 0, 0), viewMatrix);
            glassLikeShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
            selectionMesh.render();

            if (getDiggingFrame() >= 0) {
                Mesh crackMesh = getMiningCrackMesh();
                modelViewMatrix = updateModelViewMatrix(actualPos, new Vector3f(0, 0, 0), viewMatrix);
                glassLikeShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                crackMesh.render();
            }
        }

        //glassLikeShaderProgram is always reached
        glassLikeShaderProgram.unbind();

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
        if (graphicsMode) {
            glEnable(GL_CULL_FACE);
        }

        //finished with standard shader
        shaderProgram.unbind();

        glassLikeShaderProgram.bind();

        if (graphicsMode) {
            glDisable(GL_BLEND);
        }

        //BEGIN HUD (3d parts)

        glClear(GL_DEPTH_BUFFER_BIT);

        projectionMatrix = getProjectionMatrix(FOV, getWindowWidth(), getWindowHeight(), Z_NEAR, 100);
        shaderProgram.setUniform("projectionMatrix", projectionMatrix);

        //draw wield hand or item
        if (getCameraPerspective() == 0) {
            //wield hand
            if (getItemInInventorySlot(getPlayerInventorySelection(),0) == null){
                modelViewMatrix = getGenericMatrixWithPosRotationScale(getWieldHandAnimationPos(), getWieldHandAnimationRot(), new Vector3d(5d, 5d, 5d), new Matrix4d());
                hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                getWieldHandMesh().render();
            //block/item
            } else if (getWieldInventory() != null){
                modelViewMatrix = getGenericMatrixWithPosRotationScale(getWieldHandAnimationPos(), getWieldHandAnimationRot(), new Vector3d(20d, 20d, 20d), new Matrix4d());
                hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                getWieldInventory().mesh.render();
            }
        }

        //finished with 3d
        glassLikeShaderProgram.unbind();

        glEnable(GL_BLEND);

        //BEGIN HUD 2D
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

        //render inverted crosshair
        if (getCameraPerspective() == 0){
            glBlendFunc(GL_ONE_MINUS_DST_COLOR, GL_ONE_MINUS_SRC_COLOR);
            modelViewMatrix = buildOrthoProjModelMatrix(new Vector3d(0,0,0),new Vector3f(0,0,0), new Vector3d(windowScale/20f,windowScale/20f,windowScale/20f));
            hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
            getCrossHairMesh().render();
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        }

        glClear(GL_DEPTH_BUFFER_BIT);

        if (!isPaused()) {

            if (isPlayerInventoryOpen()) {

                //inventory backdrop
                {
                    modelViewMatrix = buildOrthoProjModelMatrix(new Vector3d(0, 0, 0), new Vector3f(0, 0, 0), new Vector3d(windowScale, windowScale, windowScale));
                    hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                    getInventoryBackdropMesh().render();
                }

                glClear(GL_DEPTH_BUFFER_BIT);

                //player inside box
                {
                    modelViewMatrix = buildOrthoProjModelMatrix(new Vector3d(-(windowScale / 3.75d), (windowScale / 2.8d), 0), getPlayerHudRotation(), new Vector3d((windowScale / 18d), (windowScale / 18d), (windowScale / 18d)));
                    hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                    getPlayerMesh().render();
                }

                glClear(GL_DEPTH_BUFFER_BIT);

                //inventory foreground
                {
                    modelViewMatrix = buildOrthoProjModelMatrix(new Vector3d(0, 0, 0), new Vector3f(0, 0, 0), new Vector3d(windowScale, windowScale, windowScale));
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
                    Vector2d mousePos = new Vector2d(getMousePos());

                    //work from the center
                    mousePos.x -= (getWindowSize().x / 2f);
                    mousePos.y -= (getWindowSize().y / 2f);
                    mousePos.y *= -1f;
                    if (getItemDefinition(getMouseInventory().name).isItem) {
                        modelViewMatrix = buildOrthoProjModelMatrix(new Vector3d((float) mousePos.x, (float) mousePos.y - (windowScale / 27d), 0), new Vector3f(0, 0, 0), new Vector3d(windowScale / 5d, windowScale / 5d, windowScale / 5d));
                    } else {
                        modelViewMatrix = buildOrthoProjModelMatrix(new Vector3d((float) mousePos.x, (float) mousePos.y - (windowScale / 55d), 0), new Vector3f(45, 45, 0), new Vector3d(windowScale / 8d, windowScale / 8d, windowScale / 8d));
                    }

                    hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);

                    getMouseInventory().mesh.render();

                    glClear(GL_DEPTH_BUFFER_BIT);

                    //stack numbers
                    if(getMouseInventory().stack > 1) {
                        glClear(GL_DEPTH_BUFFER_BIT);
                        modelViewMatrix = buildOrthoProjModelMatrix(new Vector3d( mousePos.x + (windowScale/47d),  mousePos.y - (windowScale / 35f), 0), new Vector3f(0, 0, 0), new Vector3d(windowScale / 48, windowScale / 48, windowScale / 48));
                        hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                        workerMesh = createTextCenteredWithShadow(Integer.toString(getMouseInventory().stack), 1f, 1f, 1f);
                        workerMesh.render();
                        workerMesh.cleanUp(false);
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

                        modelViewMatrix = buildOrthoProjModelMatrix(new Vector3d(-windowScale / 2.105f + (i * (windowScale / 19.5d)), (-windowSize.y / 2d) + (windowScale / 6.8d) + jiggle, 0), new Vector3f(0, 0, 0), new Vector3d((windowScale / 20f), (windowScale / 20f), (windowScale / 20f)));
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
                    modelViewMatrix = buildOrthoProjModelMatrix(new Vector3d(0,  (-windowSize.y / 2d) + (windowScale / 16.5d), 0), new Vector3f(0, 0, 0), new Vector3d((windowScale), (windowScale), (windowScale)));
                    hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                    getHotBarMesh().render();
                }

                glClear(GL_DEPTH_BUFFER_BIT);

                //selection bar (in the hotbar)
                {
                    modelViewMatrix = buildOrthoProjModelMatrix(new Vector3d((getPlayerInventorySelection() - 4) * (windowScale / 9.1d),  (-windowSize.y / 2f) + ((windowScale / 8.25f) / 2f), 0), new Vector3f(0, 0, 0), new Vector3d(windowScale / 8.25f, windowScale / 8.25f, windowScale / 8.25f));
                    hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                    getHotBarSelectionMesh().render();
                }

                //THESE GO LAST!

                glClear(GL_DEPTH_BUFFER_BIT);

                //version info
                {
                    modelViewMatrix = buildOrthoProjModelMatrix(new Vector3d(-windowSize.x / 2d, (windowSize.y / 2.1d), 0), new Vector3f(0, 0, 0), new Vector3d(windowScale / 30d, windowScale / 30d, windowScale / 30d));
                    hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                    getVersionInfoText().render();
                }

                if(getDebugInfo()) {

                    //x info
                    {

                        glClear(GL_DEPTH_BUFFER_BIT);
                        modelViewMatrix = buildOrthoProjModelMatrix(new Vector3d((-windowSize.x / 2d), (windowSize.y / 2.3d), 0), new Vector3f(0, 0, 0), new Vector3d(windowScale / 30d, windowScale / 30d, windowScale / 30d));
                        hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                        workerMesh = createTextWithShadow("X:" + getPlayerPos().x, 1f, 1f, 1f);
                        workerMesh.render();
                        workerMesh.cleanUp(false);
                    }

                    //y info
                    {

                        glClear(GL_DEPTH_BUFFER_BIT);
                        modelViewMatrix = buildOrthoProjModelMatrix(new Vector3d((-windowSize.x / 2d), (windowSize.y / 2.6d), 0), new Vector3f(0, 0, 0), new Vector3d(windowScale / 30d, windowScale / 30d, windowScale / 30d));
                        hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                        workerMesh = createTextWithShadow("Y:" + getPlayerPos().y, 1f, 1f, 1f);
                        workerMesh.render();
                        workerMesh.cleanUp(false);
                    }

                    //z info
                    {
                        glClear(GL_DEPTH_BUFFER_BIT);
                        modelViewMatrix = buildOrthoProjModelMatrix(new Vector3d((-windowSize.x / 2d), (float) (windowSize.y / 3d), 0), new Vector3f(0, 0, 0), new Vector3d(windowScale / 30d, windowScale / 30d, windowScale / 30d));
                        hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                        workerMesh = createTextWithShadow("Z:" + getPlayerPos().z, 1f, 1f, 1f);
                        workerMesh.render();
                        workerMesh.cleanUp(false);
                    }


                    Mesh[] runtimeInfo = getRuntimeInfoText();
                    for (int i = 0; i < runtimeInfo.length; i++){
                        if (runtimeInfo[i] != null){
                            glClear(GL_DEPTH_BUFFER_BIT);
                            modelViewMatrix = buildOrthoProjModelMatrix(new Vector3d((-windowSize.x / 2d), (float) (windowSize.y / 3d) + ((-i - 1) * (windowSize.y/20d)), 0), new Vector3f(0, 0, 0), new Vector3d(windowScale / 30d, windowScale / 30d, windowScale / 30d));
                            hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                            runtimeInfo[i].render();
                        }
                    }


                    //render fps
                    {
                        glClear(GL_DEPTH_BUFFER_BIT);
                        modelViewMatrix = buildOrthoProjModelMatrix(new Vector3d((-windowSize.x / 2d), (windowSize.y / 3d + (-7 * (windowSize.y/20d))), 0), new Vector3f(0, 0, 0), new Vector3d(windowScale / 30d, windowScale / 30d, windowScale / 30d));
                        hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                        workerMesh = getFPSMesh();
                        workerMesh.render();
                    }
                } else {
                    //only show FPS
                    {
                        glClear(GL_DEPTH_BUFFER_BIT);
                        modelViewMatrix = buildOrthoProjModelMatrix(new Vector3d((-windowSize.x / 2d), (windowSize.y / 2.3d), 0), new Vector3f(0, 0, 0), new Vector3d(windowScale / 30d, windowScale / 30d, windowScale / 30d));
                        hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                        workerMesh = getFPSMesh();
                        workerMesh.render();
                    }
                }

                //render items in hotbar
                for (int x = 1; x <= 9; x++) {

                    Item thisItem = getItemInInventorySlot(x - 1, 0);
                    if (thisItem != null) {

                        glClear(GL_DEPTH_BUFFER_BIT);

                        if (getItemDefinition(thisItem.name).isItem) {
                            modelViewMatrix = buildOrthoProjModelMatrix(new Vector3d(((x - 5d) * (windowScale / 9.1d)), (-windowSize.y / 2d) + (windowScale / 48d), 0), new Vector3f(0, 0, 0), new Vector3d(windowScale / 5d, windowScale / 5d, windowScale / 5d));
                        } else {
                            modelViewMatrix = buildOrthoProjModelMatrix(new Vector3d(((x - 5d) * (windowScale / 9.1d)), (-windowSize.y / 2d) + (windowScale / 24d), 0), new Vector3f(45, 45, 0), new Vector3d(windowScale / 8.01d, windowScale / 8.01d, windowScale / 8.01d));
                        }
                        hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                        getItemInInventorySlot(x - 1, 0).mesh.render();


                        glClear(GL_DEPTH_BUFFER_BIT);

                        //render hotbar counts if greater than 1

                        if (getItemInInventorySlot(x - 1, 0).stack > 1) {
                            modelViewMatrix = buildOrthoProjModelMatrix(new Vector3d(((x - 4.8d) * (windowScale / 9.1d)),  (-windowSize.y / 2d) + (windowScale / 32d), 0), new Vector3f(0, 0, 0), new Vector3d(windowScale / 48, windowScale / 48, windowScale / 48));
                            hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                            workerMesh = createTextCenteredWithShadow(Integer.toString(getItemInInventorySlot(x - 1, 0).stack), 1f, 1f, 1f);
                            workerMesh.render();
                            workerMesh.cleanUp(false);
                        }
                    }
                }


            }
        } else {
            //render inventory base
            {
                modelViewMatrix = buildOrthoProjModelMatrix(new Vector3d(0, 0, 0), new Vector3f(0, 0, 0), new Vector3d(windowSize.x, windowSize.y, windowScale));
                hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                getMenuBgMesh().render();
            }

            glClear(GL_DEPTH_BUFFER_BIT);

            renderGameGUI();
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

        Vector2d offset = new Vector2d((double)inventory.getSize().x/2d,(double)inventory.getSize().y/2d);

        Matrix4d modelViewMatrix;
        workerMesh = getInventorySlotMesh();

        double yProgram;
        if (inventory.isMainInventory()) {
            for (int x = 0; x < inventory.getSize().x; x++) {
                for (int y = 0; y < inventory.getSize().y; y++) {

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

                    modelViewMatrix = buildOrthoProjModelMatrix(new Vector3d(((double) x + 0.5d - offset.x + startingPoint.x) * (scale + spacing), ((y * -1d) - 0.5d + startingPoint.y + offset.y + yProgram) * (scale + spacing), 0), new Vector3f(0, 0, 0), new Vector3d(scale, scale, scale));
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
                            modelViewMatrix = buildOrthoProjModelMatrix(new Vector3d(((double) x + 0.5d - offset.x + startingPoint.x) * (scale + spacing), ((y * -1d) - 0.5d + startingPoint.y + offset.y + yProgram) * (scale + spacing) - (blockScale / 3.25d), 0), new Vector3f(0, 0, 0), new Vector3d(itemScale, itemScale, itemScale));
                        } else {
                            modelViewMatrix = buildOrthoProjModelMatrix(new Vector3d(((double) x + 0.5d - offset.x + startingPoint.x) * (scale + spacing), ((y * -1d) - 0.5d + startingPoint.y + offset.y + yProgram) * (scale + spacing) - (blockScale / 7d), 0), new Vector3f(45, 45, 0), new Vector3d(blockScale, blockScale, blockScale));
                        }
                        hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                        thisItem.mesh.render();

                        //render item stack number
                        if (thisItem.stack > 1) {
                            glClear(GL_DEPTH_BUFFER_BIT);
                            modelViewMatrix = buildOrthoProjModelMatrix(new Vector3d(((double) x + 0.7d - offset.x + startingPoint.x) * (scale + spacing), ((y * -1d) - 0.6d + startingPoint.y + offset.y + yProgram) * (scale + spacing) - (blockScale / 7d), 0), new Vector3f(0, 0, 0), new Vector3d(textScale, textScale, textScale));
                            hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                            Mesh itemStackLabel = createTextCenteredWithShadow(Integer.toString(thisItem.stack), 1, 1, 1);
                            itemStackLabel.render();
                            itemStackLabel.cleanUp(false);
                        }
                    }

                }
            }
        } else {
            for (int x = 0; x < inventory.getSize().x; x++) {
                for (int y = 0; y < inventory.getSize().y; y++) {

                    //background of the slot
                    glClear(GL_DEPTH_BUFFER_BIT);

                    modelViewMatrix = buildOrthoProjModelMatrix(new Vector3d(((double) x + 0.5d - offset.x + startingPoint.x) * (scale + spacing), ((y * -1d) - 0.5d + startingPoint.y + offset.y) * (scale + spacing), 0), new Vector3f(0, 0, 0), new Vector3d(scale, scale, scale));
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
                            modelViewMatrix = buildOrthoProjModelMatrix(new Vector3d(((double) x + 0.5d - offset.x + startingPoint.x) * (scale + spacing), ((y * -1d) - 0.5d + startingPoint.y + offset.y) * (scale + spacing) - (blockScale / 3.25d), 0), new Vector3f(0, 0, 0), new Vector3d(itemScale, itemScale, itemScale));
                        } else {
                            modelViewMatrix = buildOrthoProjModelMatrix(new Vector3d(((double) x + 0.5d - offset.x + startingPoint.x) * (scale + spacing), ((y * -1d) - 0.5d + startingPoint.y + offset.y) * (scale + spacing) - (blockScale / 7d), 0), new Vector3f(45, 45, 0), new Vector3d(blockScale, blockScale, blockScale));
                        }
                        hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                        thisItem.mesh.render();

                        //render item stack number
                        if (thisItem.stack > 1) {
                            glClear(GL_DEPTH_BUFFER_BIT);
                            modelViewMatrix = buildOrthoProjModelMatrix(new Vector3d(((double) x + 0.7d - offset.x + startingPoint.x) * (scale + spacing), ((y * -1d) - 0.6d + startingPoint.y + offset.y) * (scale + spacing) - (blockScale / 7d), 0), new Vector3f(0, 0, 0), new Vector3d(textScale, textScale, textScale));
                            hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                            Mesh itemStackLabel = createTextCenteredWithShadow(Integer.toString(thisItem.stack), 1, 1, 1);
                            itemStackLabel.render();
                            itemStackLabel.cleanUp(false);
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


            Matrix4d modelViewMatrix = buildOrthoProjModelMatrix(new Vector3d(xPos, yPos, 0), new Vector3f(0, 0, 0), new Vector3d(windowScale / 20d, windowScale / 20d, windowScale / 20d));
            hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
            thisButton.textMesh.render();

            float xAdder = 20 / thisButton.buttonScale.x;
            float yAdder = 20 / thisButton.buttonScale.y;

            modelViewMatrix = buildOrthoProjModelMatrix(new Vector3d(xPos, yPos, 0), new Vector3f(0, 0, 0), new Vector3d(windowScale / xAdder, windowScale / yAdder, windowScale / 20d));
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
    }

    private static double getChunkDistanceFromPlayer(int x, int z){
        Vector3i currentChunk = getPlayerCurrentChunk();
        return Math.max(getDistance(0,0,currentChunk.z, 0, 0, z), getDistance(currentChunk.x,0,0, x, 0, 0));
    }
}
