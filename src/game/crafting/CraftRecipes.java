package game.crafting;

import game.item.Item;
import org.joml.Vector2i;

import java.util.ArrayList;

public class CraftRecipes {
    private static CraftRecipeObject[] recipes;

    //craft recipe initializer
    public static void registerCraftRecipes(){

        ArrayList<CraftRecipeObject> craftRecipeAccumulator = new ArrayList<>();


        String[][] wood = {
                {"Tree"},
        };
        craftRecipeAccumulator.add(new CraftRecipeObject(wood, "Wood", 4, new Vector2i(1)));


        String[][] workbench = {
                {"Wood","Wood" },
                {"Wood","Wood" }
        };
        craftRecipeAccumulator.add(new CraftRecipeObject(workbench, "Workbench", 1, new Vector2i(2)));


        //dump recipes into the recipe array
        recipes = new CraftRecipeObject[craftRecipeAccumulator.toArray().length];
        int count = 0;
        for (CraftRecipeObject thisRecipe : craftRecipeAccumulator){
            recipes[count] = thisRecipe;
            count++;
        }
    }


    public static CraftRecipeObject recipeScan(InventoryObject inventory){
        String returningRecipe = null;
        for (int i = 0; i < recipes.length; i++){
            CraftRecipeObject thisRecipe = recipes[i];

            //skip recipes that are too big
            if (thisRecipe.size.x > inventory.getSize().x || thisRecipe.size.y > inventory.getSize().y){
                continue;
            }


            //single item recipe
            boolean found1 = false;
            boolean lockOut = false;
            if (thisRecipe.size.x == 1){
                for (int x = 0; x < inventory.getSize().x; x++){
                    for (int y = 0; y < inventory.getSize().y; y++){
                        Item thisItem = inventory.get(x,y);
                        if (thisItem != null){
                            if (thisItem.name.equals(thisRecipe.recipe[0][0])){
                                found1 = !lockOut;
                                lockOut = true;
                            } else {
                                lockOut = true;
                                found1 = false;
                            }
                        }
                    }
                }
            }
            if (found1){
                System.out.println("Found: " + thisRecipe.output);
                return thisRecipe;
            }
        }
        return null;
    }
}
