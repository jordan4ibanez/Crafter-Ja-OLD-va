package game.crafting;

import game.player.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class CraftRecipes {

    private final Player player;

    ArrayList<CraftRecipeObject> recipes = new ArrayList<>();

    public CraftRecipes(Player player){

        this.player = player;

        //everything is case-sensitive

        String[] materials = new String[]{
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
        for (String material : materials) {
            String recipeItem = material;
            if (recipeItem.equals("stone")){
                recipeItem = "cobble";
            }
            String[][] pick = {
                    {recipeItem, recipeItem, recipeItem},
                    {"",         "stick",    ""},
                    {"",         "stick",    ""}
            };
            registerRecipe(pick, material + "pick", 1);

            String[][] shovel = {
                    {recipeItem},
                    {"stick"   },
                    {"stick"   }
            };
            registerRecipe(shovel, material + "shovel", 1);

            //create symmetrical axes
            String[][] axeLeft = {
                    {recipeItem, recipeItem},
                    {recipeItem, "stick"},
                    {"",         "stick"}
            };
            registerRecipe(axeLeft, material + "axe", 1);

            String[][] axeRight = {
                    {recipeItem, recipeItem},
                    {"stick",    recipeItem},
                    {"stick",    ""        }
            };
            registerRecipe(axeRight, material + "axe", 1);
        }

        String[][] wood = {
                {"tree"}
        };
        registerRecipe(wood, "wood", 4);


        String[][] workbench = {
                {"wood","wood",},
                {"wood","wood"}
        };
        registerRecipe(workbench, "workbench", 1);


        String[][] boat = {
                {"wood",""    , "wood" ,},
                {"wood","wood", "wood" ,}
        };
        registerRecipe(boat, "boat", 1);


        String[][] door = {
                {"wood", "wood",},
                {"wood", "wood",},
                {"wood", "wood"}
        };
        registerRecipe(door, "door", 1);

        String[][] stick = {
                {"wood"},
                {"wood"}
        };
        registerRecipe(stick, "stick", 4);


        String[][] torch = {
                {"coal"},
                {"stick"}
        };
        registerRecipe(torch, "torchItem", 4);


        String[][] cobbleStairRight = {
                {"cobble", "",       ""},
                {"cobble", "cobble", ""},
                {"cobble", "cobble", "cobble"},
        };
        registerRecipe(cobbleStairRight, "cobble stair", 4);


        String[][] cobbleStairLeft = {
                {"",       "",       "cobble"},
                {"",       "cobble", "cobble"},
                {"cobble", "cobble", "cobble"},
        };

        registerRecipe(cobbleStairLeft, "cobble stair", 4);



        String[][] woodStairRight = {
                {"wood", "",     ""},
                {"wood", "wood", ""},
                {"wood", "wood", "wood"},
        };
        registerRecipe(woodStairRight, "wood stair", 4);


        String[][] woodStairLeft = {
                {"",     "",     "wood"},
                {"",     "wood", "wood"},
                {"wood", "wood", "wood"},
        };

        registerRecipe(woodStairLeft, "wood stair", 4);

        String[][] cobbleSlab = {
                {"cobble", "cobble", "cobble"},
        };

        registerRecipe(cobbleSlab, "cobble slab", 3);

        String[][] woodSlab = {
                {"wood", "wood", "wood"},
        };

        registerRecipe(woodSlab, "wood slab", 3);

        String[][] cobbleVerticalSlab = {
                {"cobble"},
                {"cobble"},
                {"cobble"},
        };

        registerRecipe(cobbleVerticalSlab, "cobble vertical slab", 3);

        String[][] woodVerticalSlab = {
                {"wood"},
                {"wood"},
                {"wood"},
        };

        registerRecipe(woodVerticalSlab, "wood vertical slab", 3);

        printAmountOfRecipes();
    }


    //pre-pattern every recipe because I'm horrible at pattern matching
    //this is absolute brute force, do not use this after alpha unless can't figure out a better way
    //this consumes memory, kb, but still memory
    private void registerRecipe(String[][] newRecipe, String newOutput, int newAmount) {
        int widthX = 0;
        int widthY = newRecipe.length;

        //find out max recipe width
        for (String[] strings : newRecipe) {
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
                        workerRecipeArray[recipeY + adjustmentY][recipeX + adjustmentX] = newRecipe[recipeY][recipeX];
                    }
                }

                //System.out.println(Arrays.deepToString(workerRecipeArray));
                recipes.add(new CraftRecipeObject(workerRecipeArray, newOutput, newAmount));
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
                            workerRecipeArray[recipeY + adjustmentY][recipeX + adjustmentX] = newRecipe[recipeY][recipeX];
                        }
                    }

                    //System.out.println(Arrays.deepToString(workerRecipeArray));

                    recipes.add(new CraftRecipeObject(workerRecipeArray, newOutput, newAmount));

                }
            }
        }

    }

    //this is a debug output now
    private void printAmountOfRecipes(){
        /*
        System.out.println(output + " created " + count + " recipes!");
        System.out.println(Arrays.deepToString(recipe.values().toArray()));
        int microCount = 0;
        for (String[][] thisThing : recipe.values()){
            System.out.print(output.get(microCount) + ": ");
            System.out.print(Arrays.deepToString(thisThing));
            System.out.print("\n");
            microCount++;
        }
         */
        System.out.println("TOTAL RECIPES: " + recipes.size());
    }


    public CraftRecipeObject recipeScan(InventoryObject inventory){

        String[][] inventoryToStringArray;

        //create basic 2d string array
        //3x3
        if (player.isAtCraftingBench()){
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
                String thisItem = inventory.getItem(x,y);
                inventoryToStringArray[y][x] = Objects.requireNonNullElse(thisItem, "");
            }
        }

        for (CraftRecipeObject aRecipe : recipes) {
            if (Arrays.deepEquals(aRecipe.getRecipe(), inventoryToStringArray)){
                return aRecipe;
            }
        }

        return null;
    }
}
