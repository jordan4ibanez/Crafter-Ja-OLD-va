package engine.sound;

import org.joml.Vector3f;

import static engine.sound.SoundManager.playSoundSource;

public class SoundAPI {

    public static void playSound(String name, Vector3f pos) {
        SoundBuffer soundBuffer = null;
        try {
            soundBuffer = new SoundBuffer("sounds/" + name + ".ogg");
        } catch (Exception e) {
            e.printStackTrace();
        }
        SoundSource thisSource = new SoundSource(false, false);
        thisSource.setBuffer(soundBuffer.getBufferId());
        thisSource.setPosition(pos);
        playSoundSource(soundBuffer, thisSource);
    }

    public static void playSound(String name) {
        SoundBuffer soundBuffer = null;
        try {
            soundBuffer = new SoundBuffer("sounds/" + name + ".ogg");
        } catch (Exception e) {
            e.printStackTrace();
        }

        SoundSource thisSource = new SoundSource(false, true);

        thisSource.setBuffer(soundBuffer.getBufferId());

        playSoundSource(soundBuffer, thisSource);
    }
}
