
package d3v.bnb.ourwimarket;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.WindowManager;

public class Preferences extends PreferenceActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        addPreferencesFromResource(R.xml.preferences);
    }
}
