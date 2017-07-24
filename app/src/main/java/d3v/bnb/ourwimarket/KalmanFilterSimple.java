/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package d3v.bnb.ourwimarket;

public class KalmanFilterSimple extends Thread
{
    
    
    //Creating  Arrays Representing Equations
        /*double[][] lhsArray = {{   3,   2,  -1}, 
                               {   2,  -2,   4}, 
                               {  -1, 0.5,  -1}};
        
        double[] rhsArray = {1, -2, 0};
 
        //Creating Matrix Objects with arrays
        Matrix lhs = new Matrix(lhsArray);
        Matrix rhs = new Matrix(rhsArray, 3);
 
        //Calculate Solved Matrix
        Matrix ans = lhs.solve(rhs);
 
        //Printing Answers
        System.out.println("x = " + Math.round(ans.get(0, 0)));
        System.out.println("y = " + Math.round(ans.get(1, 0)));
        System.out.println("z = " + Math.round(ans.get(2, 0)));
        
        //---------------------------------------------------------------
        
        // create a symmetric positive definite matrix
      double[][] a = new double[N][N];
      
        for (int i = 0; i < N; i++)
          for (int j = 0; j < N; j++)
              a[i][j] = 1.0 / (i + j + 1);
              
      Matrix A = new Matrix(a);
      Matrix B = A.inverse();
      Matrix I = Matrix.identity(N, N);

      if (N < 7) A.print(8, 6);
      System.out.println("condition number = " + A.cond());
      System.out.println("error = " + A.times(B).minus(I).normInf());*/
    
    
    private static float PERCENT_VAR = 0.05f;   //Noise variance estimation in percent
    private static float GAIN = 0.8f;           //Filter gain
    
    //private int dimension = WifiPositioning.SAMPLES_NODE_GRAPH;
    private static int dimension = 2;//initialValues.length;
    
    float[]     noiseVar = new float[dimension];// Noise variance
    float[]    corrected = new float[dimension];// Corrected/filtered value
    float[] predictedVar = new float[dimension];// Predicted variance
    float[]     observed = new float[dimension];// Observed value due to measurement
    float[]       kalman = new float[dimension];// The Kalman gain
    float[] correctedVar = new float[dimension];// The corrected variance
    float[]    predicted = new float[dimension];// The predicted value

    @Override
    public void run() 
    {
        try 
        {

        }
        catch(Exception e) 
        {
            
        }
    } 
    
    // Initializes the filter with some initial values and defines the dimension used.
    public void init() 
    {
        for (int i = 0; i < dimension; i++) 
        {
            noiseVar[i] = PERCENT_VAR;
            predicted[i] = 0.0f;
        }

        predictedVar = noiseVar;
    }

    //Updates the Kalman filter.
    public void update(final float[] observedValue) 
    {
        new Thread(new Runnable() 
        {
            public void run() 
            {
                // if dimensions do not match throw an exception
                if (observedValue.length != observed.length) 
                {
                    throw new RuntimeException("Array dimensions do not match");
                }

                observed = observedValue;

                // compute the Kalman gain for each dimension
                for (int i = 0; i < kalman.length; i++) 
                {
                    kalman[i] = predictedVar[i] / (predictedVar[i] + noiseVar[i]);
                }

                // update the sensor prediction with the measurement for each dimension
                for (int i = 0; i < corrected.length; i++) 
                {
                    corrected[i] = GAIN * predicted[i] + (1.0f - GAIN) * observed[i] + kalman[i] * (observed[i] - predicted[i]);
                }

                // update the variance estimation
                for (int i = 0; i < correctedVar.length; i++) 
                {
                    correctedVar[i] = predictedVar[i] * (1.0f - kalman[i]);
                }

                // System.out.println("Observed  X: " + observedValue[0]);
                // System.out.println("Observed  Y: " + observedValue[1]);
                // System.out.println("Predicted X: " + predicted[0]);
                // System.out.println("Predicted Y: " + predicted[1]);
                // System.out.println("Corrected X: " + corrected[0]);
                // System.out.println("Corrected Y: " + corrected[1]);

                // predict next variances and values
                predictedVar = correctedVar;
                predicted = corrected;
            }
        }).start();
    }
    
    // Provides the caller with the filtered values since the last update.
    // A float array storing the filtered values.
    public float[] getCorrectedValues() 
    {
        return corrected;
    }
}