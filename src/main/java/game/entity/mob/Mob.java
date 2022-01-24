package game.entity.mob;

import engine.sound.SoundAPI;
import engine.time.Delta;
import game.chunk.Chunk;
import game.crafting.InventoryLogic;
import game.entity.Entity;
import game.entity.EntityContainer;
import game.entity.collision.Collision;
import game.player.Player;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;

public class Mob extends Entity {

    public Mob(Chunk chunk, EntityContainer entityContainer, Vector3d pos, Vector3f inertia, boolean item, boolean mob, boolean particle) {
        super(chunk, entityContainer, pos, inertia, item, mob, particle);
    }

    public void onTick(){

        double delta = getDelta();

        //mob table key is int
        for (int thisMob : getMobKeys()){

            //this is the mob definition ID
            int thisMobDefinitionID = getMobID(thisMob);

            //these are object pointers
            Vector3d thisMobPos = getMobPos(thisMob);
            Vector3i thisMobOldFlooredPos = getMobOldFlooredPos(thisMob);

            //these are immutable primitives - they need to be set after they are modified
            float thisMobHurtTimer = getMobHurtTimer(thisMob);
            float thisMobLightTimer = getMobLightTimer(thisMob);
            float thisMobDeathRotation = getMobDeathRotation(thisMob);
            float thisMobWidth = getMobDefinitionWidth(thisMobDefinitionID);
            float thisMobHeight = getMobDefinitionHeight(thisMobDefinitionID);
            byte thisMobHealth = getMobHealth(thisMob);
            float thisMobDeathTimer = 0;

            thisMobLightTimer += delta;

            //only collision detect if alive
            if (getMobHealth(thisMob) > 0) {
                mobSoftPlayerCollisionDetect(thisMob, thisMobPos, thisMobHeight, thisMobWidth);
                mobSoftCollisionDetect(thisMob, thisMobPos, thisMobHeight, thisMobWidth);
            }

            //interface consumes object - no need for re-assignment to vars
            //only do it during alive state
            if (thisMobHealth > 0) {
                getMobDefinitionInterface(thisMobDefinitionID).onTick(thisMob);
            }

            if (thisMobPos.y < 0){
                deletionQueue.add(thisMob);
                continue;
            }

            //mob is now dead
            if (thisMobHealth <= 0){
                //mob dying animation
                if (thisMobDeathRotation < 90) {
                    //System.out.println(thisMobDeathRotation);
                    thisMobDeathRotation += delta * 300f;
                    if (thisMobDeathRotation >= 90) {
                        thisMobDeathRotation = 90;
                        setMobDeathTimer(thisMob, 0.0001f);
                    }
                    setMobDeathRotation(thisMob, thisMobDeathRotation);
                //mob will now sit there for a second
                } else {
                    thisMobDeathTimer = getMobDeathTimer(thisMob);
                    thisMobDeathTimer += delta;
                    setMobDeathTimer(thisMob, thisMobDeathTimer);
                }
            }

            currentFlooredPos.set((int)Math.floor(thisMobPos.x), (int)Math.floor(thisMobPos.y), (int)Math.floor(thisMobPos.z));

            //poll local light every quarter second
            if (thisMobLightTimer >= 0.25f || !currentFlooredPos.equals(thisMobOldFlooredPos)){
                setMobLight(thisMob, getLight(currentFlooredPos.x, currentFlooredPos.y, currentFlooredPos.z));
                thisMobLightTimer = 0f;
            }

            if (thisMobHealth <= 0 && thisMobDeathTimer >= 0.5f && thisMobDeathRotation == 90){
                getMobDefinitionInterface(thisMobDefinitionID).onDeath(thisMob);
                deletionQueue.add(thisMob);
            }

            //count down hurt timer
            if(thisMobHurtTimer > 0f && thisMobHealth > 0){ //don't reset when dead
                thisMobHurtTimer -= delta;
                if (thisMobHurtTimer <= 0){
                    thisMobHurtTimer = 0;
                    setMobHurtAdder(thisMob, (byte) 0);
                }

                setMobHurtTimer(thisMob, thisMobHurtTimer);
            }

            setMobLightTimer(thisMob, thisMobLightTimer);
            setMobOldFlooredPos(thisMob, currentFlooredPos.x, currentFlooredPos.y, currentFlooredPos.z);
            setMobOldPos(thisMob, thisMobPos.x, thisMobPos.y, thisMobPos.z);
        }

        while (!deletionQueue.isEmpty()){
            int thisMobGlobalID = deletionQueue.pop();
            //System.out.println("mob " + thisMobGlobalID + " was deleted!");
            deleteMob(thisMobGlobalID);
        }
    }


    @Override
    public void onTick(Entity entity, Player player, Delta delta) {

    }

    @Override
    public void onTick(Entity entity, InventoryLogic inventoryLogic, Player player, Delta delta) {

    }

    @Override
    public void onTick(Entity entity, SoundAPI soundAPI, InventoryLogic inventoryLogic, Player player, Delta delta) {

    }

    @Override
    public void onTick(Collision collision, Entity entity, SoundAPI soundAPI, InventoryLogic inventoryLogic, Player player, Delta delta) {

    }
}
