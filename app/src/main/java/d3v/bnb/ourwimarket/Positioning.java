package d3v.bnb.ourwimarket;

/**
 * Created by bruno on 07/22/2017.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Vector;

public class Positioning
{
    private static final int GAUSSIAN_SIZE = 3;                     // 3 meters
    private static final int GAUSSIAN_MAX_SIZE = 20;                // 20 meters

    public  static final float GAUSSIAN_CARTESIAN_ERROR = 2.0f;     // 2 meters

    private static final float GAUSSIAN_POLAR_THETA_ERROR = (float) Math.PI / 4.0f;
    private static final float GAUSSIAN_POLAR_RADIUS_ERROR = 1.0f;

    private static final int CONVERGING_PERIOD = 5000;              // 5 seconds

    private static int[] matrixDimension;

    //probability density function
    private static float[][] pdf = null;
    private static float[][] pdfWifi = null;

    private static Map map;

    private static WifiPositioning wifi;
    private static Lateration lateration;
    private static InertialNavigationSystem ins;

    private static Integrate integrate;
    //private static KalmanFilterSimple kfs;

    private static boolean integrateProcess = false;

    private static int integrateCount;
    private static int processCount;

    private static Object lockerPdf;
    private static Object lockerWifi;
    private static Object lockerMap;
    private static Object lockerINS;
    private static Object lockerSettings;
    private static Object lockerLateration;
    private static Object lockerKalman;

    private static int levelView;
    public  static int size;

    // inicia o wimarket ---------------------------------------------------------------
    public static void init(
            String file, // nome do ficheiro (mapa)
            float scale, // escala do mapa
            int northMap,// norte em graus
            int[] entry, // posição inicial
            ArrayList<AccessPoint> access_points, // pontos de acesso
            ArrayList<RadioMap> radio_maps)       // mapa de potencias
    {
        map = new Map(file, scale, northMap, entry);                //mapa, escala, norte do mapa e a posição inicial

        matrixDimension = map.getMatrixDimension();

        pdf = new float[matrixDimension[0]][matrixDimension[1]];
        pdfWifi = new float[matrixDimension[0]][matrixDimension[1]];

        lateration = new Lateration(access_points, radio_maps, matrixDimension);       // lateração
        wifi = new WifiPositioning(access_points, radio_maps, matrixDimension);  // os pontos de acesso e o radiomap
        ins = new InertialNavigationSystem();                                   // movimento

        levelView = 0;
        size = GAUSSIAN_SIZE;

        lockerPdf = new Object();
        lockerWifi = new Object();
        lockerMap = new Object();
        lockerINS = new Object();
        lockerSettings = new Object();
        lockerLateration = new Object();
        lockerKalman = new Object();

        integrate = new Integrate();

        //kfs = new KalmanFilterSimple();
    }

    // ideia basica
    // Direction(compass) + Displacement(accelerometer) = Directional Trail

    //wifi ------------------------------------------------------------------------------
    public static void updateWifiList(ArrayList<HashMap<String, Object>> foundWifi)
    {
        synchronized(lockerWifi)                {wifi.updateWifiList(foundWifi);}
    }

    public static void updateError(int pos, int col, int row, float error)
    {
        synchronized(lockerWifi)                {wifi.updateError(pos, col, row, error);}
    }

    public static void updateRadiomapBSSID (int pos, int col, int row, float avg, float error)
    {
        synchronized(lockerWifi)                {wifi.updateRadiomapBSSID(pos, col, row, avg, error);}
    }

    public static void addWifiBSSID(String bssid, String ssid, int frequency)
    {
        synchronized(lockerWifi)                {wifi.addWifiBSSID(bssid, ssid, frequency);}
    }

    public static void addRadiomapBSSID(String bssid, int col, int row, float avg)
    {
        synchronized(lockerWifi)                {wifi.addRadiomapBSSID(bssid, col, row, avg);}
    }

    public static float[] wifiPoint()
    {
        synchronized (lockerWifi)               {return Util.copyArray(wifi.wifiPoint());}
    }

    public static ArrayList getWifiList()
    {
        synchronized(lockerWifi)                {return wifi.getWifiList();}
    }

    public static ArrayList getRadiomapList()
    {
        synchronized(lockerWifi)                {return wifi.getRadiomapList();}
    }

    public static int findWifiBSSID(String bssid)
    {
        synchronized(lockerWifi)                {return wifi.findWifiBSSID(bssid);}
    }

    public static int findRadiomapBSSID(String bssid)
    {
        synchronized(lockerWifi)                {return wifi.findRadiomapBSSID(bssid);}
    }

    public static boolean isWifiActive(int id)
    {
        synchronized(lockerWifi)                {return wifi.isWifiActive(id);}
    }

    public static LinkedList getAcessPointOrder()
    {
        synchronized(lockerWifi)                {return wifi.getAcessPointOrder();}
    }

    //kalman filter -----------------------------------------------------------------------

    public static float[] wifiKalman()
    {
        synchronized (lockerKalman)             {return Util.copyArray(integrate.getCorrectedValues());}
    }

    //map ---------------------------------------------------------------------------------

    public static Bitmap getMap()
    {
        synchronized(lockerMap)                 {return map.getMap();}
    }

    public static String getFileMap()
    {
        synchronized(lockerMap)                 {return map.getFileMap();}
    }

    public static float getFileScale()
    {
        synchronized(lockerMap)                 {return map.getFileScale();}
    }

    public static int getFileNorth()
    {
        synchronized(lockerMap)                 {return map.getFileNorth();}
    }

    public static int[] getFileEntry()
    {
        //synchronized(lockerMap)
        {return map.getFileEntry();}
    }

    public static float[] getMapDimension()
    {
        synchronized(lockerMap)                 {return map.getMapDimension();}
    }

    public static float getMapResolutionScaleCell()
    {
        synchronized(lockerMap)                 {return map.getMapResolutionScaleCell();}
    }

    public static int[] getMatrixDimension()
    {
        synchronized(lockerMap)                 {return matrixDimension;}
    }

    public static int[] getMapCellPosition(float x, float y)
    {
        synchronized(lockerMap)                 {return map.getMapCelPosition(x, y);}
    }

    public static int[] getMatrixCellPosition(float x, float y)
    {
        synchronized(lockerMap)                 {return map.getMatrixCellPosition(x, y);}
    }

    //movement --------------------------------------------------------------------------

    public static void resetINS()
    {
        synchronized(lockerINS)                 {ins.reset();}
    }

    public static void updateINS()
    {
        synchronized(lockerINS)                 {ins.addMotion();}
    }

    public static float[] getDisplacement()
    {
        synchronized(lockerINS)                 {return ins.displacement();}
    }

    // preferences ------------------------------------------------------------------------- getcontext();
    private static Context context;
    public static void setContext(Context c)    {context = c;}
    public static Context getContext()          {return context;}

    // compass
    private static int sensitivity;
    private static float stepLenght;
    // radio map
    private static double maxVarianceAvg;
    private static int maxDistanceAllowed;
    private static int samplesAverage;
    // wifi
    private static int accessPointVisible;
    private static int tolerance;
    private static int samplesStdDev;
    private static boolean deleteHistory;
    private static boolean offImmediately;
    private static boolean wifiTracking;
    private static double goodStdDev;
    private static double badStdDev;
    private static int samplesMode;
    private static int compassMap;
    private static boolean rotateMap;
    // lateration
    private static int laterationSensitivity;
    private static int laterationTolerance;
    private static boolean wifiObstacles;
    // panic
    private static String SMSPanic;

    public static void updatePreferences()
    {
        synchronized(lockerSettings)
        {
            // pedometer
            sensitivity = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString("step_sensitivity", "30"));
            stepLenght = Float.parseFloat(PreferenceManager.getDefaultSharedPreferences(context).getString("step_length", "55"));

            // wifi
            maxVarianceAvg = Double.parseDouble(PreferenceManager.getDefaultSharedPreferences(context).getString("average_variance", "0.5"));
            maxDistanceAllowed = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString("real_distance", "10")) * 100;
            samplesAverage = PreferenceManager.getDefaultSharedPreferences(context).getInt("samples_average", 50);
            accessPointVisible = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString("minimum_access_point_visible", "3"));
            tolerance = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString("tolerance_dbm", "5"));
            samplesStdDev = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString("number_samples_standard_deviation", "6"));
            deleteHistory = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("delete_dbm_history", false);
            offImmediately = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("off_immediately", true);
            wifiTracking = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("wifi_tracking", true);
            goodStdDev = Double.valueOf(PreferenceManager.getDefaultSharedPreferences(context).getFloat("good_stddev", 1.5f));
            badStdDev = Double.valueOf(PreferenceManager.getDefaultSharedPreferences(context).getFloat("bad_stddev", 2.5f));
            samplesMode = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString("number_samples_mode", "1"));
            rotateMap = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("rotate_map", true);

            // compass
            compassMap = PreferenceManager.getDefaultSharedPreferences(context).getInt("map_compass", getFileNorth());

            // trilateration (cm)
            laterationSensitivity = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString("lateration_sensitivity", "1")) * 100;
            laterationTolerance = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString("lateration_tolerance", "1"));
            wifiObstacles = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("wifi_obstacles", true);

            // panic
            SMSPanic = PreferenceManager.getDefaultSharedPreferences(context).getString("panic_number","");
        }
    }

    public static int getSensitivity()
    {
        synchronized(lockerSettings)            {return sensitivity;}
    }

    public static float getStepLength()
    {
        synchronized(lockerSettings)            {return stepLenght;}
    }

    // RadioMap
    public static double getMaxVarianceAvg()
    {
        synchronized(lockerSettings)            {return maxVarianceAvg;}
    }

    public static int getMaxDistanceAllowed()
    {
        synchronized(lockerSettings)            {return maxDistanceAllowed;}
    }

    public static int getSamplesAverage()
    {
        synchronized(lockerSettings)            {return samplesAverage;}
    }

    // WiFi
    public static int getMinAccessPointVisible()
    {
        synchronized(lockerSettings)            {return accessPointVisible;}
    }

    public static int getMaxTolerance()
    {
        synchronized(lockerSettings)            {return tolerance;}
    }

    public static int getSamplesStdDev()
    {
        synchronized(lockerSettings)            {return samplesStdDev;}
    }

    public static boolean isDeleteDbmHistory()
    {
        synchronized(lockerSettings)            {return deleteHistory;}
    }

    public static boolean isOffImmediately()
    {
        synchronized(lockerSettings)            {return offImmediately;}
    }

    public static boolean isAllWifiTracking()
    {
        synchronized(lockerSettings)            {return wifiTracking;}
    }

    public static double getGoodStdDev()
    {
        synchronized(lockerSettings)            {return goodStdDev;}
    }

    public static double getBadStdDev()
    {
        synchronized(lockerSettings)            {return badStdDev;}
    }

    public static int getSamplesMode()
    {
        synchronized(lockerSettings)            {return samplesMode;}
    }

    public static int getCompassMap()
    {
        synchronized(lockerSettings)            {return compassMap;}
    }

    public static boolean isRotateMap()
    {
        synchronized(lockerSettings)            {return rotateMap;}
    }

    // trilateration
    public static int getLaterationSensitivity()
    {
        synchronized(lockerSettings)            {return laterationSensitivity;}
    }

    public static int getLaterationTolerance()
    {
        synchronized(lockerSettings)            {return laterationTolerance;}
    }

    public static boolean isObstacles()
    {
        synchronized(lockerSettings)            {return wifiObstacles;}
    }

    // panic
    public static String getSMSPanicNumber()
    {
        synchronized(lockerSettings)            {return SMSPanic;}
    }

    // trilateration - multilateration --------------------------------------------------

    public static void updateLaterationRadioMap()
    {
        synchronized(lockerLateration)          {lateration.updateLaterationRadioMap();}
    }

    public static Vector getLaterationList()
    {
        synchronized(lockerLateration)          {return lateration.correlate();}
    }

    public static float getLaterationDistance(double rssi)
    {
        synchronized(lockerLateration)          {return lateration.getLaterationDistance(rssi);}
    }

    public static boolean isLaterationPosition(int col, int row)
    {
        synchronized(lockerLateration)          {return lateration.isLaterationPosition(col, row);}
    }

    // pdf ------------------------------------------------------------------------------

    public static float[][] getPdfProbability()
    {
        synchronized(lockerPdf)                 {return pdf;}
    }

    //-----------------------------------------------------------------------------------

    private static boolean isAcceptBSSID = false;
    public static void startAcceptBSSID()                                           {isAcceptBSSID = true;}
    public static void stopAcceptBSSID()                                            {isAcceptBSSID = false;}
    public static boolean isAcceptBSSID()                                           {return isAcceptBSSID;}

    //-----------------------------------------------------------------------------------

    private static String msgNote = "";
    private static String defaultNote = "Inertial Navigation System - Wifi Signal Lateration";
    public static void setNote(String msg)
    {
        msgNote = msg;

        //timeNote(System.currentTimeMillis());
    }
    public static void setDefaultNote()                                             {setNote(defaultNote);}
    public static String getNote()
    {
        if (msgNote.equals(""))
        {
            setDefaultNote();
        }

        return msgNote;
    }

    //-----------------------------------------------------------------------------------

    public static void setLevelView(int levelview)                                  {levelView = levelview;}
    public static int getLevelView()                                                {return levelView;}

    //-----------------------------------------------------------------------------------

    private static boolean isPositioning = false;
    public static void startPositioning()
    {
        isPositioning = true;

        processCount = 0;
        integrateCount = 0;

        int[] entry = Positioning.getFileEntry();
        int[] cell = Positioning.getMatrixCellPosition((float)entry[0], (float)entry[1]);

        float[] pos = new float[2];

        pos[0] = cell[0];
        pos[1] = cell[1];

        pdf[cell[0]][cell[1]] = 1.0f;
        pdfWifi[cell[0]][cell[1]] = 1.0f;

        //kfs.init();
        integrate.init();
        //integrate.updateKalman(pos);

        matchedPoint(pos);
    }
    public static void stopPositioning()                                            {isPositioning = false;}
    public static boolean isPositioning()                                           {return isPositioning;}

    //-----------------------------------------------------------------------------------

    // porcentagem de integrações em relação às capturas totais
    public static int[] integrateLevel()
    {
        int[] a = new int[2];

        a[0] = processCount;
        a[1] = integrateCount;

        return a;
    }

    //-----------------------------------------------------------------------------------

    private static boolean isViewWifiList = true;
    public static void enableViewWifiList()                                         {isViewWifiList = true;}
    public static void disableViewWifiList()                                        {isViewWifiList = false;}
    public static boolean isViewWifiList()                                          {return isViewWifiList;}

    //-----------------------------------------------------------------------------------

    private static boolean isRadioMap = false;
    public static void enableRadioMap()                                             {isRadioMap = true;}
    public static void disableRadioMap()                                            {isRadioMap = false;}
    public static boolean isRadioMap()                                              {return isRadioMap;}

    //-----------------------------------------------------------------------------------

    private static boolean isManageRadioMap = false;
    public static void enableManageRadioMap()                                       {isManageRadioMap = true;}
    public static void disableManageRadioMap()                                      {isManageRadioMap = false;}
    public static boolean isManageRadioMap()                                        {return isManageRadioMap;}

    //-----------------------------------------------------------------------------------

    private static boolean isUnsignedSignal = false;
    public static void enableUnsignedSignal()                                       {isUnsignedSignal = true;}
    public static void disableUnsignedSignal()                                      {isUnsignedSignal = false;}
    public static boolean isUnsignedSignal()                                        {return isUnsignedSignal;}

    //-----------------------------------------------------------------------------------

    private static boolean isMapView = false;
    public static void enableMap()                                                  {isMapView = true;}
    public static void disableMap()                                                 {isMapView = false;}
    public static boolean isMapView()                                               {return isMapView;}

    //-----------------------------------------------------------------------------------

    private static boolean isConverging = false;
    public static void startConverging()                                            {isConverging = true;}
    public static void stopConverging()                                             {isConverging = false;}
    public static boolean isConverging()                                            {return isConverging;}

    //-----------------------------------------------------------------------------------

    private static long timeConverging = 0;
    public static void timeConverging(long time)                                    {timeConverging = time;}
    public static long timeConverging()                                             {return timeConverging;}

    //-----------------------------------------------------------------------------------

    private static long timeNote = 0;
    public static void timeNote(long time)                                          {timeNote = time;}
    public static long timeNote()                                                   {return timeNote;}

    //-----------------------------------------------------------------------------------

    // posição final
    private static float[] matchedPoint = new float[2];// pontos comuns (resultado final)
    public static void matchedPoint(float[] point)                                  {Util.copyArray(matchedPoint, point);}
    public static float[] matchedPoint()                                            {return Util.copyArray(matchedPoint);}

    //-----------------------------------------------------------------------------------

    // posição wifi (sem uso)
    //private static float[] wifiPoint = new float[2];// ponto wifi
    //public static void wifiPoint(float[] point)                                      {Util.copyArray(wifiPoint, point);}
    //public static float[] wifiPoint()                                                {return Util.copyArray(wifiPoint);}

    //-----------------------------------------------------------------------------------

    // posição ins
    private static float[] displacement = new float[2];// ponto ins
    public static void displacement(float[] point)                                  {Util.copyArray(displacement, point);}
    public static float[] displacement()                                            {return Util.copyArray(displacement);}

    //-----------------------------------------------------------------------------------

    // posição trilateração (sem uso)
    /*private static float[] trilateration = new float[2];// ponto trilateração
    public static void trilateration(float[] point)                                 {Util.copyArray(trilateration, point);}
    public static float[] trilateration()                                           {return Util.copyArray(trilateration);}*/

    //-----------------------------------------------------------------------------------

    private static boolean isLoaded = false;
    public static void isLoaded(boolean flag)                                       {isLoaded = flag;}
    public static boolean isLoaded()                                                {return isLoaded;}

    //-----------------------------------------------------------------------------------

    private static String fileSelected = "";
    public static void setFileSelected(String file)
    {
        if (!fileSelected.equals(file));
        {
            fileSelected = file;
        }
    }
    public static String getFileSelected()                                          {return fileSelected;}

    //-----------------------------------------------------------------------------------

    public static void process()
    {
        processCount++;

        // se o ponto inicial já foi determinado,
        // processa os dados unindo diferentes metodos de posicionamento

        // 3ª fase
        if (!isConverging())
        {
            // no caso de existir um erro de calculos volta a executa-los (evita estacionar em 0,0)
            //if (matchedPoint[0] == -1 && matchedPoint[1] == -1)
            //    pdf = wifi.correlate();

            integrate.integrate();
        }

        // se a posição inicial ainda não foi determinado,
        // é dado um periodo de tempo para que o determine
        else
        {
            // enquanto espera pela convergencia

            // 1ª fase
            if (timeConverging() == 0)
            {
                timeConverging(System.currentTimeMillis());
                integrate.integrate();

                // procura a posição inicial através do wifi dando 5 segundos para se posicionar
                pdfWifi = wifi.correlate();
            }
            else
            {
                // 2ª fase
                long now = System.currentTimeMillis();

                // tempo para convergir (long now = System.currentTimeMillis();)
                if (now - timeConverging() > CONVERGING_PERIOD)// ...passando 5 segundos
                {
                    stopConverging();
                    timeConverging(0);
                }
            }
        }
    }

    //-------------------------------------------------------------------------------------------

    // thread sempre activa para evitar pausas de inicio de processo (evitar o run da memoria)
    private static class Integrate
    {
        private static float PERCENT_VAR = 0.05f;   // Estimativa de ruído de variância em percentagem
        private static float GAIN = 0.8f;           // Ganho do filtro

        private static final int DIMENSION = 2;

        private static float[]     noiseVar;   // variância do ruído
        private static float[]    corrected;   // Valor corrigido / filtrado
        private static float[] predictedVar;   // Variância prevista
        private static float[]     observed;   // Valor observado devido à medição
        private static float[]       kalman;   // O ganho de Kalman
        private static float[] correctedVar;   // A variância corrigida
        private static float[]    predicted;   // O valor previsto

        // Initializes the filter with some initial values and defines the DIMENSION used.
        public void init()
        {
            noiseVar = new float[DIMENSION];// variância do ruído
            corrected = new float[DIMENSION];// Valor corrigido / filtrado
            predictedVar = new float[DIMENSION];// Variância prevista
            observed = new float[DIMENSION];// Valor observado devido à medição
            kalman = new float[DIMENSION];// O ganho de Kalman
            correctedVar = new float[DIMENSION];// A variância corrigida
            predicted = new float[DIMENSION];// O valor previsto

            for (int i = 0; i < DIMENSION; i++)
            {
                noiseVar[i] = PERCENT_VAR;
                predicted[i] = 0.0f;
            }

            predictedVar = noiseVar;
        }

        // Provides the caller with the filtered values since the last update.
        // A float array storing the filtered values.
        public float[] getCorrectedValues()
        {
            return corrected;
        }

        // integra todos os resultados para obter a posição final
        private void integrate()
        {
            if (!integrateProcess)
            {
                integrateProcess = true;

                new Thread(new Runnable()
                {
                    public void run()
                    {
                        // recebe o ultimo movimento
                        float[] displacement = getDisplacement();

                        // adapta a posição à rotação do mapa
                        displacement = Map.toMapCS(displacement);
                        displacement(displacement);

                        // prepara novo movimento (à medida que vai actualizando o canvas, vai actualizando o ultimo movimento)
                        resetINS();

                        double theta = Math.atan2(displacement[1], displacement[0]);
                        double r = Util.magnitude(displacement);

                        float[][] newpdf = new float[matrixDimension[0]][matrixDimension[1]];
                        //float[] lastWifiPoint = new float[2];

                        // raio
                        int originx = size;
                        int originy = size;

                        //integra o movimento (INS)
                        float[][] gauss = new float[size * 2 + 1][size * 2 + 1];

                        // prepara o square do (INS)
                        // com movimento
                        if (r != 0)
                        {
                            double varR = Util.square(GAUSSIAN_POLAR_RADIUS_ERROR * r);
                            double varT = Util.square(GAUSSIAN_POLAR_THETA_ERROR);

                            for (int col = -size; col <= size; col++)
                            {
                                for (int row = -size; row <= size; row++)
                                {
                                    double x = row / 2.0;
                                    double y = col / 2.0;

                                    double diffsqrR = Util.square(Util.magnitude(x, y) - r);
                                    double diffsqrT = Util.square(Math.atan2(y, x) - theta);

                                    gauss[col + originy][row + originx] =
                                            (float) Math.exp(-diffsqrR / (2*varR+1) - diffsqrT / (2*varT*r));
                                }
                            }
                        }
                        else // sem movimento
                        {
                            double var = Util.square(GAUSSIAN_CARTESIAN_ERROR);

                            for (int col = -size; col <= size; col++)
                            {
                                for (int row = -size; row <= size; row++)
                                {
                                    double x = row / 2.0;
                                    double y = col / 2.0;

                                    gauss[col + originy][row + originx] =
                                            (float) Math.exp(-Util.square(x) / (2*var) - Util.square(y) / (2*var));
                                }
                            }
                        }

                        //square (a partir de agora trabalha com uma matrix envolvente ao ponto "ideal". [-3; 3]

                        //  0.5   0.6   0.7   0.7   0.7   0.6   0.5
                        //  0.6	  0.7   0.8   0.8   0.8	  0.7   0.6
                        //  0.7	  0.8   0.9   0.9   0.9	  0.8   0.7
                        //  0.7	  0.8   0.9   1.0   0.9	  0.8   0.7
                        //  0.7	  0.8   0.9   0.9   0.9	  0.8   0.7
                        //  0.6	  0.7   0.8   0.8   0.8	  0.7   0.6
                        //  0.5	  0.6   0.7   0.7   0.7	  0.6   0.5

                        // actualiza o wifi
                        pdfWifi = wifi.correlate();

                        // actualiza a lateração
                        //pdfLateration = lateration.correlate();

                        //integra o wifi e o movimento
                        float[] maxpos = new float[2];

                        maxpos[0] = -1;
                        maxpos[1] = -1;

                        float max = Float.MIN_VALUE;

                        // anexa o movimento à matriz
                        for (int col = 0; col < matrixDimension[0]; col++)
                        {
                            for (int row = 0; row < matrixDimension[1]; row++)
                            {
                                // O pdf precedente é utilizado como a "posição confiável" da sua posição
                                // Com a distribuição gaussian do vetor do deslocamento,
                                // utilizando a probabilidade do último pdf como peso, somam-se estes
                                // isto é, a anterior função de probabilidade de densidade para uma nova matriz de probabilidade de densidade acumulada.
                                // (razão que demora a actualizar a nova posição)

                                newpdf[col][row] = 0;

                                // sobrepõe as matrizes
                                for (int coljoin = -size; coljoin <= size; coljoin++)
                                {
                                    for (int rowjoin = -size; rowjoin <= size; rowjoin++)
                                    {
                                        // verifica de pode sobrepor uma celula cumprindo os limites da matrix
                                        if ((col + coljoin >= 0) && (col + coljoin < matrixDimension[0]))
                                        {
                                            if ((row + rowjoin >= 0) && (row + rowjoin < matrixDimension[1]))
                                            {
                                                newpdf[col][row] += (double) pdf[col + coljoin][row + rowjoin] * (double) gauss[originy - coljoin][originx - rowjoin];
                                            }
                                        }
                                    }
                                }

                                //0,125	0,182	0,245	0,311	0,364	0,410	0,447	0,471	0,482	0,479	0,463	0,433	0,390	0,338	0,269	0,202	0,140
                                //0,172	0,249	0,336	0,427	0,499	0,563	0,612	0,645	0,660	0,657	0,635	0,593	0,535	0,464	0,369	0,277	0,192
                                //0,216	0,314	0,422	0,536	0,628	0,707	0,770	0,811	0,830	0,826	0,798	0,746	0,672	0,583	0,464	0,348	0,242
                                //0,251	0,365	0,492	0,624	0,731	0,823	0,896	0,944	0,966	0,961	0,929	0,868	0,783	0,678	0,540	0,405	0,282
                                //0,260	0,378	0,509	0,646	0,756	0,852	0,927	0,977  -1,000-	0,995	0,961	0,898	0,810	0,702	0,559	0,419	0,291
                                //0,252	0,366	0,493	0,627	0,733	0,826	0,899	0,948	0,970	0,965	0,933	0,871	0,786	0,681	0,542	0,407	0,283
                                //0,217	0,316	0,425	0,540	0,632	0,712	0,775	0,816	0,836	0,831	0,803	0,751	0,677	0,587	0,467	0,351	0,244
                                //0,174	0,252	0,339	0,431	0,504	0,568	0,618	0,652	0,667	0,664	0,641	0,599	0,540	0,468	0,373	0,280	0,194
                                //0,127	0,184	0,248	0,315	0,369	0,416	0,453	0,477	0,488	0,486	0,469	0,438	0,395	0,343	0,273	0,205	0,142

                                // posição actual * posição wifi
                                newpdf[col][row] *= (double) pdfWifi[col][row];

                                // posição estimada com o movimento
                                if (newpdf[col][row] > max)
                                {
                                    max = newpdf[col][row];
                                    maxpos[0] = col;
                                    maxpos[1] = row;
                                }
                            }
                        }

                        // divide todos os elementos pelo valor maximo. A posição ideal ficara a 1 (100%)
                        for (int col = 0; col < matrixDimension[0]; col++)
                        {
                            for (int row = 0; row < matrixDimension[1]; row++)
                            {
                                newpdf[col][row] /= (double)max;
                            }
                        }

                        // se for uma posição válida
                        if (maxpos[0] != -1 && maxpos[1] != -1)
                        {
                            size = GAUSSIAN_SIZE;

                            // actualiza nova posição se em movimento
                            //if (DeviceSensor.isMoving())
                            //{
                            updateKalman(maxpos);
                            matchedPoint(wifiKalman());

                            //lastWifiPoint = Positioning.wifiPoint();

                            //pdf = newpdf;
                            //}
                            /*else
                            {
                                float[] a = Positioning.wifiPoint();

                                // só actualiza o pdf se a posição wifi se mantiver fiel à posição - NÃO FUNCIONA
                                if ((int)a[0] == (int)lastWifiPoint[0] &&
                                    (int)a[1] == (int)lastWifiPoint[1]) pdf = newpdf;
                            }*/
//tenho de fazer algo que não deixe recuar a posição wifi se estiver a deslocar-me no sentido oposto
                            pdf = newpdf;
                        }
                        else // aumenta o raio para aumentar as hipoteses de encontrar uma posição
                        {
                            if (size < GAUSSIAN_MAX_SIZE) size++;
                        }

                        integrateProcess = false;
                        integrateCount++;
                    }

                    // Updates the Kalman filter.
                    public void updateKalman(float[] observedValue)
                    {
                        observed = observedValue;

                        // calcula o ganho de Kalman para cada dimensão
                        for (int i = 0; i < kalman.length; i++)
                        {
                            kalman[i] = predictedVar[i] / (predictedVar[i] + noiseVar[i]);
                        }

                        // update the sensor prediction with the measurement for each DIMENSION
                        for (int i = 0; i < corrected.length; i++)
                        {
                            corrected[i] = GAIN * predicted[i] + (1.0f - GAIN) * observed[i] + kalman[i] * (observed[i] - predicted[i]);
                        }

                        // update the variance estimation
                        for (int i = 0; i < correctedVar.length; i++)
                        {
                            correctedVar[i] = predictedVar[i] * (1.0f - kalman[i]);
                        }

                        // predict next variances and values
                        predictedVar = correctedVar;
                        predicted = corrected;
                    }
                }).start();
            }
        }
    }
}
