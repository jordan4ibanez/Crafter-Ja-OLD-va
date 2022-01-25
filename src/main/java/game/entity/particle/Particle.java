package game.entity.particle;

import engine.graphics.Mesh;
import engine.graphics.Texture;
import engine.sound.SoundAPI;
import engine.time.Delta;
import game.blocks.BlockDefinitionContainer;
import game.chunk.Chunk;
import game.crafting.InventoryLogic;
import game.entity.Entity;
import game.entity.EntityContainer;
import game.entity.collision.Collision;
import game.entity.collision.ParticleCollision;
import game.player.Player;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.*;

public class Particle extends Entity {

    private final Mesh mesh;


    private final Vector3i currentFlooredPos = new Vector3i();

    private final Deque<Integer> deletionQueue = new ArrayDeque<>();

    public Particle(EntityContainer entityContainer, Vector3d pos, Vector3f inertia, float width, float height, boolean item, boolean mob, boolean particle) {
        super(entityContainer, pos, inertia, width, height, item, mob, particle);

        BlockDefinitionContainer blockDefinitionContainer = new BlockDefinitionContainer();
        mesh = createParticleMesh(blockDefinitionContainer, 5, new Texture("textures/textureAtlas.png"));
    }

    public void onTick(BlockDefinitionContainer blockDefinitionContainer, ParticleCollision collision, Delta delta){

        double dtime = delta.getDelta();


        collision.applyInertia(this.getPos(), this.getInertia(), true,true);

        float timer = (float) (this.getTimer() + dtime);

        this.setTimer(timer);

        if (timer > 1f){
            this.delete();
        }


    }


    public Mesh getMesh(){
        return mesh;
    }

    private Mesh createParticleMesh(BlockDefinitionContainer blockDefinitionContainer, int blockID, Texture textureAtlas) {

        final float textureScale = (float)Math.ceil(Math.random() * 3f);
        final float pixelScale = (float)(int)textureScale / 25f;

        final float pixelX = (float)Math.floor(Math.random()*(16f-(textureScale+1f)));
        final float pixelY = (float)Math.floor(Math.random()*(16f-(textureScale+1f)));

        final float pixelXMin = pixelX/16f/32f;
        final float pixelXMax = (pixelX+textureScale)/16f/32f;

        final float pixelYMin = pixelY/16f/32f;
        final float pixelYMax = (pixelY+textureScale)/16f/32f;


        final float[] positions    = new float[12];
        final float[] textureCoord = new float[8];
        final int[] indices        = new int[6];
        final float[] light        = new float[12];

        //front
        positions[0]  = (pixelScale);
        positions[1]  = (pixelScale*2);
        positions[2]  = (0f);
        positions[3]  = (-pixelScale);
        positions[4]  = (pixelScale*2);
        positions[5]  = (0f);
        positions[6]  = (-pixelScale);
        positions[7]  = (0f);
        positions[8]  = (0f);
        positions[9]  = (pixelScale);
        positions[10] = (0f);
        positions[11] = (0f);
        
        //front
        for (int i = 0; i < 12; i++) {
            light[i] = 1;
        }
        //front
        indices[0] = (0);
        indices[1] = (1);
        indices[2] = (2);
        indices[3] = (0);
        indices[4] = (2);
        indices[5] = (3);

        final int selection = (int)Math.floor(Math.random()*6f);

        float[] texturePoints = switch (selection) {
            case 1 -> blockDefinitionContainer.getBackTexturePoints(blockID, (byte) 0);
            case 2 -> blockDefinitionContainer.getRightTexturePoints(blockID, (byte) 0);
            case 3 -> blockDefinitionContainer.getLeftTexturePoints(blockID, (byte) 0);
            case 4 -> blockDefinitionContainer.getTopTexturePoints(blockID);
            case 5 -> blockDefinitionContainer.getBottomTexturePoints(blockID);
            default -> blockDefinitionContainer.getFrontTexturePoints(blockID, (byte) 0);
        };


        //front
        textureCoord[0] = (texturePoints[0] + pixelXMax);//1
        textureCoord[1] = (texturePoints[2] + pixelYMin);//2
        textureCoord[2] = (texturePoints[0] + pixelXMin);//0
        textureCoord[3] = (texturePoints[2] + pixelYMin);//2
        textureCoord[4] = (texturePoints[0] + pixelXMin);//0
        textureCoord[5] = (texturePoints[2] + pixelYMax);//3
        textureCoord[6] = (texturePoints[0] + pixelXMax);//1
        textureCoord[7] = (texturePoints[2] + pixelYMax);//3

        return new Mesh(positions, light, indices, textureCoord, textureAtlas);
    }

    /*
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

     */
}