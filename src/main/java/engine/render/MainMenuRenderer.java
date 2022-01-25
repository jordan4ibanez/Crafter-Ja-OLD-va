package engine.render;

import engine.Utils;
import engine.Window;
import engine.graphics.Camera;
import engine.graphics.Mesh;
import engine.graphics.ShaderProgram;
import engine.graphics.Transformation;
import engine.gui.GUI;
import engine.gui.GUIObject;
import engine.gui.TextHandling;
import engine.time.Delta;
import game.mainMenu.MainMenu;
import game.mainMenu.MainMenuAssets;

import static org.lwjgl.opengl.GL11C.*;


public class MainMenuRenderer {

    private final TextHandling textHandling = new TextHandling();
    private final MainMenuAssets mainMenuAssets = new MainMenuAssets();

    private Transformation transformation;
    private Window window;
    private Camera camera;
    private MainMenu mainMenu;
    private GUI gui;
    private Delta delta;

    private final float FOV = (float) Math.toRadians(72.0f);
    private final ShaderProgram shaderProgram;
    private final ShaderProgram hudShaderProgram;

    public void setWindow(Window window){
        if (this.window == null){
            this.window = window;
        }
    }

    public void setCamera(Camera camera){
        if (this.camera == null){
            this.camera = camera;
            transformation = new Transformation(this.camera,this.window);
        }
    }

    public void setMainMenu(MainMenu mainMenu){
        if (this.mainMenu == null){
            this.mainMenu = mainMenu;
        }
    }
    public void setGui(GUI gui){
        if (this.gui == null){
            this.gui = gui;
        }
    }
    public void setDelta(Delta delta){
        if (this.delta == null){
            this.delta = delta;
        }
    }


    public MainMenuRenderer(){
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
    }

    public void clearScreen(){
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }


