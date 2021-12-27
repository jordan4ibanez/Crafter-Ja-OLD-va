package engine.render;

import engine.graphics.Mesh;
import engine.graphics.ShaderProgram;
import engine.gui.GUIObject;

import static engine.time.Time.getDelta;
import static engine.Window.getWindowHeight;
import static engine.Window.getWindowWidth;
import static engine.credits.Credits.getCreditParts;
import static engine.graphics.Transformation.*;
import static engine.gui.GUI.*;
import static engine.render.GameRenderer.*;
import static game.mainMenu.MainMenu.*;
import static game.mainMenu.MainMenuAssets.getTitleBackGroundMeshTile;
import static game.mainMenu.MainMenuAssets.getTitleBlockMesh;
import static org.lwjgl.opengl.GL44.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL44.glClear;

public class MainMenuRenderer {


    private static final float FOV = (float) Math.toRadians(72.0f);
    private static final ShaderProgram shaderProgram = getShaderProgram();
    private static final ShaderProgram hudShaderProgram = getHudShaderProgram();

    public static void renderMainMenu(){

        clearScreen();

        rescaleWindow();

        shaderProgram.bind();

        //keep the camera located in the correct location for the 3D block text
        resetViewMatrix();

        //update projection matrix
        resetProjectionMatrix(FOV, getWindowWidth(), getWindowHeight(), getzNear(), 100f);
        shaderProgram.setUniform("projectionMatrix", getProjectionMatrix());

        shaderProgram.setUniform("texture_sampler", 0);


        boolean onTitleScreen = getMainMenuPage() == 0;
        boolean onWorldsScreen = getMainMenuPage() == 3;

        //get background tile mesh

        //render scrolling background
        //ultra-wide screen compatible, for some reason
        for (int x = -15; x <= 15; x++){
            for (int y = -15; y <= 15; y++) {
                float scale = 5f;
                //these calculations are done to perfectly center the background in front of the camera (hopefully)
                updateViewMatrixWithPosRotationScale(x * scale, (y + getBackGroundScroll()) * scale, -18, 0, 0, 0,scale, scale, 0);
                shaderProgram.setUniform("modelViewMatrix", getModelMatrix());
                getTitleBackGroundMeshTile().render();
            }
        }



        if (onWorldsScreen) {
            byte[][] worldTitleBlocks = getWorldTitleBlocks();
            double[][] worldTitleOffsets = getWorldTitleOffsets();
            glClear(GL_DEPTH_BUFFER_BIT);
            //render title (in blocks)
            for (int x = 0; x < worldTitleBlocks.length; x++) {
                //assume equal lengths
                for (int y = 0; y < worldTitleBlocks[0].length; y++) {
                    if (worldTitleBlocks[x][y] == 1) {
                        //these calculations are done to perfectly center the title in front of the camera (hopefully)
                        updateViewMatrix(y - (27d / 2d), -x + (5d / 2d), -18 + worldTitleOffsets[x][y], 0, 0, 0);
                        shaderProgram.setUniform("modelViewMatrix", getModelMatrix());
                        getTitleBlockMesh().render();
                    }
                }
            }
        } else if (onTitleScreen) {
            byte[][] titleBlocks = getTitleBlocks();
            double[][] titleBlockOffsets = getTitleBlockOffsets();
            glClear(GL_DEPTH_BUFFER_BIT);
            //render title (in blocks)
            for (int x = 0; x < titleBlocks.length; x++) {
                //assume equal lengths
                for (int y = 0; y < titleBlocks[0].length; y++) {
                    if (titleBlocks[x][y] == 1) {
                        //these calculations are done to perfectly center the title in front of the camera (hopefully)
                        updateViewMatrix(y - (27d / 2d), -x + (5d / 2d), -18 + titleBlockOffsets[x][y], 0, 0, 0);
                        shaderProgram.setUniform("modelViewMatrix", getModelMatrix());
                        getTitleBlockMesh().render();
                    }
                }
            }
        }

        //finished with 3d
        shaderProgram.unbind();

        //BEGIN HUD 2D
        glClear(GL_DEPTH_BUFFER_BIT);

        //TODO: BEGIN HUD SHADER PROGRAM!
        hudShaderProgram.bind();
        hudShaderProgram.setUniform("texture_sampler", 0);
        resetOrthoProjectionMatrix(); // needed to get current screen size

        float windowScale = getWindowScale();

        if (onTitleScreen) {
            //title screen gag
            {
                glClear(GL_DEPTH_BUFFER_BIT);

                //process scale of title screen gag text
                float scale = (15f / (float) getTitleScreenGagLength());
                if (scale > 1) {
                    scale = 1;
                }
                scale *= windowScale / 30;

                scale += getTitleBounce();


                glClear(GL_DEPTH_BUFFER_BIT);

                //gray shadow part
                updateOrthoModelMatrix(windowScale / 2.27d, windowScale / 3.27f, 0, 0, 0, 20f, scale, scale, scale);
                hudShaderProgram.setUniform("modelViewMatrix", getOrthoModelMatrix());
                //workerMesh = createTextCentered(getTitleScreenGag(), 0.2f, 0.2f, 0f);
                //workerMesh.render();
                //workerMesh.cleanUp(false);

                glClear(GL_DEPTH_BUFFER_BIT);

                //yellow part
                updateOrthoModelMatrix(windowScale / 2.25d, windowScale / 3.25f, 0, 0, 0, 20f, scale, scale, scale);
                hudShaderProgram.setUniform("modelViewMatrix", getOrthoModelMatrix());
                //workerMesh = createTextCentered(getTitleScreenGag(), 1f, 1f, 0f);
                //workerMesh.render();
                //workerMesh.cleanUp(false);
            }

        }

        if (getMainMenuPage() != 4) {
            renderMainMenuGUI();
        } else {
            Mesh[] creditParts = getCreditParts();
            boolean on = true;
            double trueY = 0;

            float scale = windowScale / 20f;
            float scroll = getCreditsScroll() * (windowScale / 10f);

            for (int y = 0; y < creditParts.length; y++){
                if (creditParts[y] != null) {
                    updateOrthoModelMatrix(0, trueY + scroll, 0, 0, 0, 0, scale, scale, scale);
                    hudShaderProgram.setUniform("modelViewMatrix", getOrthoModelMatrix());
                    creditParts[y].render();


                    if (y <= 2){
                        if (y < 2){
                            trueY -= (windowScale / 3.27f) * 0.5;
                        } else {
                            trueY -= (windowScale / 3.27f) * 1.7;
                        }
                    } else if (y <= 16){

                        if (y == 16) {
                            trueY -= (windowScale / 3.27f) * 3.5;
                        }else {
                            if (on) {
                                trueY -= (windowScale / 3.27f) * 0.5;
                            } else {
                                trueY -= (windowScale / 3.27f) * 1.7;
                            }
                            on = !on;
                        }
                    } else if (y == 17){
                        trueY -= (windowScale / 3.27f) * 0.5;
                    }

                }
            }
        }



        hudShaderProgram.unbind();
    }

