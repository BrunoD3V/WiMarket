
package d3v.bnb.ourwimarket;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.lang.reflect.Method;

public class WiMarket extends Activity
{
    public static final String WIMARKET_USER_DETAILS = "WiMarketUserDetails";
    
    private static final int DIALOG_SD_CARD_ID = 0;
    private static final int DIALOG_TABLET_ID = 1;
    
    private Thread splashThread;
    
    protected boolean active = true;
    protected int splashTime = 8000;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        //remove o título da janela
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        //define a janela inteira
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //define o layout a ser utilizado
        setContentView(R.layout.wimarket);
        
        try
        {
            // verifica a existencia de um cartão de memoria
            if(!createDirectory(getString(R.string.app_name)))//if (!new File("/sdcard/").canWrite())
            {
                showDialog(DIALOG_SD_CARD_ID);  
            }
            else 
            {
                // verifica se é um Tablet
                if (isTabletDevice())
                {
                    showDialog(DIALOG_TABLET_ID);  
                }
                else
                {
                    // user editor
                    SharedPreferences userDetails = getSharedPreferences(WIMARKET_USER_DETAILS, MODE_PRIVATE);

                    // eliminar esta preferencia (para testes de arranque) 
                    SharedPreferences.Editor editor = userDetails.edit();
                        editor.remove("isFirstRun");
                        editor.commit(); 
                    //----------------------------------------------------
                    
                    if (userDetails.getBoolean("isFirstRun", true)) //true
                    {
                        // welcome message
                        Toast toast = Toast.makeText(getApplicationContext(), getString(R.string.welcome_message), Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        
                        LinearLayout toastView = (LinearLayout) toast.getView();
                        
                        ImageView imageCodeProject = new ImageView(getApplicationContext());
                        imageCodeProject.setImageResource(R.drawable.icon48);
                        
                        toastView.addView(imageCodeProject, 0);
                        toast.show();
                        
                        editor.putBoolean("isFirstRun", false); // false
                        editor.commit();
                    }

                    init();
                }
            }
        } catch (Exception e) {} 
    }

    @Override
    public void onDestroy()
    {
        active = false;
        
        super.onDestroy();
    }
    
    // Splash screen
    public void init()
    {
        // The thread to wait for splash screen events
        splashThread = new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    synchronized(this)
                    {
                        wait(splashTime);
                    }
                } catch(InterruptedException ex) { }

                try
                {
                    //dialog.dismiss();
                    
                    if (active)
                    {
                        startActivity(new Intent(WiMarket.this, HomeActivity.class));
                    }
                } catch(Exception e) { }
            }
        };

        splashThread.start();
    }
    
    public static boolean createDirectory(String path) 
    {
        boolean result = true;

        File file = new File(Environment.getExternalStorageDirectory(), path);
    
        if (!file.exists()) 
        {
            if (!file.mkdirs()) 
            {
                result = false;
            }
        }
    
        return result;
    }

    @Override
    protected Dialog onCreateDialog(int id) 
    {
        final Dialog dialog;
            
        ImageView img;
        TextView text;
        Button button;
        
        dialog = new Dialog(this);  
        dialog.setTitle("Error dialog box");
        dialog.setContentView(R.layout.customdialog);
        dialog.setCancelable(false);
        
        img = (ImageView) dialog.findViewById(R.id.ImageView);
        text = (TextView) dialog.findViewById(R.id.TextView);
        
        button = (Button) dialog.findViewById(R.id.Button);
        
        switch(id) 
        {
            case DIALOG_SD_CARD_ID:
                
                img.setImageResource(R.drawable.memory);
                text.setText(R.string.sd_card_error);
                
                button.setOnClickListener(new OnClickListener() 
                {
                    @Override
                    public void onClick(View v) 
                    {
                        dialog.dismiss();

                        finish();
                    }
                });
                
                break;
                
            case DIALOG_TABLET_ID:
                
                img.setImageResource(R.drawable.tablet);
                text.setText(R.string.tablet_error);
                
                button.setOnClickListener(new OnClickListener() 
                {
                    @Override
                    public void onClick(View v) 
                    {
                        dialog.dismiss();

                        finish();
                    }
                });
                
                break;
                
            default:
                
	        return null;
        }
        
        return dialog;
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        if (event.getAction() == MotionEvent.ACTION_DOWN)
        {
            try
            {
                if (splashThread.isAlive())
                {
                    synchronized(splashThread)
                    {
                        splashThread.notifyAll();
                    }
                }
                else
                {
                    //dialog.dismiss();
                    
                    startActivity(new Intent(WiMarket.this, HomeActivity.class));
                }
            } catch (Exception e) {}
        }
        
        return true;
    }
    
    // verifica se é um Tablet
    private boolean isTabletDevice() 
    {
        if (android.os.Build.VERSION.SDK_INT >= 11) // honeycomb
        { 
            // test screen size, use reflection because isLayoutSizeAtLeast is only available since 11
            Configuration con = getResources().getConfiguration();
            
            try 
            {
                Method mIsLayoutSizeAtLeast = con.getClass().getMethod("isLayoutSizeAtLeast", int.class);
                Boolean r = (Boolean) mIsLayoutSizeAtLeast.invoke(con, 0x00000004); // Configuration.SCREENLAYOUT_SIZE_XLARGE
                
                return r;
            } 
            catch (Exception x) 
            {                
                return false;
            }
        }
        
        return false;
    }
}
