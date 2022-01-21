package game.tnt;

import org.joml.Vector3d;

import engine.FancyMath.getDistance;
import engine.sound.SoundAPI.playSound;
import game.blocks.BlockDefinition.getBlockName;
import game.chunk.Chunk.getBlock;
import game.chunk.Chunk.setBlock;
import game.entity.item.ItemEntity.throwItem;
import game.light.Light.lightFloodFill;
import game.tnt.TNTEntity.createTNT;

public class Explosion {

    //todo: document this better
    public void boom(Vector3d pos, int boomDistance) {
        playSound("tnt_explode", pos.x, pos.y, pos.z, false);
        
        for (int x = (int)Math.floor(pos.x) - boomDistance; x < (int)Math.floor(pos.x) + boomDistance; x++) {
            for (int y = (int)Math.floor(pos.y) - boomDistance; y < (int)Math.floor(pos.y) + boomDistance; y++) {
                for (int z = (int)Math.floor(pos.z) - boomDistance; z < (int)Math.floor(pos.z) + boomDistance; z++) {
                    if (getDistance(pos.x, pos.y, pos.z, x, y, z) <= boomDistance) {
                        byte currentBlock = getBlock(x, y, z);
                        //don't destroy bedrock
                        if(currentBlock != 5) {
                            setBlock(x, y, z, (byte) 0, (byte) 0);
                            if (currentBlock > 0 && currentBlock != 6 && Math.random() > 0.994) {
                                throwItem(getBlockName(currentBlock), x,y,z, 1,0);
                            } else if (currentBlock == 6){
                                createTNT(x, y, z, (float)(Math.random() * 1f) + 1.0f, false);
                            }
                        }
                    }
                }
            }
        }

        lightFloodFill((int)pos.x,(int)pos.y,(int)pos.z);
    }
}
