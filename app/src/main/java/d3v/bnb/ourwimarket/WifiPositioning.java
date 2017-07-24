package d3v.bnb.ourwimarket;

/**
 * Created by bruno on 07/22/2017.
 */
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Vector;

public class WifiPositioning
{
    // access points ----------------------------------------------------------------------

    public static final int SAMPLES_NODE_GRAPH = 30;    // tamanho da lista (graph) - 10
    public static final int MAX_COUNT = 5;              // numero de amostras para a posição média (addPosition)
    private static final int MAX_DISABLE_COUNT = 3;     // nº leituras ausentes para desactivar um Access Point definitivamente

    private static int[] matrixDimension;
    private static float[][] pdf = null;

    private int size;

    // o que esta classe gere -------------------------------------------------------------
    private static ArrayList<AccessPoint> access_points = new ArrayList<AccessPoint>();
    private static ArrayList<RadioMap> radio_maps = new ArrayList<RadioMap>();
    //-------------------------------------------------------------------------------------

    public WifiPositioning(ArrayList<AccessPoint> access_points, ArrayList<RadioMap> radio_maps, int[] matrixDimension)
    {
        WifiPositioning.access_points = access_points;
        WifiPositioning.radio_maps = radio_maps;

        WifiPositioning.matrixDimension = matrixDimension;

        pdf = new float[matrixDimension[0]][matrixDimension[1]];
    }

    //-------------------------------------------------------------------------------------
    // access points

    // devolve o numero de access points na lista
    public int getWifiListSize()
    {
        return access_points.size();
    }

    // devolve a lista de access points
    public ArrayList getWifiList()
    {
        return (ArrayList<AccessPoint>) access_points;
    }

    // adiciona novo access point à lista
    public void addWifiBSSID(String bssid, String ssid, int frequency)
    {
        access_points.add(new AccessPoint(bssid, ssid, 0, 0, 0));
    }

    // actualiza a lista de access points
    @SuppressWarnings("empty-statement")
    public void updateWifiList(ArrayList<HashMap<String, Object>> foundWifi)
    {
        // como a activação de Threads causa uma significativa demora, optou-se por não utiliza-las
        synchronized (this)
        {
            LinkedList visited = new LinkedList();
            HashMap<String, Object> rssi = new HashMap<String, Object>();

            // actualiza os activos
            for (int i = 0; i < foundWifi.size(); i++)
            {
                rssi.putAll(foundWifi.get(i));

                AccessPoint ac = (AccessPoint) access_points.get(rssi.get("id").hashCode());

                ac.ssid(rssi.get("ssid").toString());
                ac.dBm(rssi.get("level").hashCode());
                ac.frequency(rssi.get("frequency").hashCode());

                // só activa quando obter a moda deste access point
                if (ac.getMode() < 0)
                {
                    ac.isActive(true);
                }

                access_points.set(rssi.get("id").hashCode(), ac);

                visited.add(rssi.get("id").hashCode());
            }

            // desactiva os restantes
            for (int i = 0; i < access_points.size(); i++)
            {
                if (!visited.contains(i)) disable(i);
            }
        }
    }

    // desactiva um determinado Ponto de Acesso
    private void disable(int i)
    {
        boolean flag = false;

        AccessPoint ac = (AccessPoint) access_points.get(i);

        if (ac.isActive())
        {
            // desactiva de imediato ou efectua contagem decrescente
            if (Positioning.isOffImmediately())
            {
                flag = true;
            }
            else
            {
                // verifica a contagem decrescente para desactivação
                if (ac.disableCount() >= MAX_DISABLE_COUNT) flag = true;
            }

            if (flag)
            {
                // desactiva
                ac.isActive(false);

                // Elimina o histórico de um ap caso fique desactivado. (não funciona no radiomap)
                // Decidir o que quero, se eliminar as amostras ou manter as ultimas, mesmo desactualizadas
                if (Positioning.isDeleteDbmHistory() && !Positioning.isRadioMap())
                    ac.dBmClearList();

                // actualiza
                access_points.set(i, ac);
            }
        }
    }

    // procura por endereço MAC
    public int findWifiBSSID(String bssid)
    {
        for (int i = 0; i < access_points.size(); i++)
        {
            AccessPoint ac = (AccessPoint) access_points.get(i);

            if (bssid.equals(ac.bssid())) return i;
        }

        return -1;
    }

    // devolve true se o access point está activo
    public boolean isWifiActive(int id)
    {
        AccessPoint ac = (AccessPoint) access_points.get(id);

        return ac.isActive();
    }

