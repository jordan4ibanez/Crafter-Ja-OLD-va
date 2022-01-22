package game.blocks;


public class BlockDefinition {

    //actual block object fields
    private final int ID;
    private final String name;
    private final Boolean dropsItem;
    private final float[][] shape;
    private final byte drawType;
    private final float[] frontTextures;  //front
    private final float[] backTextures;   //back
    private final float[] rightTextures;  //right
    private final float[] leftTextures;   //left
    private final float[] topTextures;    //top
    private final float[] bottomTextures; //bottom

    private final boolean walkable;
    private final boolean steppable;
    private final boolean isLiquid;
    private final String placeSound;
    private final String digSound;
    private final boolean isRightClickable;
    private final boolean isOnPlaced;
    private final float viscosity;
    private final boolean pointable;
    private final float stoneHardness;
    private final float dirtHardness;
    private final float woodHardness;
    private final float leafHardness;
    private final String droppedItem;
    private final BlockModifier blockModifier;

    public BlockDefinition(
            int ID,
            float stoneHardness,
            float dirtHardness,
            float woodHardness,
            float leafHardness,
            String name,
            boolean dropsItem,
            byte[] front,
            byte[] back,
            byte[] right,
            byte[] left,
            byte[] top,
            byte[] bottom,
            float[][] shape,
            byte drawType,
            boolean walkable,
            boolean steppable,
            boolean isLiquid,
            String placeSound,
            String digSound,
            boolean isRightClickable,
            boolean isOnPlaced,
            float viscosity,
            boolean pointable,
            String droppedItem,
            BlockModifier blockModifier
    ){
        this.ID = ID;
        this.stoneHardness = stoneHardness;
        this.dirtHardness = dirtHardness;
        this.woodHardness = woodHardness;
        this.leafHardness = leafHardness;
        this.name = name;
        this.dropsItem = dropsItem;
        this.frontTextures  = calculateTexture(  front[0],  front[1] );
        this.backTextures   = calculateTexture(   back[0],   back[1] );
        this.rightTextures  = calculateTexture(  right[0],  right[1] );
        this.leftTextures   = calculateTexture(   left[0],   left[1] );
        this.topTextures    = calculateTexture(    top[0],    top[1] );
        this.bottomTextures = calculateTexture( bottom[0], bottom[1] );
        this.shape = shape;
        this.drawType = drawType;
        this.walkable = walkable;
        this.steppable = steppable;
        this.isLiquid = isLiquid;
        this.placeSound = placeSound;
        this.digSound = digSound;
        this.isRightClickable = isRightClickable;
        this.isOnPlaced = isOnPlaced;
        this.viscosity = viscosity;
        this.pointable = pointable;
        this.droppedItem = droppedItem;
        this.blockModifier = blockModifier;
    }


    /*
    public void onDigCall(byte ID, int posX, int posY, int posZ) {
        if(dropsItems[ID]){
            //dropped defined item
            if (droppedItems[ID] != null){
                throwItem(droppedItems[ID], posX + 0.5d,posY + 0.5d, posZ + 0.5d,1, 2.5f);
            }
            //drop self
            else {
                throwItem(names[ID], posX + 0.5d, posY + 0.5d, posZ + 0.5d, 1, 2.5f);
            }
        }
        if(blockModifiers[ID] != null){
            try {
                blockModifiers[ID].onDig(posX, posY, posZ);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (!digSounds[ID].equals("")) {
            playSound(digSounds[ID]);
        }
    }

    public void onPlaceCall(byte ID, int posX, int posY, int posZ) {

        if (blockModifiers[ID] != null){
            try {
                blockModifiers[ID].onPlace(posX,posY,posZ);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (!placeSounds[ID].equals("")) {
            playSound(placeSounds[ID]);
        }
    }
     */

    private float[] calculateTexture(byte x, byte y){

        byte atlasSizeX = 32;
        byte atlasSizeY = 32;

        float[] texturePoints = new float[4];
        texturePoints[0] = (float)x/(float)atlasSizeX;     //min x (-)
        texturePoints[1] = (float)(x+1)/(float)atlasSizeX; //max x (+)

        texturePoints[2] = (float)y/(float)atlasSizeY;     //min y (-)
        texturePoints[3] = (float)(y+1)/(float)atlasSizeY; //max y (+)
        return texturePoints;
    }

    public int getID(){
        return this.ID;
    }

    public String getBlockName(){
        return this.name;
    }

    public boolean getDropsItem() {
        return this.dropsItem;
    }

