package game.tnt;

import game.chunk.Chunk;
import org.joml.Vector3d;
import org.joml.Vector3i;

public class Explosion {

    public void boom(Vector3d pos, int boomDistance, Chunk chunk) {
        //playSound("tnt_explode", pos.x, pos.y, pos.z, false);
        
        for (int x = (int)Math.floor(pos.x) - boomDistance; x < (int)Math.floor(pos.x) + boomDistance; x++) {
            for (int y = (int)Math.floor(pos.y) - boomDistance; y < (int)Math.floor(pos.y) + boomDistance; y++) {
                for (int z = (int)Math.floor(pos.z) - boomDistance; z < (int)Math.floor(pos.z) + boomDistance; z++) {
                    if (getDistance(pos.x, pos.y, pos.z, x, y, z) <= boomDistance) {
                        byte currentBlock = chunk.getBlock(new Vector3i(x, y, z));
                        //don't destroy bedrock
                        if(currentBlock != 5) {
                            chunk.setBlock(new Vector3i(x, y, z), (byte) 0, (byte) 0);
                            if (currentBlock > 0 && currentBlock != 6 && Math.random() > 0.994) {
                                //throwItem(getBlockName(currentBlock), x,y,z, 1,0);
                            } else if (currentBlock == 6){
                                //createTNT(x, y, z, (float)(Math.random() * 1f) + 1.0f, false);
                            }
                        }
                    }
                }
            }
        }

        //lightFloodFill((int)pos.x,(int)pos.y,(int)pos.z);
    }
    private double getDistance(double x1, double y1, double z1, double x2, double y2, double z2){
        return Math.hypot((x1 - x2), Math.hypot((y1 - y2),(z1 - z2)));
    }
}
