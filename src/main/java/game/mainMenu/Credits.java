package game.mainMenu;


import engine.graphics.Mesh;
import engine.gui.MeshCreator2D;
import engine.gui.TextHandling;

public class Credits {

    private final TextHandling textHandling = new TextHandling();
    private final MeshCreator2D meshCreator2D = new MeshCreator2D();

    //all the stuff for the credits are stored here
    private final Mesh[] creditParts = new Mesh[19];

    public Credits(){
        this.initializeCredits();
    }

    public Mesh[] getCreditMeshArray(){
        return creditParts;
    }

    public Mesh getCreditMesh(int ID){
        return creditParts[ID];
    }

    private void initializeCredits() {
        creditParts[0] = textHandling.createTextCenteredWithShadow("Inspired by:", 1,1,1);
        creditParts[1] = meshCreator2D.create2DMesh(8.4375f,1f,"textures/minetest_logo.png");
        creditParts[2] = meshCreator2D.create2DMesh(9.34782608696f,1f,"textures/mineclone2.png");

        creditParts[3] = textHandling.createTextCenteredWithShadow("Programmer: jordan4ibanez", 1,1,1);
        creditParts[4] = meshCreator2D.create2DMesh(2f,2f,"textures/jordan4ibanez.png");

        creditParts[5] = textHandling.createTextCenteredWithShadow("Textures: gerold55", 1,1,1);
        creditParts[6] = meshCreator2D.create2DMesh(2f,2f,"textures/tools/stonepick.png");

        creditParts[7] = textHandling.createTextCenteredWithShadow("Textures: MineClone 2", 1,1,1);
        creditParts[8] = meshCreator2D.create2DMesh(9.34782608696f,1f,"textures/mineclone2.png");

        creditParts[9] = textHandling.createTextCenteredWithShadow("Title Music: Dark Reaven Music", 1,1,1);
        creditParts[10] = meshCreator2D.create2DMesh(2f,2f,"textures/dark_reaven.png");

        creditParts[11] = textHandling.createTextCenteredWithShadow("Java Library: LWJGL", 1,1,1);
        creditParts[12] = meshCreator2D.create2DMesh(2f,2f,"textures/lwjgl.png");

        creditParts[13] = textHandling.createTextCenteredWithShadow("Sound Library: OpenAL", 1,1,1);
        creditParts[14] = meshCreator2D.create2DMesh(1.84049079755f,1f,"textures/openal.png");

        creditParts[15] = textHandling.createTextCenteredWithShadow("OpenGL Library: GLFW", 1,1,1);
        creditParts[16] = meshCreator2D.create2DMesh(2f,2f,"textures/glfw.png");

        creditParts[17] = textHandling.createTextCenteredWithShadow("Thank you for playing my game! :]", 1,1,1);
        creditParts[18] = meshCreator2D.create2DMesh(2f,2f,"textures/icon.png");
    }
}