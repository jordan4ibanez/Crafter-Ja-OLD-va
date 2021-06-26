package game.mob;

import org.joml.Vector3d;
import org.joml.Vector3f;

import static engine.FancyMath.randomByte;
import static engine.FancyMath.randomIntFromMinToMaxNegativePositive;
import static engine.time.Time.getDelta;
import static game.chunk.Chunk.getMobSpawnYPos;
import static game.mob.Mob.spawnMob;
import static game.player.Player.getPlayerPos;

public class MobSpawning {

    private static double spawnTimer = 0;

    private static final float spawnGoal = 0.01f; //every 10 seconds

    public static void runSpawningAlgorithm(){
        spawnTimer += getDelta();

        if (spawnTimer >= spawnGoal){
            //CHANGE THIS TO CHECK FOR PLAYERS POSITION WHEN TRANSLATING TO MULTIPLAYER
            trySpawn(new Vector3d(getPlayerPos()));
            spawnTimer = 0;
        }
    }


    //this is a square distance, acceptable is 24-56 blocks away from the player
    private static void trySpawn(Vector3d pos){
        //a 2d calculation
        int x = (int)pos.x;//(int)pos.x + randomIntFromMinToMaxNegativePositive(24,56);
        int z = (int)pos.z;//(int)pos.z + randomIntFromMinToMaxNegativePositive(24,56);
        int yPos = (int)pos.y;//getMobSpawnYPos(x,z);

        if (yPos >= 0){
            spawnMob((byte) 0/*randomByte((byte) 2)*/, new Vector3d(x,yPos,z), new Vector3f(0));
        }
    }

}
