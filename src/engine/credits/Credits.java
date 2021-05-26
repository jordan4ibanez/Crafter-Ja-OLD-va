package engine.credits;

import engine.graphics.Mesh;

import static engine.gui.GUI.create2DMesh;
import static engine.gui.TextHandling.createTextCenteredWithShadow;

public class Credits {

    //all the stuff for the credits are stored here
    private static final Mesh[] creditParts = new Mesh[19];

    public static Mesh[] getCreditParts(){
        return creditParts;
    }

    public static void initializeCredits() throws Exception {
        creditParts[0] = createTextCenteredWithShadow("Inspired by:", 1,1,1);
        creditParts[1] = create2DMesh(8.4375f,1f,"textures/minetest_logo.png");
        creditParts[2] = create2DMesh(9.34782608696f,1f,"textures/mineclone2.png");

        creditParts[3] = createTextCenteredWithShadow("Programmer: jordan4ibanez", 1,1,1);
        creditParts[4] = create2DMesh(2f,2f,"textures/jordan4ibanez.png");

        creditParts[5] = createTextCenteredWithShadow("Textures: gerold55", 1,1,1);
        creditParts[6] = create2DMesh(2f,2f,"textures/tools/stonepick.png");

        creditParts[7] = createTextCenteredWithShadow("Textures: MineClone 2", 1,1,1);
        creditParts[8] = create2DMesh(9.34782608696f,1f,"textures/mineclone2.png");

        creditParts[9] = createTextCenteredWithShadow("Title Music: Dark Reaven Music", 1,1,1);
        creditParts[10] = create2DMesh(2f,2f,"textures/dark_reaven.png");

        creditParts[11] = createTextCenteredWithShadow("Java Library: LWJGL", 1,1,1);
        creditParts[12] = create2DMesh(2f,2f,"textures/lwjgl.png");

        creditParts[13] = createTextCenteredWithShadow("Sound Library: OpenAL", 1,1,1);
        creditParts[14] = create2DMesh(1.84049079755f,1f,"textures/openal.png");

        creditParts[15] = createTextCenteredWithShadow("OpenGL Library: GLFW", 1,1,1);
        creditParts[16] = create2DMesh(2f,2f,"textures/glfw.png");

        creditParts[17] = createTextCenteredWithShadow("Thank you for playing my game! :]", 1,1,1);
        creditParts[18] = create2DMesh(2f,2f,"textures/icon.png");
    }
}