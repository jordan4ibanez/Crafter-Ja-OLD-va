package engine.sound;

import engine.Window;
import engine.graphics.Camera;
import org.joml.Math;
import org.joml.Vector3d;

import static org.lwjgl.openal.AL10.AL_PITCH;

public class SoundAPI {

    private SoundManager soundManager;
    private Camera camera;
    private Window window;

    public SoundAPI(){

    }

    public void setCamera(Camera camera){
        if (this.camera == null){
            this.camera = camera;
        }
    }
    public void setWindow(Window window){
        if (this.window == null){
            this.window = window;
            this.soundManager = new SoundManager(this.camera, this.window);
        }
    }

    public void updateListenerPos(){
        this.soundManager.updateListenerPosition();
    }

    public void playSound(String name, float posX, float posY, float posZ, boolean randomPitch) {
        SoundBuffer soundBuffer = null;
        try {
            soundBuffer = new SoundBuffer("sounds/" + name + ".ogg");
        } catch (Exception e) {
            e.printStackTrace();
        }
        SoundSource thisSource = new SoundSource(false, false);
        assert soundBuffer != null;

        thisSource.setBuffer(soundBuffer.getBufferId());

        thisSource.setPosition(posX, posY, posZ);

        if (randomPitch){
            thisSource.setProperty(AL_PITCH, 0.75f + (float)(Math.random()/2f));
        }

        soundManager.playSoundSource(soundBuffer, thisSource);
    }

    //overload for ease of use
    //intakes doubles XYZ and casts to float XYZ
    //precision is lost
    public void playSound(String name, double posX, double posY, double posZ, boolean randomPitch) {
        SoundBuffer soundBuffer = null;
        try {
            soundBuffer = new SoundBuffer("sounds/" + name + ".ogg");
        } catch (Exception e) {
            e.printStackTrace();
        }
        SoundSource thisSource = new SoundSource(false, false);

        assert soundBuffer != null;

        thisSource.setBuffer(soundBuffer.getBufferId());

        thisSource.setPosition((float) posX, (float) posY, (float) posZ);

        if (randomPitch){
            thisSource.setProperty(AL_PITCH, 0.75f + (float)(Math.random()/2f));
        }

        soundManager.playSoundSource(soundBuffer, thisSource);
    }


    //locationless sound playing
    public SoundSource playSound(String name) {
        SoundBuffer soundBuffer = null;
        try {
            soundBuffer = new SoundBuffer("sounds/" + name + ".ogg");
        } catch (Exception e) {
            e.printStackTrace();
        }

        SoundSource thisSource = new SoundSource(false, true);

        assert soundBuffer != null;
        thisSource.setBuffer(soundBuffer.getBufferId());

        soundManager.playSoundSource(soundBuffer, thisSource);

        return thisSource;
    }

    //puts music into the music buffer
    public SoundSource playMusic(String name) {
        SoundBuffer soundBuffer = null;
        try {
            soundBuffer = new SoundBuffer("sounds/" + name + ".ogg");
        } catch (Exception e) {
            e.printStackTrace();
        }
        assert soundBuffer != null;
        soundBuffer.setLock(true);
        SoundSource thisSource = new SoundSource(false, true);
        thisSource.setBuffer(soundBuffer.getBufferId());
        soundManager.playSoundSource(soundBuffer, thisSource);
        return thisSource;
    }

    //play sound locationless with random pitch?
    public void playSound(String name, boolean randomPitch) {
        SoundBuffer soundBuffer = null;
        try {
            soundBuffer = new SoundBuffer("sounds/" + name + ".ogg");
        } catch (Exception e) {
            e.printStackTrace();
        }

        SoundSource thisSource = new SoundSource(false, true);

        assert soundBuffer != null;
        thisSource.setBuffer(soundBuffer.getBufferId());
        thisSource.setProperty(AL_PITCH, 0.75f + (float)(Math.random()/2f));

        soundManager.playSoundSource(soundBuffer, thisSource);
    }
}
