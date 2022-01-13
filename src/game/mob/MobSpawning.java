package game.mob;

import org.joml.Vector3d;

import static engine.FancyMath.randomByte;
import static engine.FancyMath.randomIntFromMinToMaxNegativePositive;
import static engine.time.Time.getDelta;
import static game.chunk.Chunk.getMobSpawnYPos;

import static game.mob.MobObject.getNumberOfMobs;
import static game.mob.MobObject.spawnMob;
import static game.player.Player.getPlayerPos;

public class MobSpawning {
    private static final int spawnLimit = 40;
    private static double spawnTimer = 1f;
    private static final float spawnGoal = 0.5f; //every 3 seconds

    public static void runSpawningAlgorithm(){
        if (getNumberOfMobs() >= spawnLimit){
            return;
        }
        //having this not count up allows a minor cool down
        spawnTimer += getDelta();

        if (spawnTimer >= spawnGoal){
            //CHANGE THIS TO CHECK FOR PLAYERS POSITION WHEN TRANSLATING TO MULTIPLAYER
            trySpawn(getPlayerPos());
            spawnTimer = 0;
        }
    }


    //this is a square distance, acceptable is 24-56 blocks away from the player
    private static void trySpawn(Vector3d pos){
        //a 2d calculation
        //24,56
        int x = (int)pos.x + randomIntFromMinToMaxNegativePositive(5,10);
        int z = (int)pos.z + randomIntFromMinToMaxNegativePositive(5,10);
        int yPos = getMobSpawnYPos(x,z);
        if (yPos >= 0){
            spawnMob(randomByte((byte) 9), x, yPos, z, 0, 0, 0);
        }
    }

}
