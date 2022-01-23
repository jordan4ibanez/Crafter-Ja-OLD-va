package game.crafting;

import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;

public class CraftRecipeObject {
    private final String[][] recipe;
    private final String output;
    private final int amount;

    public CraftRecipeObject(String[][] recipe, String output, int amount){
        this.recipe = recipe;
        this.output = output;
        this.amount = amount;
    }

    public String[][] getRecipe(){
        return recipe;
    }

    public String getOutput(){
        return output;
    }

    public int getAmount(){
        return amount;
    }
}