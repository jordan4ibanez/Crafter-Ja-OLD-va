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
    boolean normalMeshIsNull = false;

    //liquid
    float[] liquidPositionsArray ;
    float[] liquidLightArray;
    int[] liquidIndicesArray;
    float[] liquidTextureCoordArray;
    boolean liquidMeshIsNull = false;

    //allFaces
    float[] allFacesPositionsArray ;
    float[] allFacesLightArray;
    int[] allFacesIndicesArray;
    float[] allFacesTextureCoordArray;
    boolean allFacesMeshIsNull = false;

}
