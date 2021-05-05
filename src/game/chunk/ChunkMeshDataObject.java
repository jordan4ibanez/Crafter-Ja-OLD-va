package game.chunk;

public class ChunkMeshDataObject {

    int chunkX;
    int chunkZ;
    int yHeight;

    //regular
    float[] positionsArray ;
    float[] lightArray;
    int[] indicesArray;
    float[] textureCoordArray;
    boolean regularIsNull = false;

    //liquid
    float[] liquidPositionsArray;
    float[] liquidLightArray;
    int[] liquidIndicesArray;
    float[] liquidTextureCoordArray;
    boolean liquidIsNull = false;

    //blockboxes
    float[] blockBoxPositionsArray ;
    float[] blockBoxLightArray;
    int[] blockBoxIndicesArray;
    float[] blockBoxTextureCoordArray;
    boolean blockBoxesIsNull = false;
}
