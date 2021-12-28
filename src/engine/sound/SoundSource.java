package engine.sound;

import static org.lwjgl.openal.AL10.*;

public class SoundSource {
    private final int sourceId;

    public SoundSource(boolean loop, boolean relative){
        this.sourceId = alGenSources();

        if (loop){
            alSourcei(sourceId, AL_LOOPING, AL_TRUE);
        }

        if(relative){
            alSourcei(sourceId, AL_SOURCE_RELATIVE, AL_TRUE);
        }
    }

    public void setBuffer(int bufferId){
        stop();
        alSourcei(sourceId, AL_BUFFER, bufferId);
    }

    public void setPosition(float posX, float posY, float posZ){
        alSource3f(sourceId, AL_POSITION, posX, posY, posZ);
    }

    public void setGain(float gain){
        alSourcef(sourceId, AL_GAIN, gain);
    }

    public void setProperty(int param, float value){
        alSourcef(sourceId, param, value);
    }

    public void play(){
        alSourcePlay(sourceId);
    }

    public boolean isPlaying(){
        return alGetSourcei(sourceId, AL_SOURCE_STATE) == AL_PLAYING;
    }

    public void pause(){
        alSourcePause(sourceId);
    }

    public void stop(){
        alSourceStop(sourceId);
    }

    public void cleanUp(){
        stop();
        alDeleteSources(sourceId);
    }
}
