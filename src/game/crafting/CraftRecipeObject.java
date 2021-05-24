package game.crafting;

import org.joml.Vector2d;
import org.joml.Vector2i;

public class CraftRecipeObject {
    String[][]recipe;
    String output;
    int amountOutput;

    public CraftRecipeObject(String[][]recipe, String output, int amountOutput){
        /*
        String[][] flippedArray = new String[size.x][size.y];

        for (int x = 0; x < size.x; x++){
            flippedArray[y][x] = recipe[x][y];
        }
         */
        this.recipe = recipe;
        this.output = output;
        this.amountOutput = amountOutput;
    }
}
