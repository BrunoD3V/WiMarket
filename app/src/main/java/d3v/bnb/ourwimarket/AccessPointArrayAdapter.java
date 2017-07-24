/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package d3v.bnb.ourwimarket;

import android.app.Activity;
import android.app.Dialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class AccessPointArrayAdapter extends ArrayAdapter<AccessPointModel>
{
    private final List<AccessPointModel> list;
    private final Activity context;

    public AccessPointArrayAdapter(Activity context, List<AccessPointModel> list) 
    {
        super(context, R.xml.buttonlayoutmodel, list);
	
        this.context = context;
	this.list = list;
    }

    static class ViewHolder 
    {
	protected TextView bssid;
        protected TextView ssid;
        
        protected TextView x;
        protected TextView y;
        protected TextView z;
        
	protected CheckBox checkbox;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) 
    {
	View view = null;

        if (convertView == null)
        {
            LayoutInflater inflator = context.getLayoutInflater();
            view = inflator.inflate(R.xml.buttonlayoutmodel, null);
	
            final ViewHolder viewHolder = new ViewHolder();
            
            viewHolder.bssid = (TextView) view.findViewById(R.id.bssid);
            viewHolder.ssid  = (TextView) view.findViewById(R.id.ssid);

            viewHolder.x = (TextView) view.findViewById(R.id.x);
            viewHolder.y = (TextView) view.findViewById(R.id.y);
            viewHolder.z = (TextView) view.findViewById(R.id.z);
            
            viewHolder.checkbox = (CheckBox) view.findViewById(R.id.check);
            
            viewHolder.checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
            {
                @Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
                {
                    AccessPointModel element = (AccessPointModel) viewHolder.checkbox.getTag();
                    element.setSelected(buttonView.isChecked());
		}
            });
            
            // para a linha toda usar view.setOnCli...
            
            view.setOnClickListener(new View.OnClickListener()
            {
                @Override
		public void onClick(View v) 
                {
                    changeSSID(v);
                }
            });
            
            view.setTag(viewHolder);
	    viewHolder.checkbox.setTag(list.get(position));
        }
        else
        {
            view = convertView;
            
            ((ViewHolder) view.getTag()).checkbox.setTag(list.get(position));
	}
	
        ViewHolder holder = (ViewHolder) view.getTag();
        
	holder.bssid.setText(list.get(position).getBSSID());
        holder.ssid .setText(list.get(position).getSSID());
        
        holder.x.setText("x: " + list.get(position).getX());
        holder.y.setText("y: " + list.get(position).getY());
        holder.z.setText("z: " + list.get(position).getZ());
        
	holder.checkbox.setChecked(list.get(position).isSelected());
	
        return view;
    }
    
    private void changeSSID(View v)
    {
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.xml.custom_dialog);
        dialog.setTitle(v.getId() + "º Dialog");
        dialog.setCancelable(true);
        
        TextView text = (TextView) dialog.findViewById(R.id.text);
        text.setText("Hello, this is a custom dialog!");
                    
        ImageView image = (ImageView) dialog.findViewById(R.id.image);
        image.setImageResource(R.drawable.icon);
        
        /*EditText edittext = (EditText) findViewById(R.id.edittext);
        edittext.addTextChangedListener(new TextWatcher() 
        {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) 
            {
                //adapter.getFilter().filter(s.toString());
            }

            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                
            }

            public void afterTextChanged(Editable arg0) {
                
            }
        });*/
        
        dialog.show();
    }
}