    private static void renderMainMenuGUI(){
        ShaderProgram hudShaderProgram = getHudShaderProgram();
        hudShaderProgram.bind();
        float windowScale = getWindowScale();

        for (GUIObject thisGUIObject : getMainMenuGUI()) {
            //button type
            if (thisGUIObject.type == 0) {

                double xPos = thisGUIObject.pos.x * (windowScale / 100d);
                double yPos = thisGUIObject.pos.y * (windowScale / 100d);

                updateOrthoModelMatrix(xPos, yPos, 0, 0, 0, 0, windowScale / 20d, windowScale / 20d, windowScale / 20d);
                hudShaderProgram.setUniform("modelViewMatrix", getOrthoModelMatrix());
                thisGUIObject.textMesh.render();

                float xAdder = 20 / thisGUIObject.buttonScale.x;
                float yAdder = 20 / thisGUIObject.buttonScale.y;

                updateOrthoModelMatrix(xPos, yPos, 0, 0, 0, 0, windowScale / xAdder, windowScale / yAdder, windowScale / 20d);
                hudShaderProgram.setUniform("modelViewMatrix", getOrthoModelMatrix());
                if (thisGUIObject.selected) {
                    getButtonSelectedMesh().render();
                } else {
                    getButtonMesh().render();
                }
            }
            //text input box type
            else if (thisGUIObject.type == 1){

                thisGUIObject.pointerTimer += getDelta();

                //cycles pointer: "_ ..   .. _ ..   .. _"
                if (thisGUIObject.pointerTimer >= 0.5f){
                    thisGUIObject.pointerTimer = -0.5f;
                    thisGUIObject.pointer = ' ';
                    thisGUIObject.updateInputBoxText(thisGUIObject.inputText + " ");
                } else if (thisGUIObject.pointerTimer >= 0 && thisGUIObject.pointer == ' '){
                    thisGUIObject.pointer = '_';
                    thisGUIObject.updateInputBoxText(thisGUIObject.inputText + '_');
                }

                double xPos = thisGUIObject.pos.x * (windowScale / 100d);
                double yPos = thisGUIObject.pos.y * (windowScale / 100d);

                double textOffset = (windowScale / 42d) * thisGUIObject.buttonScale.x;


                updateOrthoModelMatrix(xPos - textOffset, yPos, 0, 0, 0, 0, windowScale / 20d, windowScale / 20d, windowScale / 20d);
                hudShaderProgram.setUniform("modelViewMatrix", getOrthoModelMatrix());
                thisGUIObject.textMesh.render();

                float xAdder = 20 / thisGUIObject.buttonScale.x;
                float yAdder = 20 / thisGUIObject.buttonScale.y;

                updateOrthoModelMatrix(xPos, yPos, 0, 0, 0, 0, windowScale / xAdder, windowScale / yAdder, windowScale / 20d);
                hudShaderProgram.setUniform("modelViewMatrix", getOrthoModelMatrix());
                if (thisGUIObject.selected) {
                    getTextInputSelectedMesh().render();
                } else {
                    getTextInputMesh().render();
                }
            }
            //plain text type
            else if (thisGUIObject.type == 2) {
                double xPos = thisGUIObject.pos.x * (windowScale / 100d);
                double yPos = thisGUIObject.pos.y * (windowScale / 100d);

                updateOrthoModelMatrix(xPos, yPos, 0, 0, 0, 0, windowScale / 20d, windowScale / 20d, windowScale / 20d);
                hudShaderProgram.setUniform("modelViewMatrix", getOrthoModelMatrix());
                thisGUIObject.textMesh.render();
            }
        }
        hudShaderProgram.unbind();
    }
}
