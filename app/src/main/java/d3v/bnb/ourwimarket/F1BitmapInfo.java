package d3v.bnb.ourwimarket;

/**
 * Created by bruno on 07/22/2017.
 */

import android.graphics.Bitmap;
import android.widget.ImageSwitcher;
import android.widget.ImageView;

public class F1BitmapInfo
{
    private Bitmap bitmap;
    private String identifier;

    private ImageView toAssign;
    private ImageSwitcher toDisplay;

    public F1BitmapInfo() {}

    public Bitmap getBitmap()                           {return bitmap;}
    public void setBitmap(Bitmap bitmap)                {this.bitmap = bitmap;}

    public String getIdentifier()                       {return identifier;}
    public void setIdentifier(String identifier)        {this.identifier = identifier;}

    public void setToAssign(ImageView toAssign)         {this.toAssign = toAssign;}
    public ImageView getToAssign()                      {return toAssign;}

    public void setToDisplay(ImageSwitcher toDisplay)   {this.toDisplay = toDisplay;}
    public ImageSwitcher getToDisplay()                 {return toDisplay;}
}
