package d3v.bnb.ourwimarket;

/**
 * Created by bruno on 07/22/2017.
 */

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Vector;

public class Lateration //(multilateração)
{
    public static final int HUMAN_HEIGHT = 150; //altura mdia em cm de uma pessoa
    public static final int GAUSSIAN_SIZE = 2;

    private static ArrayList<AccessPoint> access_points;
    private static ArrayList<RadioMap> radio_maps;

    private static int[] matrixDimension;

    public Lateration(ArrayList<AccessPoint> access_points, ArrayList<RadioMap> radio_maps, int[] matrixDimension)
    {
        Lateration.access_points = access_points;
        Lateration.radio_maps = radio_maps;

        Lateration.matrixDimension = matrixDimension;
    }

    // actualiza a array do mapa de potências para esta classe
    public void updateLaterationRadioMap()
    {
        radio_maps = Positioning.getRadiomapList();
    }

    // formulas --------------------------------------------------------------------------
    // formulas utilizadas até 10 metros

    // com paredes (em casa)
    private double calcDistanceWithWall(double rssi)
    {
        // devolve em cm (dm * 10 = cm)
        return ((((-rssi - 40.0) + 1.0) * 2.0) + 8.0) * 10;
    }

    // sem paredes
    private double calcDistanceWithoutWall(double rssi)
    {
        // devolve em cm
        return Math.pow(10, (rssi + 2.425) / (-23.28));
    }

    // formula da potencia
    private double calcRSSI(double distance)
    {
        // devolve em dBm
        return -23.28 * Math.log10(distance) - 2.425;
    }

    //-----------------------------------------------------------------------------------

    public float getLaterationDistance(double rssi)
    {
        // devolve em cm
        if (Positioning.isObstacles())
            return (float) calcDistanceWithWall(rssi);
        else
            return (float) calcDistanceWithoutWall(rssi);
    }

    // confirma a credebilidade da posição
    public boolean isLaterationPosition(int col, int row)
    {
        float bestMatrix = Float.MIN_VALUE;

        Vector laterationList = correlate();

        // 1ª fase - Procura pela posição de maior valor da lateração
        if (laterationList != null)
        {
            for (int i = 0; i < laterationList.size(); i++)
            {
                Vector temp = (Vector) laterationList.elementAt(i);

                if ((Float) temp.elementAt(2) > bestMatrix)
                {
                    bestMatrix = (Float) temp.elementAt(2);
                }
            }

            // caso exista alguma posição
            if (bestMatrix != Float.MIN_VALUE)
            {
                // 2ª fase - verifica se a possivel posição corresponde efectivamente à melhor lateração
                for (int i = 0; i < laterationList.size(); i++)
                {
                    Vector temp = (Vector) laterationList.elementAt(i);

                    if (bestMatrix == (Float)   temp.elementAt(2) &&
                            col == (Integer) temp.elementAt(0) &&
                            row == (Integer) temp.elementAt(1)) return true;

                }
            }
        }

        return false;
    }

    // correlate
    public Vector correlate()
    {
        LinkedList list = Positioning.getAcessPointOrder();

        // no trilateration
        if (Positioning.getMinAccessPointVisible() - list.size() > Positioning.getLaterationTolerance())
            return null;

        int tolerance = Positioning.getLaterationSensitivity();
        float[][] newpdf = new float[matrixDimension[0]][matrixDimension[1]];

        int listSize = list.size();

        for (int k = 0; k < list.size(); k++)
        {
            // processa os pontos de acesso visiveis (activos)
            AccessPoint ac = (AccessPoint) access_points.get(list.get(k).hashCode());

            int pos = Positioning.findRadiomapBSSID(ac.bssid());

            // se existir no radiomap valores referentes ao mac (access point)
            if (pos >= 0)
            {
                RadioMap rm = (RadioMap) radio_maps.get(pos);

                // obtem as coordenadas do radiomap
                Vector vec = rm.coordinateList();

                // se tiver valores gravados no radio map
                if (!vec.isEmpty())
                {
                    for (int i = 0; i < vec.size(); i++)
                    {
                        Vector coordinated = (Vector)vec.elementAt(i);

                        int col = (Integer)coordinated.elementAt(0);
                        int row = (Integer)coordinated.elementAt(1);

                        // dbm da posição
                        //float dbmMatrix = rm.getDbmMatrix(col, row);

                        // ...que distancia teria se estivesse na posição (col/2, row/2) a 150cm do chão.
                        if (isLaterationPoint(ac, (col * 100) + (100 / 2), (row * 100) + (100 / 2), tolerance))
                            newpdf[col][row] += 1.0;
                    }
                }
            }
            else
            {
                // posso ter mais pontos de acesso activos do que os registados no radio map
                listSize--;
            }
        }

        Vector laterationList = new Vector();
        Vector pair = null;

        for (int col = 0; col < matrixDimension[0]; col++)
        {
            for (int row = 0; row < matrixDimension[1]; row++)
            {
                if (newpdf[col][row] != 0)
                {
                    pair = new Vector();

                    pair.addElement(new Integer( col ));
                    pair.addElement(new Integer( row ));
                    pair.addElement(new Float(newpdf[col][row] / listSize));

                    laterationList.addElement(pair);
                }
            }
        }

        if (laterationList.isEmpty())
            return null;

        return laterationList;
    }

    public boolean isLaterationPoint(AccessPoint ac, int x, int y, float tolerance)
    {
        // teorema de pitagoras
        // h^2 = c^2 + c^2

        // coloca o ponto no centro da coluna
        //int distance_x = (ac.distance_x() - (ac.distance_x() % 100)) + (100 / 2);

        // coloca o ponto no centro da linha
        //int distance_y = (ac.distance_y() - (ac.distance_y() % 100)) + (100 / 2);

        //        | A
        //       /|
        //      / |
        //     /  |
        //    /   |
        // B /----- C        distancia em 3D vai de A a B
        //  /     |
        // /-------

        float distance = Util.distance(ac.distance_x(), ac.distance_y(), ac.distance_z(), x, y, HUMAN_HEIGHT);

        //        | A
        //       /|
        //      / |
        //     /  |
        //    /   |
        // B /----- C        distancia em 2D vai de A a C
        //  /     |
        // /-------

        double realDistance = Math.sqrt(
                Math.pow(distance, 2) - Math.pow(ac.distance_z() - HUMAN_HEIGHT, 2));

        float laterationDistance = getLaterationDistance(ac.getMode()) + tolerance;

        if (realDistance <= laterationDistance)
            return true;

        return false;
    }
}

