
package d3v.bnb.ourwimarket;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

public class HomeActivity extends DashboardActivity 
{
    protected Button home_btn_feature1;
    protected Button home_btn_feature2;
    protected Button home_btn_feature3;
    protected Button home_btn_feature4;

    /**
     * onCreate - called when the activity is first created.
     * Called when the activity is first created. 
     * This is where you should do all of your normal static set up: create views, bind data to lists, etc. 
     * This method also provides you with a Bundle containing the activity's previously frozen state, if there was one.
     * 
     * Always followed by onStart().
     *
     */
    
    //--------------------------------------------------------------------------
    
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) 
    {
        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate if the process is
        // killed and restarted.
        savedInstanceState.putBoolean("MyBoolean", true);
        savedInstanceState.putDouble("myDouble", 1.9);
        savedInstanceState.putInt("MyInt", 1);
        savedInstanceState.putString("MyString", "Welcome back to Android");
        // etc.
        super.onSaveInstanceState(savedInstanceState);
    }
    
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) 
    {
        super.onRestoreInstanceState(savedInstanceState);
        // Restore UI state from the savedInstanceState.
        // This bundle has also been passed to onCreate.
        boolean myBoolean = savedInstanceState.getBoolean("MyBoolean");
        double myDouble = savedInstanceState.getDouble("myDouble");
        int myInt = savedInstanceState.getInt("MyInt");
        
        String myString = savedInstanceState.getString("MyString");
    }

    //--------------------------------------------------------------------------


    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.home_main);
        
        //home_btn_feature1 = (Button)findViewById(R.id.home_btn_feature1);
        //home_btn_feature2 = (Button)findViewById(R.id.home_btn_feature2);
        //home_btn_feature3 = (Button)findViewById(R.id.home_btn_feature3);
        //home_btn_feature4 = (Button)findViewById(R.id.home_btn_feature4);
        
        //home_btn_feature1.setEnabled(false);
        //home_btn_feature2.setEnabled(false);
        //home_btn_feature3.setEnabled(false);
        //home_btn_feature4.setEnabled(false);
        
        //TextView txt = (TextView) findViewById(R.id.home_btn_feature1);  
        //Typeface font = Typeface.createFromAsset(getAssets(), "Chantelli_Antiqua.ttf");  
        //txt.setTypeface(font);

        MyPagerAdapter adapter = new MyPagerAdapter();
	ViewPager pager = (ViewPager) findViewById(R.id.panelpager);
	
        pager.setAdapter(adapter);
	pager.setCurrentItem(1); 
    }
    
    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) 
    {
        // Inflate the options menu from XML
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default

        return true;
    }*/

    /**
     * onDestroy
     * A ultima chamada antes da sua actividade ser destruída.
     * Isso pode acontecer porque a atividade está a terminar com o metodo finish(),
     * ou porque o sistema está temporariamente destruir essa instância da atividade para economizar espaço.
     * Pode-se distinguir entre estes dois cenários com o metodo isFinishing().
     */

    @Override
    protected void onDestroy() 
    {
        super.onDestroy();
    }

    /**
     * onPause
     * Called when the system is about to start resuming a previous activity. 
     * This is typically used to commit unsaved changes to persistent data, stop animations 
     * and other things that may be consuming CPU, etc. 
     * Implementations of this method must be very quick because the next activity will not be resumed 
     * until this method returns.
     * Followed by either onResume() if the activity returns back to the front, 
     * or onStop() if it becomes invisible to the user.
     *
     */

    @Override
    protected void onPause()
    {
        super.onPause();
    }

    /**
     * onRestart
     * Called after your activity has been stopped, prior to it being started again.
     * Always followed by onStart().
     *
     */

    @Override
    protected void onRestart()
    {
        super.onRestart();
    }

    /**
     * onResume
     * Called when the activity will start interacting with the user. 
     * At this point your activity is at the top of the activity stack, with user input going to it.
     * Always followed by onPause().
     *
     */

    @Override
    protected void onResume()
    {
        super.onResume();
    }

    /**
     * onStart
     * Called when the activity is becoming visible to the user.
     * Followed by onResume() if the activity comes to the foreground, or onStop() if it becomes hidden.
     *
     */

    @Override
    protected void onStart()
    {
        super.onStart();
    }

    /**
     * onStop
     * Called when the activity is no longer visible to the user
     * because another activity has been resumed and is covering this one. 
     * This may happen either because a new activity is being started, an existing one 
     * is being brought in front of this one, or this one is being destroyed.
     *
     * Followed by either onRestart() if this activity is coming back to interact with the user, 
     * or onDestroy() if this activity is going away.
     */

    @Override
    protected void onStop()
    {
        super.onStop();
    }

    /**
     */
    // Click Methods
    

    /**
     */
    // More Methods

    private class MyPagerAdapter extends PagerAdapter 
    {
	public int getCount() 
        {
            return 3;
	}

	public Object instantiateItem(View collection, int position) 
        {
            LayoutInflater inflater = (LayoutInflater) collection.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            int resId = 0;
	
            switch (position) 
            {
		case 0:
			resId = R.layout.home_legal;
			break;
		case 1:
			resId = R.layout.home_activity; 
			break;
		case 2:
			resId = R.layout.home_credits;
			break;
            }

            View view = inflater.inflate(resId, null);
            
            ((ViewPager) collection).addView(view, 0);
            
            return view;
	}

	@Override
	public void destroyItem(View arg0, int arg1, Object arg2) 
        {
            ((ViewPager) arg0).removeView((View) arg2);
	}

	@Override
	public void finishUpdate(View arg0) 
        {
            // TODO Auto-generated method stub
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) 
        {
            return arg0 == ((View) arg1);
	}

	@Override
	public void restoreState(Parcelable arg0, ClassLoader arg1) 
        {
            // TODO Auto-generated method stub
	}

	@Override
	public Parcelable saveState() 
        {
            // TODO Auto-generated method stub
            return null;
	}

	@Override
	public void startUpdate(View arg0) 
        {
            // TODO Auto-generated method stub
	}
    }
    
} // end class
