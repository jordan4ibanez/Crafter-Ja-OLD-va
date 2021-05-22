package game.crafting;

import org.joml.Vector2d;
import org.joml.Vector2i;

public class CraftRecipeObject {
    String[][] recipe;
    String output;
    int amountOutput;
    Vector2i size;

    public CraftRecipeObject(String[][]recipe, String output, int amountOutput, Vector2i size){
        this.recipe = recipe;
        this.output = output;
        this.amountOutput = amountOutput;
        this.size = size;
    }
}
