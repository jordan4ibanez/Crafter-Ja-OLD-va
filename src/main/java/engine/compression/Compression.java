package engine.compression;

import com.fasterxml.jackson.databind.ObjectMapper;
import engine.disk.PrimitiveChunkObject;
import game.chunk.ChunkData;
import org.joml.Vector2i;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Compression {

    private final ObjectMapper mapper = new ObjectMapper();

    public byte[] convertChunkToCompressedByteArray(ChunkData thisChunk) throws IOException {
        PrimitiveChunkObject savingObject = new PrimitiveChunkObject(new Vector2i(thisChunk.x, thisChunk.z), thisChunk.block, thisChunk.rotation, thisChunk.light, thisChunk.heightMap);

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

    public ChunkData decompressByteArrayToChunkObject(byte[] bytes) throws IOException {

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
        PrimitiveChunkObject thisChunkLoaded;

        //attempt to convert it from the decompressed string using Jackson
        try {
            thisChunkLoaded = this.mapper.readValue(byteArrayOutputStream.toString(), PrimitiveChunkObject.class);
        } catch (IOException e) {
            //silently return null
            return null;
        }

        //double safety
        if (thisChunkLoaded == null){
            return null;
        }

        //triple safety
        if (thisChunkLoaded.block == null){
            return null;
        }

        //assign compressed variables to full variable names
        ChunkData abstractedChunk = new ChunkData();

        abstractedChunk.x = thisChunkLoaded.pos.x;
        abstractedChunk.z = thisChunkLoaded.pos.y;
        abstractedChunk.block = thisChunkLoaded.block;
        abstractedChunk.rotation = thisChunkLoaded.rotation;
        abstractedChunk.light = thisChunkLoaded.light;
        abstractedChunk.heightMap = thisChunkLoaded.heightMap;

        return abstractedChunk;
    }
}
