package engine;

public class Vector3dn {
    public double x;
    public double y;
    public double z;
    public String name;

    public Vector3dn(){
        x = 0;
        y = 0;
        z = 0;
        name = "";
    }

    public Vector3dn(String name, double x,double y,double z){
        this.x = x;
        this.y = y;
        this.z = z;
        this.name = name;
    }
}
