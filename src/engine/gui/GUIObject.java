package engine.gui;

import engine.graph.Mesh;
import org.joml.Vector2d;
import org.joml.Vector2f;

import static engine.gui.TextHandling.*;

public class GUIObject {
    /*
    types:
    0 - button
    1 - slider bar
     */
    public byte type;
    public Mesh textMesh;
    public Vector2d pos = new Vector2d();
    public Vector2f buttonScale = new Vector2f();
    public boolean selected;

    //initializer for button (auto scaled width)
    public GUIObject(String text, Vector2d pos){
        this.textMesh = createTextCentered(text, 1,1,1);
        float totalLengthReal = 0;
        //pre-poll the actual length
        for (char letter : text.toCharArray()) {
            float[] thisCharacterArray = translateCharToArray(letter);
            totalLengthReal += thisCharacterArray[4] + 0.1f;
        }
        totalLengthReal -= 0.1f;
        this.buttonScale.x = totalLengthReal/2f;
        this.pos = pos;
        this.type = 0;
        this.selected = false;
    }

    //initializer for button with fixed width
    public GUIObject(String text, Vector2d pos, float width){
        this.textMesh = createTextCenteredWithShadow(text, 1,1,1);
        this.buttonScale.x = width;
        this.pos = pos;
        this.type = 0;
        this.selected = false;
    }


}
