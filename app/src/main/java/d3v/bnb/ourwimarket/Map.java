package d3v.bnb.ourwimarket;

/**
 * Created by bruno on 07/22/2017.
 */

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Map extends Activity
{
    public static final float NORTH = 0;//-0.221992086112412f;

    private static String file; // planta a carregar
    private static float scale; // como o mapa tem 200px de largura e devia ter 100px é dividido por metade
    // quanto menor for o valor, maior o mapa -> 0.5 = 200; 0.25 = 400
    // 1 pixel = 0.1 meter
    private static int northMap;
    private static int[] entry;

    private static Bitmap map;

    //normalizar as medidas para metro
    public Map(String file, float scale, int northMap, int[] entry)
    {
        if (file.equals("") || scale == 0)
        {
            file = "no_image.png";
            scale = 0.0f;
            northMap = 0;

            entry[0] = 0;
            entry[1] = 0;
        }

        Map.file  = file;
        Map.scale = scale;
        Map.northMap = northMap;
        Map.entry = entry;

        map = BitmapFactory.decodeFile("/sdcard/" + file);
    }

    // north - a distância em direção ao norte, em metros
    //  east - a distância em direção a este, em metros
    // return the distance in x and y axis in pixel
    public static float[] toMapCS(float[] csEarth)
    {
        // Input coordinate system:
        // ^ y = north
        // |
        // |
        // |
        // |
        // o---------> x = east
        //
        // >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
        // Building coordinate system
        //
        // y \    | north
        //    \ a |       ,
        //     \  |     , x
        //      \ |   ,
        //       \| ,
        //        o
        //
        // Roration Matrix for rotate the coordinate system "a" degree
        //
        // [   cos a sin a ] * [ x ]
        // [ - sin a cos a ]   [ y ]
        // >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
        // Map coordinate system
        //
        // \ a | north ,
        //  \  |     , x
        //   \ |   ,
        //    \| ,
        //     o
        //      \
        //       \
        //        \y
        // Flip Matrix
        //
        // [ 1  0 ] * [ x ]
        // [ 0 -1 ]   [ y ]

        float[] csBuilding = new float[2];// building coordinate system
        float[] csMap = new float[2];// map coordinate system
        float a = -Map.NORTH;// rotation angle (counterclockwise)

        // rotate earth coordinate system to get building coordinate system
        // [   cos a sin a ] * [ x ]
        // [ - sin a cos a ]   [ y ]
        csBuilding[0] = (float) (  Math.cos(a) * csEarth[0] + Math.sin(a) * csEarth[1]);
        csBuilding[1] = (float) (- Math.sin(a) * csEarth[1] + Math.cos(a) * csEarth[1]);

        // flip building coordinate system and convert from meters to pixels
        // to get map coordinate system
        // [ 1  0 ] * [ x ]
        // [ 0 -1 ]   [ y ]
        csMap[0] = csBuilding[0];
        csMap[1] = -csBuilding[1];

        return csMap;
    }

    //Convert the vector from cm to pixels
    public static float[] toMapScale(float[] cm)
    {
        return toMapScale(cm[0], cm[1]);
    }

    public static float toMapScale(float value)
    {
        float pixel;

        // passa para centimetros
        value = value / 10;

        pixel = value / Map.scale;

        return pixel;
    }

    //Convert the vector from cm to pixels
    public static float[] toMapScale(float x, float y)
    {
        float[] pixel = new float[2];

        pixel[0] = toMapScale(x);
        pixel[1] = toMapScale(y);

        return pixel;
    }

    // devolve a dimensão do mapa em pixels
    public float[] getMapDimension()
    {
        float[] dim = new float[2];

        dim[0] = map.getWidth();
        dim[1] = map.getHeight();

        return dim;
    }

    // calcula a escala do mapa para matriz do "Map activity"
    public float getMapResolutionScaleCell()
    {
        return 10 / Map.scale;
    }

    // devolve a dimensão da matriz
    public int[] getMatrixDimension()
    {
        int[] dim = new int[2];

        dim[0] = Math.round(map.getWidth()  / getMapResolutionScaleCell());
        dim[1] = Math.round(map.getHeight() / getMapResolutionScaleCell());

        // arredonda para cima
        if (map.getWidth()  / getMapResolutionScaleCell() >  0) dim[0] += 1;
        if (map.getHeight() / getMapResolutionScaleCell() >  0) dim[1] += 1;

        return dim;
    }

    // recebe a distancia em pixels (na escala) e devolve a posição da celula no mapa
    public int[] getMapCelPosition(float x, float y)
    {
        int pos[] = new int[2];

        float resolution = getMapResolutionScaleCell();

        for (int i = 0; i < x - resolution; i += resolution)
            pos[0] += (int)resolution;

        for (int i = 0; i < y - resolution; i += resolution)
            pos[1] += (int)resolution;

        return pos;
    }

    // recebe a distancia em cm e devolve a posição da celula no mapa
    public int[] getMatrixCellPosition(float x, float y)
    {
        float resolution = getMapResolutionScaleCell();
        int[] cel = getMapCelPosition(toMapScale(x), toMapScale(y));

        int[] pos = new int[2];
        pos[0] = (int) (cel[0] / resolution) + 1;
        pos[1] = (int) (cel[1] / resolution) + 1;

        return pos;
    }

    // devolve a imagem do mapa
    public Bitmap getMap()
    {
        return map;
    }

    // devolve o nome do arquivo do mapa
    public String getFileMap()
    {
        return file;
    }

    // devolve a escala do mapa
    public float getFileScale()
    {
        return scale;
    }

    // devolve a escala do mapa
    public int[] getFileEntry()
    {
        return entry;
    }

    // devolve o norte do mapa
    public int getFileNorth()
    {
        return northMap;
    }
}

