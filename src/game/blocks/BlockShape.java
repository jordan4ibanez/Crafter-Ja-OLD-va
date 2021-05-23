package game.blocks;

public class BlockShape {
    private final double[][] boxes;

    public BlockShape(double[][] thisBox){
        for (double[] thisCheck : thisBox) {
            if (thisCheck.length != 6) {
                throw new IllegalStateException("Error creating new Block Shape! Block definition needs exactly 6 dimensions to work!");
            }
        }
        this.boxes = thisBox;
    }

    public double[][] getBoxes() {
        return boxes;
    }
}
