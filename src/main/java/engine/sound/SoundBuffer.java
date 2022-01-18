package engine.sound;

import engine.Utils;
import org.lwjgl.stb.STBVorbisInfo;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.stb.STBVorbis.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class SoundBuffer {
    public final int bufferId;

    private ShortBuffer pcm = null;
    private boolean lock = false;

    public SoundBuffer(String file) throws Exception{
        this.bufferId = alGenBuffers();
        try (STBVorbisInfo info = STBVorbisInfo.malloc()){
            ShortBuffer pcm = readVorbis(file, 16 * 1024, info);

            //copy to buffer
            alBufferData(bufferId, info.channels() == 1 ? AL_FORMAT_MONO16 : AL_FORMAT_STEREO16, pcm, info.sample_rate());
        }
    }

    public boolean isLocked(){
        return lock;
    }

    public void setLock(boolean isLocked){
        this.lock = isLocked;
    }

    public int getBufferId(){
        return this.bufferId;
    }

    public void cleanUp() {
        alDeleteBuffers(this.bufferId);
        if (pcm != null){
            MemoryUtil.memFree(pcm);
        }
    }

    private ShortBuffer readVorbis(String resource, int bufferSize, STBVorbisInfo info) throws Exception{
        try(MemoryStack stack = MemoryStack.stackPush()) {
            ByteBuffer vorbis = Utils.ioResourceToByteBuffer(resource, bufferSize);
            IntBuffer error = stack.mallocInt(1);
            long decoder = stb_vorbis_open_memory(vorbis, error, null);

            if (decoder == NULL) {
                throw new RuntimeException("Failed to open Ogg vorbis file. Error: " + error.get(0));

            }

            stb_vorbis_get_info(decoder, info);

            int channels = info.channels();

            int lengthSamples = stb_vorbis_stream_length_in_samples(decoder);

            pcm = MemoryUtil.memAllocShort(lengthSamples);

            pcm.limit(stb_vorbis_get_samples_short_interleaved(decoder, channels, pcm) * channels);

            stb_vorbis_close(decoder);

            return pcm;
        }
    }
}
