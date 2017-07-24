package d3v.bnb.ourwimarket;

/**
 * Created by bruno on 07/22/2017.
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

public class DeviceReader {
    private String path;
    private String file;
    private String line;

    private FileInputStream fis;
    private InputStreamReader isr;
    private BufferedReader in;

    public DeviceReader() {
        path = "/sdcard/";
        file = "";

        initialize();
    }

    public DeviceReader(String s) {
        if (s.contains("/")) {
            this.path = s.substring(0, s.lastIndexOf("/") + 1);//s.substring(0, s.lastIndexOf("/") + 1);
            this.file = s.substring(s.lastIndexOf("/") + 1);//s.substring(s.lastIndexOf("/") + 1);
        } else {
            this.path = "/sdcard/";
            this.file = s;
        }

        initialize();
    }

    public String readln() {
        try {
            this.line = in.readLine();
        } catch (IOException e) {
        }

        return this.line;
    }

    public void close() {
        try {
            this.in.close();
            this.isr.close();
            this.fis.close();
        } catch (IOException e) {
        }
    }

    private void initialize() {
        File f = new File(this.path + this.file);

        try {
            this.fis = new FileInputStream(f);
        } catch (FileNotFoundException e) {
        }

        this.isr = new InputStreamReader(this.fis);
        this.in = new BufferedReader(isr);
    }

    @Override
    public String toString() {
        return this.path + this.file;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return this.path;
    }

    public String getFileName() {
        return this.file;
    }
}