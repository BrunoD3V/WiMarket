
package d3v.bnb.ourwimarket;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.sql.SQLOutput;
import java.util.ArrayList;

/**
 * Loads images from SD card. 
 */
public class SDCardFloorplanActivity extends Activity implements OnItemClickListener 
{
    /**
     * Grid view holding the images.
     */
    private GridView sdcardImages;
    
    /**
     * Image adapter for the grid view.
     */
    private ImageAdapter imageAdapter;
    
    /**
     * Display used for getting the width of the screen. 
     */
    private Display display;

    /**
     * Creates the content view, sets up the grid, the adapter, and the click listener.
     * 
     * @see Activity#onCreate(Bundle)
     */

    protected final int END_PROGRESS = 9;
    protected final int ERROR_PROGRESS = 8;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Request progress bar
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.sdcard_floorplan_activity);

        display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

        setupViews();
        setProgressBarIndeterminateVisibility(true);
        loadImages();
    }

    /**
     * Free up bitmap related resources.
     */
    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        /*final GridView grid = sdcardImages;
        final int count = grid.getChildCount();

        View v = null;

        try
        {
            for (int i = 0; i < count; i++)
            {
                v = (ImageView) grid.getChildAt(i);
                v.destroyDrawingCache();
            }
        }
        catch (Exception e)
        {
            mHandler.sendEmptyMessage(ERROR_PROGRESS);
        }*/
    }

    private Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            if(msg.what == END_PROGRESS)
            {
                Toast.makeText(getApplicationContext(), "No floor plan found!", Toast.LENGTH_LONG).show();
                finish();
            }
            else if (msg.what == ERROR_PROGRESS)
            {
                Toast.makeText(getApplicationContext(), "Unreadable SDcard!", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    };

    /**
     * Setup the grid view.
     */
    private void setupViews()
    {
        sdcardImages = (GridView) findViewById(R.id.gridview);

        //sdcardImages.setNumColumns(display.getWidth() / 95); // faz o numero de colunas
        //sdcardImages.setNumColumns(4);

        sdcardImages.setClipToPadding(false);
        sdcardImages.setOnItemClickListener(SDCardFloorplanActivity.this);

        imageAdapter = new ImageAdapter(this);
        sdcardImages.setAdapter(imageAdapter);
    }

    /**
     * Load images.
     */
    private void loadImages()
    {
        final Object data = getLastNonConfigurationInstance();

        if (data == null)
        {
            new LoadImagesFromSDCard().execute();
        }
        else
        {
            final LoadedImage[] photos = (LoadedImage[]) data;

            if (photos.length == 0)
            {
                new LoadImagesFromSDCard().execute();
            }

            for (LoadedImage photo : photos)
            {
                addImage(photo);
            }
        }
    }

    /**
     * Add image(s) to the grid view adapter.
     *
     * @param value Array of LoadedImages references
     */
    private void addImage(LoadedImage... value)
    {
        for (LoadedImage image : value)
        {
            imageAdapter.addPhoto(image);
            imageAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Save bitmap images into a list and return that list.
     *
     * @see Activity#onRetainNonConfigurationInstance()
     */
    @Override
    public Object onRetainNonConfigurationInstance()
    {
        final GridView grid = sdcardImages;
        final int count = grid.getChildCount();
        final LoadedImage[] list = new LoadedImage[count];

        for (int i = 0; i < count; i++)
        {
            final ImageView v = (ImageView) grid.getChildAt(i);
            list[i] = new LoadedImage(((BitmapDrawable) v.getDrawable()).getBitmap());
        }

        return list;
    }

    /**
     * Async task for loading the images from the SD card.
     *
     * @author Mihai Fonoage
     *
     */
    class LoadImagesFromSDCard extends AsyncTask<Object, LoadedImage, Object>
    {
        /**
         * Load images from SD Card in the background, and display each image on the screen.
         *
         *
         */
        @Override
        protected Object doInBackground(Object... params)
        {
            setProgressBarIndeterminateVisibility(true);
            //getString(R.string.app_name))

            Bitmap bitmap = null;
            Bitmap newBitmap = null;
            Uri uri = null;

            try
            {
                File sdcard = new File(Environment.getExternalStorageDirectory().getPath());
                if ( !(sdcard.canRead() && sdcard.isDirectory()))
                {
                    System.out.println("If sd cant read e sd is directory");
                    mHandler.sendEmptyMessage(END_PROGRESS);
                }
                else
                {
                    //url.toString() return a String in the following format: "file:///mnt/sdcard/myPicture.jpg",
                    //url.getPath() returns a String in the following format: "/mnt/sdcard/myPicture.jpg",
                    // Set up an array of the Thumbnail Image ID column we want
                    String[] projection = {MediaStore.Images.Thumbnails._ID};

                    // Create the cursor pointing to the SDCard
                    Cursor cursor = managedQuery(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            projection,
                            MediaStore.Images.Thumbnails.DATA + " like ? ",
                            new String[] {"%" + getString(R.string.app_name) + "%"},
                            null);

                    int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Thumbnails._ID);

                    int size = 0;

                    for (int i = 0; i < cursor.getCount(); i++)
                    {
                        cursor.moveToPosition(i);
                        int imageID = cursor.getInt(columnIndex);

                        // obtain the image URI
                        //uri = Uri.withAppendedPath(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, "" + imageID);
                        uri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, Integer.toString(imageID));
                        String url = uri.toString();

                        // Set the content of the image based on the image URI
                        int originalImageId = Integer.parseInt(url.substring(url.lastIndexOf("/") + 1, url.length()));

                        //bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));

                        // There are two types of thumbnails available:
                        // MINI_KIND: 512 x 384 thumbnail
                        // MICRO_KIND: 96 x 96 thumbnail
                        bitmap = MediaStore.Images.Thumbnails.getThumbnail(getContentResolver(),
                                originalImageId,
                                MediaStore.Images.Thumbnails.MINI_KIND,
                                null);

                        if (bitmap != null)
                        {
                            int origWidth = bitmap.getWidth();
                            int origHeight = bitmap.getHeight();

                            // colocar isto em xml para igualar dados entre aqui e o grid_view <ImageView...
                            int newWidth = 100;
                            int newHeight = 70;

                            float scaleWidth;
                            float scaleHeight;

                            if (origWidth >= origHeight)
                            {
                                scaleWidth = (float) newWidth / origWidth;
                                scaleHeight = scaleWidth;
                            }
                            else
                            {
                                scaleHeight = (float) newHeight / origHeight;
                                scaleWidth = scaleHeight;
                            }

                            newWidth = Math.round(origWidth * scaleWidth);
                            newHeight = Math.round(origHeight * scaleWidth);

                            newBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
                            //newBitmap = Bitmap.createScaledBitmap(bitmap, 75, 75, true);
                            //newBitmap = Bitmap.createBitmap(bitmap);

                            bitmap.recycle();

                            if (newBitmap != null)
                            {
                                publishProgress(new LoadedImage(newBitmap));

                                size++;
                            }
                        }
                    }

                    // If size is 0, there are no images on the SD Card.
                    if (size == 0)
                    {
                        //No Images available
                        System.out.println("Size == 0");
                        mHandler.sendEmptyMessage(END_PROGRESS);
                    }

                    cursor.close();
                }
            }
            catch (Exception e)
            {
                mHandler.sendEmptyMessage(ERROR_PROGRESS);
            }

            return null;
        }

        /**
         * Add a new LoadedImage in the images grid.
         *
         * @param value The image.
         */
        @Override
        public void onProgressUpdate(LoadedImage... value)
        {
            addImage(value);
        }

        /**
         * Set the visibility of the progress bar to false.
         *
         * @see AsyncTask#onPostExecute(Object)
         */
        @Override
        protected void onPostExecute(Object result) 
        {
            setProgressBarIndeterminateVisibility(false);
        }
    }

    /**
     * Adapter for our image files. 
     * 
     * @author Mihai Fonoage
     *
     */
    class ImageAdapter extends BaseAdapter 
    {

        private Context mContext; 
        private LayoutInflater layoutInflater;
        private ArrayList<LoadedImage> photos = new ArrayList<LoadedImage>();

        public ImageAdapter(Context context) 
        { 
            mContext = context; 
            layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        } 

        public void addPhoto(LoadedImage photo) 
        { 
            photos.add(photo); 
        } 

        public int getCount() 
        { 
            return photos.size(); 
        } 

        public Object getItem(int position) 
        { 
            return photos.get(position); 
        } 

        public long getItemId(int position) 
        { 
            return position; 
        } 

        public View getView(int position, View convertView, ViewGroup parent)
        { 
            final View v;
            
            if (convertView == null) 
            { 
                //We inflate the xml which gives us a view 
                v = layoutInflater.inflate(R.layout.sdcard_floorplan_grid_item, parent, false);
            } 
            else 
            { 
                v = convertView;
            } 
                
            ImageView iv = (ImageView) v.findViewById(R.id.grid_item_image);
            iv.setImageBitmap(photos.get(position).getBitmap());
            
            TextView tv = (TextView) v.findViewById(R.id.grid_item_floorplan_name);
	    tv.setText("Colocar nome diretorio" + position);

            tv = (TextView) v.findViewById(R.id.grid_item_floorplan_description_I);
	    tv.setText("Floor plan n.º " + position);
            
            tv = (TextView) v.findViewById(R.id.grid_item_floorplan_description_II);
	    tv.setText("Floor plan n.º " + position);

            iv.setPadding(8, 8, 8, 8);
            
            return v; 
        }
    }       

    /**
     * A LoadedImage contains the Bitmap loaded for the image.
     */
    private static class LoadedImage 
    {
        Bitmap mBitmap;

        LoadedImage(Bitmap bitmap)
        {
            mBitmap = bitmap;
        }

        public Bitmap getBitmap() 
        {
            return mBitmap;
        }
    }
    
    /**
     * When an image is clicked, load that image as a puzzle. 
     */
    public void onItemClick(AdapterView<?> parent, View v, int position, long id)
    {
        System.out.println("ONCLICK");
        int columnIndex;
        String[] projection = {MediaStore.Images.Media.DATA};
        
        Cursor cursor = managedQuery(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, 
                projection,
                null, 
                null, 
                null);
        
        if (cursor != null) 
        {
            System.out.println("IF CURSOR != NULL");
            columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToPosition(position);
            String imagePath = cursor.getString(columnIndex);

            System.out.println("ALERT: " + imagePath);
            FileInputStream is = null;
            BufferedInputStream bis = null;
            
            try 
            {
                System.out.println("TRY");
                is = new FileInputStream(new File(imagePath));
                bis = new BufferedInputStream(is);
                
                Bitmap bitmap = BitmapFactory.decodeStream(bis);
                //Bitmap useThisBitmap = Bitmap.createScaledBitmap(bitmap, parent.getWidth(), parent.getHeight(), true);
                bitmap.recycle();

// aqui. guardar imagem                //Display bitmap (useThisBitmap)
            } 
            catch (Exception e) 
            {
                //Try to recover
            }
            finally 
            {
                try 
                {
                    if (bis != null)
                    {
                        bis.close();
                    }
                    
                    if (is != null) 
                    {
                        is.close();
                    }

                    cursor.close();
                    projection = null;
                    
                } catch (Exception e) { }
            }
        }
    }
}