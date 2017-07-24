/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package d3v.bnb.ourwimarket;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.List;

public class AccessPointList extends ListActivity 
{
    /** Called when the activity is first created.
     * @param icicle */
    @Override
    public void onCreate(Bundle icicle) 
    {
	super.onCreate(icicle);
        
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Create an array of Strings, that will be put to our ListActivity
	ArrayAdapter<AccessPointModel> adapter = new AccessPointArrayAdapter(this, getModel());
	setListAdapter(adapter);
    }
//fazer de maneira a mostrar o status e usar isto como gerir pontos de acesso avariados e sinaliza-los como isso (imagem != bandeira)
    private List<AccessPointModel> getModel() 
    {
	List<AccessPointModel> list = new ArrayList<AccessPointModel>();

        ArrayList<AccessPoint> access_points = Positioning.getWifiList();
        
        for (int i = 0; i < access_points.size(); i++)
        {
            AccessPoint ac = (AccessPoint) access_points.get(i);
        
            list.add(get(ac));
        }
	
        // Initially select one of the items
	//list.get(1).setSelected(true);
	
        return list;
    }

    private AccessPointModel get(AccessPoint ac) 
    {
	return new AccessPointModel(ac);
    }
}