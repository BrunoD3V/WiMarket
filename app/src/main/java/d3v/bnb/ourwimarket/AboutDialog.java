package d3v.bnb.ourwimarket;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.text.util.Linkify;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class AboutDialog extends Dialog
{
    private static Context context = null;
	
    public AboutDialog(Context context) 
    {
	super(context);

        this.context = context;
    }
	
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        setContentView(R.layout.about);

        TextView tv = (TextView)findViewById(R.id.legal_text);
        tv.setText(readRawTextFile(R.raw.legal));
        tv = (TextView)findViewById(R.id.info_text);
        tv.setText(Html.fromHtml(readRawTextFile(R.raw.info)));
        tv.setLinkTextColor(Color.WHITE);
        Linkify.addLinks(tv, Linkify.ALL);
    }
	
    public static String readRawTextFile(int id) 
    {
        InputStream inputStream = context.getResources().openRawResource(id);
        InputStreamReader in = new InputStreamReader(inputStream);
        
        BufferedReader buf = new BufferedReader(in);
        
        String line;
        
        StringBuilder text = new StringBuilder();
        
        try 
        {
            while (( line = buf.readLine()) != null) text.append(line);
        } 
        catch (IOException e) 
        {
            return null;
        }
        
        return text.toString();
    }
}
