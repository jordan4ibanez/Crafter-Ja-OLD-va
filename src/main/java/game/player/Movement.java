package game.player;

import engine.Controls;
import engine.graphics.Camera;
import engine.time.Delta;
import game.chunk.Chunk;
import game.entity.collision.Collision;
import org.joml.Vector2f;
import org.joml.Vector3d;
import org.joml.Vector3f;

public class Movement {
    private final float height           = 1.9f;
    private final float width            = 0.3f;
    private final float collectionHeight = 0.7f;
    private final float eyeHeight        = 1.5f;
    private int oldY                     = 0;
    private boolean sneaking             = false;
    private boolean running              = false;
    private boolean jumping              = false;
    private boolean onGround             = false;
    private boolean wasOnGround          = false;

    private boolean inWater = false;
    private boolean wasInWater = false;
    private float wasInWaterTimer = 0f;
    private boolean waterLockout = false;

    private final Vector3d pos = new Vector3d(0,0,0);
    private final Vector3d oldPos = new Vector3d(0,0,0);
    private final Vector3d posWithEyeHeight = new Vector3d(pos.x,pos.y + eyeHeight,pos.z);
    private final Vector3d posWithCollectionHeight = new Vector3d();
    private final Vector3f inertia       = new Vector3f(0,0,0);
    private final Vector3f inertiaBuffer = new Vector3f(0,0,0);

    public Movement(){
    }

    public void onTick(Chunk chunk, Camera camera, Controls controls, Player player, Delta delta, Collision collision){
        oldPos.set(pos);
        wasOnGround = onGround;

        if (wasInWaterTimer > 0.f){
            //System.out.println(wasInWaterTimer);
            waterLockout = true;
            wasInWaterTimer -= delta.getDelta();
            if (wasInWaterTimer <= 0){
                //System.out.println("turned off lockout");
                waterLockout = false;
            }
        }

        if (jumping && onGround){
            jumping = false;
        }

        //values for application of inertia
        applyInertiaBuffer(camera,delta,controls);


        //stop players from falling forever
        //this only applies their inertia if they are within a loaded chunk, IE
        //if the server doesn't load up something in time, they freeze in place
        if (chunk.chunkExists(player.getPlayerCurrentChunk())) {
            onGround = collision.applyInertia(pos, inertia, true, width, height, true, sneaking, true, true, true);
        }

        //apply the eyeHeight offset to the eyeHeight position
        posWithEyeHeight.set(pos.x, pos.y + eyeHeight, pos.z);

        //apply the collection height offset to the collection position
        posWithCollectionHeight.set(pos.x, pos.y + collectionHeight, pos.z);

        //play sound when player lands on the ground
        if (onGround && !wasOnGround){
            System.out.println("skadoosh");
            //playSound("dirt_" + (int)(Math.ceil(Math.random()*3)));
        }

        //fall damage
        if (onGround){

            int currentY = (int)Math.floor(pos.y);

            if (currentY < oldY){
                if (oldY - currentY > 6){
                    player.hurtPlayer(oldY - currentY - 6);
                }
            }
            oldY = currentY;
        }
    }

    public Vector3d getOldPos(){
        return oldPos;
    }

    public void setPos(Vector3d newPos) {
        pos.set(newPos.x,newPos.y, newPos.z);
    }

    public Vector3d getPos(){
        return pos;
    }

    public void addInertia(Vector3f inertia){
        this.inertia.add(inertia);
    }

    public void setInertia(Vector3f inertia){
        this.inertia.set(inertia);
    }

    public Vector3f getInertia(){
        return inertia;
    }

    public void setJumping(boolean jumping){
        this.jumping = jumping;
    }

    public boolean getJumping(){
        return(jumping);
    }

    public void setRunning(boolean running){
        if (!sneaking && this.running) {
            this.running = running;
        } else {
            this.running = false;
        }
    }

    public boolean getRunning(){
        return running;
    }

    public void setInWater(boolean inWater){
        this.inWater = inWater;
        if (inWater && jumping){
            jumping = false; //reset jumping mechanic
        }
    }

    public void setSneaking(boolean sneaking){
        this.sneaking = sneaking;
    }

    public boolean getSneaking() {
        return sneaking;
    }


    private void applyInertiaBuffer(Camera camera, Delta delta, Controls controls){
        double dtime = delta.getDelta();

        float accelerationMultiplier = 0.07f;
        float movementAcceleration = 1000.f;

        if (controls.getForward()){
            float yaw = (float)Math.toRadians(camera.getCameraRotation().y) + (float)Math.PI;
            inertia.x += (float)(Math.sin(-yaw) * accelerationMultiplier) * movementAcceleration * dtime;
            inertia.z += (float)(Math.cos(yaw)  * accelerationMultiplier) * movementAcceleration * dtime;
        }
        if (controls.getBackward()){
            //no mod needed
            float yaw = (float)Math.toRadians(camera.getCameraRotation().y);
            inertia.x += (float)(Math.sin(-yaw) * accelerationMultiplier) * movementAcceleration * dtime;
            inertia.z += (float)(Math.cos(yaw)  * accelerationMultiplier) * movementAcceleration * dtime;
        }

        if (controls.getRight()){
            float yaw = (float)Math.toRadians(camera.getCameraRotation().y) - (float)(Math.PI /2);
            inertia.x += (float)(Math.sin(-yaw) * accelerationMultiplier) * movementAcceleration * dtime;
            inertia.z += (float)(Math.cos(yaw)  * accelerationMultiplier) * movementAcceleration * dtime;
        }

        if (controls.getLeft()){
            float yaw = (float)Math.toRadians(camera.getCameraRotation().y) + (float)(Math.PI /2);
            inertia.x += (float)(Math.sin(-yaw) * accelerationMultiplier) * movementAcceleration * dtime;
            inertia.z += (float)(Math.cos(yaw)  * accelerationMultiplier) * movementAcceleration * dtime;
        }

        if (!inWater && controls.getJump() && onGround){
            inertia.y += 8.75f; //do not get delta for this
            jumping = true;
            //the player comes to equilibrium with the water's surface
            // if this is not implemented like this
        } else if (controls.getJump() && inWater && !waterLockout){
            wasInWater = true;
            if(inertia.y <= 4.f){
                inertia.y += 100.f * dtime;
            }
        }

        if (wasInWater && !inWater){
            wasInWaterTimer = 0.2f;
        }

        inertia.x += inertiaBuffer.x;
        inertia.y += inertiaBuffer.y;
        inertia.z += inertiaBuffer.z;

        //max speed todo: make this call from a player object's maxSpeed!
        Vector2f inertia2D = new Vector2f(inertia.x, inertia.z);

        float maxSpeed;

        if (sneaking) {
            maxSpeed = 1.f;
        } else if (running) {
            maxSpeed = 6.f;
        } else {
            maxSpeed = 4.f;
        }

        //speed limit the player's movement
        if(inertia2D.isFinite() && inertia2D.length() > maxSpeed){
            inertia2D.normalize().mul(maxSpeed);
            inertia.x = inertia2D.x;
            inertia.z = inertia2D.y;
        }

        //reset buffer
        inertiaBuffer.x = 0f;
        inertiaBuffer.y = 0f;
        inertiaBuffer.z = 0f;
    }

    public float getHeight(){
        return height;
    }
    public float getWidth(){
        return width;
    }

    public Vector3d getPosEyeHeight(){
        return posWithEyeHeight;
    }

    public Vector3d getPosCollectionHeight(){
        return posWithCollectionHeight;
    }

    public Boolean getOnGround() {
        return onGround;
    }
}
