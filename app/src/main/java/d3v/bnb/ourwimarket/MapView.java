
package d3v.bnb.ourwimarket;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.telephony.SmsManager;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

public class MapView extends View 
{
    private static final int COMPASS_RADIUS = 15;
    private static final int COMPASS_FAN = 60;

    // maxNode view access points list
    private static final int MAX_VIEW_ACCESS_POINTS = 10;

    // view selected
    private static final int DBM_VIEW = 0;
    private static final int GRAPHIC_VIEW = 1;
    private static final int SIGNAL_VIEW = 2;
    private static final int GRID_VIEW = 3;
    private static final int TRILATERACTION_VIEW = 4;
    private static final int DISTANCE_VIEW = 5;
    private static final int STEP_VIEW = 6;

    // accelerometer y z
    //public final static int MAX_VALUES = 300;
    public final static int SCALING = 3;
    public final static int GSCALING = 2;
    public final static int MARGIN_TOP = 90;
    public final static int SEPARATION = 5;
    public final static int MARGIN_LEFT = 10;

    // help msg
    private static final int HELP_TIME = 10000;// 10 segundos
    
    private static final int NETWORK = 0;
    private static final int BATTERY = 1;

    private float halfRectHeight;

    private float xYLine;
    private float yYLine;
    private float zYLine;

    private float pX;
    private float pY;
    private float pZ;
    
    private float lastLeft = Float.MIN_VALUE;
    private float lastTop = Float.MIN_VALUE;

    // dialog
    public static ProgressDialog averageProcessDialog;
    private static int avgTotalProcessDialog;    // total de amostras a usar no dialog
    private static int[] visitedPosition;   // total de amostras obtidas no dialog
    
    // tamanho do ecrã
    private int sw;
    private int sh;

    // coordenada central do mapa
    private float x;
    private float y;
    
    // coordenadas central do gridview
    private int xGrid;
    private int yGrid;

    private float lastMotionX;
    private float lastMotionY;

    // mapa centrado (sim/não)
    public boolean isScreenMapCenter = true;
    private boolean isPositioning;
    private boolean isRadioMap;
    private int levelView;  

    private Bitmap 
            map, 
            miniMap, 
            scratch, 
            updown, 
             redFlag48,
           greenFlag48,
           blackFlag48,
        needleYellow32, 
        needleYellow48,
         needleWhite48,
          needleBlue32,   
          needleBlue48,
               pushPin,
             flagOff48,     
               flag048,
               flag148,
               flag248,
               flag348,
               flag448;
            
    private static Bitmap batteryStatus = null;
    private static Bitmap networkStatus = null;

    private Paint paintPath;
    private Paint paintPoint;
    private Paint paintAccessPoint;
    private Paint paintText;
    private Paint paintLineDistance;
    private Paint paintNote;
    private Paint paintGrid;
    private Paint paintCompass;
    private Paint paintBorder;
    private Paint paintBackground;
    private Paint paintFan;
    private Paint hilite;
    private Paint light;
    private Paint drawingPaint;

    // variaveis necessárias a esta classe
    private float[] mapDimension;
    private int[] matrixDimension;
    private int[] matrixPosition;
    
    // tamanho da celula (metro quadrado)
    private float cell;
    
    // não permite alterações de deslocamento sem estar ativado o posicionamento
    private boolean endAllTouchRectdBm = false;

    // pad inicializado a false (true quando pressionado)
    public static boolean dpad = false;
    public static boolean dpadProcess = false;
    //private static boolean visitedUpdate = true;
    //public  static boolean dpadProcessThread = false;

    // variaveis para o panico
    private TouchThread touchThread = null;
    public static boolean touchProcessThread = false;
    public static boolean isLongTouch = false;
    public static boolean SMSSend = false;

    // lista de access points
    private static ArrayList<AccessPoint> access_points;

    // lista de radio maps
    private static ArrayList<RadioMap> radio_maps;
    
    // radio map
    private static LinkedList visited = new LinkedList();

    // touch
    private Rect simpleTouchPoints;
    private Rect[] touchRectdBm;

    // access point escolhido no modo grafico de linhas
    private int graphViewSSID = -1;// nenhum selecionado
    
    // multitouch and distances
    private ArrayList<PointF> multiTouchPoints;
    private static final int CIRCLE_RADIUS = 20;
    private boolean isMultiTouch = false;
    private int pathEffectPhase = 0;
    private int distanceEffectPhase = 0;

    private float headingMap;
    private float headingCompass;
    
    int positionX = 5;  
    int positionY = 15;  
    
    // scale measure ------------------------------------------------------
    
    final int state_pressed = 1;  
    final int state_normal = 2;  
    final int state_enabled = 3;  
    final int state_disabled = 4;  
    
    int buttonState1;  
    int buttonState2;  
   
    Region region1;  
    Region region2; 
    
    public MapView(Context context)
    {
        super(context);

        // obtem o mapa (imagem)
        map = Positioning.getMap();
        miniMap = Bitmap.createScaledBitmap(map, map.getWidth() / 8, map.getHeight() / 8, true);
        
        // outras imagens
            scratch = BitmapFactory.decodeResource(getResources(), R.drawable.icon_unsigned);
             updown = BitmapFactory.decodeResource(getResources(), R.drawable.updown);
            pushPin = BitmapFactory.decodeResource(getResources(), R.drawable.pushpin);
        
        greenFlag48 = BitmapFactory.decodeResource(getResources(), R.drawable.greenflag48);
        blackFlag48 = BitmapFactory.decodeResource(getResources(), R.drawable.blackflag48);
          redFlag48 = BitmapFactory.decodeResource(getResources(), R.drawable.redflag48);
   
     needleYellow32 = BitmapFactory.decodeResource(getResources(), R.drawable.needleyellow32);       
     needleYellow48 = BitmapFactory.decodeResource(getResources(), R.drawable.needleyellow48);
      needleWhite48 = BitmapFactory.decodeResource(getResources(), R.drawable.needlewhite48);
       needleBlue32 = BitmapFactory.decodeResource(getResources(), R.drawable.needleblue32);       
       needleBlue48 = BitmapFactory.decodeResource(getResources(), R.drawable.needleblue48);
   
          flagOff48 = BitmapFactory.decodeResource(getResources(), R.drawable.flagoff48);     
            flag048 = BitmapFactory.decodeResource(getResources(), R.drawable.flag048);
            flag148 = BitmapFactory.decodeResource(getResources(), R.drawable.flag148);
            flag248 = BitmapFactory.decodeResource(getResources(), R.drawable.flag248);
            flag348 = BitmapFactory.decodeResource(getResources(), R.drawable.flag348);
            flag448 = BitmapFactory.decodeResource(getResources(), R.drawable.flag448);
        
        //-----------------------------------------------------------------------------------------------------------------

        // obtem a dimensão do mapa em pixels (float)
        mapDimension = Positioning.getMapDimension();

        // pixel central do mapa 
        x = mapDimension[0] / 2;
        y = mapDimension[1] / 2;
        
        // obtem a dimensão do mapa em matriz
        matrixDimension = Positioning.getMatrixDimension();

        // tamanho da celula
        cell = Positioning.getMapResolutionScaleCell();

        // obtem a conversão de pixels em showMatrix
        matrixPosition = Positioning.getMapCellPosition(x, y);

        // cursor central da matriz
        xGrid = matrixPosition[0];
        yGrid = matrixPosition[1];

        // construtor do dialog (barra de progresso - média)
        averageProcessDialog = new ProgressDialog(getContext());
        averageProcessDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        averageProcessDialog.setCancelable(false);
        averageProcessDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Abort", new DialogInterface.OnClickListener() 
        {
            public void onClick(DialogInterface dialog, int whichButton) 
            {
                averageProcessDialog.setProgress(0);
                averageProcessDialog.dismiss();
                
                dpadProcess = false;
            }
        });

        // arrays iniciais
        access_points = Positioning.getWifiList();
        radio_maps = Positioning.getRadiomapList();

        // rectangulo superior
        simpleTouchPoints = new Rect();
        
        // multitoutch
        multiTouchPoints = new ArrayList<PointF>();
        
        // touch do graph
        touchRectdBm = new Rect[access_points.size()];

        // paints
        paintPath = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintPath.setStyle(Style.STROKE);
        paintPath.setStrokeWidth(6);
        paintPath.setColor(Color.argb(100, 0, 0, 255));

        paintGrid = new Paint();
        paintGrid.setAntiAlias(true);
        paintGrid.setStrokeWidth(2);

        paintCompass = new Paint();
        paintCompass.setAntiAlias(true);
        paintCompass.setStyle(Style.STROKE);
        paintCompass.setStrokeWidth(2);
        paintCompass.setColor(Color.argb(100, 0, 0, 255));

        paintFan = new Paint(paintCompass);
        paintFan.setStyle(Style.FILL);

        hilite = new Paint();
        hilite.setColor(Color.DKGRAY);// linha sombra

        light = new Paint();
        light.setColor(Color.YELLOW);// linha principal

        paintBorder = new Paint();
        paintBorder.setARGB(125, 255, 0, 0);//150, 200, 200, 200);
        paintBorder.setAntiAlias(true);
        paintBorder.setStyle(Style.STROKE);
        paintBorder.setStrokeWidth(2);

        paintBackground = new Paint();
        paintBackground.setAntiAlias(true);
        paintBackground.setStyle(Style.FILL);
        paintBackground.setColor(Color.argb(150, 0, 0, 0));// (150, 0, 0, 0)

        paintPoint = new Paint();
        paintPoint.setAntiAlias(true);
        paintPoint.setStyle(Style.FILL);
        paintPoint.setColor(Color.RED);

        paintAccessPoint = new Paint();
        paintAccessPoint.setAntiAlias(true);
        paintAccessPoint.setStyle(Style.FILL);

        paintText = new Paint();
        paintText.setAntiAlias(true);
        paintText.setStyle(Style.FILL);
        paintText.setTextSize(10);

        paintLineDistance = new Paint();
        paintLineDistance.setAntiAlias(true);
        paintLineDistance.setStyle(Style.FILL);
        paintLineDistance.setColor(Color.argb(255, 8, 87, 107));

        paintNote = new Paint();
        paintNote.setAntiAlias(true);
        paintNote.setStyle(Style.FILL);
        paintNote.setColor(Color.CYAN);
        paintNote.setTextAlign(Align.CENTER);
        paintNote.setTextSize(10);

