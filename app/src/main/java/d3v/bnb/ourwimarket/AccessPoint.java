package d3v.bnb.ourwimarket;

/**
 * Created by bruno on 07/22/2017.
 */

import android.net.wifi.WifiManager;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Vector;

public class AccessPoint
{
    private boolean averageFlag = false;

    private LinkedList mode;
    private LinkedList average;
    private LinkedList stddev;
    private LinkedList graph;

    //private KalmanFilterSimple kfs;

    private int disableCount = 0;

    private String bssid;
    public String bssid()                       {return bssid;}

    private String ssid;
    public void ssid(String ssid)               {this.ssid = ssid;}
    public String ssid()                        {return ssid;}

    private int distance_x;
    public void setDistance_x(int x)            {this.distance_x = x;}
    public int distance_x()                     {return distance_x;}

    private int distance_y;
    public void setDistance_y(int y)            {this.distance_y = y;}
    public int distance_y()                     {return distance_y;}

    private int distance_z;
    public void setDistance_z(int z)            {this.distance_z = z;}
    public int distance_z()                     {return distance_z;}

    private Boolean isActive;
    public void isActive(boolean isActive)      {this.isActive = isActive;}
    public boolean isActive()                   {return isActive;}

    private boolean isSelected;
    public void setSelected(boolean isSelected) {this.isSelected = isSelected;}
    public boolean isSelected()                 {return isSelected;}

    private int frequency;
    public void frequency(int frequency)        {this.frequency = frequency;}
    public int frequency()                      {return frequency;}

    public AccessPoint(String bssid, String ssid, int x, int y, int z)
    {
        mode = new LinkedList();
        average = new LinkedList();
        stddev = new LinkedList();
        graph = new LinkedList();

        //kfs = new KalmanFilterSimple();

        this.bssid = bssid;
        this.ssid = ssid;

        this.distance_x = x;
        this.distance_y = y;
        this.distance_z = z;

        this.isActive   = false;// "disable"
        this.isSelected = true; // por defeito ao carregar um mapa todos os pontos de acesso são selecionados
        this.frequency  = 0;    // frequency
    }

    // limpa o histório das leituras recolhidas
    public void dBmClearList()
    {
        // Standard Deviation (desvio padrão)
        // O desvio padrão define-se como a raiz quadrada da variância e
        // mede a variabilidade dos valores à volta da média

        average.clear();
        stddev.clear();
        graph.clear();
        mode.clear();
    }

    // recebe dBm
    public void dBm(int dBm)
    {
        int dif = 0;
        int last_dBm = dBm;

        disableCount = 0;

        if (mode.size() > 0)
        {
            last_dBm = mode.getLast().hashCode();
            dif = dBm - last_dBm;
        }

        // elimina a leitura mais desactualizada da lista mantendo as ultimas leituras
        if (mode.size() == Positioning.getSamplesMode()) mode.removeFirst();
        if (graph.size() == WifiPositioning.SAMPLES_NODE_GRAPH) graph.removeFirst();
        if (stddev.size() == Positioning.getSamplesStdDev()) stddev.removeFirst();

        int tolerance = Positioning.getMaxTolerance();

        if (dif >= 0 + tolerance)//(-50) entrou agora -(-60) ultimo a entrar -50+60 = 10
            dBm = last_dBm + tolerance;//-59

        if (dif <= 0 - tolerance)//(-50) entrou agora -(-40) ultimo a entrar -50+40 = -10
            dBm = last_dBm - tolerance;//-41

        stddev.addLast(dBm);
        graph.addLast(dBm);
        mode.addLast(dBm);

        // para a media
        if (averageFlag)
        {
            if (average.size() < Positioning.getSamplesAverage())
            {
                // se estiver dentro dos padrões
                if (getStdDev() < Positioning.getMaxVarianceAvg()) average.addLast(dBm);
            }
            else
            {
                averageFlag = false;
            }
        }
    }

    // contagem decrescente para desactivação do Access Point
    public int disableCount()
    {
        return ++disableCount;
    }

    // inicializa o calculo da média
    public void startCalcAvg()
    {
        average.clear();

        averageFlag = true;
    }

    // devolve o ultimo dbm capturado
    public int getLastdbm()
    {
        return mode.getLast().hashCode();
    }

    // devolve o progresso da media
    public int getAvgDialogProcess()
    {
        return average.size();
    }

    // devolve a ultima media durante a execução
    public float getLastCalcAvg()
    {
        float count = 0.0f;

        for (int i = 0; i < average.size(); i++)
            count += average.get(i).hashCode();

        return count / average.size();
    }

