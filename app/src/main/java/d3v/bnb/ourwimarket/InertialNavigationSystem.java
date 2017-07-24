package d3v.bnb.ourwimarket;

/**
 * Created by bruno on 07/22/2017.
 */

import java.util.LinkedList;

public class InertialNavigationSystem
{
    public static final int MAX_SIZE = 10;// fiz o projecto com 1000, porquê? Muitos objectos "motion" para ocupar memoria?

    private LinkedList<Motion> motions;
    private float stepLength;

    public InertialNavigationSystem()
    {
        motions = new LinkedList<Motion>();
    }

    public void reset()
    {
        if (!motions.isEmpty()) motions.clear();

        stepLength = Positioning.getStepLength() / 100;

        motions.add(new Motion(System.currentTimeMillis()));
    }

    public void addMotion()
    {
        if (motions.size() == MAX_SIZE)
        {
            motions.removeFirst();
        }

        long time = System.currentTimeMillis();
        // velocidade (V) = variação do espaço(D) dividido pela varição de tempo (T)
        // V=D/T

        float dt = (time - motions.getLast().time) / 1000.0f;
        float[] d = Util.copyArray(motions.getLast().distance);

        Motion current = new Motion(time);

        if (DeviceSensor.isMoving())
        {
            // distancia
            float distance = stepLength * dt;
//fazer confirmação pela velocidade? não pode ultrapassar x km/hora logo não valida o passo?
            // direcção
            float heading = (float) Math.toRadians(DeviceSensor.getCompassHeading() + (Positioning.getCompassMap() - 90));

            current.distance[0] = d[0] + distance * (float) Math.sin(heading);
            current.distance[1] = d[1] + distance * (float) Math.cos(heading);
        }
        else
        {
            current.distance[0] = d[0];
            current.distance[1] = d[1];
        }

        motions.addLast(current);
    }

    public float[] displacement()
    {
        float[] d = Util.copyArray(motions.getLast().distance);

        return d;
    }
}
