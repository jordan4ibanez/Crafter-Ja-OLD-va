package engine;

import engine.graph.*;
import game.chunk.ChunkObject;
import game.falling.FallingEntityObject;
import game.item.Item;
import game.mob.MobObject;
import game.particle.ParticleObject;
import game.weather.RainDropEntity;
import org.joml.Matrix4f;
import org.joml.Vector2d;
import org.joml.Vector3f;

import java.util.Collection;

import static engine.MouseInput.getMousePos;
import static engine.graph.Camera.*;
import static engine.graph.Transformation.*;
import static engine.graph.Transformation.buildOrthoProjModelMatrix;
import static engine.Hud.*;
import static game.falling.FallingEntity.getFallingEntities;
import static game.item.ItemDefinition.getItemDefinition;
import static game.item.ItemEntity.*;
import static game.mob.Mob.getAllMobs;
import static game.mob.Mob.getMobDefinition;
import static game.particle.Particle.getAllParticles;
import static game.tnt.TNTEntity.*;
import static engine.Window.*;
import static game.chunk.Chunk.*;
import static game.player.Inventory.*;
import static game.player.Player.*;
import static game.weather.Weather.getRainDropMesh;
import static game.weather.Weather.getRainDrops;
import static org.lwjgl.opengl.GL11.*;

public class Renderer {

    private static float FOV = (float) Math.toRadians(72.0f); //todo: make this a calculator method ala calculateFOV(float);

    private static float HUD_FOV = (float)Math.toRadians(60f);

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
        Matrix4f projectionMatrix = getProjectionMatrix(FOV, getWindowWidth(), getWindowHeight(), Z_NEAR, Z_FAR);
        shaderProgram.setUniform("projectionMatrix", projectionMatrix);

        //update the view matrix
        Matrix4f viewMatrix = getViewMatrix();

        shaderProgram.setUniform("texture_sampler", 0);

        Matrix4f modelViewMatrix;

        //render each chunk (standard blocks)
        for (ChunkObject thisChunk : getMap()){
            if (thisChunk == null){
                continue;
            }
            if (thisChunk.mesh == null){
                continue;
            }
            for (Mesh thisMesh : thisChunk.mesh){
                if (thisMesh != null){
                    modelViewMatrix = getModelViewMatrix(viewMatrix);
                    shaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                    thisMesh.render();
                }
            }
        }

        //render each chunk liquid mesh
        for (ChunkObject thisChunk : getMap()){
            if (thisChunk == null){
                continue;
            }
            if (thisChunk.liquidMesh == null){
                continue;
            }
            for (Mesh thisMesh : thisChunk.liquidMesh){
                if (thisMesh != null){
                    modelViewMatrix = getModelViewMatrix(viewMatrix);
                    shaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                    thisMesh.render();
                }
            }
        }

        //render each blockbox mesh
        for (ChunkObject thisChunk : getMap()){
            if (thisChunk == null){
                continue;
            }
            if (thisChunk.blockBoxMesh == null){
                continue;
            }
            for (Mesh thisMesh : thisChunk.blockBoxMesh){
                if (thisMesh != null){
                    modelViewMatrix = getModelViewMatrix(viewMatrix);
                    shaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                    thisMesh.render();
                }
            }
        }

//        render each item entity
        for (Item thisItem : getAllItems()){
            modelViewMatrix = updateModelViewMatrix(new Vector3f(thisItem.pos).add(0,thisItem.hover,0), thisItem.rotation, viewMatrix);
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
            modelViewMatrix = getGenericMatrixWithPosRotationScale(thisObject.pos, new Vector3f(0,0,0), new Vector3f(2.5f,2.5f,2.5f), viewMatrix);
            shaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
            thisObject.mesh.render();
        }


