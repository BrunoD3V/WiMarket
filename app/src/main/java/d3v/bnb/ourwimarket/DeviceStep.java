
package d3v.bnb.ourwimarket;

public class DeviceStep
{
    private static int countStep = 0;
    private static float distance = 0.0f;

    public static Object lockStep = new Object();

    // limpa o step
    public static void resetStep()
    {
        synchronized (lockStep)
        {
            distance = 0.0f;
            countStep = 0;
        }
    }

    // adiciona step
    public static void onStep()
    {
        synchronized (lockStep)
        {
            countStep ++;

            distance += (float)(Positioning.getStepLength() / 100.0); // centimeters/meters
        }
    }

    // devolve step
    public static int getStepCount()
    {
        synchronized (lockStep)
        {
            return countStep;
        }
    }

    // devolve a distancia dos passos
    public static int getStepDistance()
    {
        synchronized (lockStep)
        {
            // km para metro
            return (int)distance;
        }
    }
}
