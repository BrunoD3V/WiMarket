
package d3v.bnb.ourwimarket;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Home extends Activity implements OnClickListener
{
    private TableLayout table1, table2, table3, table4, table5;
    private TableRow row1, row2, row3, row4, row5;

    private Button btnMapActivity;
    private Button btnRouteActivity;
    private Button btnLoadActivity;
    private Button btnSaveActivity;
    private Button btnSave;
    private Button btnMenu;
    private Button btnSearch;

    private AutoCompleteTextView selectChoiceItem;
    
    private TextView title;
    private EditText fileSave;

    // files
    private File fileList[];
    private List<String> fileSelect;
    private ListView listView;

    private static ArrayList<AccessPoint> access_points;
    private static ArrayList<RadioMap> radio_maps;

    private String newfile = "";
    private float newscale = 0.0f;
    private int newnorth = 0;
    private int[] newentry = new int[2];

    public ArrayAdapter adapterChoice;
    public ArrayAdapter adapterItem;

    @Override
    public void onCreate(Bundle bundle)
    {
        super.onCreate(bundle);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.home);

        // load activity
        table1 = (TableLayout)findViewById(R.id.table1);
        row1 = (TableRow)findViewById(R.id.row1);

        // back to menu
        table2 = (TableLayout)findViewById(R.id.table2);
        row2 = (TableRow)findViewById(R.id.row2);

        // menu
        table3 = (TableLayout)findViewById(R.id.table3);
        row3 = (TableRow)findViewById(R.id.row3);

        // save activity
        table4 = (TableLayout)findViewById(R.id.table4);
        row4 = (TableRow)findViewById(R.id.row4);

        // route activity
        table5 = (TableLayout)findViewById(R.id.table5);
        row5 = (TableRow)findViewById(R.id.row5);

        title = (TextView) findViewById(R.id.title);
        fileSave = (EditText) findViewById(R.id.fileSave);

        // buttons of menu
        btnMapActivity = (Button) findViewById(R.id.btnMapActivity);
        btnRouteActivity = (Button) findViewById(R.id.btnRouteActivity);
        btnLoadActivity = (Button) findViewById(R.id.btnLoadActivity);
        btnSaveActivity = (Button) findViewById(R.id.btnSaveActivity);
        
        // route 
        selectChoiceItem = (AutoCompleteTextView) findViewById(R.id.editDestination);
        selectChoiceItem.setDropDownWidth(280);// comprimento horizontal drop
        
        btnSearch = (Button) findViewById(R.id.btnSearchRoute);
        btnSearch.setEnabled(false);

        // regras finais
        btnRouteActivity.setEnabled(false);

        fileSelect = new ArrayList<String>();
        access_points = new ArrayList<AccessPoint>();
        radio_maps = new ArrayList<RadioMap>();

        btnMenu = (Button) findViewById(R.id.btnMenu);
        btnSave = (Button) findViewById(R.id.btnSave);
        
        // se encontrar ficheiros de configuração apresenta uma janela a pedir se deseja carregar já algum
        if (listFiles() > 0)
        {
            loadFile();
        }
        else
        {
            homeMenu();
        }
    }

    @Override
    public void onRestart()
    {
        super.onRestart();

        try
        {
            // se foi carregado um mapa válido, activa o save
            if (Positioning.getWifiList().size() > 0)
            {
                if (btnLoadActivity.isEnabled())
                    btnSaveActivity.setEnabled(true);

                Positioning.disableRadioMap();
                Positioning.disableManageRadioMap();
                
                Positioning.setDefaultNote();
            }
            else
            {
                btnSaveActivity.setEnabled(false);
            }
        } catch (Exception e) {}
    }

    @Override
    public void onResume()// acontecimento button back
    {
        super.onResume();

        try
        {
            if (Positioning.getWifiList().size() > 0)
            {
                if (btnLoadActivity.isEnabled())
                    btnSaveActivity.setEnabled(true);
            }
            else
            {
                btnSaveActivity.setEnabled(false);
            }
        } catch (Exception e) {}
    }
    
    private void homeMenu()
    {
        table1.removeView(row1);
        table2.removeView(row2);
        table3.removeView(row3);
        table4.removeView(row4);
        table5.removeView(row5);

        table3.addView(row3);
        
        title.setText("Menu");

        btnMapActivity.setPressed(false);
        btnMapActivity.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                init();

                Positioning.enableMap();

                startActivity(new Intent(Home.this, MapActivity.class));
            }
        });

        btnLoadActivity.setPressed(false);
        btnLoadActivity.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                table1.addView(row1);
                table2.addView(row2);

                loadFile();
            }
        });

        btnSaveActivity.setPressed(false);
        btnSaveActivity.setEnabled(false);
        btnSaveActivity.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                table2.addView(row2);
                table4.addView(row4);

                saveFileSelect(Positioning.getFileSelected());
            }
        });
    }

    private void loadFile()
    {
        table3.removeView(row3);
        table4.removeView(row4);
        table5.removeView(row5);

        title.setText(getString(R.string.file_new));

        listView = (ListView) findViewById(R.id.list);
        adapterChoice = new ArrayAdapter(this, android.R.layout.test_list_item, fileSelect);
        listView.setAdapter(adapterChoice);

        listView.setOnItemClickListener(new OnItemClickListener()
        {
            public void onItemClick(AdapterView adapter, View view, int position, long id)
            {
                openFileConfirmation(position);
            }
        });

        btnMenu.setPressed(false);
        btnMenu.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                homeMenu();
            }
        });
    }

    public void saveFileSelect(String file)
    {
        table1.removeView(row1);
        table3.removeView(row3);
        table5.removeView(row5);

        title.setText(getString(R.string.btn_save_activity));
        String filename = "New File";

        if (Positioning.isLoaded())
        {
            filename = Positioning.getFileSelected().substring(0, Positioning.getFileSelected().lastIndexOf("."));
        }

        fileSave.setText(filename);

        btnSave.setPressed(false);
        btnSave.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                if(fileSave.getText().toString().equals("") || fileSave.getText().toString().matches("."))
                {
                    Toast.makeText(Home.this, getString(R.string.file_invalid), Toast.LENGTH_SHORT).show();
                }
                else
                {
                    saveFile();
                }
            }
        });

        btnMenu.setPressed(false);
        btnMenu.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                homeMenu();
            }
        });
    }
    
    public void onClick(View v) {}

    public void openFileConfirmation(int position)
    {
        final String fileSelected = fileSelect.get(position);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(getString(R.string.file_sure) + " " + fileSelected.substring(0, fileSelected.lastIndexOf(".")) + "?").setCancelable(false)
            .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {
                if (!loadFiles(fileSelected))
                {
                    Toast.makeText(Home.this, getString(R.string.file_invalid), Toast.LENGTH_SHORT).show();

                    loadFile();
                }

                if (Positioning.isLoaded())
                {
                    Toast.makeText(Home.this, getString(R.string.file_selected), Toast.LENGTH_SHORT).show();

                    homeMenu();
                }
            }
        })
            .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {
                Toast.makeText(Home.this, getString(R.string.canceled), Toast.LENGTH_SHORT).show();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    // le um conjunto de arquivos
    public boolean loadFiles(String file)
    {
        access_points.clear();
        radio_maps.clear();

        Positioning.isLoaded(false);

        if (!loadAccessPoint(file)) return false;
        
        if (!loadRadioMap(file)) return false;

        Positioning.setFileSelected(file);
        Positioning.isLoaded(true);

        return true;
    }

    // carrega os pontos de acesso
    private boolean loadAccessPoint(String file)
    {
        try
        {
            DeviceReader in = new DeviceReader(file);

            // 1ª linha a desenvolver
            String line;
            // 2ª linha com o nome da planta, escala correspondente e a localização da entrada
            line = in.readln();

            if (line != null && line.contains(":"))
            {
                String[] variables = line.split(":");

                newfile  = variables[0];
                newscale = Float.parseFloat(variables[1]);

                newentry[0] = Integer.parseInt(variables[2]);
                newentry[1] = Integer.parseInt(variables[3]);
            }
            else
            {
                return false;
            }

            // 3ª norte do mapa
            line = in.readln();

            if (line != null)
            {
                newnorth = Integer.parseInt(line);
            }
            else
            {
                newnorth = 0;
            }

            while (true)
            {
                line = in.readln();

                if(line == null) break;

                String[] col = line.split(":");

                String bssid = col[4] + ":" + col[5] + ":" + col[6] + ":" + col[7] + ":" + col[8] + ":" + col[9];
                String ssid  = col[3].toString();

                int x = Integer.parseInt(col[0]);
                int y = Integer.parseInt(col[1]);
                int z = Integer.parseInt(col[2]);
                
                access_points.add(new AccessPoint(bssid, ssid, x, y, z));
            }

            in.close();

        }catch(Exception e) {return false;}

        return true;
    }
    
    // carrega o radiomap
    private boolean loadRadioMap(String file)
    {
        RadioMap rm;

        try
        {
            File radiomapFile = new File("/sdcard/" + file.substring(0, file.indexOf(".")) + ".rmp");

            if (radiomapFile.exists())
            {
                DeviceReader in = new DeviceReader(file.substring(0, file.indexOf(".")) + ".rmp");

                int count = 0;
                int[] dim = new int[2];

                // 1ª linha - dimensão do mapa
                String line = in.readln();
                dim[0] = Integer.parseInt(line.substring(0, line.indexOf(":")));
                dim[1] = Integer.parseInt(line.substring(line.lastIndexOf(":") + 1));

                while (true)
                {
                    line = in.readln();

                    if(line == null) break;

                    // cria uma nova matrix para um novo access point
                    if (line.contains("bssid:"))
                    {
                        rm = new RadioMap(dim);

                        rm.bssid(line.substring(line.indexOf(":") + 1, line.length()));

                        count = 0;

                        radio_maps.add(rm);
                    }
                    else
                    {
                        // insere os valores da matrix
                        String[] col = line.split("\t");

                        rm = (RadioMap) radio_maps.get(radio_maps.size() - 1);

                        for (int i = 0; i < col.length; i++)
                        {
                            rm.addDbmMatrix(i, count, Float.parseFloat(col[i]));
                        }

                        count++;
                    }
                }

                in.close();
            }
        }catch(Exception e) {return false;}

        return true;
    }

    // guarda os pontos de acesso e o radiomap
    public void saveFile()
    {
        AccessPoint ac;
        RadioMap rm;
        
        access_points = Positioning.getWifiList();
        radio_maps = Positioning.getRadiomapList();

        DeviceWriter out = new DeviceWriter(fileSave.getText().toString() + ".mkt");

        // 1ª linha 
        out.write("\n");

        int[] entry = Positioning.getFileEntry();

        // 2ª linha
        out.write(Positioning.getFileMap() + ":" + Positioning.getFileScale() + ":" + entry[0] + ":" + entry[1] + "\n");
                
        // 3ª linha
        out.write(Positioning.getFileNorth() + "\n");

        for (int i = 0; i < access_points.size(); i++)
        {
            ac = (AccessPoint) access_points.get(i);

            StringBuilder sb = new StringBuilder();

            sb.append(String.valueOf(ac.distance_x())).append(":");
            sb.append(String.valueOf(ac.distance_y())).append(":");
            sb.append(String.valueOf(ac.distance_z())).append(":");
            sb.append(ac.ssid()).append(":");
            sb.append(ac.bssid()).append("\n");

            out.write(sb.toString());
        }

        out.close();

        int[] matrixDimension = Positioning.getMatrixDimension();

        out = new DeviceWriter(fileSave.getText().toString() + ".rmp");

        out.write(matrixDimension[0] + ":" + matrixDimension[1] + "\n");
        
        for (int i = 0; i < radio_maps.size(); i++)
        {
            rm = (RadioMap) radio_maps.get(i);
            
            StringBuilder sb = new StringBuilder();

            sb.append("bssid:").append(rm.bssid()).append("\n");
            sb.append(rm.toSave());

            out.write(sb.toString());
        }

        out.close();

        Toast.makeText(Home.this, getString(R.string.file_saved), Toast.LENGTH_SHORT).show();

        homeMenu();
    }
    
    private int listFiles()
    {
        fileSelect.clear();
        
        fileList = Environment.getExternalStorageDirectory().listFiles();

        for (int i=0; i<fileList.length; i++)
        {
            if (fileList[i].toString().endsWith(".mkt"))
            {
                fileSelect.add(fileList[i].getName());
            }
        }

        if (fileSelect.isEmpty())
        {
            btnLoadActivity.setEnabled(false);
        }
        else
        {
            btnLoadActivity.setEnabled(true);
        }

        return fileSelect.size();
    }
    
    private void init()
    {
        // nome do mapa, escala do mapa, posição inicial, pontos de acesso, radio map
        if (!Positioning.isLoaded())
            Positioning.init(  ""   ,   0.0f  , newnorth, newentry, access_points, radio_maps);
        else
            Positioning.init(newfile, newscale, newnorth, newentry, access_points, radio_maps);
    }
}