    public void renderMainMenu(){

        clearScreen();

        shaderProgram.bind();

        //update projection matrix
        float z_NEAR = 0.1f;
        transformation.resetProjectionMatrix(FOV, window.getWidth(), window.getHeight(), z_NEAR, 100f);
        shaderProgram.setUniform("projectionMatrix",  transformation.getProjectionMatrix());

        shaderProgram.setUniform("texture_sampler", 0);


        boolean onTitleScreen = mainMenu.getMainMenuPage() == 0;
        boolean onWorldsScreen = mainMenu.getMainMenuPage() == 3;

        //get background tile mesh

        //render scrolling background
        //ultra-wide screen compatible, for some reason
        for (int x = -15; x <= 15; x++){
            for (int y = -15; y <= 15; y++) {
                float scale = 5f;
                //these calculations are done to perfectly center the background in front of the camera (hopefully)
                transformation.updateViewMatrixWithPosRotationScale(x * scale, (y + mainMenu.getBackGroundScroll()) * scale, -18, 0, 0, 0,scale, scale, 0);
                shaderProgram.setUniform("modelViewMatrix", transformation.getModelMatrix());
                mainMenuAssets.getTitleBackGroundMeshTile().render();
            }
        }



        if (onWorldsScreen) {
            byte[][] worldTitleBlocks = mainMenu.getWorldTitleBlocks();
            double[][] worldTitleOffsets = mainMenu.getWorldTitleOffsets();
            glClear(GL_DEPTH_BUFFER_BIT);
            //render title (in blocks)
            for (int x = 0; x < worldTitleBlocks.length; x++) {
                //assume equal lengths
                for (int y = 0; y < worldTitleBlocks[0].length; y++) {
                    if (worldTitleBlocks[x][y] == 1) {
                        //these calculations are done to perfectly center the title in front of the camera (hopefully)
                        transformation.updateViewMatrix(y - (27d / 2d), -x + (5d / 2d), -18 + worldTitleOffsets[x][y], 0, 0, 0);
                        shaderProgram.setUniform("modelViewMatrix", transformation.getModelMatrix());
                        mainMenuAssets.getTitleBlockMesh().render();
                    }
                }
            }
        } else if (onTitleScreen) {
            byte[][] titleBlocks = mainMenu.getTitleBlocks();
            double[][] titleBlockOffsets = mainMenu.getTitleBlockOffsets();
            glClear(GL_DEPTH_BUFFER_BIT);
            //render title (in blocks)
            for (int x = 0; x < titleBlocks.length; x++) {
                //assume equal lengths
                for (int y = 0; y < titleBlocks[0].length; y++) {
                    if (titleBlocks[x][y] == 1) {
                        //these calculations are done to perfectly center the title in front of the camera (hopefully)
                        transformation.updateViewMatrix(y - (27d / 2d), -x + (5d / 2d), -18 + titleBlockOffsets[x][y], 0, 0, 0);
                        shaderProgram.setUniform("modelViewMatrix", transformation.getModelMatrix());
                        mainMenuAssets.getTitleBlockMesh().render();
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
        transformation.resetOrthoProjectionMatrix(); // needed to get current screen size

        float windowScale = window.getScale();

        if (onTitleScreen) {
            //title screen text
            {
                glClear(GL_DEPTH_BUFFER_BIT);

                //process scale of title screen text
                float scale = (15f / (float) mainMenu.getTitleScreenTextLength());
                if (scale > 1) {
                    scale = 1;
                }
                scale *= windowScale / 30;

                scale += mainMenu.getTitleBounce();



                //create a new mesh every frame
                if (mainMenu.titleScreenIsRandom()) {
                    glClear(GL_DEPTH_BUFFER_BIT);

                    //gray shadow part
                    transformation.updateOrthoModelMatrix(windowScale / 2.27d, windowScale / 3.27f, 0, 0, 0, 20f, scale, scale, scale);
                    hudShaderProgram.setUniform("modelViewMatrix", transformation.getOrthoModelMatrix());
                    Mesh myMesh = textHandling.createTextCentered(mainMenu.getTitleScreenText(), 0.2f, 0.2f, 0f);
                    myMesh.render();
                    myMesh.cleanUp(false);

                    glClear(GL_DEPTH_BUFFER_BIT);

                    //yellow part
                    transformation.updateOrthoModelMatrix(windowScale / 2.25d, windowScale / 3.25f, 0, 0, 0, 20f, scale, scale, scale);
                    hudShaderProgram.setUniform("modelViewMatrix", transformation.getOrthoModelMatrix());
                    final Mesh myMesh2 = textHandling.createTextCentered(mainMenu.getTitleScreenText(), 1f, 1f, 0f);
                    myMesh2.render();
                    myMesh2.cleanUp(false);
                }
                //constant mesh for text
                else {
                    glClear(GL_DEPTH_BUFFER_BIT);

                    //gray shadow part
                    transformation.updateOrthoModelMatrix(windowScale / 2.27d, windowScale / 3.27f, 0, 0, 0, 20f, scale, scale, scale);
                    hudShaderProgram.setUniform("modelViewMatrix", transformation.getOrthoModelMatrix());
                    mainMenu.getTitleScreenTextMeshBackGround().render();

                    glClear(GL_DEPTH_BUFFER_BIT);

                    //yellow part
                    transformation.updateOrthoModelMatrix(windowScale / 2.25d, windowScale / 3.25f, 0, 0, 0, 20f, scale, scale, scale);
                    hudShaderProgram.setUniform("modelViewMatrix", transformation.getOrthoModelMatrix());
                    mainMenu.getTitleScreenTextMeshForeGround().render();
                }
            }

        }

        if (mainMenu.getMainMenuPage() != 4) {
            renderMainMenuGUI();
        } else {
            Mesh[] creditParts = mainMenu.getCredits().getCreditMeshArray();
            boolean on = true;
            double trueY = 0;

            float scale = windowScale / 20f;
            float scroll = mainMenu.getCreditsScroll() * (windowScale / 10f);

            for (int y = 0; y < creditParts.length; y++){
                //if (creditParts[y] != null) {
                transformation.updateOrthoModelMatrix(0, trueY + scroll, 0, 0, 0, 0, scale, scale, scale);
                hudShaderProgram.setUniform("modelViewMatrix", transformation.getOrthoModelMatrix());
                if (creditParts[y] != null) {
                    creditParts[y].render();
                }


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

               //}
            }
        }



        hudShaderProgram.unbind();
    }

    private void renderMainMenuGUI(){
        hudShaderProgram.bind();
        float windowScale = window.getScale();

        for (GUIObject thisGUIObject : mainMenu.getMainMenuGUI()) {
            //button type
            if (thisGUIObject.type == 0) {

                double xPos = thisGUIObject.pos.x * (windowScale / 100d);
                double yPos = thisGUIObject.pos.y * (windowScale / 100d);

                transformation.updateOrthoModelMatrix(xPos, yPos, 0, 0, 0, 0, windowScale / 20d, windowScale / 20d, windowScale / 20d);
                hudShaderProgram.setUniform("modelViewMatrix", transformation.getOrthoModelMatrix());
                thisGUIObject.textMesh.render();

                float xAdder = 20 / thisGUIObject.buttonScale.x;
                float yAdder = 20 / thisGUIObject.buttonScale.y;

                transformation.updateOrthoModelMatrix(xPos, yPos, 0, 0, 0, 0, windowScale / xAdder, windowScale / yAdder, windowScale / 20d);
                hudShaderProgram.setUniform("modelViewMatrix", transformation.getOrthoModelMatrix());
                if (thisGUIObject.selected) {
                    gui.getButtonSelectedMesh().render();
                } else {
                    gui.getButtonMesh().render();
                }
            }
            //text input box type
            else if (thisGUIObject.type == 1){

                thisGUIObject.pointerTimer += delta.getDelta();

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


                transformation.updateOrthoModelMatrix(xPos - textOffset, yPos, 0, 0, 0, 0, windowScale / 20d, windowScale / 20d, windowScale / 20d);
                hudShaderProgram.setUniform("modelViewMatrix", transformation.getOrthoModelMatrix());
                thisGUIObject.textMesh.render();

                float xAdder = 20 / thisGUIObject.buttonScale.x;
                float yAdder = 20 / thisGUIObject.buttonScale.y;

                transformation.updateOrthoModelMatrix(xPos, yPos, 0, 0, 0, 0, windowScale / xAdder, windowScale / yAdder, windowScale / 20d);
                hudShaderProgram.setUniform("modelViewMatrix", transformation.getOrthoModelMatrix());
                if (thisGUIObject.selected) {
                    gui.getTextInputSelectedMesh().render();
                } else {
                    gui.getTextInputMesh().render();
                }
            }
            //plain text type
            else if (thisGUIObject.type == 2) {
                double xPos = thisGUIObject.pos.x * (windowScale / 100d);
                double yPos = thisGUIObject.pos.y * (windowScale / 100d);

                transformation.updateOrthoModelMatrix(xPos, yPos, 0, 0, 0, 0, windowScale / 20d, windowScale / 20d, windowScale / 20d);
                hudShaderProgram.setUniform("modelViewMatrix", transformation.getOrthoModelMatrix());
                thisGUIObject.textMesh.render();
            }
        }
        hudShaderProgram.unbind();
    }
}
