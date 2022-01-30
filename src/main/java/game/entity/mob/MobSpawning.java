package game.entity.mob;

import engine.time.Delta;
import game.chunk.Chunk;
import game.entity.EntityContainer;
import game.player.Player;
import org.joml.Vector3d;
import org.joml.Vector3f;

import java.util.Random;

public class MobSpawning {
    private double spawnTimer = 1f;
    private final Random random = new Random();

    public void runSpawningAlgorithm(Player player, Chunk chunk, Delta delta, MobMeshBuilder mobMeshBuilder, EntityContainer entityContainer){
        int spawnLimit = 20;
        //having this not count up allows a minor cool down
        spawnTimer += delta.getDelta();

        //every 3 seconds
        float spawnGoal = 3f;
        if (spawnTimer >= spawnGoal){
            //CHANGE THIS TO CHECK FOR PLAYERS POSITION WHEN TRANSLATING TO MULTIPLAYER
            trySpawn(player.getPos(), chunk, mobMeshBuilder, entityContainer);
            spawnTimer = 0;
        }
    }


    //this is a square distance, acceptable is 24-56 blocks away from the player
    private void trySpawn(Vector3d pos, Chunk chunk, MobMeshBuilder mobMeshBuilder, EntityContainer entityContainer){
        //BLARF
        if (true){
            return;
        }
        //a 2d calculation
        int x = (int)pos.x + randomInt(24,56);
        int z = (int)pos.z + randomInt(24,56);
        int yPos = chunk.getMobSpawnYPos(x,z);
        if (yPos > 0){
            //randomByte(9){
            //}
            //spawnMob(randomByte((byte) 9), x, yPos, z, 0, 0, 0);
            new Zombie(mobMeshBuilder, entityContainer, new Vector3d(x, yPos, z), new Vector3f(0,0,0));
        }
    }
    private final int[] dirArray = new int[]{-1,1};

    public int randomInt(int min, int max){
        int x = min + random.nextInt(max - min + 1);
        return x * dirArray[random.nextInt(2)];
    }
}
