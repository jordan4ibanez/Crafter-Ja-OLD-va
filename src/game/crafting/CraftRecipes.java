package game.crafting;

import game.item.Item;

import java.util.ArrayList;
import java.util.Arrays;

import static game.crafting.Inventory.isAtCraftingBench;

public class CraftRecipes {
    private static CraftRecipeObject[] recipes;
    private static ArrayList<CraftRecipeObject> craftRecipeAccumulator = new ArrayList<>();

    private final static String[] materials = new String[]{
            "wood",
            "stone",
            "iron",
            "gold",
            "lapis",
            "diamond",
            "emerald",
            "sapphire",
            "ruby",
    };

    //craft recipe initializer
    public static void registerCraftRecipes(){

        //everything is case sensitive


        for (String material : materials) {
            String recipeItem = material;
            if (recipeItem.equals("stone")){
                recipeItem = "cobblestone";
            }
            String[][] pick = {
                    {recipeItem, recipeItem, recipeItem},
                    {"",         "stick",    ""},
                    {"",         "stick",    ""}
            };
            generateRecipe(pick, material + "pick", 1);

            String[][] shovel = {
                    {recipeItem},
                    {"stick"   },
                    {"stick"   }
            };
            generateRecipe(shovel, material + "shovel", 1);

            //create symmetrical axes
            String[][] axeLeft = {
                    {recipeItem, recipeItem},
                    {recipeItem, "stick"},
                    {"",         "stick"}
            };
            generateRecipe(axeLeft, material + "axe", 1);

            String[][] axeRight = {
                    {recipeItem, recipeItem},
                    {"stick",    recipeItem},
                    {"stick",    ""        }
            };
            generateRecipe(axeRight, material + "axe", 1);
        }

        String[][] wood = {
                {"tree"}
        };
        generateRecipe(wood, "wood", 4);


        String[][] workbench = {
                {"wood","wood",},
                {"wood","wood"}
        };
        generateRecipe(workbench, "workbench", 1);


        String[][] boat = {
                {"wood",""    , "wood" ,},
                {"wood","wood", "wood" ,}
        };
        generateRecipe(boat, "boat", 1);


        String[][] door = {
                {"wood", "wood",},
                {"wood", "wood",},
                {"wood", "wood"}
        };
        generateRecipe(door, "door", 1);

        String[][] stick = {
                {"wood"},
                {"wood"}
        };
        generateRecipe(stick, "stick", 4);

        finalizeRecipes();
    }


    //pre-pattern every recipe because I'm horrible at pattern matching
    //this is absolute brute force, do not use this after alpha unless can't figure out a better way
    //this consumes memory, kb, but still memory
    private static void generateRecipe(String[][] recipe, String output, int amount) {
        int widthX = 0;
        int widthY = recipe.length;

        //find out max recipe width
        for (String[] strings : recipe) {
            if (strings.length > widthX) {
                widthX = strings.length;
            }
        }

        //System.out.println(widthX + " " + widthY);

        //int count=0;
        //large crafting grid
        //4 because 1 greater than large crafting grid
        for (int adjustmentX = 0; adjustmentX < 4 - widthX; adjustmentX++){
            for (int adjustmentY = 0; adjustmentY < 4 - widthY; adjustmentY++) {

                String[][] workerRecipeArray = {
                        {"", "", ""},
                        {"", "", ""},
                        {"", "", ""}
                };

                for (int recipeX = 0; recipeX < widthX; recipeX++) {
                    for (int recipeY = 0; recipeY < widthY; recipeY++) {
                        workerRecipeArray[recipeY + adjustmentY][recipeX + adjustmentX] = recipe[recipeY][recipeX];
                    }
                }

                //System.out.println(Arrays.deepToString(workerRecipeArray));

                craftRecipeAccumulator.add(new CraftRecipeObject(workerRecipeArray, output, amount));
                //count++;
            }
        }

        //small crafting grid
        //3 because 1 greater than small crafting grid
        //see if recipe size is less than 3
        if (widthY < 3 && widthX < 3) {
            for (int adjustmentX = 0; adjustmentX < 3 - widthX; adjustmentX++) {
                for (int adjustmentY = 0; adjustmentY < 3 - widthY; adjustmentY++) {

                    String[][] workerRecipeArray = {
                            {"", ""},
                            {"", ""},
                    };

                    for (int recipeX = 0; recipeX < widthX; recipeX++) {
                        for (int recipeY = 0; recipeY < widthY; recipeY++) {
                            workerRecipeArray[recipeY + adjustmentY][recipeX + adjustmentX] = recipe[recipeY][recipeX];
                        }
                    }

                    //System.out.println(Arrays.deepToString(workerRecipeArray));

                    craftRecipeAccumulator.add(new CraftRecipeObject(workerRecipeArray, output, amount));
                    //count++;
                }
            }
        }

        //System.out.println(output + " created " + count + " recipes!");
    }

    private static void finalizeRecipes(){
        //dump recipes into the recipe array
        recipes = new CraftRecipeObject[craftRecipeAccumulator.toArray().length];
        int count = 0;
        for (CraftRecipeObject thisRecipe : craftRecipeAccumulator){
            recipes[count] = thisRecipe;
            count++;
        }
        count--;
        System.out.println("RECIPES FINALIZED - CONVERTED FROM OBJECT ARRAYLIST TO PRIMITIVE OBJECT ARRAY");
        System.out.println("TOTAL RECIPES: " + count);
        craftRecipeAccumulator.clear();
        craftRecipeAccumulator = null; //force this to go to GC
    }


    public static CraftRecipeObject recipeScan(InventoryObject inventory){
        CraftRecipeObject returningRecipe = null;

        String[][] inventoryToStringArray;

        //create basic 2d string array
        //3x3
        if (isAtCraftingBench()){
            inventoryToStringArray = new String[][]{
                    {"", "", ""},
                    {"", "", ""},
                    {"", "", ""}
            };
        }
        //2x2
        else {
            inventoryToStringArray = new String[][]{
                    {"", ""},
                    {"", ""},
            };
        }

        //dump item strings into array
        for (int x = 0; x < inventory.getSize().x; x++) {
            for (int y = 0; y < inventory.getSize().y; y++) {
                Item thisItem = inventory.get(x, y);
                if (thisItem != null) {
                    inventoryToStringArray[y][x] = thisItem.name;
                } else {
                    inventoryToStringArray[y][x] = "";
                }
            }
        }

        for (CraftRecipeObject thisRecipe : recipes) {
            if (Arrays.deepEquals(thisRecipe.recipe, inventoryToStringArray)){
                returningRecipe = thisRecipe;
            }
        }

        return returningRecipe;
    }
}
