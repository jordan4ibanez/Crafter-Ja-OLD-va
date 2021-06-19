package engine.compression;

import com.fasterxml.jackson.databind.ObjectMapper;
import engine.disk.ChunkSavingObject;
import game.chunk.ChunkObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Compression {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static byte[] convertChunkToCompressedByteArray(ChunkObject thisChunk) throws IOException {
        ChunkSavingObject savingObject = new ChunkSavingObject();

        savingObject.I = thisChunk.ID;
        savingObject.x = thisChunk.x;
        savingObject.z = thisChunk.z;
        savingObject.b = thisChunk.block;
        savingObject.r = thisChunk.rotation;
        savingObject.l = thisChunk.naturalLight;
        savingObject.t = thisChunk.torchLight;
        savingObject.h = thisChunk.heightMap;

        String stringedChunk = mapper.writeValueAsString(savingObject);

        //data byte conversions
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(stringedChunk.getBytes());


        //compression data stream
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream);

        //walk through raw bytes
        byte[] buffer = new byte[4096];
        int len;
        while((len=byteArrayInputStream.read(buffer)) != -1){
            gzipOutputStream.write(buffer, 0, len);
        }

        //close resources
        //this needs to be before the final send
        //this is because the final pieces of data are written
        //when you close them
        gzipOutputStream.close();
        byteArrayOutputStream.close();
        byteArrayInputStream.close();

        return byteArrayOutputStream.toByteArray();
    }

    public static ChunkObject decompressByteArrayToChunkObject(byte[] bytes) throws IOException {

        //decoded output stream
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        //raw data and decompression data input streams
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        GZIPInputStream gzipInputStream = new GZIPInputStream(byteArrayInputStream);

        //walk through raw bytes
        byte[] buffer = new byte[4096];
        int len;
        while((len = gzipInputStream.read(buffer)) != -1){
            byteArrayOutputStream.write(buffer, 0, len);
        }


        //close resources
        //this needs to be before the final send
        //this is because the final pieces of data are written
        //when you close them
        gzipInputStream.close();
        byteArrayInputStream.close();
        byteArrayOutputStream.close();

        //create new generic chunk saving object
        ChunkSavingObject thisChunkLoaded;

        //attempt to convert it from the decompressed string using Jackson
        try {
            thisChunkLoaded = mapper.readValue(byteArrayOutputStream.toString(), ChunkSavingObject.class);
        } catch (IOException e) {
            //silently return null
            return null;
        }

        //double safety
        if (thisChunkLoaded == null){
            return null;
        }

        //triple safety
        if (thisChunkLoaded.b == null){
            return null;
        }

        //assign compressed variables to full variable names
        ChunkObject abstractedChunk = new ChunkObject();

        abstractedChunk.ID = thisChunkLoaded.I;
        abstractedChunk.x = thisChunkLoaded.x;
        abstractedChunk.z = thisChunkLoaded.z;
        abstractedChunk.block = thisChunkLoaded.b;
        abstractedChunk.rotation = thisChunkLoaded.r;
        abstractedChunk.naturalLight = thisChunkLoaded.l;
        abstractedChunk.torchLight = thisChunkLoaded.t;
        abstractedChunk.heightMap = thisChunkLoaded.h;

        return abstractedChunk;
    }
}
