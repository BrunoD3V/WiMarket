package d3v.bnb.ourwimarket;

/**
 * Created by bruno on 07/22/2017.
 */


public class Util
{
    public static void copyArray(int[] a, int[] b)
    {
        for (int i = 0; i < a.length; i++)
            a[i] = b[i];
    }

    public static void copyArray(float[] a, float[] b)
    {
        for (int i = 0; i < a.length; i++)
            a[i] = b[i];
    }

    public static int[] copyArray(int[] a)
    {
        int[] r = new int[a.length];

        for (int i = 0; i < a.length; i++)
            r[i] = a[i];

        return r;
    }

    public static float[] copyArray(float[] a)
    {
        float[] r = new float[a.length];

        for (int i = 0; i < a.length; i++)
            r[i] = a[i];

        return r;
    }

    /*
     * ============================================================================
     * Mathematics methods
     * ============================================================================
     */

    public static int getMin(int a, int b)
    {
        if (a > b)
            return b;
        else
            return a;
    }

    public static int getMin(int[] values)
    {
        int smallest = Integer.MAX_VALUE;

        for (int i = 0; i < values.length; i++)
        {
            if (values[i] < smallest)
                smallest = values[i];
        }

        return smallest;
    }

    public static int getMax(int a, int b)
    {
        if (a > b)
            return a;
        else
            return b;
    }

    // devolve o RSSI maximo da lista dos ultimos valores adquiridos desse access point
    public static int getMax(int[] values)
    {
        int largest = Integer.MIN_VALUE;

        for (int i = 0; i < values.length; i++)
        {
            if (values[i] > largest)
                largest = values[i];
        }

        return largest;
    }

    public static boolean between(int x, int a, int b)
    {
        int min = getMin(a, b);
        int max = getMax(a, b);

        if ((x >= min) && ( x <= max))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public static float magnitude(float[] v)
    {
        float result = 0;

        for (int i = 0; i < v.length; i++)
        {
            result += square(v[i]);
        }

        result = (float) Math.sqrt(result);
        return result;
    }

    public static double magnitude(double x, double y)
    {
        return Math.sqrt(square(x) + square(y));
    }

    public static int distancePointLine(int x, int y, int[] p1, int[] p2, int[] m)
    {
        int distance = Integer.MAX_VALUE;

        if (p1[0] == p2[0])
        {
            if (between(y, p1[1], p2[1]))
            {
                distance = Math.abs(p1[0] - x);
                m[0] = p1[0]; m[1] = y;
            }
            else
            {
                if (distance(x, y, p1[0], p1[1]) > distance(x, y, p2[0], p2[1]))
                {
                    m[0] = p1[0]; m[1] = p1[1];
                    distance = (int) distance(x, y, p1[0], p1[1]);
                }
                else
                {
                    m[0] = p2[0];
                    m[1] = p2[1];
                    distance = (int) distance(x, y, p2[0], p2[1]);
                }
            }
        }
        else if (p1[1] == p2[1])
        {
            if (between(x, p1[0], p2[0]))
            {
                distance = Math.abs(p1[1] - y);
                m[0] = x;
                m[1] = p1[1];
            }
            else
            {
                if (distance(x, y, p1[0], p1[1]) > distance(x, y, p2[0], p2[1]))
                {
                    m[0] = p1[0];
                    m[1] = p1[1];
                    distance = (int) distance(x, y, p1[0], p1[1]);
                }
                else
                {
                    m[0] = p2[0];
                    m[1] = p2[1];
                    distance = (int) distance(x, y, p2[0], p2[1]);
                }
            }
        }

        return distance;
    }

    public static float distance(int x1, int y1, int x2, int y2)
    {
        return (float) Math.sqrt(square(x1 - x2) + square(y1 - y2));
    }

    public static float distance(int x1, int y1, int z1, int x2, int y2, int z2)
    {
        return (float) Math.sqrt(square(x1 - x2) + square(y1 - y2) + square(z1 - z2));
    }

    public static float distance(double x1, double y1, double x2, double y2)
    {
        return (float) Math.sqrt(square(x1 - x2) + square(y1 - y2));
    }

    public static float distance(double x1, double y1, double z1, double x2, double y2, double z2)
    {
        return (float) Math.sqrt(square(x1 - x2) + square(y1 - y2) + square(z1 - z2));
    }

    public static int square(int n)
    {
        return n*n;
    }

    public static float square(float n)
    {
        return n*n;
    }

    public static double square(double n)
    {
        return n*n;
    }

    /*
     * ============================================================================
     * Equations
     * ============================================================================
     */

    // declive da recta
    public static float slopeStraightX(float x1, float y1, float x2, float y2, float x)
    {
        // m = (y2 - y1) / (x2 - x1)
        float m = (y2 - y1) / (x2 - x1);

        // y - y1 = m(x - x1)
        float y = m * (x - x1) + y1;

        return y;
    }

    public static float slopeStraightY(float x1, float y1, float x2, float y2, float y)
    {
        // m = (y2 - y1) / (x2 - x1)
        float m = (y2 - y1) / (x2 - x1);

        // y - y1 = m(x - x1)
        float x = (y - y1) / m + x1;

        return x;
    }

    /*
     * ============================================================================
     * Other methods
     * ============================================================================
     */

    public static float adjustAngle(float degrees)
    {
        while (degrees < 0)
        {
            degrees += (float) 359.0;
        }

        while (degrees > 359)
        {
            degrees -= (float) 359.0;
        }

        // se ainda for negativo ou muito alto
        if (degrees < 0 || degrees > 359) adjustAngle(degrees);

        return degrees;
    }

    public static boolean inside(int x, int y, int left, int top, int right, int bottom)
    {
        if ((x < left) || (x > right) || (y < top) || (y > bottom))
        {
            return false;
        }
        else
        {
            return true;
        }
    }

    public static boolean inside(int[] p, int left, int top, int right, int bottom)
    {
        return inside(p[0], p[1], left, top, right, bottom);
    }

        /*DeviceWriter out = new DeviceWriter("teste.tst");

        for (int i = 0; i < newpdf[0].length; i++)
        {
            for (int j = 0; j < newpdf.length; j++)
            {
                out.write(newpdf[j][i] + "\t");
            }

            out.write("\n");
        }

        out.close();*/

    // Encontrando a determinante
    public static double determinant(double[][] matriz)
    {
        double result = 0;

        if (matriz.length == 1)
        {
            result = matriz[0][0];

            return result;
        }

        if (matriz.length == 2)
        {
            result = matriz[0][0] * matriz[1][1] - matriz[0][1] * matriz[1][0];

            return result;
        }

        for (int i = 0; i < matriz[0].length; i++)
        {
            double temp[][] = new double[matriz.length - 1][matriz[0].length - 1];

            for (int j = 1; j < matriz.length; j++)
            {
                for (int k = 0; k < matriz[0].length; k++)
                {

                    if (k < i)
                    {
                        temp[j-1][k] = matriz[j][k];
                    }
                    else if (k > i)
                    {
                        temp[j-1][k-1] = matriz[j][k];
                    }
                }
            }

            result += matriz[0][i] * Math.pow(-1, (double) i) * determinant(temp);
        }

        return result;
    }
}
