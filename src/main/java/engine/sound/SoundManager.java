package engine.sound;

import org.joml.Vector3d;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL11;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALCCapabilities;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import engine.graphics.Camera.*;
import engine.graphics.Transformation.*;
import engine.sound.SoundListener.*;
import org.lwjgl.openal.AL10.alDistanceModel;
import org.lwjgl.openal.ALC10.*;
import org.lwjgl.system.MemoryUtil.NULL;

public class SoundManager {
    private long device;
    private long context;
    private int currentIndex = 0;
    private final int maxSounds = 64;

    private SoundBuffer[] soundBufferList = new SoundBuffer[maxSounds];
    private SoundSource[] soundSourceArray = new SoundSource[maxSounds];

    private final Vector3d at = new Vector3d();
    private final Vector3d up = new Vector3d();

    public void initSoundManager() {
        device = alcOpenDevice((ByteBuffer) null);

        if(device == NULL){
            throw new IllegalStateException("Failed to open the default OpenAL device");
        }

        ALCCapabilities deviceCaps = ALC.createCapabilities(device);

        context = alcCreateContext(device, (IntBuffer) null);

        if(context == NULL){
            throw new IllegalStateException("Failed to create OpenAL context");
        }

        alcMakeContextCurrent(context);

        AL.createCapabilities(deviceCaps);

        setAttenuationModel(AL11.AL_LINEAR_DISTANCE);

        createSoundListener();
    }

    public void playSoundSource(SoundBuffer soundBuffer, SoundSource soundSource) {
        //THIS IS PRETTY HORRIBLE AND CAN CAUSE A FREEZE IF YOU PLAY 64 MUSIC TRACKS somehow
        //todo: make this better somehow
        boolean found = false;
        while (!found) {
            if (soundBufferList[currentIndex] != null && soundBufferList[currentIndex].isLocked()) {
                currentIndex++;
                if (currentIndex >= maxSounds) {
                    currentIndex = 0;
                }
                continue;
            }
            found = true;
            if (soundBufferList[currentIndex] != null) {
                soundBufferList[currentIndex].cleanUp();
            }
            soundBufferList[currentIndex] = soundBuffer;


            if (soundSourceArray[currentIndex] != null) {
                soundSourceArray[currentIndex].stop();
                soundSourceArray[currentIndex].cleanUp();
            }
            soundSourceArray[currentIndex] = soundSource;
            soundSourceArray[currentIndex].play();

            currentIndex++;
            if (currentIndex >= maxSounds) {
                currentIndex = 0;
            }
        }
    }

    public void updateListenerPosition() {

        // Update camera matrix with camera data
        updateOpenALSoundMatrix(getCameraPositionX(),getCameraPositionY(),getCameraPositionZ(), getCameraRotationX(), getCameraRotationY());
        setSoundPosition(getCameraPositionX(),getCameraPositionY(),getCameraPositionZ());
        getOpenALMatrix().positiveZ(at).negate();
        getOpenALMatrix().positiveY(up);
        setSoundOrientation(at.x, at.y, at.z, up.x, up.y, up.z);
    }

    public void setAttenuationModel(int model) {
        alDistanceModel(model);
    }

    public void cleanupSoundManager() {
        for (SoundSource soundSource : soundSourceArray) {
            if (soundSource != null) {
                soundSource.cleanUp();
            }
        }

        soundSourceArray = null;

        for (SoundBuffer soundBuffer : soundBufferList) {
            if (soundBuffer != null) {
                soundBuffer.cleanUp();
            }
        }

        soundBufferList = null;

        if (context != NULL) {
            alcDestroyContext(context);
        }

        if (device != NULL) {
            alcCloseDevice(device);
        }
    }
}
