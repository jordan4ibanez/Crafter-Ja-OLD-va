package game.tnt;

import org.joml.Vector3d;

import static engine.FancyMath.getDistance;
import static game.blocks.BlockDefinition.getBlockDefinition;
import static game.chunk.Chunk.getBlock;
import static game.chunk.Chunk.setBlock;
import static game.item.ItemEntity.createItem;
import static game.light.Light.lightFloodFill;
import static game.tnt.TNTEntity.createTNT;

public class Explosion {

    public static void boom(Vector3d pos, int boomDistance) throws Exception {
        for (int x = (int)Math.floor(pos.x) - boomDistance; x < (int)Math.floor(pos.x) + boomDistance; x++) {
            for (int y = (int)Math.floor(pos.y) - boomDistance; y < (int)Math.floor(pos.y) + boomDistance; y++) {
                for (int z = (int)Math.floor(pos.z) - boomDistance; z < (int)Math.floor(pos.z) + boomDistance; z++) {
                    if (getDistance(pos.x, pos.y, pos.z, x, y, z) <= boomDistance) {
                        int currentBlock = getBlock(x, y, z);
                        //don't destroy bedrock
                        if(currentBlock != 5) {
                            setBlock(x, y, z, 0, 0);
                            if (currentBlock > 0 && currentBlock != 6 && Math.random() > 0.994) {
                                createItem(getBlockDefinition(currentBlock).name, new Vector3d(x,y,z), 1);
                            } else if (currentBlock == 6){
                                createTNT(new Vector3d(x, y, z), (float)(Math.random() * 1f) + 1.0f, false);
                            }
                        }
                    }
                }
            }
        }

        lightFloodFill((int)pos.x,(int)pos.y,(int)pos.z);
    }
}