    public boolean getRightClickable(){
        return this.isRightClickable;
    }

    public boolean getIsOnPlaced(){
        return this.isOnPlaced;
    }

    public byte getDrawType(){
        return this.drawType;
    }

    public boolean getIfLiquid(){
        return this.isLiquid;
    }

    public float[][] getBlockShape(byte rot){

        float[][] newBoxes = this.shape;

        byte index = 0;

        //automated as base, since it's the same
        switch (rot) {
            case 1 -> {
                for (float[] thisShape : this.shape) {

                    float blockDiffZ = 1f - thisShape[5];
                    float widthZ = thisShape[5] - thisShape[2];

                    newBoxes[index][0] = blockDiffZ;
                    newBoxes[index][1] = thisShape[1];//-y
                    newBoxes[index][2] = thisShape[0]; // -z

                    newBoxes[index][3] = blockDiffZ + widthZ;
                    newBoxes[index][4] = thisShape[4];//+y
                    newBoxes[index][5] = thisShape[3]; //+z
                    index++;
                }
            }
            case 2 -> {
                for (float[] thisShape : this.shape) {

                    float blockDiffZ = 1f - thisShape[5];
                    float widthZ = thisShape[5] - thisShape[2];

                    float blockDiffX = 1f - thisShape[3];
                    float widthX = thisShape[3] - thisShape[0];

                    newBoxes[index][0] = blockDiffX;
                    newBoxes[index][1] = thisShape[1];//-y
                    newBoxes[index][2] = blockDiffZ; // -z

                    newBoxes[index][3] = blockDiffX + widthX;
                    newBoxes[index][4] = thisShape[4];//+y
                    newBoxes[index][5] = blockDiffZ + widthZ; //+z
                    index++;
                }
            }
            case 3 -> {
                for (float[] thisShape : this.shape) {
                    float blockDiffX = 1f - thisShape[3];
                    float widthX = thisShape[3] - thisShape[0];

                    newBoxes[index][0] = thisShape[2];
                    newBoxes[index][1] = thisShape[1];//-y
                    newBoxes[index][2] = blockDiffX; // -z

                    newBoxes[index][3] = thisShape[5];
                    newBoxes[index][4] = thisShape[4];//+y
                    newBoxes[index][5] = blockDiffX + widthX; //+z
                    index++;
                }
            }
        }
        return newBoxes;
    }

    public boolean isBlockWalkable(){
        return this.walkable;
    }

    public boolean isSteppable(){
        return this.steppable;
    }

    public boolean blockHasOnRightClickCall(){
        return(this.isRightClickable && this.blockModifier != null);
    }

    public float[] getFrontTexturePoints(byte rotation){
        return switch (rotation) {
            case 1 -> this.rightTextures;
            case 2 -> this.backTextures;
            case 3 -> this.leftTextures;
            default -> this.frontTextures;
        };
    }
    public float[] getBackTexturePoints(byte rotation){
        return switch (rotation) {
            case 1 -> this.leftTextures;
            case 2 -> this.frontTextures;
            case 3 -> this.rightTextures;
            default -> this.backTextures;
        };

    }
    public float[] getRightTexturePoints(byte rotation){
        return switch (rotation) {
            case 1 -> this.backTextures;
            case 2 -> this.leftTextures;
            case 3 -> this.frontTextures;
            default -> this.rightTextures;
        };
    }
    public float[] getLeftTexturePoints(byte rotation){
        return switch (rotation) {
            case 1 -> this.frontTextures;
            case 2 -> this.rightTextures;
            case 3 -> this.backTextures;
            default -> this.leftTextures;
        };
    }

    public BlockModifier getBlockModifier(){
        return this.blockModifier;
    }

    public float getStoneHardness(){
        return this.stoneHardness;
    }

    public float getDirtHardness(){
        return this.dirtHardness;
    }

    public float getWoodHardness(){
        return this.woodHardness;
    }

    public float getLeafHardness(){
        return this.leafHardness;
    }

    public boolean isBlockLiquid(){
        return this.isLiquid;
    }

    public String getPlaceSound(){
        return this.placeSound;
    }

    public float getBlockViscosity(){
        return this.viscosity;
    }
    public float[] getTopTexturePoints(){
        return this.topTextures;
    }
    public float[] getBottomTexturePoints(){
        return this.bottomTextures;
    }

    public boolean isBlockPointable(){
        return this.pointable;
    }

    public String getDigSound(){
        return this.digSound;
    }

    public String getDroppedItem(){
        return this.droppedItem;
    }
}
