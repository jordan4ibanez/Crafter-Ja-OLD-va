package engine.sound;

import org.joml.Math;

import static engine.sound.SoundManager.playSoundSource;
import static org.lwjgl.openal.AL10.AL_PITCH;

public class SoundAPI {

    public static void playSound(String name, float posX, float posY, float posZ, boolean randomPitch) {
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

        playSoundSource(soundBuffer, thisSource);
    }

    //overload for ease of use
    //intakes doubles XYZ and casts to float XYZ
    //precision is lost
    public static void playSound(String name, double posX, double posY, double posZ, boolean randomPitch) {
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

        playSoundSource(soundBuffer, thisSource);
    }


    //locationless sound playing
    public static SoundSource playSound(String name) {
        SoundBuffer soundBuffer = null;
        try {
            soundBuffer = new SoundBuffer("sounds/" + name + ".ogg");
        } catch (Exception e) {
            e.printStackTrace();
        }

        SoundSource thisSource = new SoundSource(false, true);

        assert soundBuffer != null;
        thisSource.setBuffer(soundBuffer.getBufferId());

        playSoundSource(soundBuffer, thisSource);

        return thisSource;
    }

    //puts music into the music buffer
    public static SoundSource playMusic(String name) {
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
        playSoundSource(soundBuffer, thisSource);
        return thisSource;
    }

    //play sound locationless with random pitch?
    public static void playSound(String name, boolean randomPitch) {
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

        playSoundSource(soundBuffer, thisSource);
    }
}