        drawingPaint = new Paint();
        drawingPaint.setColor(Color.argb(100, 0, 0, 0));

        // scale measure ------------------------------------------------------

        buttonState2 = state_normal;
        buttonState1 = state_normal;
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        // se na eventualidade não existir um mapa, sai
        if (map == null) return;

        // get screen info
        Rect screen = canvas.getClipBounds();

        sw = screen.width();
        sh = screen.height();

        isPositioning = Positioning.isPositioning();
        isRadioMap = Positioning.isRadioMap();

        levelView = Positioning.getLevelView();

        // recebe a posição do posicionamento final
        if (isPositioning)
        {
            float[] position = Positioning.matchedPoint();
            //float[] position = Positioning.wifiKalman();

            //position[0] = position[0] * (int)Positioning.getMapResolutionScaleCell() +
            //                            (int)Positioning.getMapResolutionScaleCell() / 2;

            //position[1] = position[1] * (int)Positioning.getMapResolutionScaleCell() +
            //                            (int)Positioning.getMapResolutionScaleCell() / 2;

            position[0] = position[0] * (int)cell + (int)cell / 2;
            position[1] = position[1] * (int)cell + (int)cell / 2;

            x = position[0];
            y = position[1];

            isScreenMapCenter = false;
        }
        else
        {
            if (!isScreenMapCenter)
            {
                x = mapDimension[0] / 2;
                y = mapDimension[1] / 2;

                isScreenMapCenter = true;

                if (!isRadioMap)
                {
                    // obtem a conversão de pixels em showMatrix
                    matrixPosition = Positioning.getMapCellPosition(x, y);

                    // cursor central da matriz
                    xGrid = matrixPosition[0];
                    yGrid = matrixPosition[1];
                }
            }
        }

        // The pixel coordinate of the screen top left corner
        float left = x - sw/2;
        float  top = y - sh/2;

        // É neste momento que a array de access points é actualizada nesta classe.
        // Como os novos AP descobertos estão por configurar (0,0,0) não é necessário
        // actualizar as respectivas classes
        if (Positioning.isAcceptBSSID()) access_points = Positioning.getWifiList();

        // se existir uma alteração no vetor de access points ou,
        // se estiver no radiomap
        if ((touchRectdBm.length != access_points.size()) || (isRadioMap))
        {
//como resolver isto (garbare colector)?
            touchRectdBm = new Rect[access_points.size()];
        }

        // recebe o norte do mapa
        headingMap = DeviceSensor.getMapHeading();
        headingCompass = DeviceSensor.getCompassHeading();

        // roda o mapa (bussula)
        if (isPositioning)
        {
            // draw the map within the screen, if not routing
            // Guardar o estado do Canvas
            canvas.save();
            canvas.rotate(headingMap, sw/2, sh/2);

            if (Positioning.isMapView()) canvas.drawBitmap(map, -left, -top, null);

            //Retornando o estado do Canvas
            canvas.restore();
        }
        else
        {
            if (Positioning.isMapView()) canvas.drawBitmap(map, -left, -top, null);

            if (levelView == STEP_VIEW)
            {
                Paint p = new Paint();
                p.setColor(Color.argb(100, 200, 200, 200));

                Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.compassrose);

                canvas.save();
                canvas.rotate(headingCompass, sw/2, sh/2);

                canvas.drawBitmap(bmp, (sw/2) - (bmp.getWidth()/2), (sh/2) - (bmp.getHeight()/2), p);

                canvas.restore();
            }