    // devolve uma lista ordenada do mais perto para o mais longe dos pontos de acesso
    // activos e registados
    public LinkedList getAcessPointOrder()
    {
        LinkedList list = new LinkedList();

        for (int i = 0; i < access_points.size(); i++)
        {
            AccessPoint ac = (AccessPoint) access_points.get(i);

            if (ac.isValid())
            {
                list.addLast(i);
            }
        }

        for (int i = 0; i < list.size() - 1; i++)
        {
            for (int j = i; j < list.size(); j++)
            {
                AccessPoint ac1 = (AccessPoint) access_points.get(list.get(i).hashCode());
                AccessPoint ac2 = (AccessPoint) access_points.get(list.get(j).hashCode());

                if (ac1.getMode() < ac2.getMode())
                {
                    int tmp = list.get(i).hashCode();
                    list.set(i, list.get(j).hashCode());
                    list.set(j, tmp);
                }
            }
        }

        return list;
    }

    //-------------------------------------------------------------------------------------
    // radio maps

    // devolve a lista de radio maps
    public ArrayList getRadiomapList()
    {
        return (ArrayList<RadioMap>) radio_maps;
    }

    // actualiza os valores de uma celula do radiomap
    public void updateRadiomapBSSID(int pos, int col, int row, float avg, float error)
    {
        RadioMap rm = (RadioMap) radio_maps.get(pos);

        rm.addDbmMatrix(col, row, avg);
        rm.addErrorMatrix(col, row, error);
    }

    // procura por endereço MAC
    public int findRadiomapBSSID(String bssid)
    {
        for (int i = 0; i < radio_maps.size(); i++)
        {
            RadioMap rm = (RadioMap) radio_maps.get(i);

            if (rm.bssid().equals(bssid)) return i;
        }

        return -1;
    }

    // adiciona novo radio map à lista
    public void addRadiomapBSSID(String bssid, int col, int row, float avg)
    {
        RadioMap rm = new RadioMap();

        rm.bssid(bssid);
        rm.addDbmMatrix(col, row, avg);
        rm.addErrorMatrix(col, row, 0);

        radio_maps.add(rm);
    }

    // insere o erro na matrix de erro
    public void updateError(int pos, int col, int row, float error)
    {
        RadioMap rm = (RadioMap) radio_maps.get(pos);
        rm.addErrorMatrix(col, row, error);
    }

    //-----------------------------------------------------------------------------------------

