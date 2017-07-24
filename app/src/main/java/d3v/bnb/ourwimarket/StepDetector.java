
package d3v.bnb.ourwimarket;

import android.hardware.SensorListener;
import android.hardware.SensorManager;

/**
 * Detects steps and notifies all listeners (that implement StepListener).
 * @author Levente Bagi
 * @todo REFACTOR: SensorListener is deprecated
 */
@SuppressWarnings("deprecation")
public class StepDetector implements SensorListener 
{
    private float[] lastValues = new float[3*2];
    private float[] scale = new float[2];

    private float yOffset;

    private float[] lastDirections = new float[3*2];
    private float[][] lastExtremes = { new float[3*2], new float[3*2] };
    private float[] lastDiff = new float[3*2];

    private int lastMatch = -1;
    
    public StepDetector()
    {
        int h = 480;

        yOffset = h * 0.5f;

        scale[0] = - (h * 0.5f * (1.0f / (SensorManager.STANDARD_GRAVITY * 2)));
        scale[1] = - (h * 0.5f * (1.0f / (SensorManager.MAGNETIC_FIELD_EARTH_MAX)));
    }
   
    @Override
    public void onAccuracyChanged(int sensor, int accuracy)
    {
        // Not used
    }
    
    @Override
    public void onSensorChanged(int sensor, float[] values)
    {
        synchronized (this)
        {
            if (sensor == SensorManager.SENSOR_ORIENTATION) 
            {
            }
            else
            {
                int j = (sensor == SensorManager.SENSOR_MAGNETIC_FIELD) ? 1 : 0;

                if (j == 0)
                {
                    float vSum = 0;

                    for (int i=0 ; i<3 ; i++)
                    {
                        final float v = yOffset + values[i] * scale[j];
                        vSum += v;
                    }

                    int k = 0;
                    float v = vSum / 3;
                    
                    float direction = (v > lastValues[k] ? 1 : (v < lastValues[k] ? -1 : 0));

                    if (direction == - lastDirections[k])
                    {
                        // Direction changed
                        int extType = (direction > 0 ? 0 : 1); // minumum or maximum?

                        lastExtremes[extType][k] = lastValues[k];

                        float diff = Math.abs(lastExtremes[extType][k] - lastExtremes[1 - extType][k]);

                        if (diff > Positioning.getSensitivity())
                        {
                            boolean isAlmostAsLargeAsPrevious = diff > (lastDiff[k]*2/3);   // é quase tão grande quanto Anterior
                            boolean isPreviousLargeEnough = lastDiff[k] > (diff/3);         // o Anterior é grande o suficiente
                            boolean isNotContra = (lastMatch != 1 - extType);               // não é Contra
                            
                            if (isAlmostAsLargeAsPrevious && isPreviousLargeEnough && isNotContra)
                            {
                                // passo dado confirmado
                                DeviceStep.onStep();

                                lastMatch = extType;
                            }
                            else
                            {
                                lastMatch = -1;
                            }
                        }

                        lastDiff[k] = diff;
                    }

                    lastDirections[k] = direction;
                    lastValues[k] = v;
                }
            }
        }
    }
}