            headingMap = 0.0f;
        }

        canvas.save();
        canvas.rotate(headingMap, sw/2, sh/2);

        // Mostra a matriz (MapView)
        if ((Positioning.isViewWifiList() && levelView == GRID_VIEW) ||
            (Positioning.isViewWifiList() && isRadioMap)) showMapGrid(canvas, -left, -top);

        // show access points canvas
        if (!isRadioMap) showMapPoint(canvas, -left, -top);

        canvas.restore();

        if (!isRadioMap && Positioning.isViewWifiList())
        {
            canvas.save();
            canvas.rotate(headingMap, sw/2, sh/2);

            // mostra os nodos
            if (levelView == DISTANCE_VIEW)
                showDistanceNode(canvas, -left, -top);

            // mostra a trilateração/multilateração
            if (levelView == TRILATERACTION_VIEW)
                showMapLateration(canvas, -left, -top);

            // mostra a notas dos access points (toutch)
            if (levelView == GRAPHIC_VIEW && headingMap == 0.0f)
                showMapNodeTouch(canvas, -left, -top);

            canvas.restore();

            // mostra o grafico de linhas do access point escolhido em showAccessPoints
            if (levelView == GRAPHIC_VIEW && !access_points.isEmpty())
                showGraphView(canvas);

            // mostra lista access points em dBm ou percentagem
            if (levelView != GRAPHIC_VIEW && levelView != STEP_VIEW)
                showAccessPointsList(canvas);

            // mostra o contador de passos
            if (levelView == STEP_VIEW)
                showStepView(canvas, -left, -top);
        }

        // Mostra os valores (dBm) do radiomap
        if (isRadioMap)
        {
            showRadioMap(canvas, -left, -top);

            if(Positioning.isManageRadioMap())
                manageRadioMap(canvas, -left, -top);

            showMiniMap(canvas);
        }

        // apresenta a bussula do mapa
        if (isPositioning)
        {
            //heading map
            canvas.drawCircle(sw/2, sh/2, COMPASS_RADIUS, paintCompass);

            RectF oval = new RectF(sw/2 - COMPASS_RADIUS, sh/2 - COMPASS_RADIUS,
                                   sw/2 + COMPASS_RADIUS, sh/2 + COMPASS_RADIUS);

            if (Positioning.isRotateMap())
            {
                canvas.drawArc(oval, Map.NORTH - 90 - COMPASS_FAN / 2, COMPASS_FAN, true, paintFan);
            }
            else
            {
                canvas.drawArc(oval, headingCompass + Map.NORTH - 90 - COMPASS_FAN / 2, COMPASS_FAN, true, paintFan);
            }

            // desenha uma cruz
            canvas.drawLine(sw/2 - 4, sh/2 - 4, sw/2 + 4, sh/2 + 4, paintPoint);
            canvas.drawLine(sw/2 - 4, sh/2 + 4, sw/2 + 4, sh/2 - 4, paintPoint);

            showMiniMap(canvas);
        }
        else // coloca um ponto no centro
        {
            canvas.drawCircle(sw/2, sh/2, 2, paintPoint);
        }

        // rectangulo de mensagens inferior
        RectF drawRectNote = new RectF(10, sh - 31, sw - 10, sh - 10);
        canvas.drawRect(10, sh - 31, sw - 10, sh - 10, paintBackground);
        canvas.drawRoundRect(drawRectNote, 5, 5, paintBorder);

        // apresenta a mensagem (note)
        canvas.drawText(Positioning.getNote(), 160, sh - 17, paintNote);// centra a msg

        // Alerta ausência de access points
        if (Positioning.isUnsignedSignal())
        {
            canvas.drawBitmap(scratch, sw/2 - scratch.getWidth()/2, sh/2 - scratch.getHeight()/2, null);
        }
        else
        {
            //Alerta o estado da bateria
            if (batteryStatus != null)
            {
                canvas.drawBitmap(batteryStatus, sw/2 - batteryStatus.getWidth()/2, sh - sh/8 - batteryStatus.getHeight(), null);
            }

            //Alerta o estado da rede
            if (networkStatus != null)
            {
                canvas.drawBitmap(networkStatus, sw/2 - networkStatus.getWidth()/2, sh/2 - networkStatus.getHeight()/2, null);
            }
        }

        // apresenta multi touch
        if (multiTouchPoints.size() > 0) multiToutch(canvas);

        // actualiza
        invalidate();
    }

    //------------------------------------------------------------------------------------------

    // actualiza a array do mapa de potências para esta classe
    public void updateRadioMap()
    {
        radio_maps = Positioning.getRadiomapList();
    }

    //------------------------------------------------------------------------------------------


    //------------------------------------------------------------------------------------------

    public void showNetwork(final String status)
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                Timer timer;
                int seconds = 5;

                try
                {
                    if (status.equals("Wifi"))
                    {
                        networkStatus = BitmapFactory.decodeResource(getResources(), R.drawable.iconsigned);
                    }
                    else
                    {
                        networkStatus = BitmapFactory.decodeResource(getResources(), R.drawable.icon_unsigned);
                    }

                } catch (Exception e) { }

                timer = new Timer();
                timer.schedule(new RemindTask(NETWORK), seconds * 1000);
            }
        }).start();
    }

    public void showBattery(final int level, final int status)
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                Timer timer;
                int seconds = 5;

                try
                {
                    if       (status == 1) {batteryStatus = BitmapFactory.decodeResource(getResources(), R.drawable.batteryplug);}// ac/dc
                    else if  (status == 2) {batteryStatus = BitmapFactory.decodeResource(getResources(), R.drawable.batterycharging);}// charging
                    else if  (status == 4) {batteryStatus = BitmapFactory.decodeResource(getResources(), R.drawable.batterynotcharging);}// not charging
                    else if  (level == 10) {batteryStatus = BitmapFactory.decodeResource(getResources(), R.drawable.battery10);}
                    else if  (level == 20) {batteryStatus = BitmapFactory.decodeResource(getResources(), R.drawable.battery20);}
                    else if  (level == 40) {batteryStatus = BitmapFactory.decodeResource(getResources(), R.drawable.battery40);}
                    else if  (level == 60) {batteryStatus = BitmapFactory.decodeResource(getResources(), R.drawable.battery60);}
                    else if  (level == 80) {batteryStatus = BitmapFactory.decodeResource(getResources(), R.drawable.battery80);}
                    else if (level == 100) {batteryStatus = BitmapFactory.decodeResource(getResources(), R.drawable.battery100);}
                    else                   {batteryStatus = BitmapFactory.decodeResource(getResources(), R.drawable.battery0);}

                } catch (Exception e) { }

                timer = new Timer();
                timer.schedule(new RemindTask(BATTERY), seconds * 1000);
            }
        }).start();
    }

    private static class RemindTask extends TimerTask
    {
        int id;

        private RemindTask(int source)
        {
            this.id = source;
        }

        @Override
        public void run()
        {
            switch (id)
            {
                case NETWORK:

                    networkStatus = null;
                    break;

                case BATTERY:

                    batteryStatus = null;
                    break;
            }
        }
    }

    //------------------------------------------------------------------------------------------

    // desenha o multitoutch no ecrã
    public void multiToutch(Canvas canvas)
    {
        DashPathEffect effect = new DashPathEffect(new float[] {7,7}, pathEffectPhase);
        PointF midpt = null;

        drawingPaint.setPathEffect(effect);

        for(int i = 1; i < multiTouchPoints.size(); ++i)
        {
            midpt = getMidPoint(multiTouchPoints.get(i - 1).x, multiTouchPoints.get(i - 1).y,
                                multiTouchPoints.get(i).x,multiTouchPoints.get(i).y);

            // primeiro dedo
            canvas.drawCircle(multiTouchPoints.get(i - 1).x, multiTouchPoints.get(i - 1).y, 1, drawingPaint);
            canvas.drawCircle(multiTouchPoints.get(i - 1).x, multiTouchPoints.get(i - 1).y, CIRCLE_RADIUS, drawingPaint);

            // segundo dedo
            canvas.drawCircle(multiTouchPoints.get(i).x, multiTouchPoints.get(i).y, 1, drawingPaint);
            canvas.drawCircle(multiTouchPoints.get(i).x, multiTouchPoints.get(i).y, CIRCLE_RADIUS, drawingPaint);

            // linha
            canvas.drawLine(multiTouchPoints.get(i - 1).x, multiTouchPoints.get(i - 1).y,
                            multiTouchPoints.get(i).x, multiTouchPoints.get(i).y, drawingPaint);

            // circulo do meio
            canvas.drawCircle(midpt.x, midpt.y, 5, drawingPaint);
        }

        ++pathEffectPhase;
    }

    //------------------------------------------------------------------------------------------

    private void showMiniMap(Canvas canvas)
    {
        //Paint border = new Paint(paintBorder);
        //paintPath.setStyle(Paint.Style.FILL_AND_STROKE);

        //int sw = (int)(p.measureText(mDefCaption) + 0.5f);

        // caixa do minimap
        RectF rectangle = new RectF(sw - miniMap.getWidth() - 20, sh - miniMap.getHeight() - 41 - 5,
                                    sw - 10, sh - 31 - 5);
        canvas.drawRect(sw - miniMap.getWidth() - 20, sh - miniMap.getHeight() - 41 - 5,
                        sw - 10, sh - 31 - 5, paintBackground);
        canvas.drawRoundRect(rectangle, 5, 5, paintBorder);

        // minimap
        canvas.drawBitmap(miniMap, sw - miniMap.getWidth() - 15, sh - miniMap.getHeight() - 41, null);

        //coloca um circulo no minimap
        float xCell = -1;
        float yCell = -1;

        if (isRadioMap)
        {
            xCell = (((xGrid / (int)cell) + 1) * (miniMap.getWidth()  / (float)matrixDimension[0]));
            yCell = (((yGrid / (int)cell) + 1) * (miniMap.getHeight() / (float)matrixDimension[1]));
        }
        else if (isPositioning)
        {
            xCell = (((x / (int)cell)) * (miniMap.getWidth()  / (float)matrixDimension[0])) + ((miniMap.getWidth()  / (float)matrixDimension[0]) / 2);
            yCell = (((y / (int)cell)) * (miniMap.getHeight() / (float)matrixDimension[1])) + ((miniMap.getHeight() / (float)matrixDimension[1]) / 2);
        }

        if (xCell != -1 && yCell != -1)
        {
            Paint circlePaint = new Paint(paintPoint);
            circlePaint.setStyle(Style.STROKE);

            canvas.drawCircle(sw - miniMap.getWidth() - 15 + xCell, sh - miniMap.getHeight() - 41 + yCell,
                             (miniMap.getWidth()  / (float)matrixDimension[0]), circlePaint);
        }

    }

    // apresenta movimentos (sem routing)
    public void showStepView(Canvas canvas, float left, float top)
    {
        Paint p = new Paint();

        Paint background = new Paint(paintBackground);
        Paint border = new Paint(paintBorder);

        Paint paintAccel = new Paint(paintText);
        paintAccel.setTextAlign(Align.CENTER);
        paintAccel.setTextSize(25);

        // rectangulo de movimento
        RectF drawRectAccel = new RectF(10, 10, sw/2 - 5, 82);
        canvas.drawRect(10, 10, sw/2 - 5, 82, background);

        if (DeviceSensor.isMoving())
        {
            border.setColor(Color.argb(200, 19, 166, 50));
            canvas.drawRoundRect(drawRectAccel, 5, 5, border);

            paintAccel.setColor(Color.GREEN);
            canvas.drawText("Moving", sw/4 - 2, 55, paintAccel);
        }
        else
        {
            border.setColor(Color.argb(200, 231, 47, 39));
            canvas.drawRoundRect(drawRectAccel, 5, 5, border);

            paintAccel.setColor(Color.RED);
            canvas.drawText("Stopped", sw/4 - 2, 55, paintAccel);
        }

        // rectangulo de passos
        drawRectAccel = new RectF(sw/2 + 5, 10, sw - 10, 82);
        canvas.drawRect(sw/2 + 5, 10, sw - 10, 82, background);
        border.setColor(Color.argb(200, 255, 200, 8));
        canvas.drawRoundRect(drawRectAccel, 5, 5, border);

        // steps
        paintAccel.setColor(Color.YELLOW);
        canvas.drawText("" + DeviceStep.getStepCount(), sw - sw/4 + 2, 55, paintAccel);

        // distance
        paintAccel.setTextSize(10);
        canvas.drawText("" + Positioning.getStepLength() + " cm", sw - sw/4 + 2, 25, paintAccel);
        canvas.drawText("" + DeviceStep.getStepDistance() + " meters", sw - sw/4 + 2, 75, paintAccel);

        Rect btnRect = new Rect(10, 10, sw - 10, 82);
        simpleTouchPoints = new Rect(btnRect);

        halfRectHeight = SCALING * 10 * GSCALING;

        xYLine = MARGIN_TOP + SCALING * 10 * GSCALING;
        yYLine = xYLine + halfRectHeight * 2 + SEPARATION;
        zYLine = yYLine + halfRectHeight * 2 + SEPARATION;

        float length = (sw - MARGIN_LEFT * 2) / DeviceSensor.MAX_VALUES;

	float xVal = MARGIN_LEFT;
	boolean first = true;

        for (float[] f : DeviceSensor.getAccelerometerFifo())
        {
            float x = f[0];
            float y = f[1];
            float z = f[2];

            if (first)
            {
                pX = x;
                pY = y;
                pZ = z;

                first = false;
            }

            //linha x
            p.setColor(Color.GREEN);
            canvas.drawLine(xVal, xYLine - pX * SCALING, xVal + length, xYLine - x * SCALING, p);

            //linha y
            p.setColor(Color.RED);
            canvas.drawLine(xVal, yYLine - pY * SCALING, xVal + length, yYLine - y * SCALING, p);

            //linha z
            p.setColor(Color.BLUE);
            canvas.drawLine(xVal, zYLine - pZ * SCALING, xVal + length, zYLine - z * SCALING, p);

            xVal += length;

            pX = x;
            pY = y;
            pZ = z;
        }

        //-----------------------------
        // se não estiver em posicionamento, sai. Só interessa mostrar a posição wifi e o movimento
        // em caso de posicionamento.

        if (!isPositioning) return;

        //-----------------------------

        float[] match = Positioning.matchedPoint();
        float[] displace = Positioning.displacement();
        float[] wifi = Positioning.wifiPoint();
        //float[] kalman = Positioning.wifiKalman();

        // ins
        p.setColor(Color.BLUE);
        canvas.drawCircle(left + (int)cell / 2 + (displace[0] + match[0]) * (int)cell,
                           top + (int)cell / 2 + (displace[1] + match[1]) * (int)cell, 4, p);

        // posição wifi
        p = new Paint(paintBorder);
        p.setColor(Color.argb(255, 255, 102, 0));

        canvas.save();
        canvas.rotate(headingMap, sw/2, sh/2);

        canvas.drawCircle(left + (int)cell / 2 + wifi[0] * (int)cell,
                           top + (int)cell / 2 + wifi[1] * (int)cell, ((cell + 1) / 3) - 1, p);

        //p.setColor(Color.YELLOW);
        //canvas.drawCircle(left + (int)cell / 2 + kalman[0] * (int)cell,
        //                   top + (int)cell / 2 + kalman[1] * (int)cell, ((cell + 1) / 3) - 1, p);

        canvas.restore();
    }

    //------------------------------------------------------------------------------------------

    // lista os access points no rectangulo superior
    public void showAccessPointsList(Canvas canvas)
    {
        int row;

        int total = access_points.size();

        if (total > MAX_VIEW_ACCESS_POINTS)
            row = countActive();
        else
            row = total;

        canvas.drawRect(10, 10, sw - 10, 42 + (row * 10), paintBackground);

        RectF drawRectF = new RectF(10, 10, sw - 10, 42 + (row * 10));
        canvas.drawRoundRect(drawRectF, 5, 5, paintBorder);

        Rect btnRect = new Rect(10, 10, sw - 10, 42 + (row * 10));
        simpleTouchPoints = new Rect(btnRect);

        paintText.setColor(Color.CYAN);

        if (total > 0 && levelView != GRAPHIC_VIEW)
        {
            row = 0;

            canvas.drawText("Access Points list: " + access_points.size(), 70, 25, paintText);

            if (isPositioning)
            {
                int[] a = Positioning.integrateLevel();
                String msg = "" + a[0] + "/" + a[1];

                float w = paintText.measureText(msg, 0, msg.length());
                canvas.drawText(msg, getWidth() - w - 20, 25, paintText);
            }

            for (int i = 0; i < access_points.size(); i++)
            {
                AccessPoint ac = (AccessPoint) access_points.get(i);

                if (total <= MAX_VIEW_ACCESS_POINTS) row = i;

                if (ac.isActive())
                {
                    if (levelView != SIGNAL_VIEW)
                    {
                        paintText.setColor(Color.LTGRAY);
                        canvas.drawText(ac.getMode() + " dBm", 20, 45 + (row * 10), paintText);
                    }
                    else
                    {
                        Paint paintLevel = paintText;

                        int level = ac.getSignalLevel();

                        // 0 - 4
                        if (level  > 2) paintLevel.setColor(Color.GREEN);
                        if (level == 2) paintLevel.setColor(Color.YELLOW);
                        if (level  < 2) paintLevel.setColor(Color.RED);

                        canvas.drawRect(62, 39 + (row * 10), 62 - (10 * level), 43 + (row * 10), paintLevel);
                    }

                    paintText.setColor(Color.GREEN);
                    canvas.drawText(ac.ssid(), 70, 45 + (row * 10), paintText);
                }
                else if (!ac.isActive() && total <= MAX_VIEW_ACCESS_POINTS)
                {
                    paintText.setColor(Color.YELLOW);
                    canvas.drawText(ac.ssid(), 70, 45 + (row * 10), paintText);
                }

                if (ac.isActive() || (!ac.isActive() && total <= MAX_VIEW_ACCESS_POINTS))
                {
                    canvas.drawText("" + ac.distance_x(), 190, 45 + (row * 10), paintText);
                    canvas.drawText("" + ac.distance_y(), 230, 45 + (row * 10), paintText);
                    canvas.drawText("" + ac.distance_z(), 270, 45 + (row * 10), paintText);

                    row++;
                }
            }
        }
        else if(total <= 0)// caso não exista nenhum access point na lista, solicita uma pesquisa
        {
            canvas.drawText("Click to add new Access Points", 160, 30, paintNote);
        }
    }

    //------------------------------------------------------------------------------------------

    // coloca os pontos da localização dos access points no mapa
    public void showMapPoint(Canvas canvas, float left, float top)
    {
        Bitmap flag = null;

        for (int i = 0; i < access_points.size(); i++)
        {
            int adjust = 0;

            AccessPoint ac = (AccessPoint) access_points.get(i);

            //coloca os pontos no mapa de access points válidos
            if (ac.distance_x() + ac.distance_y() + ac.distance_z() > 0)
            {
                if (levelView == SIGNAL_VIEW)
                {
                    adjust = 17;

                    if (ac.isActive())
                    {
                        int level = ac.getSignalLevel();

                        // 0 - 4
                        if (level == 0) flag = flag048;
                        if (level == 1) flag = flag148;
                        if (level == 2) flag = flag248;
                        if (level == 3) flag = flag348;
                        if (level == 4) flag = flag448;
                    }
                    else
                    {
                        flag = flagOff48;
                    }
                }
                else if (ac.isActive())
                {
                    flag = greenFlag48;
//2 -  permitir a deslocação da caixa pelo mapa
                }
                else
                {
                    flag = redFlag48;
                }

                //horizontal, vertical (0,0)
                float[] position = Map.toMapScale(ac.distance_x(), ac.distance_y());

                canvas.drawBitmap(flag, left + position[0] - flag.getWidth() / 2 + adjust,
                                         top + position[1] - flag.getHeight(), null);
            }
        }
    }

    //------------------------------------------------------------------------------------------

    // mostra os access points (ssid) no mapa
    public void showMapNodeTouch(Canvas canvas, float left, float top)
    {
        float[] position = null;

        int length;

        double goodStdDev = Positioning.getGoodStdDev();
        double badStdDev  = Positioning.getBadStdDev();

        Paint border = new Paint(paintBorder);
        border.setColor(Color.argb(255, 255, 102, 0));

        paintText.setColor(Color.WHITE);

        // corrige o deslocamento do toutch
        if (isPositioning && lastLeft != Float.MIN_VALUE && lastTop != Float.MIN_VALUE)
        {
            moveAllTouchRectdBm((int)left - (int)lastLeft, (int)top - (int)lastTop);

            endAllTouchRectdBm = true;
        }
        else
        {
            // processa pela ultima vez?
            if (endAllTouchRectdBm)
            {   // não origina erro porque depende de um primeiro posicionamento
                moveAllTouchRectdBm(left - lastLeft, top - lastTop);

                endAllTouchRectdBm = false;
            }
        }

        for (int i = 0; i < access_points.size(); i++)
        {
            Rect btnRect;

            AccessPoint ac = (AccessPoint) access_points.get(i);

            // inicializa uma primeira vez
            if (touchRectdBm[i] == null)
            {
                btnRect = new Rect();

                touchRectdBm[i] = new Rect(btnRect);
            }

            // é criado uma caixa de toque para cada ponto de acesso activo valido
            if (ac.isValid())
            {
                // define o tamanho da caixa
                if (ac.ssid().length() <= 7)
                {
                    length = Math.round(paintText.getTextSize() * 5);
                }
                else
                {
                    length = Math.round(paintText.measureText(ac.ssid(), 0, ac.ssid().length()) + paintText.getTextSize());
                }

                // define a cor em relação ao desvio padrão actual
                if (ac.getStdDev() < goodStdDev)
                {
                    border.setColor(Color.argb(200, 106, 168, 82));
                }
                else if(ac.getStdDev() >= goodStdDev && ac.getStdDev() < badStdDev)
                {
                    border.setColor(Color.argb(200, 245, 223, 113));
                }
                else if(ac.getStdDev() >= badStdDev)
                {
                    border.setColor(Color.argb(200, 224, 73, 87));
                }

                if (touchRectdBm[i].isEmpty())// novo touch
                {

                    // cria a caixa de toque se não estiver em rotação
                    //if (headingMap == 0.0f)

                    position = Map.toMapScale(ac.distance_x(), ac.distance_y());

                    // armazena a sua posição para o toque seleccionado
                    touchRectdBm[i].left   = Math.round(left + position[0]);
                    touchRectdBm[i].top    = Math.round(top  + position[1]);
                    touchRectdBm[i].right  = Math.round(left + position[0] + length - 8);
                    touchRectdBm[i].bottom = Math.round(top  + position[1] + 12);//19

                    // faz caixa de toque
                    btnRect = new Rect(touchRectdBm[i].left - 7, touchRectdBm[i].top - 7,
                                       touchRectdBm[i].right   , touchRectdBm[i].bottom);

                    touchRectdBm[i] = btnRect;
                }

                // desenho da caixa
                canvas.drawRect(touchRectdBm[i].left - 7, touchRectdBm[i].top - 7,
                                touchRectdBm[i].right   , touchRectdBm[i].bottom, paintBackground);

                // rebordo da caixa
                RectF drawRectF = new RectF(touchRectdBm[i].left - 7, touchRectdBm[i].top - 7,
                                            touchRectdBm[i].right   , touchRectdBm[i].bottom);

                canvas.drawRoundRect(drawRectF, 5, 5, border);

                // coloca a imagem do pin
                canvas.drawBitmap(pushPin, touchRectdBm[i].left - pushPin.getWidth()  / 2 + 6 ,
                                           touchRectdBm[i].top  - pushPin.getHeight() / 2 - 11, null);

                // apresenta o ssid e o dBm
                canvas.drawText(ac.getMode() + " dBm", touchRectdBm[i].left + 5, touchRectdBm[i].top + 5 , paintText);//+5;-7
                canvas.drawText(ac.ssid(),             touchRectdBm[i].left + 5, touchRectdBm[i].top + 15, paintText);//+5;+3
            }
        }

        lastLeft = left;
        lastTop = top;
    }

    //------------------------------------------------------------------------------------------

    // mostra o MapView
    public void showMapGrid(Canvas canvas, float left, float top)
    {
        @SuppressWarnings("UseOfObsoleteCollectionType")
        Vector vec = new Vector();

        // Draw the minor MapView lines
        for (int k = 0; k < mapDimension[0]; k += cell)
        {
            canvas.drawLine(left + k - 1, top - 6, left + k - 1, top + 5 + mapDimension[1] - 1, light);
            canvas.drawLine(left + k,     top - 6, left + k,     top + 5 + mapDimension[1] - 1, hilite);
        }

        for (int k = 0; k < mapDimension[1]; k += cell)
        {
            canvas.drawLine(left - 6, top + k - 1, left + 5 + mapDimension[0] - 1, top + k - 1, light);
            canvas.drawLine(left - 6, top + k,     left + 5 + mapDimension[0] - 1, top + k, hilite);
        }

        // coloca bandeiras a sinalizar o local do ponto de acesso
        if (radio_maps.size() > 0)
        {
            Bitmap needle;

            for (int i = 0; i < radio_maps.size(); i++)
            {
                RadioMap rm = (RadioMap) radio_maps.get(i);

                // obtem as coordenadas principais de um radiomap (bssid)
                vec = rm.coordinateList();

                for (int k = 0; k < vec.size(); k++)
                {
                    @SuppressWarnings("UseOfObsoleteCollectionType")
                    Vector temp = (Vector) vec.elementAt(k);

                    int col = (Integer) temp.elementAt(0);
                    int row = (Integer) temp.elementAt(1);

                    if (isRadioMap)
                    {
                        if (col == (xGrid / (int)cell) && row == (yGrid / (int)cell))
                        {
                            // optar por colocar outra coisa ou até nada
                            needle = needleYellow48;
                        }
                        else
                        {
                            needle = needleYellow48;
                        }
                    }
                    else
                    {
                        needle = needleYellow32;
                    }

                    canvas.drawBitmap(needle,
                        (left + col * (int)cell + (int)cell / 2) - needle.getWidth() / 2,
                         (top + row * (int)cell + (int)cell / 2) - needle.getHeight(), null);
                }
            }
        }

        // apresenta a matrix com as porcentagens dos calculos
        if (isPositioning)//---------------------------------------------- rapido
        {
            Paint info = new Paint(light);
            info.setColor(Color.LTGRAY);
            info.setTextSize(10);

            float[][] probability = Positioning.getPdfProbability();

            for (int col = 0; col < matrixDimension[0]; col++)
            {
                for (int row = 0; row < matrixDimension[1]; row++)
                {
                    if ((int)(probability[col][row] * 100) > 0)
                    {
                        canvas.drawRect(left + col * (int)cell + 1,
                                         top + row * (int)cell + 1,
                                        left + col * (int)cell + (int)cell - 1,
                                         top + row * (int)cell + (int)cell - 1, paintBackground);

                        String d = "" + (probability[col][row] * 100);
                        int lenght = d.indexOf(".");

                        d = d.substring(0, lenght);

                        float w = paintText.measureText(d, 0, d.length());

                        canvas.drawText(d, left + col * (int)cell + (cell / 2) - (w / 2),
                                            top + row * (int)cell + (cell / 2) + (paintText.getTextSize() / 2) - 1, info);
                    }
                }
            }
        }
    }

    //------------------------------------------------------------------------------------------

    // Mostra o radiomap
    public void showRadioMap(Canvas canvas, float left, float top)
    {
        Rect drawRect;
        RectF drawRectF;

        paintText.setColor(Color.BLUE);

        // renova a lista enquanto não iniciar o processo de captura (false por defeito)
        if (!dpadProcess)
        {
            visited.clear();

            // prepara a lista dos pontos de acesso a serem capturados num raio de 10 metros
            for (int i = 0; i < access_points.size(); i++)
            {
                AccessPoint ac = (AccessPoint) access_points.get(i);

                if (ac.isValid())
                {
                    float distance = Util.distance(
                            ac.distance_x(),                            // x1
                            ac.distance_y(),                            // y1
                            ac.distance_z(),                            // z1
                            ((xGrid / (int)cell) * 100) + (100 / 2),    // x2
                            ((yGrid / (int)cell) * 100) + (100 / 2),    // y2
                            Lateration.HUMAN_HEIGHT);                   // z2

                    double realDistance = Math.sqrt(
                            Math.pow(distance, 2) - Math.pow(ac.distance_z() - Lateration.HUMAN_HEIGHT, 2));

                    // se um ponto de acesso estiver num raio de 10 metros, aceita-o para captura
                    if (realDistance < Positioning.getMaxDistanceAllowed())
                        visited.add(i);
                }
            }
        }

        int height = visited.size() * 10;
        //boolean positionFound = false;

        if (height == 0)
        {
            // caixa "now"
            drawRect = new Rect(10, 10, sw - 10, 42);
            canvas.drawRect(10, 10, sw - 10, 42, paintBackground);

            drawRectF = new RectF(10, 10, sw - 10, 42);
            canvas.drawRoundRect(drawRectF, 5, 5, paintBorder);

            paintText.setColor(Color.argb(255, 229, 183, 90));
            canvas.drawText("Access points missing or outside of the range of calculation.", 20, 30, paintText);
        }
        else
        {
            // caixa "now"
            drawRect = new Rect(10, 10, sw/2 + 20, 32 + height);
            canvas.drawRect(10, 10, sw/2 + 20, 32 + height, paintBackground);

            drawRectF = new RectF(10, 10, sw/2 + 20, 32 + height);
            canvas.drawRoundRect(drawRectF, 5, 5, paintBorder);

            // caixa "map"
            drawRect = new Rect(sw/2 + 30, 10, sw - 10, 32 + height);
            canvas.drawRect(sw/2 + 30, 10, sw - 10, 32 + height, paintBackground);

            drawRectF = new RectF(sw/2 + 30, 10, sw - 10, 32 + height);
            canvas.drawRoundRect(drawRectF, 5, 5, paintBorder);

            for (int i = 0; i < visited.size(); i++)
            {
                AccessPoint ac = (AccessPoint) access_points.get(visited.get(i).hashCode());

                if (ac.isActive())
                {
                    paintText.setColor(Color.LTGRAY);
                    canvas.drawText(ac.getMode() + " dBm", 20, 30 + (i * 10), paintText);

                    paintText.setColor(Color.GREEN);
                }
                else
                {
                    paintText.setColor(Color.YELLOW);
                }

                canvas.drawText(ac.ssid(), 70, 30 + (i * 10), paintText);

                if (averageProcessDialog.isShowing())
                {
                    paintText.setColor(Color.YELLOW);
                    canvas.drawText("Acquiring samples: " + ac.getAvgDialogProcess(), sw/2 + 40, 30 + (i * 10), paintText);
                }
                else
                {
                    int pos = Positioning.findRadiomapBSSID(ac.bssid());

                    if (pos >= 0)
                    {
                        RadioMap rm = (RadioMap) radio_maps.get(pos);

                        float   dbm = rm.getDbmMatrix(  xGrid / (int)cell, yGrid / (int)cell);
                        float error = rm.getErrorMatrix(xGrid / (int)cell, yGrid / (int)cell);

                        if (dbm < 0)
                        {
                            int mode = ac.getMode();
                            // média
                            paintText.setColor(Color.GREEN);
                            canvas.drawText(dbm + "00000", 0, 5, sw/2 + 40, 30 + (i * 10), paintText);

                            // diferença actual
                            paintText.setColor(Color.YELLOW);
                            canvas.drawText(Math.abs(dbm - mode) + "0000", 0, 4, sw/2 + 80, 30 + (i * 10), paintText);

                            // actualiza erro
                            if (Math.abs(dbm - mode) > error)
                            {
                                error = Math.abs(dbm - mode);

                                Positioning.updateError(pos, xGrid / (int)cell, yGrid / (int)cell, error);
                            }

                            // diferença maxima
                            paintText.setColor(Color.CYAN);
                            canvas.drawText(error + "0000", 0, 4, sw/2 + 120, 30 + (i * 10), paintText);

                            //positionFound = true;
                        }
                        else
                        {
                            paintText.setColor(Color.argb(255, 229, 183, 90));
                            canvas.drawText("Missing point.", sw/2 + 40, 30 + (i * 10), paintText);
                        }
                    }
                }
            }
        }

        // executa as threads que vão ser acionadas para efectuar a media
        // se não estiver já em captura e existir o minimo de pontos de acesso
        if (dpad && height > 0)// && !dpadProcessThread)// && rssi.length >= WifiPositioning.MIN_AP_VISIBLE)
        {
            dpadProcess = true;

            boolean matrixFound = false;

            //se já existe um ponto pergunta se deseja eliminar ou actualizar
            //(desenvolver para um ponto especifico e não para todos)
            for (int i = 0; i < radio_maps.size(); i++)
            {
                RadioMap rm = (RadioMap) radio_maps.get(i);

                if (rm.isMatrix(xGrid / (int)cell, yGrid / (int)cell))
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());

                    builder.setPositiveButton("Update Point", new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int id)
                        {
                            showProceedRadioMap();
                        }
                    })
                    .setNegativeButton("Delete Point", new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int id)
                        {
                            for (int i = 0; i < radio_maps.size(); i++)
                            {
                                RadioMap rm = (RadioMap) radio_maps.get(i);

                                // limpa a posição
                                rm.addDbmMatrix(xGrid / (int)cell, yGrid / (int)cell, 0);
                            }

                            dpadProcess = false;
                        }
                    })
                    .setNeutralButton("Cancel Operation", new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int id)
                        {
                            dpadProcess = false;

                            dialog.cancel();
                        }
                    });

                    AlertDialog alert = builder.create();
                    alert.setCancelable(false);
                    alert.show();

                    matrixFound = true;

                    // basta-me encontrar um radiomap
                    break;
                }
            }

            // caso não exista, avança directamente para o processamento de dados
            if (!matrixFound) showProceedRadioMap();

            dpad = false;
        }

        // caixa "cursor"
        /*drawRect = new Rect((int)left + xGrid + (int)cell / 3,
                             (int)top + yGrid + (int)cell / 3,
                            (int)left + xGrid + (int)cell - (int)cell / 3,
                             (int)top + yGrid + (int)cell - (int)cell / 3);

        paintGrid.setColor(Color.argb(100, 51, 153, 255));
        paintGrid.setStyle(Paint.Style.FILL);
        canvas.drawRect(drawRect, paintGrid);

        drawRectF = new RectF((int)left + xGrid + (int)cell / 4,
                               (int)top + yGrid + (int)cell / 4,
                              (int)left + xGrid + (int)cell - (int)cell / 4,
                               (int)top + yGrid + (int)cell - (int)cell / 4);

        if (positionFound)
        {
            paintGrid.setColor(Color.argb(200, 240, 64, 40));
        }
        else
        {
            paintGrid.setColor(Color.argb(200, 129, 43, 146));
        }

        paintGrid.setStyle(Paint.Style.STROKE);
        canvas.drawRoundRect(drawRectF, 5, 5, paintGrid);*/

        // pionese (pin)
        canvas.drawBitmap(needleWhite48,
                        (left + xGrid + (int)cell / 2) - needleWhite48.getWidth() / 2,
                         (top + yGrid + (int)cell / 2) - needleWhite48.getHeight(), null);

        // em caso de estar a efectuar calculos
        if (averageProcessDialog.isShowing())
        {
            // Aviso
            Positioning.setNote("Please wait... Don't move... Doing extreme calculations...");
        }
        else
        {
            // Posição na matriz
            Positioning.setNote("Col - " + (xGrid / (int)cell + 1) + " : Row - " + (yGrid / (int)cell + 1));
        }
    }

    //-----------------------------------------------------------------------------------------

    // Gestão do radiomap
    public void manageRadioMap(Canvas canvas, float left, float top)
    {
        Paint paint = new Paint();
        paint.setTextSize(25);
        paint.setColor(0xFF0000FF);
        paint.setTextSize(16);

        Bitmap button1 = BitmapFactory.decodeResource(getResources(), R.drawable.media_record);
        Bitmap button2 = BitmapFactory.decodeResource(getResources(), R.drawable.media_record);

        switch(buttonState1)
        {
            case state_pressed:

                //button1.recycle();
                //button1 = BitmapFactory.decodeResource(getResources(), R.drawable.media_record);

                //break;

            case state_normal:

                //button1.recycle();
                //button1 = BitmapFactory.decodeResource(getResources(), R.drawable.media_record);

                break;

            case state_enabled:

                //button1.recycle();
                //button1 = BitmapFactory.decodeResource(getResources(), R.drawable.up2);

                break;

            case state_disabled:

                //button1.recycle();
                //button1 = BitmapFactory.decodeResource(getResources(), R.drawable.up2);

                break;
        }

        Rect buttonRect = new Rect(sw/2 - button1.getWidth()/2, (sh/2 - sh/4) - button1.getHeight()/2,
                                   sw/2 + button1.getWidth()/2, (sh/2 - sh/4) + button1.getHeight()/2);
        region1 = new Region(buttonRect);
        canvas.drawBitmap(button1, null, buttonRect, paint);

        switch(buttonState2)
        {
            case state_pressed:

                //button2.recycle();
                //button2 = BitmapFactory.decodeResource(getResources(), R.drawable.media_record);

                break;

            case state_normal:

                //button2.recycle();
                //button2 = BitmapFactory.decodeResource(getResources(), R.drawable.media_record);

                break;

            case state_enabled:

                //button2.recycle();
                //button2 = BitmapFactory.decodeResource(getResources(), R.drawable.down2);

                break;

            case state_disabled:

                //button2.recycle();
                //button2 = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.back_pressed);

                break;
        }

        Rect buttonRect1 = new Rect(sw/2 - button1.getWidth()/2, (sh/2 + sh/4) - button1.getHeight()/2,
                                    sw/2 + button1.getWidth()/2, (sh/2 + sh/4) + button1.getHeight()/2);
        region2 = new Region(buttonRect1);
        canvas.drawBitmap(button2, null, buttonRect1, paint);

        canvas.drawBitmap(updown, sw/2 - updown.getWidth()/2, sh/2 - updown.getHeight()/2, null);
    }

    //------------------------------------------------------------------------------------------

    // mostra as localizações dos pontos de acesso e as suas distâncias
    public void showDistanceNode(Canvas canvas, float left, float top)
    {
        LinkedList process = new LinkedList();

        Paint paintCircle = new Paint(paintFan);
        paintCircle.setColor(Color.argb(50, 0, 50, 175));// azul claro

        Paint paintBox = new Paint(paintFan);
        paintBox.setColor(Color.argb(100, 255, 255, 255));

        DashPathEffect effect = new DashPathEffect(new float[] {7,7}, distanceEffectPhase);
        paintLineDistance.setPathEffect(effect);

        // coloca as circunferencias de percentagem de dBm
        for (int i = 0; i < access_points.size(); i++)
        {
            AccessPoint ac = (AccessPoint) access_points.get(i);

            if (ac.distance_x() + ac.distance_y() + ac.distance_z() > 0)
            {
                float position[] = Map.toMapScale(ac.distance_x(), ac.distance_y());

                if (ac.isActive())
                {
                    process.add(i);

                    float range = Map.toMapScale(Positioning.getLaterationDistance(ac.getMode()));

                    canvas.drawCircle(left + position[0], top + position[1], range, paintCircle);

                    paintText.setColor(Color.GREEN);
                }
                else
                {
                    paintText.setColor(Color.YELLOW);
                }

                // se não estiver a rodar, mostra os SSID nos pontos laterais do ecrã em caso de invisibilidade
                if (headingMap == 0.0f)
                {
                    //comprimento em canvas da string
                    float length = paintText.measureText(ac.ssid(), 0, ac.ssid().length()) + 1;

                    float posX = left + position[0]; //posição do accelerometer
                    float posY = top + position[1];  //posição do y

                    float straightX = 0; //x da equação da recta
                    float straightY = 0; //y da equação da recta

                    // se a posição do AP sair do ecrã coloca o SSID da sua direcção na borda do ecrã
                    if (left + position[0] < 0 || left + position[0] > sw)
                    {
                        // esquerda
                        if (left + position[0] < 0)
                        {
                               length = 0;
                                 posX = 1;
                            straightX = 0;
                        }
                        else// direita
                        {
                                 posX = sw - length;
                            straightX = sw;
                        }

                        posY = Util.slopeStraightX(left + position[0], top + position[1], sw/2, sh/2, straightX);

                        if (posY < paintText.getTextSize()) posY = paintText.getTextSize();
                        if (posY > sh - 4) posY = sh - 4;
                    }
                    else if (top + position[1] < paintText.getTextSize() || top + position[1] > sh)
                    {
                        straightY = top + position[1];

                        // em cima
                        if (top + position[1] < paintText.getTextSize())
                        {
                            posY = paintText.getTextSize();
                        }
                        else// em baixo
                        {
                            posY = sh - 4;
                        }

                        posX = Util.slopeStraightY(left + position[0], top + position[1], sw/2, sh/2, straightY);

                        if (posX > sw - length) posX = sw - length;
                    }

                    // coloca o ssid
                    if (posX != left + position[0] || posY != top + position[1])
                        canvas.drawText(ac.ssid(), posX, posY, paintText);
                }
            }

            // traça linhas a todos os outros pontos de acesso
            if (ac.isActive())
            {
                paintText.setColor(Color.argb(200, 0, 0, 0));

                // obtem a distancia entre pontos, regista a sua distancia e coloca uma linha entre eles
                for (int k = 0; k < process.size(); k++)
                {
                    if (i == process.get(k).hashCode()) break;

                    AccessPoint ac2 = (AccessPoint) access_points.get(process.get(k).hashCode());

                    double distance = Util.distance( ac.distance_x(),  ac.distance_y(),  ac.distance_z(),
                                                    ac2.distance_x(), ac2.distance_y(), ac2.distance_z());

                    double actualHeight = Math.sqrt(
                            Math.pow(distance, 2) - Math.pow(ac.distance_z() - Lateration.HUMAN_HEIGHT, 2));

                    // converte à escala do mapa
                    float beginPoint[] = Map.toMapScale( ac.distance_x(), ac.distance_y() );
                    float   endPoint[] = Map.toMapScale(ac2.distance_x(), ac2.distance_y());

                    String d = (int)actualHeight + "cm";
                    float w = paintText.measureText(d, 0, d.length());

                    // caixa
                    canvas.drawRect(left + beginPoint[0] + (endPoint[0] - beginPoint[0]) / 2 - (w / 2 + 3),
                                     top + beginPoint[1] + (endPoint[1] - beginPoint[1]) / 2 - 11,
                                    left + beginPoint[0] + (endPoint[0] - beginPoint[0]) / 2 + (w / 2 + 3),
                                     top + beginPoint[1] + (endPoint[1] - beginPoint[1]) / 2 + 4, paintBox);

                    // distancia
                    canvas.drawText(d, left + beginPoint[0] + (endPoint[0] - beginPoint[0]) / 2 - (w / 2 + 1),
                                        top + beginPoint[1] + (endPoint[1] - beginPoint[1]) / 2, paintText);

                    // linha
                    canvas.drawLine(left + beginPoint[0], top + beginPoint[1],
                                    left + endPoint[0],   top + endPoint[1], paintLineDistance);
                }
            }
        }

        ++distanceEffectPhase;
    }

    //------------------------------------------------------------------------------------------

    // mostra os nodos
    public void showMapLateration(Canvas canvas, float left, float top)
    {
        //Atenuação:
        //      = 20 log(d) + 20 log(f) + 92,44

        Paint paintRadius = new Paint(paintFan);

        if (!isPositioning)
        {
            // parte que mostra o mapa da trilateração/multilateração
            Vector laterationList = Positioning.getLaterationList();

            if (laterationList == null) return;

            paintRadius.setStyle(Style.FILL_AND_STROKE);
            paintText.setColor(Color.BLUE);

            for (int i = 0; i < laterationList.size(); i++)
            {
                Vector temp = (Vector) laterationList.elementAt(i);

                // transparencia. Quanto mais perto estivermos menor a transparencia
                float transparence = 150 * (Float) temp.elementAt(2);

                int r = 0;
                int g = 0;
                int b = 0;
//como considerar o verde - todos ou quase todos. por adiante para o amarelo e vermelho
                if ((Float) temp.elementAt(2) <= 1.0)    {r =  19; g = 166; b =  55;} // verde
                if ((Float) temp.elementAt(2) <= 0.7)    {r = 255; g = 200; b =   8;} // amarelo
                if ((Float) temp.elementAt(2) <= 0.4)    {r = 231; g =  47; b =  39;} // vermelho

                paintRadius.setARGB((int)transparence, r, g, b);

                canvas.drawCircle(left + (Integer) temp.elementAt(0) * (int)cell + ((int)cell / 2),
                                   top + (Integer) temp.elementAt(1) * (int)cell + ((int)cell / 2), cell / 2, paintRadius);

                String d = "" + (Float) temp.elementAt(2) + "0000";
                d = d.substring(0, 3);

                float w = paintText.measureText(d, 0, d.length());

                canvas.drawText(d, left + (Integer) temp.elementAt(0) * (int)cell + ((int)cell / 2) - (w / 2),
                                    top + (Integer) temp.elementAt(1) * (int)cell + ((int)cell / 2) + (paintText.getTextSize() / 2) - 1, paintText);
            }
        }
        else
        {
            float r;
            int tolerance = Positioning.getLaterationSensitivity();

            paintRadius.setStyle(Style.STROKE);
            paintRadius.setColor(Color.GRAY);

            // coloca a circunferencia em todos os pontos 2D
            for (int i = 0; i < access_points.size(); i++)
            {
                AccessPoint ac = (AccessPoint) access_points.get(i);

                if (ac.isValid())
                {
                    float[] position = Map.toMapScale(ac.distance_x(), ac.distance_y());

                    r = Map.toMapScale(Positioning.getLaterationDistance(ac.getMode()) + tolerance);
                    canvas.drawCircle(left + position[0], top + position[1], (int) r, paintRadius);
                }
            }
        }
    }

    //------------------------------------------------------------------------------------------

    // mostra o grafico de linhas do respectivo access point
    protected void showGraphView(Canvas canvas)
    {
        float height = 100;//getHeight();
        float border = 20;

        canvas.drawRect(10, 10, sw - 10, (int)height - border + 10, paintBackground);

        RectF drawRect = new RectF(10, 10, sw - 10, (int)height - border + 10);
        canvas.drawRoundRect(drawRect, 5, 5, paintBorder);

        Rect btnRect = new Rect(10, 10, sw - 10, (int)height - (int)border + 10);
        simpleTouchPoints = new Rect(btnRect);

        int good = 0, med = 0, bad = 0;
        int count = 0;

        // se estiver em rotação coloca uma linha representando o nivel da qualidade total do desvio padrão
        if (headingMap != 0.0f)
        {
            Paint p = new Paint(paintText);
            p.setTextAlign(Align.CENTER);
            p.setTextSize(12);
            
            double goodStdDev = Positioning.getGoodStdDev();
            double badStdDev = Positioning.getBadStdDev();

            for (int i = 0; i < access_points.size(); i++)
            {
                AccessPoint ac = (AccessPoint) access_points.get(i);

                if (ac.isValid())
                {
                    double standardDeviation = ac.getStdDev();
                    
                    if (standardDeviation     <  goodStdDev)
                        good++;
                    else if(standardDeviation >= goodStdDev && standardDeviation < badStdDev)
                        med++;
                    else if(standardDeviation >= bad)
                        bad++;
                    
                    count += 2;
                }
            }

            int div = sw/4;

            p.setColor(Color.RED);
            canvas.drawText("" + bad, div * 1, (int)height - border - 25, p);
            canvas.drawText("Bad",    div * 1, (int)height - border, p);

            p.setColor(Color.YELLOW);
            canvas.drawText("" + med, div * 2, (int)height - border - 25, p);
            canvas.drawText("Med",    div * 2, (int)height - border, p);

            p.setColor(Color.GREEN);
            canvas.drawText("" + good, div * 3, (int)height - border - 25, p);
            canvas.drawText("Good",    div * 3, (int)height - border, p);

            int value = (((med * 1) + (good * 2)) * 100) / count;
            float width = ((sw - 40) * value) / 100;
            
            div = (sw - 40) / 3;
            
            if     (width <= div * 1)   p.setColor(Color.RED);
            else if(width <= div * 2)   p.setColor(Color.YELLOW);
            else if(width <= div * 3)   p.setColor(Color.GREEN);

            canvas.drawRect(20, 20, 20 + width, 30, p);

            // sair
            return;
        }
        
        // no caso de não estar em rotação, apresenta o histograma desse ponto de acesso
        int[] values = null; //values = new int[WifiPositioning.SAMPLES_NODE_GRAPH]

        float max = 0;
	float min = 0;
	float diff = max - min;
        
        String ssid = "Select Access Point";
        String msg = "";

        if (graphViewSSID > -1)
        {
            AccessPoint ac = (AccessPoint) access_points.get(graphViewSSID);
            ssid = ac.ssid();

            if (ac.isActive())
            {
                values = ac.getList();

                // intervalo de confiança
                double[] ic = ac.getConfidenceInterval();
                msg = "IC [ " + (int)ic[0] + " ; " + (int)ic[1] + " ]";
                
                max = Util.getMax(values);
                min = Util.getMin(values);
	        diff = max - min;
                
                if (min == max) msg = "Static dBm level";
            }
            else
            {
                msg = "Lost Signal";
            }
        }
        
        int verlines = 6;
        int horlines = WifiPositioning.SAMPLES_NODE_GRAPH - 1;

        Paint p = new Paint();
        p.setTextAlign(Align.CENTER);
        p.setColor(Color.DKGRAY);

	float horstart = border * 2;
	float width = getWidth() - horstart;
        
	float graphheight = height - (2 * border);
	float graphwidth  = width  - (2 * border);
        
        for (int i = 0; i < verlines + 1; i++)
        {
            float pos_y = ((graphheight / verlines) * i) + border;
            canvas.drawLine(horstart, pos_y, width, pos_y, p);
	}
	
        for (int i = 0; i < horlines + 1; i++)
        {
            float pos_x = ((graphwidth / horlines) * i) + horstart;
            canvas.drawLine(pos_x, height - border, pos_x, border, p);
	}

        p.setColor(Color.GREEN);
	canvas.drawText("" + (int)max, 25, border + 4, p);

        p.setColor(Color.YELLOW);
	canvas.drawText("" + (int)min, 25, height - border + 4, p);

        p.setColor(Color.WHITE);
        canvas.drawText(msg, (getWidth() / 2), (height / 2), p);

        if (values != null)
        {
            float datalength = values.length;
            float colwidth = (width - (2 * border)) / datalength;
            float halfcol = colwidth / 2;
            float lasth = 0;

            p.setColor(Color.RED);

            for (int i = 0; i < values.length; i++)
            {
                float val = values[i] - min;
                float rat = val / diff;
                float h = graphheight * rat;

                if (i > 0)
                    canvas.drawLine(((i - 1) * colwidth) + (horstart + 1) + halfcol, (border - lasth) + graphheight,
                            (i * colwidth) + (horstart + 1) + halfcol, (border - h) + graphheight, p);

                lasth = h;
            }
        }

        p.setColor(Color.argb(255, 136, 152, 206));
	canvas.drawText(ssid, (getWidth() / 2) + (getWidth() / 4), border + 6, p);
    }
    
    //------------------------------------------------------------------------------------------
    
    // pesquisa novos pontos de acesso
    public void wifiDiscovery()
    {
        Positioning.startAcceptBSSID();

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        builder.setMessage("Stop searching?").setCancelable(false)
            .setNeutralButton(R.string.accept, new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int id)
                {
                    Positioning.stopAcceptBSSID();
                }
            });

        AlertDialog alert = builder.create();
        alert.setCancelable(false);
        alert.show();
    }
    
    //------------------------------------------------------------------------------------------
      
    // trata o mapa
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event)
    {
        final int MOVE = (int)cell / 4;
        
        if (!Positioning.isLoaded()) return super.onKeyUp(keyCode, event);
        //boolean isLoaded = Positioning.isLoaded();

        switch (keyCode)
        {
            // para cima
            case KeyEvent.KEYCODE_DPAD_UP: 
            {
                if (!isRadioMap /* && isLoaded */) y += MOVE;
                else
                {
                    if (yGrid > (int)cell - 1) yGrid -= (int)cell;
                    if (mapDimension[1] > 480 && y > (int)cell) y -= (int)cell;
                }

                break;
            }
            // para a esquerda
            case KeyEvent.KEYCODE_DPAD_LEFT:
            {
                if (!isRadioMap /* && isLoaded */) x += MOVE;
                else
                {
                    if (xGrid > (int)cell - 1) xGrid -= (int)cell; // para mapa pequeno
                    if (mapDimension[0] > 320 && x > (int)cell) x -= (int)cell; // para mapa grande
                }

                break;
            }
            // para a direita
            case KeyEvent.KEYCODE_DPAD_RIGHT: 
            {
                if (!isRadioMap /* && isLoaded */) x -= MOVE;
                else
                {
                    if (xGrid < mapDimension[0] - (int)cell - 1) xGrid += (int)cell;
                    if (mapDimension[0] > 320 && x < matrixDimension[0] * (int)cell - (int)cell) x += (int)cell;
                }

                break;
            }
            // para baixo
            case KeyEvent.KEYCODE_DPAD_DOWN:
            {
                if (!isRadioMap /* && isLoaded */) y -= MOVE;
                else
                {
                    if (yGrid < mapDimension[1] - (int)cell - 1) yGrid += (int)cell;
                    if (mapDimension[1] > 480 && y < matrixDimension[1] * (int)cell - (int)cell) y += (int)cell;
                }

                break;
            }
            
            case KeyEvent.KEYCODE_DPAD_CENTER:
            {
                if (isRadioMap && !dpadProcess)
                {
                    dpad = true;
                }

                break;
            }
        }

        return super.onKeyUp(keyCode, event);
    }
    
    //------------------------------------------------------------------------------------------

    // trata o toque do dedo
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        int touchX = (int)event.getX();
        int touchY = (int)event.getY();
           
        int action = event.getAction() & MotionEvent.ACTION_MASK;
        
        // se estiver no menu do radiomap, não deixa movimentar o mapa. Sai fora
        if (isRadioMap) //return true;
        {
            if (Positioning.isManageRadioMap())
            {
                switch(action)  
                {  
                    case MotionEvent.ACTION_DOWN: 
                    {  
                        if(region1.contains(touchX, touchY) == true)  
                        {  
                            buttonState1 = state_pressed;  
                        }  
                        else if(region2.contains(touchX, touchY) == true )
                        {  
                            buttonState1 = state_disabled;  
                            buttonState2 = state_pressed;  
                        }  

                        invalidate();  

                        break;  
                    }  
                    case MotionEvent.ACTION_UP: 
                    {  
                        if(region1.contains(touchX, touchY)== true)  
                        {  
                            buttonState1 = state_normal;  
                        }  
                        else if(region2.contains(touchX, touchY) == true)
                        {  
                            buttonState1 = state_normal;  
                            buttonState2 = state_normal;  
                        }  

                        invalidate();  

                        break;  
                    }  
                }  
            }
            
            return true; 
        }
        
        // se estiver em posicionamento é necessário distinguir um toque curto de um toque longo
        if (Positioning.isPositioning())
        {
            if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE)
            {
                //Start timer
                if (!touchProcessThread)
                {
                    isLongTouch = false;
                    touchProcessThread = true;

                    touchThread = new TouchThread();
                    touchThread.setEvent(event);

                    touchThread.start();
                }
                else
                {
                    touchThread.setEvent(event);
                }
            }
            else
            {
                touchProcessThread = false;

                touchDown(touchX, touchY);

                invalidate();
            }
        }
        // se não estiver em posicionamento, permite fazer o scroll do mapa
        else
        {
            switch (action)
            {
                case MotionEvent.ACTION_DOWN:

                    // Remember where we started
                    lastMotionX = touchX;
                    lastMotionY = touchY;

                    touchDown(touchX, touchY);

                    invalidate();

                    break;

                case MotionEvent.ACTION_POINTER_DOWN:
                        
                    isMultiTouch = true;
                                
                    setPoints(event);
                    invalidate();
                                                                
                    break;
                    
                case MotionEvent.ACTION_POINTER_UP:
             
                    isMultiTouch = false;
                    multiTouchPoints.clear();
                                
                    break;
                    
                case MotionEvent.ACTION_MOVE:

                    if(isMultiTouch)
                    {
                        setPoints(event);
                    }
                    else
                    {
                        float pressure = event.getPressure();

                        //High pressure
                        if (pressure > 0.25)
                        {
                            // Calculate the distance moved
                            final float distanceX = touchX - lastMotionX;
                            final float distanceY = touchY - lastMotionY;

                            // Move the object
                            x += -distanceX;
                            y += -distanceY;
                            
//separa entre um mapa existente e um novo
                       
                            if (levelView == GRAPHIC_VIEW && graphViewSSID > -1)// && !touchRectdBm[graphViewSSID].isEmpty())
                            {
                                if (!touchRectdBm[graphViewSSID].isEmpty())
                                {
                                    int   left = (int) (touchRectdBm[graphViewSSID].left   + distanceX);
                                    int    top = (int) (touchRectdBm[graphViewSSID].top    + distanceY);
                                    int  right = (int) (touchRectdBm[graphViewSSID].right  + distanceX);
                                    int bottom = (int) (touchRectdBm[graphViewSSID].bottom + distanceY);

                                    // se o toque foi numa caixa, move-se ele proprio 
                                    if (touchRectdBm[graphViewSSID].contains(touchX, touchY))
                                    {
                                        // Reset the object
                                        x -= -distanceX;
                                        y -= -distanceY;    

                                        // só esta posição se move
                                        touchRectdBm[graphViewSSID].left   = left;
                                        touchRectdBm[graphViewSSID].top    = top;
                                        touchRectdBm[graphViewSSID].right  = right;
                                        touchRectdBm[graphViewSSID].bottom = bottom;

                                        touchRectdBm[graphViewSSID].offsetTo(left, top);
                                    }
                                    else
                                    {
                                        // caso contrario (toque no ecra), todos se movem juntamente com o mapa
                                        moveAllTouchRectdBm(distanceX, distanceY);    
                                    }
                                }
                            }// se ainda não criou o vetor de toutchs, sai
                            else //if (touchFlag)
                            {
                                // no caso de não estar no graph e o mapa for movimentado, 
                                // então as caixas tb devem acompanhar o mapa
                                moveAllTouchRectdBm(distanceX, distanceY);
                            }
                        
                            // Remember this touch position for the next move event
                            lastMotionX = touchX;
                            lastMotionY = touchY;
                        }
                    }

                    invalidate();

                    break;
            }
        }

        return true;
    }
    
    // ################################################################################
    
    private void moveAllTouchRectdBm(float distanceX, float distanceY) 
    {        
        for (int i = 0; i < touchRectdBm.length; i++)
        {
            if (touchRectdBm[i] != null)
            {
                if (!touchRectdBm[i].isEmpty())
                {
                    touchRectdBm[i].left   += distanceX;
                    touchRectdBm[i].top    += distanceY;
                    touchRectdBm[i].right  += distanceX;
                    touchRectdBm[i].bottom += distanceY;

                    touchRectdBm[i].offsetTo(touchRectdBm[i].left, touchRectdBm[i].top);
                }
            }
        }
    }
    
    //------------------------------------------------------------------------------------------
    
    public void setPoints(MotionEvent event)
    {
        multiTouchPoints.clear();
                
        int pointerIndex = 0;
                
        for(int i = 0; i < event.getPointerCount(); ++i)
        {
            pointerIndex = event.getPointerId(i);
                        
            multiTouchPoints.add(new PointF(event.getX(pointerIndex), event.getY(pointerIndex)));
        }
    }
    
    //------------------------------------------------------------------------------------------

    // devolve o ponto medio da distância entre os dois dedos (touch)
    private PointF getMidPoint(float x1, float y1, float x2, float y2) 
    {
        PointF point = new PointF();
                
        float pointX = x1 + x2;
        float pointY = y1 + y2;
                
        point.set(pointX / 2, pointY / 2);
                
        return point;
    }
    
    //------------------------------------------------------------------------------------------

    // procede conforme o local do toque
    private void touchDown(int xx, int yy)
    {
        // numero de access points activos e registados
        int totalMapRSSI = totalMapRSSI();
        
        // verifica se o toque foi no painel de visualização
        if (simpleTouchPoints.contains(xx,yy))
        {
            // toque na lista de access points vazia. Activa uma pesquisa
            if (access_points.isEmpty())
            {
                wifiDiscovery();
            }
            else
            {
                // roda os vários tipos de visualização de access points
                if (Positioning.getLevelView() < 6)
                {
                    Positioning.setLevelView(++levelView);

                    // caso não tenha o numero minimo de APs, avança para o proximo view
                    if ((levelView == DISTANCE_VIEW || Positioning.getLevelView() == TRILATERACTION_VIEW) &&
                            totalMapRSSI < Positioning.getMinAccessPointVisible())
                        touchDown(xx, yy);// avança como um clique

                    if (levelView == GRAPHIC_VIEW && totalMapRSSI == 0)
                        touchDown(xx, yy);
                    
                }
                else
                {
                    Positioning.setLevelView(0);
                }
            }
	}
        // verifica se o toque foi nos rectangulos correspondentes aos pontos de acesso
        else if (totalMapRSSI > 0 && Positioning.isViewWifiList() && levelView == GRAPHIC_VIEW)
        {
            for (int i = 0; i < touchRectdBm.length; i++)
            {
                if (touchRectdBm[i].contains(xx,yy))
                {
                    graphViewSSID = i;
                    
                    break;
                }
            }
        }

        // acrescentar o speak (um toque)
    }

    //------------------------------------------------------------------------------------------
    
    // conta o tempo do toque para verificar se é um toque longo
    private class TouchThread extends Thread
    {
        private MotionEvent event = null;
        private long startTime = System.currentTimeMillis();
        
        public void setEvent(MotionEvent e)
        {
            event = e;
        }

        @Override
        public void run()
        {
            while((event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) &&
                    touchProcessThread == true)
            {
                long time = System.currentTimeMillis() - startTime;

                if (time > 3000) Positioning.setNote("" + Math.round(time/1000) + " sec");

                if (time > HELP_TIME)
                {
                    try
                    {
                        float[] match = Positioning.matchedPoint();

                        SmsManager sms = SmsManager.getDefault();
                        sms.sendTextMessage(Positioning.getSMSPanicNumber(), null,
                                "Need help in coordinated: Col - " + ((int)match[0] + 1) +
                                                       " : Row - " + ((int)match[1] + 1), null, null);

                        Positioning.setNote("Panic message sent! Do not move until someone.");

                        isLongTouch = true;
                    } catch (Exception e) {}

                    break;
                }
            }
        }
    }
    
    //------------------------------------------------------------------------------------------

    // devolve o numero de access points activos
    public int countActive()
    {
        int n = 0;

        for (int i=0; i < access_points.size(); i++)
        {
            AccessPoint ac = (AccessPoint) access_points.get(i);

            if (ac.isActive()) n++;
        }

        return n;
    }
    
    //------------------------------------------------------------------------------------------

    // devolve o numero de access points activos no mapa (com posição, accelerometer+y+z > 0)
    public static int totalMapRSSI()
    {
        int n = 0;

        for (int i = 0; i < access_points.size(); i++)
        {
            AccessPoint ac = (AccessPoint) access_points.get(i);

            if (ac.isValid()) n++;
        }

        return n;
    }
    
    //------------------------------------------------------------------------------------------
    
    // prepara a captura do radiomap
    public void showProceedRadioMap()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());

        builder.setPositiveButton("Proceed with " + visited.size() + " access points?", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {
                avgTotalProcessDialog = Positioning.getSamplesAverage() * visited.size();

                averageProcessDialog.setMax(avgTotalProcessDialog);
                averageProcessDialog.setProgress(0);
                averageProcessDialog.show();

                avgDpad t = new avgDpad(xGrid / (int)cell, yGrid / (int)cell);
                t.start();
            }
        })
        .setNegativeButton("Not Proceed", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id) 
            {
                dpadProcess = false;
            }
        });

        AlertDialog alert = builder.create();
        alert.setCancelable(false);
        alert.show();
    }
    
    //------------------------------------------------------------------------------------------

    // Thread para libertar o cursor Radiomap ---------------------------------------------
    public static class avgDpad extends Thread
    {
        private int col;
        private int row;

        public avgDpad(int col, int row)
        {
            this.col = col;
            this.row = row;
        }

        @Override
        @SuppressWarnings("SleepWhileInLoop")
        public void run()
        {
            int lastProgress = 0;
            
            visitedPosition = new int[visited.size()];
            
            for (int i = 0; i < visited.size(); i++)
            {
                avgAccessPoint t = new avgAccessPoint(i, visited.get(i).hashCode());
                t.start();
            }

            // enquanto não terminar a captura ou não tenha sido cancelado
            while (averageProcessDialog.getProgress() < avgTotalProcessDialog && dpadProcess)
            {
                try
                {
                    Thread.sleep(300);
                } catch (Exception e) {}

                int progressNow = 0;
                        
                for (int i = 0; i < visitedPosition.length; i++)
                    progressNow += visitedPosition[i];

                averageProcessDialog.incrementProgressBy((progressNow - lastProgress));

                lastProgress = progressNow;
            }

            // caso não tenha sido cancelado, processa os novos resultados
            if (dpadProcess)
            {
                // armazena no radiomap os dados capturados
                for (int i = 0; i < visited.size(); i++)
                {
                    AccessPoint ac = (AccessPoint) access_points.get(visited.get(i).hashCode());

                    int pos = Positioning.findRadiomapBSSID(ac.bssid());

                    if (pos >= 0)
                    {
                        Positioning.updateRadiomapBSSID(pos, col, row, ac.getLastCalcAvg(), 0);
                    }
                    else
                    {
                        Positioning.addRadiomapBSSID(ac.bssid(), col, row, ac.getLastCalcAvg());
                    }
                }

                averageProcessDialog.setProgress(0);
                averageProcessDialog.dismiss();

                dpadProcess = false;
            }
        }
    }
    
    //------------------------------------------------------------------------------------------

    // Thread individual para obter a média de cada access point para o Radiomap
    public static class avgAccessPoint extends Thread
    {
        private int id;
        private int position;

        public avgAccessPoint(int position, int accessPointNumber)
        {
            this.position = position;
            this.id = accessPointNumber;
        }

        @Override
        @SuppressWarnings("SleepWhileInLoop")
        public void run()
        {
            int dialogProcess = 0;
            int oldDialogProcess = 0;
            
            visitedPosition[position] = 0;

            AccessPoint ac = (AccessPoint) access_points.get(id);
            ac.startCalcAvg();

            while (ac.getAvgDialogProcess() < Positioning.getSamplesAverage() && dpadProcess)
            {
                try
                {
                    Thread.sleep(400);
                } catch (Exception e){}

                dialogProcess = ac.getAvgDialogProcess();
                visitedPosition[position] += dialogProcess - oldDialogProcess;
                oldDialogProcess = dialogProcess;
            }
        }
    }
}

    
    