    // Em teoria da probabilidade e estatística, correlação, também chamada de coeficiente de correlação,
    // indica a força e a direção do relacionamento linear entre duas variáveis aleatórias no uso estatístico geral
    public float[][] correlate()
    {
        //recebe a lista de access points activos e registados do mais perto para o mais longe
        LinkedList list = getAcessPointOrder();
        LinkedList visited = new LinkedList();

        boolean correspondence = true;

        float[] maxMatrix = new float[list.size()];
        float[][]  maxpos = new float[list.size()][2];

        for (int i = 0; i < list.size(); i++)
        {
            maxpos[i][0] = -1;
            maxpos[i][1] = -1;
        }

        float max = Float.MIN_VALUE;
        int bestMatrix = -1;

        //http://pt.wikipedia.org/wiki/Correlação

        // estatisticas
        //double     goodStdDev = Positioning.getGoodStdDev();
        //double      badStdDev = Positioning.getBadStdDev();
        //int toleranceVariance = Positioning.getMaxTolerance();

        int minAccessPointVisible = Positioning.getMinAccessPointVisible();

        // verifica todos os access points activos do mais perto para o mais longe
        for (int k = 0; k < minAccessPointVisible; k++)
        {
            AccessPoint ac = (AccessPoint) access_points.get(list.get(k).hashCode());

            double[] ic = ac.getConfidenceInterval();

            // Se estiver num intervalo de confiança de 95%, ACEITA
            if ((ac.getMode() >= (int) ic[0]) && (ac.getMode() <= (int) ic[0]))
            {
                int pos = findRadiomapBSSID(ac.bssid());

                // se existir no radio map valores referentes ao access point
                if (pos >= 0)
                {
                    RadioMap rm = (RadioMap) radio_maps.get(pos);

                    // obtem as coordenadas do radiomap
                    Vector vec = rm.coordinateList();

                    // se tiver valores gravados no radio map
                    if (!vec.isEmpty())
                    {
                        // incrementa o numero de access points validos
                        if (!visited.contains(list.get(k).hashCode())) visited.add(list.get(k).hashCode());

                        for (int i = 0; i < vec.size(); i++)
                        {
                            Vector coordinated = (Vector) vec.elementAt(i);

                            int col = (Integer) coordinated.elementAt(0);
                            int row = (Integer) coordinated.elementAt(1);

                            // dbm da posição
                            float dbmMatrix = rm.getDbmMatrix(col, row);

                            //processa calculos e apresenta em percentagem (perto = 1, longe = 0)
                            double square = (double) Util.square(ac.getMode() - dbmMatrix);
                            double result = (double) Math.exp(square / (5 * ac.getMode()));// exp = e^x

                            float higher = (float) (1 * result);

                            if (higher > maxMatrix[k])//-0,983471454- (posição mais proxima de cada matrix)
                            {
                                maxMatrix[k] = higher;
                                maxpos[k][0] = col;
                                maxpos[k][1] = row;

                                if (maxMatrix[k] > max)//-0,983471454- (posição mais proxima)
                                {
                                    max = higher;

                                    bestMatrix = k;
                                }
                            }
                        }
                    }
                }
            }

            // se pretendo tratar a informação só com o access point mais fidedigno (o mais perto),
            // fico por aqui e ignoro os seguintes
            if (!Positioning.isAllWifiTracking()) break;
        }

        String msg = "Visited: " + visited.size() + " | Best Array: " + bestMatrix + " | ";

        // se não processou nenhum ponto devido à baixa variancia, mantem a anterior posição
        if (visited.isEmpty() || bestMatrix == -1)
        {
            Positioning.setNote(msg + "Position INS only   ");

            return pdf;
        }

        // verifica se as posições das matrizes estão na mesma coordenada
        for (int k = 0; k < minAccessPointVisible; k++)
        {
            if (maxpos[k][0] != -1 && maxpos[k][1] != -1)
            {
                if ((maxpos[bestMatrix][0] != maxpos[k][0]) || (maxpos[bestMatrix][1] != maxpos[k][1]))
                {
                    correspondence = false;

                    break;
                }
            }
        }

        // se não existir correspondencia de posições,
        // verifica se a nova posição está dentro dos circulos da lateração
        if (!correspondence)
        {
            if (Positioning.isLaterationPosition((int) maxpos[bestMatrix][0], (int) maxpos[bestMatrix][1]))
            {
                Positioning.setNote(msg + "Lateration used     ");
            }
            else
            {
                Positioning.setNote(msg + "Unconfirmed!        ");

                return pdf;
            }
        }
        else
        {
            Positioning.setNote(msg + "All readings confirm");
        }

        // cria a matrix final da posição wifi
        size = Positioning.size;// actualiza o raio

        int originx = size;
        int originy = size;

        float[][] gauss = new float[size * 2 + 1][size * 2 + 1];

        double var = Util.square(Positioning.GAUSSIAN_CARTESIAN_ERROR);

        for (int col = -size; col <= size; col++)
        {
            for (int row = -size; row <= size; row++)
            {
                double x = row / 2.0;
                double y = col / 2.0;

                gauss[col + originy][row + originx] = (float) Math.exp(-Util.square(x) / (2*var) - Util.square(y) / (2*var));
            }
        }

        float[][] newpdf = new float[matrixDimension[0]][matrixDimension[1]];

        // sobrepõe as matrizes
        int col = (int) maxpos[bestMatrix][0];
        int row = (int) maxpos[bestMatrix][1];

        for (int coljoin = -size; coljoin <= size; coljoin++)
        {
            for (int rowjoin = -size; rowjoin <= size; rowjoin++)
            {
                // verifica de pode sobrepor uma celula cumprindo os limites da matrix
                if ((col + coljoin >= 0) && (col + coljoin < matrixDimension[0]))
                {
                    if ((row + rowjoin >= 0) && (row + rowjoin < matrixDimension[1]))
                    {
                        newpdf[col + coljoin][row + rowjoin] = (maxMatrix[bestMatrix]) * (gauss[originy - coljoin][originx - rowjoin]);
                    }
                }
            }
        }

        // actualiza o pdf
        pdf = newpdf;

        // media para evitar saltos enormes
        addPosition(maxpos[bestMatrix]);

        return pdf;
    }

    // variaveis utilizadas pelos metodos seguintes
    private static float[][] wifiPoint = new float[MAX_COUNT][2];
    private static float[] average = new float[2];
    private static int count = 0;

    public void addPosition(float[] newpos)
    {
        float[] d = new float[MAX_COUNT];
        double sum = 0;

        wifiPoint[count][0] = newpos[0];
        wifiPoint[count][1] = newpos[1];

        int length;

        if (wifiPoint[MAX_COUNT-1][0] + wifiPoint[MAX_COUNT-1][1] == 0)
        {
            length = count+1;
        }
        else
        {
            length = MAX_COUNT;
        }

        count++;

        if (count == MAX_COUNT) count = 0;

        if (length > 1)
        {
            for (int i = 0; i < length; i++)
            {
                d[i] = Util.distance(wifiPoint[i][0], wifiPoint[i][1], average[0], average[1]);
                sum += d[i];
            }

            average[0] = 0;
            average[1] = 0;

            for (int i = 0; i < length; i++)
            {
                average[0] += ((float) wifiPoint[i][0]) * (sum - d[i]) / ((length - 1) * sum);
                average[1] += ((float) wifiPoint[i][1]) * (sum - d[i]) / ((length - 1) * sum);
            }
        }
        else
        {
            average[0] = wifiPoint[0][0];
            average[1] = wifiPoint[0][1];
        }
    }

    // devolve a posição wifi
    public float[] wifiPoint()
    {
        float[] value = Util.copyArray(wifiPoint[count]);

        return value;
    }
}
