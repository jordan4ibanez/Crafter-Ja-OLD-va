package engine.gui;

import engine.graph.Mesh;
import org.joml.Vector2d;
import org.joml.Vector2f;

import static engine.gui.GUI.getButtonMesh;
import static engine.gui.TextHandling.createTextCentered;
import static engine.gui.TextHandling.translateCharToArray;

public class GUIObject {
    public byte type;
    public Mesh textMesh;
    public Vector2d pos = new Vector2d();
    public Vector2f buttonScale = new Vector2f();

    //initializer for button
    public GUIObject(String text, Vector2d pos){
        this.textMesh = createTextCentered(text, 1,1,1);
        float totalLengthReal = 0;
        //pre-poll the actual length
        for (char letter : text.toCharArray()) {
            float[] thisCharacterArray = translateCharToArray(letter);
            totalLengthReal += thisCharacterArray[4] + 0.1f;
        }
        this.buttonScale.x = totalLengthReal/2f;
        this.pos = pos;
    }
}
