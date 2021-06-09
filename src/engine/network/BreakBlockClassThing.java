package engine.network;

import org.joml.Vector3i;

public class BreakBlockClassThing {
    public Vector3i breakingPos;

    public BreakBlockClassThing(){

    }

    public BreakBlockClassThing(Vector3i thePos){
        this.breakingPos = thePos;
    }
}
