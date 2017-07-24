package d3v.bnb.ourwimarket;

/**
 * Created by bruno on 07/22/2017.
 */
// objecto que deriva do Inertial Navigation System
// 0 m/s2 along x axis
// 0 m/s2 along y axis
// 9,80665 m/s2 along z axis

public class Motion
{
    public float[] distance;
    public long time;

    public Motion()
    {
        this.distance = new float[2];
        this.time = 0;
    }

    public Motion(long time)
    {
        this.distance = new float[2];
        this.time = time;
    }

    public Motion(Motion m)
    {
        this.distance = Util.copyArray(m.distance);
        this.time = m.time;
    }
}
