package engine.sound;

final public class SoundListener {

    //0,0,0 is initial position
    public void createSoundListener(){
        alListener3f(AL_POSITION, 0,0,0);
        alListener3f(AL_VELOCITY, 0,0,0);
    }

    public void setSoundSpeed(float speedX, float speedY, float speedZ){
        alListener3f(AL_VELOCITY, speedX, speedY, speedZ);
    }

    //auto casted, sound imprecision is less noticeable
    public void setSoundPosition(double positionX, double positionY, double positionZ){
        alListener3f(AL_POSITION, (float)positionX,(float)positionY,(float)positionZ);
    }

    public void setSoundOrientation(double atX, double atY, double atZ, double upX, double upY, double upZ){
        float[] data = new float[]{
            (float)atX, (float)atY, (float)atZ,
            (float)upX, (float)upY, (float)upZ
        };
        alListenerfv(AL_ORIENTATION, data);
    }



}
