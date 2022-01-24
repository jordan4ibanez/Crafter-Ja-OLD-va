package engine.gui;

import engine.graphics.Mesh;
import engine.graphics.Texture;

public class MeshCreator2D {
    public Mesh create2DMesh(float width, float height, String texture) {
        float[] positions = new float[12];
        float[] textureCoord = new float[8];
        int[] indices = new int[6];
        float[] light = new float[12];

        positions[0] = (width);
        positions[1] = (height);
        positions[2] = (0f);
        positions[3] = (-width);
        positions[4] = (height);
        positions[5] = (0f);
        positions[6] = (-width);
        positions[7] = (-height);
        positions[8] = (0f);
        positions[9] = (width);
        positions[10] = (-height);
        positions[11] = (0f);

        for (int i = 0; i < 12; i++) {
            light[i] = 1f;
        }

        indices[0] = (0);
        indices[1] = (1);
        indices[2] = (2);
        indices[3] = (0);
        indices[4] = (2);
        indices[5] = (3);

        textureCoord[0] = (1f);
        textureCoord[1] = (0f);
        textureCoord[2] = (0f);
        textureCoord[3] = (0f);
        textureCoord[4] = (0f);
        textureCoord[5] = (1f);
        textureCoord[6] = (1f);
        textureCoord[7] = (1f);

        return new Mesh(positions, light, indices, textureCoord, new Texture(texture));
    }

    public Mesh create2DMeshOffsetRight() {
        float[] positions = new float[12];
        float[] textureCoord = new float[8];
        int[] indices = new int[6];
        float[] light = new float[12];

        positions[0] = ((float) 0.5 *2f);
        positions[1] = ((float) 0.5);
        positions[2] = (0f);

        positions[3] = (0);
        positions[4] = ((float) 0.5);
        positions[5] = (0f);

        positions[6] = (0);
        positions[7] = (-(float) 0.5);
        positions[8] = (0f);

        positions[9] = ((float) 0.5 *2f);
        positions[10] = (-(float) 0.5);
        positions[11] = (0f);

        for (int i = 0; i < 12; i++) {
            light[i] = 1f;
        }

        indices[0] = (0);
        indices[1] = (1);
        indices[2] = (2);
        indices[3] = (0);
        indices[4] = (2);
        indices[5] = (3);

        textureCoord[0] = (1f);
        textureCoord[1] = (0f);
        textureCoord[2] = (0f);
        textureCoord[3] = (0f);
        textureCoord[4] = (0f);
        textureCoord[5] = (1f);
        textureCoord[6] = (1f);
        textureCoord[7] = (1f);

        return new Mesh(positions, light, indices, textureCoord, new Texture("textures/chat_box.png"));
    }

    //overloaded for texture width (used for half hearts)
    public Mesh createHalf2DMesh(float width, float height, float textureWidth, String texture) {
        float[] positions = new float[12];
        float[] textureCoord = new float[8];
        int[] indices = new int[6];
        float[] light = new float[12];

        positions[0] = (width - textureWidth);
        positions[1] = (height);
        positions[2] = (0f);
        positions[3] = (-width);
        positions[4] = (height);
        positions[5] = (0f);
        positions[6] = (-width);
        positions[7] = (-height);
        positions[8] = (0f);
        positions[9] = (width - textureWidth);
        positions[10] = (-height);
        positions[11] = (0f);

        for (int i = 0; i < 12; i++) {
            light[i] = 1f;
        }

        indices[0] = (0);
        indices[1] = (1);
        indices[2] = (2);
        indices[3] = (0);
        indices[4] = (2);
        indices[5] = (3);

        textureCoord[0] = (textureWidth);
        textureCoord[1] = (0f);
        textureCoord[2] = (0f);
        textureCoord[3] = (0f);
        textureCoord[4] = (0f);
        textureCoord[5] = (1f);
        textureCoord[6] = (textureWidth);
        textureCoord[7] = (1f);

        return new Mesh(positions, light, indices, textureCoord, new Texture(texture));
    }
}
