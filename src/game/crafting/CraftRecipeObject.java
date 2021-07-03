package game.crafting;

public class CraftRecipeObject {
    String[][]recipe;
    String output;
    int amountOutput;

    public CraftRecipeObject(String[][]recipe, String output, int amountOutput){
        this.recipe = recipe;
        this.output = output;
        this.amountOutput = amountOutput;
    }
}
