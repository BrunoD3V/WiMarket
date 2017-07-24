package d3v.bnb.ourwimarket;

/**
 * Created by bruno on 07/22/2017.
 */

public class AccessPointModel
{
    private AccessPoint ac;

    public AccessPointModel(AccessPoint ac)
    {
        this.ac = ac;
    }

    public String getBSSID()                        {return ac.bssid();}
    //public void setBSSID(String bssid)              {this.bssid = bssid;}

    public String getSSID()                         {return ac.ssid();}
    public void setSSID(String ssid)                {this.ac.ssid(ssid);}

    public int getX()                               {return ac.distance_x();}
    public void setX(int x)                         {this.ac.setDistance_x(x);}

    public int getY()                               {return ac.distance_y();}
    public void setY(int y)                         {this.ac.setDistance_y(y);}

    public int getZ()                               {return ac.distance_z();}
    public void setZ(int z)                         {this.ac.setDistance_z(z);}

    public boolean isSelected()                     {return ac.isSelected();}
    public void setSelected(boolean selected)       {this.ac.setSelected(selected);}
}
