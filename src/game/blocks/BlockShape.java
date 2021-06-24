package game.blocks;

public class BlockShape {
    private final float[][] boxes;

    public BlockShape(float[][] thisBox){
        for (float[] thisCheck : thisBox) {
            if (thisCheck.length != 6) {
                throw new IllegalStateException("Error creating new Block Shape! Block definition needs exactly 6 dimensions to work!");
            }
        }
        this.boxes = thisBox;
    }

    public float[][] getBoxes() {
        return boxes;
    }
}