    // devolve a lista dos ultimos dBm recebidos
    public int[] getList()
    {
        int[] a = new int[WifiPositioning.SAMPLES_NODE_GRAPH];

        // como o ultimo é o mais actual, inverto a lista (grafico). Last in - First out
        for (int i = 0; i < graph.size(); i++)
            a[i] = graph.get(graph.size() - i - 1).hashCode(); // LIFO

        return a;
    }

    // devolve a moda
    public int getMode()
    {
        int values[] = new int[Positioning.getSamplesMode()];

        for (int i = 0; i < mode.size(); i++)
            values[i] = mode.get(i).hashCode();

        return mode(values);
    }

    // calcula a moda
    private static int mode(int[] values)
    {
        int temp_counter;
        int counter = 0;
        int num_occured = 0;

        for(int i = 0; i < values.length; i++)
        {
            temp_counter = 0;

            for(int k = 0; k < values.length; k++)
            {
                if(values[i] == values[k])
                {
                    temp_counter++;

                    if(counter < temp_counter)
                    {
                        counter = temp_counter;
                        num_occured = values[i];
                    }
                }
            }
        }

        return num_occured;
    }

    // devolve a média
    public float getMean(int[] values)
    {
        float sum = 0.0f;

        for(float a : values)
            sum += a;

        return sum / values.length;
    }

    // calcula a variancia
    public float getVariance()
    {
        int values[] = new int[Positioning.getSamplesStdDev()];

        for (int i = 0; i < stddev.size(); i++)
            values[i] = stddev.get(i).hashCode();

        float mean = getMean(values);
        float temp = 0.0f;

        for(float a : values)
            temp += (a - mean) * (a - mean);

        return temp / values.length;
    }

    // devolve o desvio padrão
    public double getStdDev()
    {
        return Math.sqrt(getVariance());
    }

    public double[] getConfidenceInterval()
    {
        // 95% approximate confidence interval
        // Intervalos de confiança são usados para indicar a confiabilidade de uma estimativa
        int values[] = new int[Positioning.getSamplesStdDev()];
        double ic[] = new double[2];

        for (int i = 0; i < stddev.size(); i++)
            values[i] = stddev.get(i).hashCode();

        float mean = getMean(values);
        double stddev = getStdDev();

        ic[0] = mean - 1.96 * stddev;
        ic[1] = mean + 1.96 * stddev;

        return ic;
    }

    // devolve a mediana
    public double median()
    {
        @SuppressWarnings("UseOfObsoleteCollectionType")
        Vector nums = new Vector();

        for (int i = 0; i < mode.size(); i++)
            nums.add(new Double(mode.get(i).hashCode()));

        Collections.sort(nums);

        int n = nums.size();
        int mid = n / 2;
        double median = (n % 2 != 0) ?
                ((Double) nums.get(mid)).doubleValue() :
                (((Double) nums.get(mid)).doubleValue() +
                        ((Double) nums.get(mid-1)).doubleValue()) / 2;

        return median;
    }

    // devolve o canal
    public int getChannel()
    {
        // Velocidade em Ondas
        // Fórmula: V = l . f
        // Uso: Usado para calcular a velocidade, frequência de uma onda
//para que? vou usar?
        int a = 0;

        switch (frequency)
        {
            case 2412:  a = 1;  break;
            case 2417:  a = 2;  break;
            case 2422:  a = 3;  break;
            case 2427:  a = 4;  break;
            case 2432:  a = 5;  break;
            case 2437:  a = 6;  break;
            case 2442:  a = 7;  break;
            case 2447:  a = 8;  break;
            case 2452:  a = 9;  break;
            case 2457:  a = 10; break;
            case 2462:  a = 11; break;
            case 2467:  a = 12; break;
            case 2472:  a = 13; break;
            case 2484:  a = 14; break;
            default:    a = 0;  break;
        }

        return a;
    }

    // verifica se é um ponto de acesso válido
    public boolean isValid()
    {
        if (isActive() && distance_x() + distance_y() + distance_z() > 0)
            return true;

        return false;
    }

    // devolve o nivel de qualidade de sinal
    public int getSignalLevel()
    {
        return WifiManager.calculateSignalLevel(getMode(), 5);
    }


    //aproveitar isto. testar em substituição do tolerance (comentar linhas)
    private static final float ALPHA = 0.2f;

    /**
     * Filter the given input against the previous values and return a low-pass filtered result.
     *
     * @param input float array to smooth.
     * @param prev float array representing the previous values.
     * @return float array smoothed with a low-pass filter.
     */
    public static float[] lowPassFilter(float[] input, float[] prev) {
        if (input==null || prev==null)
            throw new NullPointerException("input and prev float arrays must be non-NULL");
        if (input.length!=prev.length)
            throw new IllegalArgumentException("input and prev must be the same length");

        for ( int i=0; i<input.length; i++ ) {
            prev[i] = prev[i] + ALPHA * (input[i] - prev[i]);
        }
        return prev;
    }

}