        //render mobs
        for (MobObject thisMob : getAllMobs()){
            int offsetIndex = 0;
            for (Mesh thisMesh : thisMob.meshes) {
                modelViewMatrix = getMobMatrix(new Vector3f(thisMob.pos), thisMob.bodyOffsets[offsetIndex], new Vector3f(0, thisMob.smoothRotation, 0), new Vector3f(thisMob.bodyRotations[offsetIndex]), new Vector3f(1f, 1f, 1f), viewMatrix);
                shaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                thisMesh.render();
                offsetIndex++;
            }
        }


        //render world selection mesh
        if (getPlayerWorldSelectionPos() != null){

            Mesh selectionMesh = getWorldSelectionMesh();
            modelViewMatrix = updateModelViewMatrix(getPlayerWorldSelectionPos(), new Vector3f(0,0,0), viewMatrix);
            shaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
            selectionMesh.render();

            if (getDiggingFrame() >= 0) {
                Mesh crackMesh = getMiningCrackMesh();
                modelViewMatrix = updateModelViewMatrix(getPlayerWorldSelectionPos(), new Vector3f(0, 0, 0), viewMatrix);
                shaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                crackMesh.render();
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
        Mesh rainDrop = getRainDropMesh();
        for (RainDropEntity thisRainDrop : getRainDrops()){
            modelViewMatrix = updateParticleViewMatrix(thisRainDrop.pos, new Vector3f(0,getCameraRotation().y,0), viewMatrix);
            shaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
            rainDrop.render();
        }


        glClear(GL_DEPTH_BUFFER_BIT);

        //draw wield hand or item
        {
            if (getItemInInventorySlot(getPlayerInventorySelection(),0) == null){
                Mesh thisMesh = getWieldHandMesh();
                modelViewMatrix = getGenericMatrixWithPosRotationScale(getWieldHandAnimationPos(), getWieldHandAnimationRot(), new Vector3f(5f, 5f, 5f), new Matrix4f());
                hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                thisMesh.render();
            } else {

                Mesh thisMesh = getItemInInventorySlot(getPlayerInventorySelection(),0).definition.mesh;
                modelViewMatrix = getGenericMatrixWithPosRotationScale(getWieldHandAnimationPos(), getWieldHandAnimationRot(), new Vector3f(20f, 20f, 20f), new Matrix4f());
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

        {
            //render inverted crosshair
            glBlendFunc(GL_ONE_MINUS_DST_COLOR, GL_ONE_MINUS_SRC_COLOR);
            modelViewMatrix = buildOrthoProjModelMatrix(new Vector3f(0,0,0),new Vector3f(0,0,0), new Vector3f(windowScale/20f,windowScale/20f,windowScale/20f));
            hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
            getCrossHairMesh().render();
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        }


        glClear(GL_DEPTH_BUFFER_BIT);

        if (!isPaused()) {
            if (isPlayerInventoryOpen()) {
                {
                    modelViewMatrix = buildOrthoProjModelMatrix(new Vector3f(0, 0, 0), new Vector3f(0, 0, 0), new Vector3f(windowScale, windowScale, windowScale));
                    hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                    getInventoryMesh().render();
                }

                glClear(GL_DEPTH_BUFFER_BIT);

                {
                    modelViewMatrix = buildOrthoProjModelMatrix(new Vector3f(-(windowScale / 3.75f), (windowScale / 2.8f), 0), getPlayerHudRotation(), new Vector3f((windowScale / 18f), (windowScale / 18f), (windowScale / 18f)));
                    hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                    getPlayerMesh().render();
                }

                glClear(GL_DEPTH_BUFFER_BIT);

                {
                    //render the actual inventory
                    for (int x = 1; x <= 9; x++) {
                        for (int y = -2; y > -5; y--) {
                            modelViewMatrix = buildOrthoProjModelMatrix(new Vector3f((x - 5) * (windowScale / 9.5f), (y + 0.3f) * (windowScale / 9.5f), 0), new Vector3f(0, 0, 0), new Vector3f(windowScale / 10.5f, windowScale / 10.5f, windowScale / 10.5f));
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
                        modelViewMatrix = buildOrthoProjModelMatrix(new Vector3f((x - 5) * (windowScale / 9.5f), -0.5f * (windowScale / 9.5f), 0), new Vector3f(0, 0, 0), new Vector3f(windowScale / 10.5f, windowScale / 10.5f, windowScale / 10.5f));
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
                                modelViewMatrix = buildOrthoProjModelMatrix(new Vector3f((x - 5) * (windowScale / 9.5f), ((y + 0.3f) * (windowScale / 9.5f)) - (windowScale / 55f), 0), new Vector3f(45, 45 + itemRotation, 0), new Vector3f(windowScale / 8f, windowScale / 8f, windowScale / 8f));
                            } else {
                                modelViewMatrix = buildOrthoProjModelMatrix(new Vector3f((x - 5) * (windowScale / 9.5f), ((y + 0.3f) * (windowScale / 9.5f)) - (windowScale / 55f), 0), new Vector3f(45, 45, 0), new Vector3f(windowScale / 8f, windowScale / 8f, windowScale / 8f));
                            }
                            hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                            getItemInInventorySlot(x - 1, ((y * -1) - 1)).definition.mesh.render();

                            //draw stack numbers
                            glClear(GL_DEPTH_BUFFER_BIT);
                            modelViewMatrix = buildOrthoProjModelMatrix(new Vector3f((x - 4.98f) * (windowScale / 9.5f), ((y + 0.28f) * (windowScale / 9.5f)) - (windowScale / 55f), 0), new Vector3f(0, 0, 0), new Vector3f(windowScale / 48, windowScale / 48, windowScale / 48));
                            hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                            workerMesh = createCustomHudText(Integer.toString(getItemInInventorySlot(x - 1, ((y * -1) - 1)).stack), 0f, 0f, 0f);
                            workerMesh.render();
                            workerMesh.cleanUp(false);

                            glClear(GL_DEPTH_BUFFER_BIT);
                            modelViewMatrix = buildOrthoProjModelMatrix(new Vector3f((x - 5) * (windowScale / 9.5f), ((y + 0.3f) * (windowScale / 9.5f)) - (windowScale / 55f), 0), new Vector3f(0, 0, 0), new Vector3f(windowScale / 48, windowScale / 48, windowScale / 48));
                            hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                            workerMesh = createCustomHudText(Integer.toString(getItemInInventorySlot(x - 1, ((y * -1) - 1)).stack), 1f, 1f, 1f);
                            workerMesh.render();
                            workerMesh.cleanUp(false);

                        }
                    }
                }

                //render items in inventory hotbar (upper part)

                for (int x = 1; x <= 9; x++) {
                    if (getItemInInventorySlot(x - 1, 0) != null) {
                        glClear(GL_DEPTH_BUFFER_BIT);
                        if (getInvSelection() != null && (x - 1) == getInvSelection()[0] && 0 == getInvSelection()[1]) {
                            itemSelected = true;
                            modelViewMatrix = buildOrthoProjModelMatrix(new Vector3f((x - 5) * (windowScale / 9.5f), (-0.5f * (windowScale / 9.5f)) - (windowScale / 55f), 0), new Vector3f(45, 45 + itemRotation, 0), new Vector3f(windowScale / 8f, windowScale / 8f, windowScale / 8f));
                        } else {
                            modelViewMatrix = buildOrthoProjModelMatrix(new Vector3f((x - 5) * (windowScale / 9.5f), (-0.5f * (windowScale / 9.5f)) - (windowScale / 55f), 0), new Vector3f(45, 45, 0), new Vector3f(windowScale / 8f, windowScale / 8f, windowScale / 8f));
                        }

                        hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                        getItemInInventorySlot(x - 1, 0).definition.mesh.render();

                        glClear(GL_DEPTH_BUFFER_BIT);

                        modelViewMatrix = buildOrthoProjModelMatrix(new Vector3f((x - 4.98f) * (windowScale / 9.5f), ((-0.52f) * (windowScale / 9.5f)) - (windowScale / 55f), 0), new Vector3f(0, 0, 0), new Vector3f(windowScale / 48, windowScale / 48, windowScale / 48));
                        hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                        workerMesh = createCustomHudText(Integer.toString(getItemInInventorySlot(x - 1, 0).stack), 0f, 0f, 0f);
                        workerMesh.render();
                        workerMesh.cleanUp(false);

                        glClear(GL_DEPTH_BUFFER_BIT);
                        modelViewMatrix = buildOrthoProjModelMatrix(new Vector3f((x - 5) * (windowScale / 9.5f), ((-0.5f) * (windowScale / 9.5f)) - (windowScale / 55f), 0), new Vector3f(0, 0, 0), new Vector3f(windowScale / 48, windowScale / 48, windowScale / 48));
                        hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                        workerMesh = createCustomHudText(Integer.toString(getItemInInventorySlot(x - 1, 0).stack), 1f, 1f, 1f);
                        workerMesh.render();
                        workerMesh.cleanUp(false);
                    }
                }
                if (!itemSelected) {
                    itemRotation = 0f;
                } else {
                    itemRotation += 1f;
                }


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

                        modelViewMatrix = buildOrthoProjModelMatrix(new Vector3f((float) mousePos.x, (float) mousePos.y - (windowScale / 55f), 0), new Vector3f(45, 45, 0), new Vector3f(windowScale / 8f, windowScale / 8f, windowScale / 8f));
                        hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                        getItemDefinition(getMouseInventory().name).mesh.render();

                        glClear(GL_DEPTH_BUFFER_BIT);

                        modelViewMatrix = buildOrthoProjModelMatrix(new Vector3f((float) mousePos.x + (windowScale/400f), (float) mousePos.y - (windowScale / 55f) - (windowScale/400f), 0), new Vector3f(0, 0, 0), new Vector3f(windowScale / 48, windowScale / 48, windowScale / 48));
                        hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                        workerMesh = createCustomHudText(Integer.toString(getMouseInventory().stack), 0f, 0f, 0f);
                        workerMesh.render();
                        workerMesh.cleanUp(false);

                        glClear(GL_DEPTH_BUFFER_BIT);
                        modelViewMatrix = buildOrthoProjModelMatrix(new Vector3f((float) mousePos.x, (float) mousePos.y - (windowScale / 55f), 0), new Vector3f(0, 0, 0), new Vector3f(windowScale / 48, windowScale / 48, windowScale / 48));
                        hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                        workerMesh = createCustomHudText(Integer.toString(getMouseInventory().stack), 1f, 1f, 1f);
                        workerMesh.render();
                        workerMesh.cleanUp(false);
                    }
                }
            } else {
                {
                    modelViewMatrix = buildOrthoProjModelMatrix(new Vector3f(0, (float) (-windowSize.y / 2f) + (windowScale / 16.5f), 0), new Vector3f(0, 0, 0), new Vector3f((windowScale), (windowScale), (windowScale)));
                    hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                    getHotBarMesh().render();
                }

                glClear(GL_DEPTH_BUFFER_BIT);

                {
                    modelViewMatrix = buildOrthoProjModelMatrix(new Vector3f((getPlayerInventorySelection() - 4) * (windowScale / 9.1f), (float) (-windowSize.y / 2f) + ((windowScale / 8.25f) / 2f), 0), new Vector3f(0, 0, 0), new Vector3f(windowScale / 8.25f, windowScale / 8.25f, windowScale / 8.25f));
                    hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                    getSelectionMesh().render();
                }

                //THESE GO LAST!
                {
                    modelViewMatrix = buildOrthoProjModelMatrix(new Vector3f((float) ((-windowSize.x / 2f) + (windowSize.x / 600f)), (float) ((windowSize.y / 2f) - (windowSize.y / 600f)), 0), new Vector3f(0, 0, 0), new Vector3f(windowScale / 30f, windowScale / 30f, windowScale / 30f));
                    hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                    getVersionInfoTextShadow().render();
                }

                glClear(GL_DEPTH_BUFFER_BIT);

                {
                    modelViewMatrix = buildOrthoProjModelMatrix(new Vector3f((float) -windowSize.x / 2f, (float) (windowSize.y / 2f), 0), new Vector3f(0, 0, 0), new Vector3f(windowScale / 30f, windowScale / 30f, windowScale / 30f));
                    hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                    getVersionInfoText().render();
                }



                //x info
                {
                    glClear(GL_DEPTH_BUFFER_BIT);

                    modelViewMatrix = buildOrthoProjModelMatrix(new Vector3f((float) ((-windowSize.x / 2f) + (windowSize.x / 600f)), (float) ((windowSize.y / 2.2f) - (windowSize.y / 600f)), 0), new Vector3f(0, 0, 0), new Vector3f(windowScale / 30f, windowScale / 30f, windowScale / 30f));
                    hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                    workerMesh = createCustomHudText("X:" + getPlayerPos().x, 0f, 0f, 0f);
                    workerMesh.render();
                    workerMesh.cleanUp(false);

                    glClear(GL_DEPTH_BUFFER_BIT);
                    modelViewMatrix = buildOrthoProjModelMatrix(new Vector3f((float)(-windowSize.x / 2f), (float) (windowSize.y / 2.2f), 0), new Vector3f(0, 0, 0), new Vector3f(windowScale / 30f, windowScale / 30f, windowScale / 30f));
                    hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                    workerMesh = createCustomHudText("X:" + getPlayerPos().x, 1f, 1f, 1f);
                    workerMesh.render();
                    workerMesh.cleanUp(false);
                    workerMesh = null;
                }

                //y info
                {
                    glClear(GL_DEPTH_BUFFER_BIT);
                    modelViewMatrix = buildOrthoProjModelMatrix(new Vector3f((float) ((-windowSize.x / 2f) + (windowSize.x / 600f)), (float) ((windowSize.y / 2.5f) - (windowSize.y / 600f)), 0), new Vector3f(0, 0, 0), new Vector3f(windowScale / 30f, windowScale / 30f, windowScale / 30f));
                    hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                    workerMesh = createCustomHudText("Y:" + getPlayerPos().y, 0f, 0f, 0f);
                    workerMesh.render();
                    workerMesh.cleanUp(false);

                    glClear(GL_DEPTH_BUFFER_BIT);
                    modelViewMatrix = buildOrthoProjModelMatrix(new Vector3f((float)(-windowSize.x / 2f), (float) (windowSize.y / 2.5f), 0), new Vector3f(0, 0, 0), new Vector3f(windowScale / 30f, windowScale / 30f, windowScale / 30f));
                    hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                    workerMesh = createCustomHudText("Y:" + getPlayerPos().y, 1f, 1f, 1f);
                    workerMesh.render();
                    workerMesh.cleanUp(false);
                    workerMesh = null;
                }

                //z info
                {
                    glClear(GL_DEPTH_BUFFER_BIT);
                    modelViewMatrix = buildOrthoProjModelMatrix(new Vector3f((float) ((-windowSize.x / 2f) + (windowSize.x / 600f)), (float) ((windowSize.y / 2.9f) - (windowSize.y / 600f)), 0), new Vector3f(0, 0, 0), new Vector3f(windowScale / 30f, windowScale / 30f, windowScale / 30f));
                    hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                    workerMesh = createCustomHudText("Z:" + getPlayerPos().z, 0f, 0f, 0f);
                    workerMesh.render();
                    workerMesh.cleanUp(false);

                    glClear(GL_DEPTH_BUFFER_BIT);
                    modelViewMatrix = buildOrthoProjModelMatrix(new Vector3f((float)(-windowSize.x / 2f), (float) (windowSize.y / 2.9f), 0), new Vector3f(0, 0, 0), new Vector3f(windowScale / 30f, windowScale / 30f, windowScale / 30f));
                    hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                    workerMesh = createCustomHudText("Z:" + getPlayerPos().z, 1f, 1f, 1f);
                    workerMesh.render();
                    workerMesh.cleanUp(false);
                    workerMesh = null;
                }

                //render items in hotbar
                for (int x = 1; x <= 9; x++) {

                    if (getItemInInventorySlot(x - 1, 0) != null) {

                        glClear(GL_DEPTH_BUFFER_BIT);

                        modelViewMatrix = buildOrthoProjModelMatrix(new Vector3f(((x - 5f) * (windowScale / 9.1f)), (float) (-windowSize.y / 2f) + (windowScale / 24f), 0), new Vector3f(45, 45, 0), new Vector3f(windowScale / 8f, windowScale / 8f, windowScale / 8f));
                        hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                        getItemInInventorySlot(x - 1, 0).definition.mesh.render();


                        glClear(GL_DEPTH_BUFFER_BIT);

//                        render hotbar counts
                        modelViewMatrix = buildOrthoProjModelMatrix(new Vector3f(((x - 4.98f) * (windowScale / 9.1f)), (float) (-windowSize.y / 2f) + (windowScale / 26f), 0), new Vector3f(0, 0, 0), new Vector3f(windowScale / 48, windowScale / 48, windowScale / 48));
                        hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                        workerMesh = createCustomHudText(Integer.toString(getItemInInventorySlot(x - 1, 0).stack), 0f, 0f, 0f);
                        workerMesh.render();
                        workerMesh.cleanUp(false);

                        glClear(GL_DEPTH_BUFFER_BIT);
                        modelViewMatrix = buildOrthoProjModelMatrix(new Vector3f(((x - 5f) * (windowScale / 9.1f)), (float) (-windowSize.y / 2f) + (windowScale / 24f), 0), new Vector3f(0, 0, 0), new Vector3f(windowScale / 48, windowScale / 48, windowScale / 48));
                        hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                        workerMesh = createCustomHudText(Integer.toString(getItemInInventorySlot(x - 1, 0).stack), 1f, 1f, 1f);
                        workerMesh.render();
                        workerMesh.cleanUp(false);
                    }
                }


            }
        } else {
            {
                modelViewMatrix = buildOrthoProjModelMatrix(new Vector3f(0, 0, 0), new Vector3f(0, 0, 0), new Vector3f((float)windowSize.x, (float)windowSize.y, windowScale));
                hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                getMenuBgMesh().render();
            }

            glClear(GL_DEPTH_BUFFER_BIT);

            {
                for (int y = 0; y > -3; y --){
                    modelViewMatrix = buildOrthoProjModelMatrix(new Vector3f(0, (y+1) * windowScale/3f, 0), new Vector3f(0, 0, 0), new Vector3f(windowScale/2f, windowScale/2f, windowScale/2f));
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
                modelViewMatrix = buildOrthoProjModelMatrix(new Vector3f(-windowScale / 5.25f, (1) * windowScale / 2.8f, 0), new Vector3f(0, 0, 0), new Vector3f(windowScale / 20f, windowScale / 20f, windowScale / 20f));
                hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                getContinueMesh().render();
            }
            glClear(GL_DEPTH_BUFFER_BIT);

            {
                modelViewMatrix = buildOrthoProjModelMatrix(new Vector3f(-windowScale / 4.5f, windowScale / 50f, 0), new Vector3f(0, 0, 0), new Vector3f(windowScale / 20f, windowScale / 20f, windowScale / 20f));
                hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                getToggleVsyncMesh().render();
            }

            glClear(GL_DEPTH_BUFFER_BIT);

            {
                modelViewMatrix = buildOrthoProjModelMatrix(new Vector3f(-windowScale / 12f, -windowScale / 3.25f, 0), new Vector3f(0, 0, 0), new Vector3f(windowScale / 20f, windowScale / 20f, windowScale / 20f));
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
