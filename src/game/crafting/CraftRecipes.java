package game.crafting;

import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectIntImmutablePair;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Objects;

import static game.crafting.Inventory.isAtCraftingBench;
import static game.crafting.InventoryObject.getInventoryAsArray;

final public class CraftRecipes {
    private static final Int2ObjectArrayMap<String> output = new Int2ObjectArrayMap<>();
    private static final Int2ObjectArrayMap<String[][]> recipe = new Int2ObjectArrayMap<>();
    private static final Int2IntArrayMap amountOutput = new Int2IntArrayMap();

    private static int count = 0;

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

        //everything is case-sensitive

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


        String[][] torch = {
                {"coal"},
                {"stick"}
        };
        generateRecipe(torch, "torchItem", 4);


        String[][] cobbleStairRight = {
                {"cobble", "",       ""},
                {"cobble", "cobble", ""},
                {"cobble", "cobble", "cobble"},
        };
        generateRecipe(cobbleStairRight, "cobble stair", 4);


        String[][] cobbleStairLeft = {
                {"",       "",       "cobble"},
                {"",       "cobble", "cobble"},
                {"cobble", "cobble", "cobble"},
        };

        generateRecipe(cobbleStairLeft, "cobble stair", 4);



        String[][] woodStairRight = {
                {"wood", "",     ""},
                {"wood", "wood", ""},
                {"wood", "wood", "wood"},
        };
        generateRecipe(woodStairRight, "wood stair", 4);


        String[][] woodStairLeft = {
                {"",     "",     "wood"},
                {"",     "wood", "wood"},
                {"wood", "wood", "wood"},
        };

        generateRecipe(woodStairLeft, "wood stair", 4);

        String[][] cobbleSlab = {
                {"cobble", "cobble", "cobble"},
        };

        generateRecipe(cobbleSlab, "cobble slab", 3);

        String[][] woodSlab = {
                {"wood", "wood", "wood"},
        };

        generateRecipe(woodSlab, "wood slab", 3);

        String[][] cobbleVerticalSlab = {
                {"cobble"},
                {"cobble"},
                {"cobble"},
        };

        generateRecipe(cobbleVerticalSlab, "cobble vertical slab", 3);

        String[][] woodVerticalSlab = {
                {"wood"},
                {"wood"},
                {"wood"},
        };

        generateRecipe(woodVerticalSlab, "wood vertical slab", 3);

        printAmountOfRecipes();
    }


    //pre-pattern every recipe because I'm horrible at pattern matching
    //this is absolute brute force, do not use this after alpha unless can't figure out a better way
    //this consumes memory, kb, but still memory
    private static void generateRecipe(String[][] newRecipe, String newOutput, int newAmount) {
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

                output.put(count, newOutput);
                recipe.put(count, workerRecipeArray);
                amountOutput.put(count, newAmount);

                count++;
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

                    output.put(count, newOutput);
                    recipe.put(count, workerRecipeArray);
                    amountOutput.put(count, newAmount);

                    count++;
                }
            }
        }

    }

    //this is a debug output now
    private static void printAmountOfRecipes(){
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
        System.out.println("TOTAL RECIPES: " + (recipe.keySet().size() - 1));
    }


    public static ObjectIntImmutablePair<String> recipeScan(String inventory){

        ObjectIntImmutablePair<String> returningRecipe = null;

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

        String[][] thisInventory = getInventoryAsArray(inventory);

        //dump item strings into array
        for (int x = 0; x < thisInventory.length; x++) {
            for (int y = 0; y < thisInventory[0].length; y++) {
                String thisItem = thisInventory[y][x];
                inventoryToStringArray[y][x] = Objects.requireNonNullElse(thisItem, "");
            }
        }

        for (int thisIndex : recipe.keySet()) {
            if (Arrays.deepEquals(recipe.get(thisIndex), inventoryToStringArray)){
                returningRecipe = new ObjectIntImmutablePair<String>(output.get(thisIndex), amountOutput.get(thisIndex));
            }
        }

        return returningRecipe;
    }
}
