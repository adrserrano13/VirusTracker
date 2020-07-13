package com.example.virustracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Activity_wifi extends AppCompatActivity
{
    StringBuilder wifisGuardadas = new StringBuilder();
    private String PREFS_KEY = "mispreferencias";
    private WifiManager manejadorWifi;
    private ListView listViewRedes;
    private ListView listViewRedesGuardadas;
    private Button botonEscaneo;
    private Button botonGuardar;
    private TextView redHogar;
    private List<ScanResult> redes;
    private ArrayList<String> arrayListRedes = new ArrayList<>();
    private ArrayList<String> arrayListRedesGuardadas = new ArrayList<>();
    private ArrayAdapter adaptador;
    private ArrayAdapter adaptadorGuardados;

    private String nuevaRed = "";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
        botonEscaneo = findViewById(R.id.botonEscanear);
        botonGuardar = findViewById(R.id.botonGuardar);
        redHogar = findViewById(R.id.redHogar);
        listViewRedes = findViewById(R.id.RedesDisponiblesLV);
        listViewRedesGuardadas = findViewById(R.id.RedesGuardadasLV);

        String[] redGuardadas = getValuePreferenceWifi(getApplicationContext()).split(",");
        List<String> fixedLenghtList = Arrays.asList(redGuardadas);
        arrayListRedesGuardadas = new ArrayList<String>(fixedLenghtList);
        if(arrayListRedesGuardadas.get(0).equals("default"))
            arrayListRedesGuardadas.clear();

        //arrayListRedesGuardadas = (ArrayList<String>) this.getIntent().getSerializableExtra("misRedes");

        // Creamos un nuevo ArrayAdapter y comprobamos si está vacío o no, si no lo está (contiene redes) las mostramos en el ListView específico para las redes.
        adaptadorGuardados = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1 , arrayListRedesGuardadas);
        if (!arrayListRedesGuardadas.isEmpty())
        {
            listViewRedesGuardadas.setAdapter(adaptadorGuardados);
        }

        // Gestor de eventos del botón de Escanear Redes, el cuál busca las redes cercanas al dispositivo y las muestra en pantalla.
        botonEscaneo.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                escanearRedes();
            }
        });

        // Gestor de clic del botón de Guardar configuración, el cuál manda (devuelve) el ArrayList con las redes almacenadas, establece el resultado del Intent a OK y finaliza este activity.
        botonGuardar.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                for(String s : arrayListRedesGuardadas){
                    wifisGuardadas.append(s);
                    wifisGuardadas.append(",");
                }
                saveValuePreferenceWifi(getApplicationContext());
                finish();
            }
        });

        // Gestor de clic en el item del ListView que indica que red se ha seleccionado como red de hogar.
        listViewRedes.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                redHogar.setText((String) adaptador.getItem(position));
                nuevaRed = (String) adaptador.getItem(position);

                if (arrayListRedesGuardadas.contains(nuevaRed))
                {
                    Toast.makeText(Activity_wifi.this, "Esta red ya ha sido guardada.", Toast.LENGTH_SHORT).show();
                } else
                {
                    Toast.makeText(Activity_wifi.this, adaptador.getItem(position) + " seleccionada como Red del Hogar.", Toast.LENGTH_SHORT).show();
                    arrayListRedesGuardadas.add((String) adaptador.getItem(position));
                }

                adaptadorGuardados = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1 , arrayListRedesGuardadas);
                listViewRedesGuardadas.setAdapter(adaptadorGuardados);
            }
        });

        manejadorWifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        // Comprobamos si el WiFi del dispositivo está apagado, de ser así mostramos un mensaje por pantalla y lo encendemos.
        if (!manejadorWifi.isWifiEnabled())
        {
            Toast.makeText(this, "Encendiendo WiFi...", Toast.LENGTH_LONG).show();
            manejadorWifi.setWifiEnabled(true);
        }
    }

    // Método que realiza el escaneo de redes WiFi cercanas.
    private void escanearRedes()
    {
        arrayListRedes.clear();
        //listViewRedes.setAdapter(null);
        registerReceiver(receptorWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        manejadorWifi.startScan();
        Toast.makeText(this, "Buscando Redes Wifi...", Toast.LENGTH_SHORT).show();

        adaptador = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1 , arrayListRedes);
        listViewRedes.setAdapter(adaptador);
    }

    // Creamos un nuevo BroadcastReceiver
    BroadcastReceiver receptorWifi = new BroadcastReceiver()
    {
        /* El método onReceive del BroadcastReceiver se ejecutará cuando termine el escaneo de redes, por eso mostramos un mensaje por pantalla diciendo que el escaneo ha finalizado
        y mostramos todos los nombres (SSID) de cada red en el ListView. */
        @Override
        public void onReceive(Context context, Intent intent)
        {
            Toast.makeText(getApplicationContext(), "Escaneo completado", Toast.LENGTH_SHORT).show();

            redes = manejadorWifi.getScanResults();
            unregisterReceiver(this);

            for (ScanResult red : redes)
            {
                arrayListRedes.add(red.SSID);
                //arrayListRedes.add(red.BSSID);
                adaptador.notifyDataSetChanged();
            }
        }
    };

    public void saveValuePreferenceWifi(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_KEY, MODE_PRIVATE);
        SharedPreferences.Editor editor;
        editor = settings.edit();
        editor.putString("redesWifi", wifisGuardadas.toString());
        editor.commit();
    }
    public String getValuePreferenceWifi(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_KEY, MODE_PRIVATE);
        return  preferences.getString("redesWifi", "default");
    }

}