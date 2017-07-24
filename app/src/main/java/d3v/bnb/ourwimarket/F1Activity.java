
package d3v.bnb.ourwimarket;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.Gallery.LayoutParams;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.ViewSwitcher;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;

public class F1Activity extends DashboardActivity implements ViewSwitcher.ViewFactory
{
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
	
        //requestWindowFeature(Window.FEATURE_NO_TITLE);

	setContentView(R.layout.f1_activity);
        
        
        
        
        
        /*Bitmap originalImage = BitmapFactory.decodeResource(getResources(),
                R.drawable.floorplan_pressed);

//Create an Image view and add our bitmap with reflection to it
       ImageView imageView = new ImageView(this);
       imageView.setImageBitmap(getRefelection(originalImage));
    
       //Add the image to a linear layout and display it
       LinearLayout linLayout = new LinearLayout(this);
       linLayout.addView(imageView,   new LinearLayout.LayoutParams(   LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT                  )            );
          
        // set LinearLayout as ContentView
        setContentView(linLayout);*/
        
        
        
        

	mSwitcher = (ImageSwitcher) findViewById(R.id.switcher);
	mSwitcher.setFactory(this);
	mSwitcher.setInAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
	mSwitcher.setOutAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out));
	mSwitcher.setClickable(true);

	Gallery g = (Gallery) findViewById(R.id.gallery);
	g.setAdapter(new ImageAdapter(this));
	g.setOnItemSelectedListener(new OnItemSelectedListener() 
        {
            @Override
            public void onItemSelected(AdapterView parent, View v, final int position, long id) 
            {
                try 
                {
                    // Load image from ImageView in Gallery to the big ImageSwitcher
                    Gallery g2 = (Gallery) findViewById(R.id.gallery);
                    
                    ImageView cachedView = (ImageView) ((ImageAdapter) g2.getAdapter()).getView(position, null, null);

                    if (cachedView != null) 
                    {
                        Drawable image = cachedView.getDrawable();
						
                        if (image != null) 
                        {
                            mSwitcher.setImageDrawable(image);
                            
                            
			
                            return;
			}
                    }
		} catch (Exception ex) { }
            }

            @Override
            public void onNothingSelected(AdapterView arg0) { }
	});

	final Button btnSetWallpaper = (Button) findViewById(R.id.btnsetwallpaper);
	btnSetWallpaper.setOnClickListener(new OnClickListener() 
        {
            @Override
            public void onClick(View arg0) 
            {
                // Save dialog
		AlertDialog.Builder builder = new AlertDialog.Builder(arg0.getContext());
				
                builder.setMessage("Do you want to save this image?")
                    .setCancelable(false).setPositiveButton("Yes", wallpaperDialogHandle).setNegativeButton("Cancel", wallpaperDialogHandle)
                    .setNeutralButton("No", wallpaperDialogHandle);
                    
                AlertDialog alert = builder.create();
                alert.show();
            }
	});
    }

    public DialogInterface.OnClickListener wallpaperDialogHandle = new DialogInterface.OnClickListener() 
    {
        @Override
	public void onClick(DialogInterface dialog, int which) 
        {
            switch (which)
            {
                case DialogInterface.BUTTON_POSITIVE: // Yes
		
                case DialogInterface.BUTTON_NEUTRAL: // No
		
                    WindowManager mWinMgr = (WindowManager) getBaseContext().getSystemService(Context.WINDOW_SERVICE);
				
                    int displayWidth = mWinMgr.getDefaultDisplay().getWidth();
                    int displayHeight = mWinMgr.getDefaultDisplay().getHeight();

                    Bitmap newwallpaper = Bitmap.createBitmap(displayWidth, displayHeight, Config.ARGB_8888);
                    
                    Canvas myCanvas = new Canvas(newwallpaper);
		
                    Gallery g = (Gallery) findViewById(R.id.gallery);
		
                    // Draw the image to make sure the aspect ratio match
                    ((ImageView) g.getSelectedView()).getDrawable().draw(myCanvas);
				
                    try 
                    {
                        setWallpaper(newwallpaper);
                    } 
                    catch (IOException e) 
                    {
                        e.printStackTrace();
                    }
                                
                    // Save file
                    if (DialogInterface.BUTTON_POSITIVE == which) 
                    {
                        Date date = new Date();
			
                        java.text.DateFormat dateFormat = new java.text.SimpleDateFormat("yyyyMMddhhmmss");
					
                        String dateTimeString = dateFormat.format(date);
			
                        try
                        {
                            FileOutputStream fos = new FileOutputStream(new File("/sdcard/" + dateTimeString + ".jpg"));
			
                            /*
                            FileOutputStream fos = openFileOutput("/sdcard/" + dateTimeString + ".jpg", MODE_WORLD_READABLE);*/

                            newwallpaper.compress(CompressFormat.JPEG, 75, fos);

                            fos.flush();
                            fos.close();
			}
                        catch (Exception e)
                        {
                            Log.e("MyLog", e.toString());
			}
                    }
		
                default: // Cancel
		
                    break;
            }
        }
    };
	

    public View makeView() 
    {
	ImageView i = new ImageView(this);
	
        i.setBackgroundColor(0xFF000000);
	i.setScaleType(ImageView.ScaleType.FIT_CENTER);
	i.setLayoutParams(new ImageSwitcher.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		
        return i;
    }

    private ImageSwitcher mSwitcher;

    public class ImageAdapter extends BaseAdapter 
    {
        public ImageAdapter(Context c)
        {
            mContext = c;
	}

	public int getCount()
        {
            return fileLinks.length;
	}

	public Object getItem(int position) 
        {
            return position;
	}

	public long getItemId(int position)
        {
            return position;
	}

	public View getView(final int position, View convertView, ViewGroup parent)
        {
            // Search if the cache already have the image,
            // this is better implemented as some kind of
            // comparison operator, but we don't have time
            // to look into that right now

            // Convert the queue to an array for easier iteration
            for (int i = 0; i < imageCache.size(); i++)
            {
                ImageView temp = imageCache.get(i);
		
                String imageTag = (String) temp.getTag();
		
                // Why string and not just position?
		// It will be easier to implement image loading
		// from the file system (images will be referred
		// to by path)
		if (imageTag.compareTo(fileLinks[position]) == 0)
                {
                    // Increase priority for the returned item
                    imageCache.remove(i);
                    imageCache.addFirst(temp);
		
                    return temp;
		}
            }

            // Load a new image if not cached
            while (imageCache.size() >= cacheThreshold)
            {
                imageCache.getLast().destroyDrawingCache();
		imageCache.removeLast();
            }
	
            final ImageView newView = new ImageView(mContext);
            newView.setAdjustViewBounds(true);
            newView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

            // Load the first image with blocking routine to make sure it will display
            if (firstImage) 
            {
                firstImage = false;
		
                F1BitmapInfo mResults = F1BitmapLoader.loadBitmap(fileLinks[position]);
				
                newView.setImageBitmap(mResults.getBitmap());
            }
            else
            {
                // Uses the asynchronous routine for other images
		Thread t = new Thread() 
                {
                    @Override
                    public void run() 
                    {
                        F1BitmapInfo mResults = F1BitmapLoader.loadBitmap(fileLinks[position]);
			mResults.setToAssign(newView);
			
                        Message downloadedMessage = new Message();
			
                        downloadedMessage.setTarget(mHandler);
			downloadedMessage.what = MESSAGE_TYPE_WALLPAPER_DOWNLOAD_COMPLETE;
			downloadedMessage.obj = mResults;
			
                        mHandler.sendMessage(downloadedMessage);
                    }
		};
	
                t.start();
            }

            newView.setTag(fileLinks[position]);
	
            // Add the changed entry back to the cache
            imageCache.addFirst(newView);

            return newView;
	}

        private Context mContext;
	private Handler mHandler = new Handler(new Callback() 
        {
            @Override
            public boolean handleMessage(Message msg) 
            {
                if (msg.what == MESSAGE_TYPE_WALLPAPER_DOWNLOAD_COMPLETE) 
                {
                    final F1BitmapInfo downloaded = (F1BitmapInfo) msg.obj;
		
                    // Fail? Try again, memory will be freed in a moment...
                    if (downloaded.getBitmap() == null)
                    {
                        Thread t = new Thread()
                        {
                            @Override
                            public void run()
                            {
                                F1BitmapInfo mResults = F1BitmapLoader.loadBitmap(downloaded.getIdentifier());
				mResults.setToAssign(downloaded.getToAssign());
				
                                Message downloadedMessage = new Message();
				
                                downloadedMessage.setTarget(mHandler);
				downloadedMessage.what = MESSAGE_TYPE_WALLPAPER_DOWNLOAD_COMPLETE;
				downloadedMessage.obj = mResults;
				
                                mHandler.sendMessage(downloadedMessage);
                            }
			};
			
                        t.start();
                    }
                    else
                        downloaded.getToAssign().setImageBitmap(downloaded.getBitmap());
		}
		
                return true;
            }
	});

	// Control first image's loading
	private boolean firstImage = true;
	private static final int MESSAGE_TYPE_WALLPAPER_DOWNLOAD_COMPLETE = 3;

	// 1 for view and 8 for scrolling
	private final int cacheThreshold = 8;
	private LinkedList<ImageView> imageCache = new LinkedList<ImageView>();
    }

    public static final String[] imageLinks = 
    {
        "http://g-android.com/3d_wallpaper_20090718_1938200673.jpg",
    };
    
    public static final String[] fileLinks = 
    {
        "apartamento.jpg", "esact.jpg", "pingo doce.jpg",
    };
    
    /*public Bitmap getRefelection(Bitmap image)
   {
     //The gap we want between the reflection and the original image
       final int reflectionGap = 4;
     
       //Get you bit map from drawable folder
       Bitmap originalImage = image ;
     
   
       int width = originalImage.getWidth();
       int height = originalImage.getHeight();
     
   
       //This will not scale but will flip on the Y axis
       Matrix matrix = new Matrix();
       matrix.preScale(1, -1);
     
       //Create a Bitmap with the flip matix applied to it.
       //We only want the bottom half of the image
       Bitmap reflectionImage = Bitmap.createBitmap(originalImage, 0, height/2, width, height/2, matrix, false);
     
         
       //Create a new bitmap with same width but taller to fit reflection
       Bitmap bitmapWithReflection = Bitmap.createBitmap(width
         , (height + height/2), Config.ARGB_8888);
   
      //Create a new Canvas with the bitmap that's big enough for
      //the image plus gap plus reflection
      Canvas canvas = new Canvas(bitmapWithReflection);
      //Draw in the original image
      canvas.drawBitmap(originalImage, 0, 0, null);
      //Draw in the gap
      Paint deafaultPaint = new Paint();
      canvas.drawRect(0, height, width, height + reflectionGap, deafaultPaint);
      //Draw in the reflection
      canvas.drawBitmap(reflectionImage,0, height + reflectionGap, null);
   
      //Create a shader that is a linear gradient that covers the reflection
      Paint paint = new Paint();
      LinearGradient shader = new LinearGradient(0, originalImage.getHeight(), 0,
        bitmapWithReflection.getHeight() + reflectionGap, 0x70ffffff, 0x00ffffff,
        TileMode.CLAMP);
      //Set the paint to use this shader (linear gradient)
      paint.setShader(shader);
      //Set the Transfer mode to be porter duff and destination in
      paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
      //Draw a rectangle using the paint with our linear gradient
      canvas.drawRect(0, height, width,
        bitmapWithReflection.getHeight() + reflectionGap, paint);
      return bitmapWithReflection;
   }*/
}