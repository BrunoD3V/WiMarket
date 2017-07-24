package d3v.bnb.ourwimarket;

/**
 * Created by bruno on 07/22/2017.
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class DeviceWriter
{
    private String path;
    private String file;
    private FileOutputStream fos;
    private OutputStreamWriter osw;

    public DeviceWriter()
    {
        path = "/sdcard/";
        file = "";
        initialize();
    }

    public DeviceWriter(String s)
    {
        if (s.contains("/"))
        {
            this.path = "";//s.substring(0, s.lastIndexOf("/") + 1);
            this.file = s;//s.substring(s.lastIndexOf("/") + 1);
        }
        else
        {
            this.path = "/sdcard/";
            this.file = s;
        }

        initialize();
    }

    public void write(String s)
    {
        try
        {
            this.osw.write(s);
            this.osw.flush();
        } catch (IOException e) {}
    }

    public void close()
    {
        try
        {
            this.osw.flush();
            this.osw.close();
            this.fos.close();
        } catch (IOException e) {}
    }

    @Override
    public String toString()
    {
        return this.path + this.file;
    }

    private void initialize()
    {
        File f = new File(this.path + this.file);

        try
        {
            this.fos = new FileOutputStream(f);
        } catch (FileNotFoundException e) {}

        this.osw = new OutputStreamWriter(this.fos);
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    public void setFileName(String file)
    {
        this.file = file;
    }

    public String getPath()
    {
        return this.path;
    }

    public String getFileName()
    {
        return this.file;
    }
}