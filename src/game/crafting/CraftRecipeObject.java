package game.crafting;

import org.joml.Vector2d;
import org.joml.Vector2i;

public class CraftRecipeObject {
    String[][] recipe;
    String output;
    int amountOutput;
    Vector2i size;

    public CraftRecipeObject(String[][]recipe, String output, int amountOutput, Vector2i size){
        String[][] flippedArray = new String[size.x][size.y];
        for (int x = 0; x < size.x; x++){
            for (int y = 0; y < size.y; y++){
                flippedArray[y][x] = recipe[x][y];
            }
        }
        this.recipe = flippedArray;
        this.output = output;
        this.amountOutput = amountOutput;
        this.size = size;
    }
}
