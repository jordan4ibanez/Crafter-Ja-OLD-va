package engine.sound;

import org.joml.Math;
import org.joml.Vector3d;
import org.joml.Vector3f;

import static engine.sound.SoundManager.playSoundSource;
import static org.lwjgl.openal.AL10.AL_PITCH;

public class SoundAPI {

    public static void playSound(String name, float posX, float posY, float posZ) {
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
        playSoundSource(soundBuffer, thisSource);
    }

    //overload for ease of use
    //intakes doubles XYZ and casts to float XYZ
    //precision is lost
    public static void playSound(String name, double posX, double posY, double posZ) {
        SoundBuffer soundBuffer = null;
        try {
            soundBuffer = new SoundBuffer("sounds/" + name + ".ogg");
        } catch (Exception e) {
            e.printStackTrace();
        }
        SoundSource thisSource = new SoundSource(false, false);
        thisSource.setBuffer(soundBuffer.getBufferId());

        thisSource.setPosition((float) posX, (float) posY, (float) posZ);
        playSoundSource(soundBuffer, thisSource);
    }

    public static void playSound(String name, Vector3f pos, boolean randomPitch) {
        SoundBuffer soundBuffer = null;
        try {
            soundBuffer = new SoundBuffer("sounds/" + name + ".ogg");
        } catch (Exception e) {
            e.printStackTrace();
        }
        SoundSource thisSource = new SoundSource(false, false);
        assert soundBuffer != null;
        thisSource.setBuffer(soundBuffer.getBufferId());
        thisSource.setPosition(pos);
        thisSource.setProperty(AL_PITCH, 0.75f + (float)(Math.random()/2f));
        playSoundSource(soundBuffer, thisSource);
    }


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
