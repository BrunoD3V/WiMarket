
package d3v.bnb.ourwimarket;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapActivity extends Activity implements SensorEventListener
{
    private static final int MENU_MY_LOCATION                   = Menu.FIRST + 1;
    private static final int MENU_CHOOSE_DESTINATION            = Menu.FIRST + 2;
    private static final int MENU_PREFERENCES                   = Menu.FIRST + 3;
    private static final int MENU_SCAN_ACCESS_POINTS            = Menu.FIRST + 4;
    private static final int MENU_ENABLE_VIEW_ACCESS_POINTS     = Menu.FIRST + 5;
    private static final int MENU_DISABLE_VIEW_ACCESS_POINTS    = Menu.FIRST + 6;
    private static final int MENU_RADIO_MAP                     = Menu.FIRST + 7;
    private static final int MENU_MANAGE_MAP                    = Menu.FIRST + 8;
    private static final int MENU_MANAGE_ACCESS_POINT           = Menu.FIRST + 9;
    private static final int MENU_RETURN                        = Menu.FIRST + 10;
    private static final int MENU_STOP                          = Menu.FIRST + 11;
    private static final int MENU_SHOW_MAP                      = Menu.FIRST + 12;
    private static final int MENU_BACK                          = Menu.FIRST + 13;

    private static final int PROGRESS_DIALOG_ID = 1;
    private static final int STEP_VIEW = 6;

    //public static final int MIN_RSSI = -90;             // valor minimo de recepção
    public static final int MIN_RSSI = -90000;

    private MapView mapView;
    private SensorManager sensorManager;
    private WifiManager wifiManager;
    private WifiReceiver wifiReceiver;
    private StepDetector stepDetector;
    private ConnectivityManager connMgr;
    
    private String networkStatus = "Wifi";

    private boolean isCompass = false;
    private boolean isPedometer = false;

    private IntentFilter filter = new IntentFilter();
            
    // secção de processamento de voz 
    
    private static final int NORTH = 0;
    private static final int  EAST = 90;
    private static final int SOUTH = 180;
    private static final int  WEST = 270;

    private static final String[] DIRECTION_NAMES =
      {"north", "north north east", "north east", "east north east", "east", "east south east",
          "south east", "south south east", "south", "south south west", "south west",
          "west south west", "west", "west north west", "north west", "north north west", "north"};

    private static final String PLEASECALIBRATE = "Please calibrate the compass by shaking your handset";

    private static final int MIN_STABLECOUNT  = 50;
    private static final int MIN_STABLESENSOR = 50; // tolerancia para confirmar o "accuracy" 

    private static final int STABLECOUNT_FOR_CALIBRATION = -200;

    // Degrees of tolerance for a reading to be considered stable
    private static final int STABLE_TOLERANCE = 5;
    private static final int CARDINAL_TOLERANCE = 1;
    private static final int NORTH_LEFT_MAX = 359;
    private static final int NORTH_RIGHT_MAX = 1;

    private static final long[] VIBE_PATTERN = {0, 1, 40, 41};

    private static float currentHeading = 0;
    private static float lastStableHeading = 0;
    private static int lastStableSensor = 0;
    private static int stableCount = 0;
    private static int lastCardinalDir = 0;

    private Vibrator vibe;
    private TextToSpeech tts;

    public static boolean sensorOk = true;

    // voz da bussula
    private static String speakLast;
    private static String speakNow;

    private boolean userWantsVoice = true;
    private boolean userWantsVibre = false;
    
    private int lastCountStep = 0;
    private int accessPointsActives = 0;

    // estado da bateria
    private int oldLevel = 100;

    //--------------------------------------------------------------------------

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // informa qual a classe onde se encontra o onPreparateMenu (para as preferencias)
        Positioning.setContext(this);

        // create view
        mapView = new MapView(this);
        
        mapView.setBackgroundColor(Color.BLACK);
        mapView.setFocusable(true);
        
        setContentView(mapView);

        // Pedometer (step)
        stepDetector = new StepDetector();

        // Start sensor
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        
        // conectividade
        connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        
        // Start wifi
        wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        wifiReceiver = new WifiReceiver();

        // Start battery
        registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        // register the broadcast receiver for wifi state events
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);

        // activa thread wifiEventReceiver e seus eventos (filtros)
        registerReceiver(wifiReceiver, filter);

        // prepara vozes e vibração para tts
        vibe = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        tts = new TextToSpeech(this, null);

        sensorOk = true;
        
        wifiManager.setWifiEnabled(true);
        wifiManager.startScan();
    }

    @Override
    public void onResume()
    {
        // limpa a memoria
        DeviceSensor.resetAccelerometer();
        DeviceStep.resetStep();

        Positioning.updatePreferences();

        super.onResume();
    }

    @Override
    public void onDestroy()
    {
        try
        {
            // desliga a recepção do acelerometro
            sensorManager.unregisterListener(this); //bussula
            sensorManager.unregisterListener(stepDetector);//step
     
            // desliga a recepção do wifi
            unregisterReceiver(wifiReceiver);//wifi
            unregisterReceiver(batteryReceiver);//bacttery

            Positioning.stopPositioning();
            Positioning.stopConverging();

            Positioning.disableRadioMap();
            Positioning.disableManageRadioMap();
            
            Positioning.enableMap();

            Positioning.setDefaultNote();

        } catch (Exception e) {}

        super.onDestroy();
    }

    // trata do botão back
    @Override
    public boolean onKeyDown(int key_code, KeyEvent event)
    {
        if ((Positioning.isPositioning() || Positioning.isRadioMap()) && KeyEvent.KEYCODE_BACK == key_code)
        {
            onPause();

            return true;
        }

        return super.onKeyDown(key_code, event);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        menu.clear();

        menu.setQwertyMode(true);

//-------------------------------------------------------------<<<<<//super.onCreateOptionsMenu(menu);
//consultar o projeto thompson

        // Enable and disable access points
        menu.add(1, MENU_ENABLE_VIEW_ACCESS_POINTS, 0, getString(R.string.menu_enable_view)).setIcon(android.R.drawable.button_onoff_indicator_off);
        menu.add(2, MENU_DISABLE_VIEW_ACCESS_POINTS, 0, getString(R.string.menu_disable_view)).setIcon(android.R.drawable.button_onoff_indicator_on);
        
        // menu principal
        menu.add(11, MENU_SCAN_ACCESS_POINTS, 0, getString(R.string.menu_scan)).setIcon(android.R.drawable.ic_menu_search);
        menu.add(12, MENU_MY_LOCATION, 0, getString(R.string.menu_my_location)).setIcon(android.R.drawable.ic_menu_view);
        menu.add(13, MENU_RADIO_MAP, 0, getString(R.string.menu_radio_map)).setIcon(android.R.drawable.ic_dialog_map);
        
        // Enable and disable map
        menu.add(18, MENU_MANAGE_MAP, 0, getString(R.string.menu_manage_map)).setIcon(android.R.drawable.ic_menu_manage);
        menu.add(19, MENU_MANAGE_ACCESS_POINT, 0, getString(R.string.menu_manage_access_point)).setIcon(android.R.drawable.ic_menu_myplaces);
        menu.add(20, MENU_CHOOSE_DESTINATION, 0, getString(R.string.menu_choose_destination)).setIcon(android.R.drawable.ic_menu_directions);
        menu.add(21, MENU_PREFERENCES, 0, getString(R.string.menu_preferences)).setIcon(android.R.drawable.ic_menu_preferences);
        
        // back, return, stop
        menu.add(29, MENU_RETURN, 0, getString(R.string.menu_return)).setIcon(android.R.drawable.ic_menu_revert);
        menu.add(30, MENU_STOP, 0, getString(R.string.menu_stop)).setIcon(android.R.drawable.ic_menu_close_clear_cancel);

        // desactivar para deixar entrar na opção radiomap com menos de 3 APs
        if (MapView.totalMapRSSI() < Positioning.getMinAccessPointVisible())
        {
            // desactiva botões
            menu.setGroupEnabled(12, false); 
            menu.setGroupEnabled(13, false);
            
            // retira botões
            if (MapView.totalMapRSSI() == 0)
            {
                menu.removeGroup(12);// my location
                menu.removeGroup(13);// radio map
            }     
        }   
        
        // Scan wifi
        if (Positioning.getWifiList().isEmpty())
        {
            menu.removeGroup(1);// enable view access points
            menu.removeGroup(2);// disable view access points
        }
        else
        {
            if (Positioning.isViewWifiList())
                menu.removeGroup(1);// enable view access points
            else
                menu.removeGroup(2);// disable view access points
        }

        // menu radiomap
        if (Positioning.isRadioMap())// Radio Map
        {
            menu.removeGroup(11);// scan access points
            menu.removeGroup(12);// my location
            menu.removeGroup(13);// radio map
            menu.removeGroup(20);// get location
            menu.removeGroup(21);// preferences
        }
        else
        {
            // botões exclusivos do radiomap
            menu.removeGroup(18);// manage map
            menu.removeGroup(19);// manage access point
            menu.removeGroup(29);// return
        }

        // menu posicionamento
        if (Positioning.isPositioning())// My location
        {
            menu.removeGroup(11);// scan access points           
            menu.removeGroup(12);// my location
            menu.removeGroup(13);// radio map
            menu.removeGroup(21);// preferences
            menu.removeGroup(22);// show map
        }
        else
        {
            // botões exclusivos do menu principal
            menu.removeGroup(20);// get location
            menu.removeGroup(30);// stop
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        switch (id)
        {
            case MENU_MY_LOCATION:

                // caso não active a bussula ou o pedometro, sai
                if (!activeCompass() || !activePedometer())
                    break;

                Positioning.startPositioning();
                Positioning.startConverging();
                Positioning.resetINS();

                isPedometer = true;
                
                break;

            case MENU_SCAN_ACCESS_POINTS:

                mapView.wifiDiscovery();

                break;

            case MENU_ENABLE_VIEW_ACCESS_POINTS:

                Positioning.enableViewWifiList();
              
                break;

            case MENU_DISABLE_VIEW_ACCESS_POINTS:

                Positioning.disableViewWifiList();

                break;

            case MENU_RADIO_MAP:// entra no radiomap

                mapView.isScreenMapCenter = false;

                Positioning.enableRadioMap();

                break;
                
            case MENU_MANAGE_MAP:// gestão do mapa

                mapView.isScreenMapCenter = false;
                
                //teste
                //startActivity(new Intent(this, AccessPointList.class));
                //startActivity(new Intent(this, HelloTabWidget.class));
//---------------------------------------                
//                Positioning.enableManageRadioMap();

                break;   
                
            case MENU_MANAGE_ACCESS_POINT:// gestão do ponto de acesso

                //mapView.isScreenMapCenter = false;
                
                //teste
                startActivity(new Intent(this, AccessPointList.class));
                
                //Positioning.enableManageRadioMap();

                break;    


            case MENU_RETURN:// sai do radiomap

                mapView.isScreenMapCenter = false;

                // ordem para actualizar a matriz do radiomap na classe mapview
                mapView.updateRadioMap();

                // ordem para actualizar a matriz da lateração (tri/multi)
                Positioning.updateLaterationRadioMap();
                
                Positioning.disableRadioMap();
                Positioning.disableManageRadioMap();
                
                Positioning.setDefaultNote();

                break;

            case MENU_STOP:

                Positioning.stopPositioning();
                Positioning.stopConverging();

                // desactiva o acelerometro
                sensorManager.unregisterListener(this);

                // coloca o acelerometro a zeros
                if (Positioning.getLevelView() != STEP_VIEW)
                    DeviceSensor.resetAccelerometer();

                isCompass = false;

                // desactiva o pedometro
                sensorManager.unregisterListener(stepDetector);

                DeviceStep.resetStep();

                isPedometer = false;

                break;

            case MENU_SHOW_MAP:

                if (Positioning.isMapView())
                {
                    Positioning.disableMap();
                }
                else
                {
                    Positioning.enableMap();
                }

                break;

            case MENU_PREFERENCES:

                startActivity(new Intent(this, Preferences.class));

                break;
        }

        Positioning.setDefaultNote();

        return super.onOptionsItemSelected(item);
    }

    @Override //Called when the accuracy of a sensor has changed.
    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {
        //sensorOk = (accuracy == SensorManager.SENSOR_STATUS_ACCURACY_HIGH);
        if (accuracy == SensorManager.SENSOR_STATUS_ACCURACY_HIGH)
        {
            sensorOk = true;
            lastStableSensor = 0;
        }
        else
        {
            if (lastStableSensor++ > MIN_STABLESENSOR)
                sensorOk = false;
        }
    }

    @Override //Called when sensor values have changed.
    public void onSensorChanged(SensorEvent event)//int sensor, float[] values
    {
        // values[0]: Azimuth, angle between the magnetic north direction and the Y axis, around the Z axis (0 to 359).
        // 0=North, 90=East, 180=South, 270=West
        
        // values[1]: Pitch, rotation around X axis (-180 to 180), with positive values when the z-axis moves toward the y-axis.
        // values[2]: Roll, rotation around Y axis (-90 to 90), with positive values when the x-axis moves toward the z-axis.

        synchronized (this)
        {
            int type = event.sensor.getType();

            float[] data = Util.copyArray(event.values);

            if (type == Sensor.TYPE_ACCELEROMETER)
            {
                DeviceSensor.setDevA(data);

                Positioning.updateINS();
            }
            else if (type == Sensor.TYPE_MAGNETIC_FIELD)
            {
                DeviceSensor.setDevM(data);
                DeviceSensor.toEarthCS();
            }
            else if (type == Sensor.TYPE_ORIENTATION)
            {
                DeviceSensor.setDevO(data);
                
                if (Positioning.isPositioning())
                {
                    processDirection();
                }
            }

            if (isPedometer && userWantsVoice)
            {
                int step = DeviceStep.getStepCount();

                if (lastCountStep != step && (step % 5) == 0) // resto divisão por 5
                {
                    lastCountStep = step;

                    speakStep(step);
                }
            }
        }
    }

    @Override
    protected Dialog onCreateDialog(int id)
    {
        switch (id)
        {
            case PROGRESS_DIALOG_ID:
            {
                ProgressDialog dialog = new ProgressDialog(this);

                int lost = Positioning.getMinAccessPointVisible() - accessPointsActives;
                dialog.setMessage("Waiting for " + lost + " access point\nPlease wait...");

                if (MapView.averageProcessDialog.isShowing())
                    dialog.getWindow().setGravity(Gravity.TOP);
                else
                    dialog.getWindow().setGravity(Gravity.CENTER);

                dialog.setIndeterminate(true);
                dialog.setCancelable(false);

                return dialog;
            }
        }

        return null;
    }
    
    protected void processDirection()
    {
        currentHeading = DeviceSensor.getCompassHeading();

        // Do not speak immediately - wait until the sensor readings have been
        // stable for some time.
        if (Math.abs(lastStableHeading - currentHeading) < STABLE_TOLERANCE)
        {
            stableCount++;
        }
        else
        {
            lastStableHeading = currentHeading;
            stableCount = 0;
        }

        if (stableCount > MIN_STABLECOUNT)
        {
            if (userWantsVoice) speakDirection();
        }

        // Do not try bother determining if a new cardinal direction
        // was reached if the sensors are not functioning correctly.
        if (!sensorOk) return;

        boolean newCardinalDir = false;
        int candidateCardinal = findCardinalDir(currentHeading);

        if (candidateCardinal != lastCardinalDir)
        {
            newCardinalDir = true;
            lastCardinalDir = candidateCardinal;
        }

        if (newCardinalDir)
        {
            // vibra num novo ponto cardinal
            if (userWantsVibre) vibe.vibrate(VIBE_PATTERN, -1);
        }
    }

    // voz da actual direcção
    public void speakDirection()
    {
        stableCount = 0;

        if (!sensorOk)
        {
            tts.speak(PLEASECALIBRATE, 0, null);
            stableCount = STABLECOUNT_FOR_CALIBRATION;

            return;
        }

        speakNow = directionToString(currentHeading);

        if (!speakNow.equals(speakLast))
        {
            if (!tts.isSpeaking())
                tts.speak(speakNow, 0, null);

            speakLast = speakNow;
        }
    }

    // voz do numero de passos
    private void speakStep(int step)
    {
        if (!tts.isSpeaking() && step > 0)
        {
            tts.speak("" + step + " steps", 1, null);
        }
    }

    private static int findCardinalDir(float heading)
    {
        if ((heading > NORTH_LEFT_MAX - CARDINAL_TOLERANCE) || (heading < NORTH_RIGHT_MAX + CARDINAL_TOLERANCE))
        {
            return NORTH;
        }
        else if ((heading > EAST - CARDINAL_TOLERANCE) && (heading < EAST + CARDINAL_TOLERANCE))
        {
            return EAST;
        }
        else if ((heading > SOUTH - CARDINAL_TOLERANCE) && (heading < SOUTH + CARDINAL_TOLERANCE))
        {
            return SOUTH;
        }
        else if ((heading > WEST - CARDINAL_TOLERANCE) && (heading < WEST + CARDINAL_TOLERANCE))
        {
            return WEST;
        }
        else
        {
            return -1;
        }
    }

    private static String directionToString(float heading)
    {
        int index = (int) ((heading * 100 + 1125) / 2250);

        return DIRECTION_NAMES[index];
    }

    private boolean activeCompass()
    {
        //------------------------------
        boolean accelerometerAvailable;
        if (sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER).isEmpty())
        {
            accelerometerAvailable = false;
        }
        else
        {
            Sensor asensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

            if (!sensorManager.registerListener(this, asensor, SensorManager.SENSOR_DELAY_FASTEST))
                accelerometerAvailable = false;
            else
                accelerometerAvailable = true;
        }

        //------------------------------
        boolean magneticAvailable;
        if (sensorManager.getSensorList(Sensor.TYPE_MAGNETIC_FIELD).isEmpty())
        {
            magneticAvailable = false;
        }
        else
        {
            Sensor msensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

            if (!sensorManager.registerListener(this, msensor, SensorManager.SENSOR_DELAY_FASTEST))
                magneticAvailable = false;
            else
                magneticAvailable = true;
        }

        //------------------------------
        boolean orientationAvailable;
        if (sensorManager.getSensorList(Sensor.TYPE_ORIENTATION).isEmpty())
        {
            orientationAvailable = false;
        }
        else
        {
            Sensor osensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);

            if (!sensorManager.registerListener(this, osensor, SensorManager.SENSOR_DELAY_FASTEST))
                orientationAvailable = false;
            else
                orientationAvailable = true;
        }

        Positioning.setNote("Accelerometer: " + (accelerometerAvailable ? "ok" : "error") + ", " +
                                 "Magnetic: " + (magneticAvailable      ? "ok" : "error") + ", " +
                              "Orientation: " + (orientationAvailable   ? "ok" : "error"));

        if (accelerometerAvailable && magneticAvailable && orientationAvailable)
        {
            isCompass = true;

            return true;
        }

        return false;
    }
    
    public boolean activePedometer()
    {
        boolean pedometerAvailable;

        if (!sensorManager.registerListener(stepDetector, SensorManager.SENSOR_ACCELEROMETER |
                                                          SensorManager.SENSOR_MAGNETIC_FIELD |
                                                          SensorManager.SENSOR_ORIENTATION,
                                                          SensorManager.SENSOR_DELAY_FASTEST))
            pedometerAvailable = false;
        else
            pedometerAvailable = true;


        if (pedometerAvailable)
        {
            isPedometer = true;

            return true;
        }

        return false;
    }

    // devolve a versão do sdk
    public int sdkVersion()
    {
        int sdkVersion = Integer.parseInt(android.os.Build.VERSION.SDK);

        return sdkVersion;
    }
    
    public void newNetworkStatus()
    {
        networkStatus = "";
        
        checkNetworkStatus();
    }

    public void checkNetworkStatus()
    {
        String status = "";
        
        NetworkInfo   wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if      (wifi.isAvailable())    {status = "Wifi";}
        else if (status.equals(""))     {status = "No Network";}
        
        if (!status.equals(networkStatus))
        {
            networkStatus = status;
            
            mapView.showNetwork(status);
            //Toast.makeText(this, networkStatus, Toast.LENGTH_LONG).show();
        }
    }
    
    // thread que trata a lista de access points
    class WifiReceiver extends BroadcastReceiver
    {
        public void onReceive(Context c, Intent intent)
        {
            synchronized (this)
            {
                if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(intent.getAction()))
                {
                    // melhora a estabilidade da recepção do sinal se "unregister" e "register"
                    unregisterReceiver(wifiReceiver);

                    // enquanto estiver no radio map a processar as threads não envia novos dados
                    if (MapView.dpad)//ProcessThread)
                    {
                        registerReceiver(wifiReceiver, filter);

                        wifiManager.startScan();

                        return;
                    }

                    accessPointsActives = 0;

                    ArrayList<HashMap<String, Object>> foundWifi = new ArrayList<HashMap<String, Object>>();
                    HashMap<String, Object> rssi;

                    List<ScanResult> hotSpots = wifiManager.getScanResults();

                    for (ScanResult hotSpot : hotSpots)
                    {
                        // procura pelo BSSID no vector access points permitidos
                        int id = Positioning.findWifiBSSID(hotSpot.BSSID);

                        if (id >= 0)
                        {
                            if (hotSpot.level > MIN_RSSI) // limite dBm minimo)
                            {
                                rssi = new HashMap<String, Object>();

                                rssi.put("id", id);
                                rssi.put("ssid", hotSpot.SSID);
                                rssi.put("bssid", hotSpot.BSSID);// MAC
                                rssi.put("level", hotSpot.level);
                                rssi.put("frequency", hotSpot.frequency);
                                
                                // conta os pontos de acesso activos pertencentes ao mapa
                                if (Positioning.isWifiActive(id)) accessPointsActives++;

                                foundWifi.add(rssi);
                            }
                        }
                        else if (Positioning.isAcceptBSSID())//novo ponto de acesso
                        {
                            Positioning.addWifiBSSID(hotSpot.BSSID, hotSpot.SSID, hotSpot.frequency);

                            if (!Positioning.isViewWifiList())
                                Toast.makeText(MapActivity.this, hotSpot.SSID, Toast.LENGTH_SHORT).show();
                        }
                    }

                    // actualiza os valores dos pontos de acesso
                    Positioning.updateWifiList(foundWifi);
                   
                    if (accessPointsActives >= Positioning.getMinAccessPointVisible())
                    {
                        if (Positioning.isUnsignedSignal())
                        {
                            Positioning.disableUnsignedSignal();
                            
                            newNetworkStatus();
                            //removeDialog(PROGRESS_DIALOG_ID);
                        }
                        
                        if (Positioning.isPositioning())
                        {
                            // executa novos calculos de posição
                            Positioning.process();
                        }
                        else
                        {
                            checkNetworkStatus();
                        }
                    }
                    else if (accessPointsActives < Positioning.getMinAccessPointVisible())
                    {
                        if (Positioning.isPositioning() || Positioning.isRadioMap())
                        {
                            Positioning.enableUnsignedSignal();
                            //showDialog(PROGRESS_DIALOG_ID);
                        }
                    }

                    // activa o histograma do acelerometro e o pedometro na opção step_view
                    if (Positioning.getLevelView() == STEP_VIEW && !Positioning.isRadioMap())
                    {
                        if (!Positioning.isPositioning()) Positioning.resetINS();

                        if (!isCompass)
                        {
                            activeCompass();
                        }

                        if (!isPedometer)
                        {
                            activePedometer();
                        }
                    }

                    // desactiva o histograma do acelerometro e o pedometro se não estiver na opção step_view
                    if (Positioning.getLevelView() != STEP_VIEW || Positioning.isRadioMap())
                    {
                        if ((isCompass && !Positioning.isPositioning()) || Positioning.isRadioMap())
                        {
                            sensorManager.unregisterListener(MapActivity.this);

                            DeviceSensor.resetAccelerometer();

                            isCompass = false;
                        }

                        if ((isPedometer && !Positioning.isPositioning()) || Positioning.isRadioMap())
                        {
                            sensorManager.unregisterListener(stepDetector);

                            DeviceStep.resetStep();

                            isPedometer = false;
                        }
                    }
                         
                    registerReceiver(wifiReceiver, filter);

                    wifiManager.startScan();
                }
            }
        }
    }

    private BroadcastReceiver batteryReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context arg0, Intent arg1)
        {
            int level, status = 0;

            if (arg1.getAction().equals(Intent.ACTION_BATTERY_CHANGED))
            {
                level = arg1.getIntExtra("level", 0);

                //Toast.makeText(MapActivity.this, "Bactery Level: " + String.valueOf(level) + "%", Toast.LENGTH_LONG).show();
                
                status = arg1.getIntExtra("status", BatteryManager.BATTERY_STATUS_UNKNOWN);
                //String strStatus;

                //if      (status == BatteryManager.BATTERY_STATUS_CHARGING)              {strStatus = "Charging";}//2
                //else if (status == BatteryManager.BATTERY_STATUS_DISCHARGING)           {strStatus = "Dis-charging";}
                //else if (status == BatteryManager.BATTERY_STATUS_NOT_CHARGING)          {strStatus = "Not charging";}
                //else if (status == BatteryManager.BATTERY_STATUS_FULL)                  {strStatus = "Full";}//5
                //else
                //{
                //    strStatus = "Unknown";
                //}
                
                mapView.showBattery(level, status);

                //Positioning.setNote("Bacttery Status: " + strStatus);

                if (Positioning.isPositioning() && level < 25 && oldLevel != level && status != BatteryManager.BATTERY_STATUS_CHARGING)
                {
                    tts.speak("Bacttery on " + String.valueOf(level) + "%", 1, null);
                    
                    oldLevel = level;
                }
            }
        }
    };

    /*public static void speakSMS(String sms)
    {
        myTts.speak(sms, 0, null);
        myTts.synthesizeToFile(sms,null, "/sdcard/myappsounds/mysms.wav");
    }*/
}
