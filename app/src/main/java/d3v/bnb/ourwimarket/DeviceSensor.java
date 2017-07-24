package d3v.bnb.ourwimarket;

/**
 * Created by bruno on 07/22/2017.
 */


import android.hardware.SensorManager;
import java.util.LinkedList;

public class DeviceSensor
{
    // secção de processamento da bussula
    private static final float MOVEMENT = 0.5f;
    private static final int   WINDOW_SUMMATION_SIZE = 30; // 50 * 20 ms = 1 second
    private static final float EARTH_GRAVITY = SensorManager.GRAVITY_EARTH; // 9.80755f
    private static final float EARTH_GEOMAGNETIC = 47.0f;
    private static final float THRESHOLD_GRAVITY = 1.0f;
    private static final float THRESHOLD_GEOMAGNETIC = 3.0f;
    private static final float RAD_TO_DEG = (float)(180.0f / Math.PI);

    public static final float MAX_VALUES = 150.0f;// tem de ser menor que o screen width

    private static float[] devA = new float[3];
    private static float[] devM = new float[3];
    private static float[] devO = new float[3];

    private static float[] gravity = new float[3];
    private static float[] geomagnetic = new float[3];

    private static float[] refA = new float[3];
    private static float[] refO = new float[3];
    private static float[] R = new float[9];
    private static float[] I = new float[9];

    private static float incl;

    private static boolean isMoving = false;

    private static int wsPosition = 0;
    private static float wsAggregate = 0;
    private static float[] wsSequence = new float[WINDOW_SUMMATION_SIZE];

    private static Object lockSensing = new Object();
    private static Object lockMoving = new Object();

    // controla o tempo do acelerometro (xyz)
    private static LinkedList<float[]> fifoAccelerometer = new LinkedList<float[]>();
    private static long timekeeper = android.os.SystemClock.uptimeMillis();

    // ACCELEROMETER
    public static void setDevA(float[] raw)
    {
        synchronized (lockSensing)
        {
            // devA -> linha; raw -> pontos
            Util.copyArray(devA, raw);

            if (android.os.SystemClock.uptimeMillis() >= timekeeper + 20)
            {
                timekeeper = android.os.SystemClock.uptimeMillis();

                if (fifoAccelerometer.size() > MAX_VALUES) fifoAccelerometer.poll();

                fifoAccelerometer.add(raw);
            }

            aggregrateMotion();

            float magnitude = Util.magnitude(devA);
            float threshold = THRESHOLD_GRAVITY;

            if (Math.abs(magnitude - EARTH_GRAVITY) <= threshold)
            {
                Util.copyArray(gravity, devA);
            }
        }
    }

    // MAGNETIC_FIELD
    public static void setDevM(float[] raw)
    {
        synchronized (lockSensing)
        {
            Util.copyArray(devM, raw);

            float magnitude = Util.magnitude(devM);

            if (Math.abs(magnitude - EARTH_GEOMAGNETIC) <= THRESHOLD_GEOMAGNETIC)
            {
                Util.copyArray(geomagnetic, devM);
            }
        }
    }

    // ORIENTATION
    public static void setDevO(float[] raw)
    {
        synchronized (lockSensing)
        {
            Util.copyArray(devO, raw);
        }
    }

    public static boolean toEarthCS()
    {
        // Lei de Gravitação
        // Fórmula: g=G.M/r^2
        // Fórmula: g=G(M+m)/r^2
//perceber melhor isto
        synchronized (lockSensing)
        {
            // When current gravity and current geomagnetic are known
            if ((Util.magnitude(devA) > 0) && (Util.magnitude(devM) > 0))
            {
                SensorManager.getRotationMatrix(R, I, gravity, geomagnetic);
                SensorManager.getOrientation(R, refO);

                refA[0] = devA[0] * R[0] + devA[1] * R[1] + devA[2] * R[2];
                refA[1] = devA[0] * R[3] + devA[1] * R[4] + devA[2] * R[5];
                refA[2] = devA[0] * R[6] + devA[1] * R[7] + devA[2] * R[8];

                incl = SensorManager.getInclination(I);

                return true;
            }
            else
            {
                return false;
            }
        }
    }

    public static float[] getRefAcceleration()
    {
        float[] result = new float[3];

        synchronized (lockSensing)
        {
            Util.copyArray(result, refA);
        }

        return result;
    }

    // devolve em graus a bussula
    public static float getCompassHeading()//bussula
    {
        float degrees;

        synchronized (lockSensing)
        {
            degrees = devO[0];
        }

        return 359.0f - degrees;
    }

    // devolve em graus a posição do mapa
    public static float getMapHeading()//mapa
    {
        if (!Positioning.isRotateMap()) return 0.0f;

        float degrees;

        synchronized (lockSensing)
        {
            degrees = devO[0];

            degrees = Positioning.getCompassMap() - degrees; // negativo para rodar à esquerda
        }

        return degrees;
    }

    // devolve em graus a posição do mapa para roteamento (para descobrir para que lado está virado)
    public static float getRouteHeading()
    {
        float degrees;

        synchronized (lockSensing)
        {
            degrees = devO[0];

            degrees = degrees - Positioning.getCompassMap();
        }

        return degrees;
    }

    // devolve fifo
    public static LinkedList<float[]> getAccelerometerFifo()
    {
        return fifoAccelerometer;
    }

    // limpa fifo
    public static void resetAccelerometer()
    {
        fifoAccelerometer.clear();
    }

    // verifica se está em movimento
    public static boolean isMoving()
    {
        synchronized (lockMoving)
        {
            return isMoving;
        }
    }

    // inclinação
    public static int getInclination()
    {
        return (int)(incl * RAD_TO_DEG);
    }

    // força do movimento (confirma movimento ou não)
    private static void aggregrateMotion()
    {
        float magnitude = Util.magnitude(devA);

        wsPosition++;

        if (wsPosition == WINDOW_SUMMATION_SIZE) wsPosition = 0;

        wsAggregate = wsAggregate - wsSequence[wsPosition] + magnitude;
        wsSequence[wsPosition] = magnitude;

        int length;

        if (wsSequence[WINDOW_SUMMATION_SIZE - 1] == 0)
        {
            length = wsPosition + 1;
        }
        else
        {
            length = WINDOW_SUMMATION_SIZE;
        }

        float mean = wsAggregate / length; // the arithmetic mean (average)
        float stdev = 0; // the standard deviation

        for (int i = 0; i < length; i++)
        {
            stdev += (wsSequence[i] - mean) * (wsSequence[i] - mean);
        }

        stdev = (float) Math.sqrt(stdev / length);

        if (stdev > MOVEMENT)
        {
            isMoving = true;

        }
        else
        {
            isMoving = false;
        }
    }
}
