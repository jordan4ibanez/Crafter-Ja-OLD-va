package engine.render;

import engine.graph.Mesh;
import engine.graph.ShaderProgram;
import engine.gui.GUIObject;
import org.joml.Matrix4d;
import org.joml.Vector2d;
import org.joml.Vector3d;
import org.joml.Vector3f;

import static engine.gui.TextHandling.createTextCentered;
import static engine.render.GameRenderer.*;
import static engine.gui.GUI.*;
import static engine.Window.*;
import static engine.graph.Transformation.*;

import static game.mainMenu.MainMenu.*;
import static game.mainMenu.MainMenuAssets.getTitleBackGroundMeshTile;
import static game.mainMenu.MainMenuAssets.getTitleBlockMesh;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;

public class MainMenuRenderer {

    //this just kind of works off of GameRender.java

    private static final float FOV = (float) Math.toRadians(72.0f);


    public static void renderMainMenu(){

        ShaderProgram shaderProgram = getShaderProgram();

        ShaderProgram hudShaderProgram = getHudShaderProgram();

        Mesh workerMesh;
        clearScreen();

        rescaleWindow();

        shaderProgram.bind();

        //update projection matrix
        Matrix4d projectionMatrix = getProjectionMatrix(FOV, getWindowWidth(), getWindowHeight(), getzNear(), getzFar());

        shaderProgram.setUniform("projectionMatrix", projectionMatrix);

        //update the view matrix
        Matrix4d viewMatrix = getViewMatrix();

        shaderProgram.setUniform("texture_sampler", 0);

        Matrix4d modelViewMatrix;

        byte[][] titleBlocks = getTitleBlocks();
        double[][] titleBlockOffsets = getTitleBlockOffsets();


        //set initial random float variables

        workerMesh = getTitleBackGroundMeshTile();

        //render scrolling background
        //ultra wide screen compatible, for some reason
        for (int x = -15; x <= 15; x++){
            for (int y = -15; y <= 15; y++) {

                float scale = 5f;
                //these calculations are done to perfectly center the background in front of the camera (hopefully)
                modelViewMatrix = getGenericMatrixWithPosRotationScale(new Vector3d(x * scale, (y + getBackGroundScroll()) * scale, -18), new Vector3f(0, 0, 0), new Vector3d(scale, scale, 0), viewMatrix);
                shaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                workerMesh.render();
            }
        }


        glClear(GL_DEPTH_BUFFER_BIT);
        workerMesh = getTitleBlockMesh();
        //render title (in blocks)
        for (int x = 0; x < titleBlocks.length; x++) {
            //assume equal lengths
            for (int y = 0; y < titleBlocks[0].length; y++) {

                if (titleBlocks[x][y] == 1) {
                    //these calculations are done to perfectly center the title in front of the camera (hopefully)
                    modelViewMatrix = updateModelViewMatrix(new Vector3d(y - (27d/2d), -x + (5d/2d), -18 + titleBlockOffsets[x][y]), new Vector3f(0, 0, 0), viewMatrix);
                    shaderProgram.setUniform("modelViewMatrix", modelViewMatrix);

                    workerMesh.render();
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

        Vector2d windowSize = getWindowSize();
        float windowScale = getWindowScale();



        //title screen gag
        {
            glClear(GL_DEPTH_BUFFER_BIT);

            //process scale of title screen gag text
            float scale = (15f/(float)getTitleScreenGagLength());
            if (scale > 1){
                scale = 1;
            }
            scale *= windowScale / 30;

            scale += getTitleBounce();



            glClear(GL_DEPTH_BUFFER_BIT);

            //gray shadow part
            modelViewMatrix = buildOrthoProjModelMatrix(new Vector3d(windowScale/2.27d,windowScale/3.27f,0), new Vector3f(0, 0, 20f), new Vector3d(scale,scale,scale));
            hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
            workerMesh = createTextCentered(getTitleScreenGag(), 0.2f, 0.2f, 0f);
            workerMesh.render();

            glClear(GL_DEPTH_BUFFER_BIT);

            //yellow part
            modelViewMatrix = buildOrthoProjModelMatrix(new Vector3d(windowScale/2.25d,windowScale/3.25f,0), new Vector3f(0, 0, 20f), new Vector3d(scale,scale,scale));
            hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
            workerMesh = createTextCentered(getTitleScreenGag(), 1f, 1f, 0f);
            workerMesh.render();
        }




        renderMainMenuGUI();



        //play button
        if (false){
            glClear(GL_DEPTH_BUFFER_BIT);
            {
                modelViewMatrix = buildOrthoProjModelMatrix(new Vector3d(0, (0) * windowScale / 3d, 0), new Vector3f(0, 0, 0), new Vector3d(windowScale / 2d, windowScale / 2d, windowScale / 2d));
                hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                getButtonMesh().render();
            }


            //play (shadow)
            glClear(GL_DEPTH_BUFFER_BIT);
            {
                modelViewMatrix = buildOrthoProjModelMatrix(new Vector3d(windowScale / 300d, windowScale / 300d, 0), new Vector3f(0, 0, 0), new Vector3d(windowScale / 20d, windowScale / 20d, windowScale / 20d));
                hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);

                workerMesh = createTextCentered("PLAY", 0.2f, 0.2f, 0.2f);
                workerMesh.render();
            }

            //play (white)
            glClear(GL_DEPTH_BUFFER_BIT);
            {
                modelViewMatrix = buildOrthoProjModelMatrix(new Vector3d(0, 0, 0), new Vector3f(0, 0, 0), new Vector3d(windowScale / 20d, windowScale / 20d, windowScale / 20d));
                hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);

                workerMesh = createTextCentered("PLAY", 1f, 1f, 1f);
                workerMesh.render();
            }
        }


        hudShaderProgram.unbind();
    }

    private static void renderMainMenuGUI(){
        for (GUIObject thisButton : getMainMenuGUI()) {
            ShaderProgram hudShaderProgram = getHudShaderProgram();

            float windowScale = getWindowScale();

            //TODO: USE THIS FOR MOUSE COLLISION DETECTION
            double xPos = thisButton.pos.x * (windowScale / 100d);
            double yPos = thisButton.pos.y * (windowScale / 100d);


            Matrix4d modelViewMatrix = buildOrthoProjModelMatrix(new Vector3d(xPos, yPos, 0), new Vector3f(0, 0, 0), new Vector3d(windowScale / 20d, windowScale / 20d, windowScale / 20d));
            hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
            thisButton.textMesh.render();


            //TODO: USE THIS FOR MOUSE COLLISION DETECTION
            float xAdder = 20 / (thisButton.buttonScale.x * 2.1f);
            float yAdder = 2.75f;

            modelViewMatrix = buildOrthoProjModelMatrix(new Vector3d(xPos, yPos, 0), new Vector3f(0, 0, 0), new Vector3d(windowScale / xAdder, windowScale / yAdder, windowScale / 20d));
            hudShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
            getButtonMesh().render();
        }
    }
}
