package engine.sound;

import org.joml.Vector3d;
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

    //overload for ease of use
    public static void playSound(String name, Vector3d pos) {
        SoundBuffer soundBuffer = null;
        try {
            soundBuffer = new SoundBuffer("sounds/" + name + ".ogg");
        } catch (Exception e) {
            e.printStackTrace();
        }
        SoundSource thisSource = new SoundSource(false, false);
        thisSource.setBuffer(soundBuffer.getBufferId());

        Vector3f floatedPos = new Vector3f();

        floatedPos.x = (float)pos.x;
        floatedPos.y = (float)pos.y;
        floatedPos.z = (float)pos.z;

        thisSource.setPosition(floatedPos);
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
