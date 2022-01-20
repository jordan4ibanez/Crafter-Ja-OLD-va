package engine.gui;

import engine.graphics.Mesh;
import org.joml.Vector2d;
import org.joml.Vector2f;

import static engine.gui.TextHandling.*;

public class GUIObject {
    /*
    types:
    0 - button
    1 - text input
    2 - just text
     */

    public byte type;
    public Mesh textMesh;
    public Vector2d pos;
    public Vector2f buttonScale = new Vector2f();
    public boolean selected;
    public String inputText;
    public char pointer;
    public float pointerTimer;

    //initializer for button (auto scaled width)
    public GUIObject(String text, Vector2d pos){
        this.textMesh = createTextCentered(text, 1,1,1);
        float totalLengthReal = 0;
        //pre-poll the actual length
        for (char letter : text.toCharArray()) {
            float[] thisCharacterArray = translateCharToArray(letter);
            totalLengthReal += thisCharacterArray[4] + 0.1f;
        }
        this.buttonScale.x = (totalLengthReal/2f) * 2.1f;
        this.buttonScale.y = 1.6f;
        this.pos = pos;
        this.type = 0;
        this.selected = false;
    }

    //initializer for button with fixed width
    public GUIObject(String text, Vector2d pos, float width){
        this.textMesh = createTextCenteredWithShadow(text, 1,1,1);
        this.buttonScale.x = width * 2.1f;
        this.buttonScale.y = 1.6f;
        this.pos = pos;
        this.type = 0;
        this.selected = false;
    }

    //initializer for generic text object
    public GUIObject(String text, boolean isJustText, Vector2d pos){
        this.textMesh = createTextCenteredWithShadow(text, 1,1,1);
        this.pos = pos;
        this.type = 2;
        this.selected = false;
    }

    //initializer for button with fixed width and height
    public GUIObject(String text, Vector2d pos, float width, float height){
        this.textMesh = createTextCenteredWithShadow(text, 1,1,1);
        this.buttonScale.x = width * 2.1f;
        this.buttonScale.y = height * 2.1f;
        this.pos = pos;
        this.type = 0;
        this.selected = false;
    }

    //initializer for text input box
    public GUIObject(Vector2d pos, float width, float height){
        this.inputText = "";
        this.textMesh = createText(this.inputText, 1,1,1);
        this.buttonScale.x = width * 2.1f;
        this.buttonScale.y = height * 2.1f;
        this.pos = pos;
        this.type = 1;
        this.selected = false;
        this.pointerTimer = 0.5f;
    }



    //initializer for text input box with preset text
    public GUIObject(Vector2d pos, float width, float height, String existingText){
        this.inputText = existingText;
        this.textMesh = createText(this.inputText, 1,1,1);
        this.buttonScale.x = width * 2.1f;
        this.buttonScale.y = height * 2.1f;
        this.pos = pos;
        this.type = 1;
        this.selected = false;
        this.pointerTimer = 0.5f;
    }

    //updater for text input box
    public void updateInputBoxText(String text){
        this.textMesh.cleanUp(false);
        this.textMesh = createText(text, 1,1,1);
    }

    public String getInputText(){
        return this.inputText;
    }

    //update statically scaled button text
    public void updateTextCenteredFixed(String text){
        this.textMesh.cleanUp(false);
        this.textMesh = createTextCenteredWithShadow(text, 1,1,1);
    }

    public void updateTextStandard(String text){
        this.textMesh.cleanUp(false);
        this.textMesh = createTextWithShadow(text, 1,1,1);
    }

    //updating dynamically scaled button text
    public void updateTextDynamic(String text){
        this.textMesh.cleanUp(false);
        this.textMesh = createTextCentered(text, 1,1,1);
        float totalLengthReal = 0;
        //pre-poll the actual length
        for (char letter : text.toCharArray()) {
            float[] thisCharacterArray = translateCharToArray(letter);
            totalLengthReal += thisCharacterArray[4] + 0.1f;
        }
        this.buttonScale.x = (totalLengthReal/2f) * 2.1f;
        this.buttonScale.y = 1.6f;
    }
}
