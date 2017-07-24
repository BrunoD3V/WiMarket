package d3v.bnb.ourwimarket;

/**
 * Created by bruno on 07/22/2017.
 */


import java.util.Vector;

public class RadioMap //(ao contrario do accesspoint.java este tem a matriz completa por cada ponto de acesso)
{
    private final int col;// = 100;
    private final int row;// = 100;

    private float[][] map;
    private float[][] error;
    private float[][] neighbor;

    private String bssidMatrix;

    public void bssid(String bssid)   {this.bssidMatrix = bssid;}
    public String bssid()             {return bssidMatrix;}

    // radiomap
    public RadioMap()
    {
        // obtem a matrix referente ao mapa utilizado (1ª vez)
        int[] matrixDimension = Positioning.getMatrixDimension();

        col = matrixDimension[0];
        row = matrixDimension[1];

        map = new float[col][row];
        error = new float[col][row];
        neighbor = new float[col][row];
    }

    // através do home (load file)
    public RadioMap(int[] matrixDimension)
    {
        col = matrixDimension[0];
        row = matrixDimension[1];

        map = new float[col][row];
        error = new float[col][row];
        neighbor = new float[col][row];
    }

    public void addDbmMatrix(int col, int row, float dbm)           {map[col][row] = dbm;}
    // colocar actualizar neighbor
    public void addErrorMatrix(int col, int row, float dbmError)    {error[col][row] = dbmError;}

    public float getDbmMatrix(int col, int row)                     {return map[col][row];}
    public float getErrorMatrix(int col, int row)                   {return error[col][row];}

    // devolve as coordenadas das celulas originais e os dBm respectivos
    public Vector coordinateList()
    {
        Vector pair = null; // Define dados das linhas do vetor
        Vector vec = new Vector(); //um container para as linhas definidadas acima

        for (int j = 0; j < row; j++)
        {
            //for (int i = 0; i < col; i++)
            for (int i = (col - 1); i >= 0; i--)// causa/motivo: radiomap pushpin
            {
                if(isMatrix(i, j))
                {
                    pair = new Vector();

                    pair.addElement(new Integer( i ));
                    pair.addElement(new Integer( j ));
                    pair.addElement(new Float(map[i][j]));

                    vec.addElement(pair);
                }
            }
        }

        return vec;
    }

    public boolean isMatrix(int col, int row)
    {
        if (map[col][row] < 0)
            return true;

        return false;
    }

    // devolve as coordenadas das celulas originais e os dBm respectivos
    public Vector neighborList()
    {
        Vector pair = null; // Define dados das linhas do vetor
        Vector vec = new Vector(); //um container para as linhas definidadas acima

        for (int i = 0; i < col; i++)
        {
            for (int j = 0; j < row; j++)
            {
                if(neighbor[i][j] < 0)
                {
                    pair = new Vector();

                    pair.addElement(new Integer( i ));
                    pair.addElement(new Integer( j ));
                    pair.addElement(new Float(neighbor[i][j]));

                    vec.addElement(pair);
                }
            }
        }

        return vec;
    }

    // cria uma matrix de possiveis dBm vizinhos
    public void neighborAdapting()
    {
        // obtem as coordenadas do radiomap
        Vector vec = coordinateList();

        if (vec.isEmpty()) return;

        for (int i = 0; i < col; i++)
        {
            System.arraycopy(map[i], 0, neighbor[i], 0, row);
        }

        int size = 2;//Positioning.size; // actualiza o raio

        int originx = size;
        int originy = size;

        float[][] gauss = new float[size*2 + 1][size*2 + 1];

        double var = -(-2);//diferença estimada de dbm (positivo);

        // prepara a matriz da diminuição prevista
        for (int col = -size; col <= size; col++)
        {
            for (int row = -size; row <= size; row++)
            {
                double x = row / 2.0;
                double y = col / 2.0;

                gauss[col + originy][row + originx] = (float) Math.exp(-Util.square(x) / (2*var) - Util.square(y) / (2*var));
            }
        }

        // adapta a previsão
        for (int i = 0; i < vec.size(); i++)
        {
            Vector coordinated = (Vector) vec.elementAt(i);

            int col = (Integer)coordinated.elementAt(0);
            int row = (Integer)coordinated.elementAt(1);

            for (int coljoin = -size; coljoin <= size; coljoin++)
            {
                for (int rowjoin = -size; rowjoin <= size; rowjoin++)
                {
                    // verifica de pode sobrepor uma celula cumprindo os limites da matrix
                    if ((col + coljoin >= 0) && (col + coljoin < neighbor.length))
                    {
                        if ((row + rowjoin >= 0) && (row + rowjoin < neighbor[0].length))
                        {
                            if (!isMatrix(col + coljoin, row + rowjoin))
                            {
                                neighbor[col + coljoin][row + rowjoin] =
                                        coordinated.elementAt(2).hashCode() * (gauss[originy - coljoin][originx - rowjoin]);
                            }
                        }
                    }
                }
            }
        }
    }

    public String toSave()
    {
        StringBuilder sb = new StringBuilder();

        for (int j = 0; j < row; j++)
        {
            for (int i = 0; i < col; i++)
            {
                if (i + 1 < col) sb.append(String.valueOf(map[i][j])).append("\t");
                else sb.append(String.valueOf(map[i][j]));
            }

            sb.append("\n");
        }

        return sb.toString();
    }
}
