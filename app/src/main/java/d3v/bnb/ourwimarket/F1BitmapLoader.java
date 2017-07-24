package d3v.bnb.ourwimarket;

/**
 * Created by bruno on 07/22/2017.
 */

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class F1BitmapLoader
{
    public static F1BitmapInfo loadBitmap(String URL)
    {
        Bitmap bitmap = null;

        try
        {
            // para fazer o download das imagens pela internet (url)
            /*URL url = new URL(URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setDoInput(true);
            conn.connect();

            InputStream is = conn.getInputStream();

            bitmap = BitmapFactory.decodeStream(is);*/

            // para ler do disco
            bitmap = BitmapFactory.decodeFile("/sdcard/" + URL);

        }
        catch (Exception ex)
        {

        }

        F1BitmapInfo result = new F1BitmapInfo();
        result.setBitmap(bitmap);
        result.setIdentifier(URL);

        return result;
    }
}