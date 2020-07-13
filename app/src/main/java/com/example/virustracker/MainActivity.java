package com.example.virustracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.virustracker.UtilidadesBD.ConexionSQLiteHelper;
import com.example.virustracker.UtilidadesBD.Utilidades;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity
{
    //boolean bluetooth = true;
    private String PREFS_KEY = "mispreferencias";
    private final int REQUEST_ACCESS_FINE = 0;
    public static  String device_id;

    Drawable azul;
    Drawable gris;
    BluetoothAdapter adaptador;
    int ACTIVAR_BLUETOOTH = 2;

    // Base de Datos
    ConexionSQLiteHelper baseDatos;

    //tiempo
    Timer temporizador;
    TimerTask tarea;
    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        device_id = getValuePreferenceDeviceId(getApplicationContext());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        redondear(getValuePreferenceColor(getApplicationContext()));
        baseDatos = new ConexionSQLiteHelper(this, "virusTrackerBBDD", null, 3);
        /*
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.BLUETOOTH_ADMIN)) {
                Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(i, ACTIVAR_BLUETOOTH);
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.BLUETOOTH_ADMIN},
                        REQUEST_ACCESS_FINE);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
            //ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_ADMIN}, REQUEST_ACCESS_FINE);
        */
        azul = getResources().getDrawable( R.drawable.rounded_imagebutton_azul);
        gris = getResources().getDrawable( R.drawable.rounded_imagebutton_gris);
        adaptador = BluetoothAdapter.getDefaultAdapter();

        if(adaptador == null) {
            Toast.makeText(getBaseContext(), "El dispositivo no soporta Bluetooth", Toast.LENGTH_LONG).show();
            cambiaColorBluetooth(gris);
        }
        else{
            if (!adaptador.isEnabled()) {
                cambiaColorBluetooth(gris);
            } else {
                cambiaColorBluetooth(azul);
            }
        }

        new Conexion().execute();
        if(getValuePreferenceColor(getBaseContext()) == R.drawable.rojo){
            actualizaServer(device_id);
        }

        //Registra el broadcast receiver
        IntentFilter filtro = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filtro);

        //Empieza el temporizador en segundo plano
        iniciarTemporizador();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_ACCESS_FINE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    //redondea el imageview y se le pasa el R.drawable del color
    public void redondear(int id)
    {
        //obtenemos el drawable original
        Drawable originalDrawable = getResources().getDrawable(id);
        Bitmap originalBitmap = ((BitmapDrawable) originalDrawable).getBitmap();

        //creamos el drawable redondeado
        RoundedBitmapDrawable roundedDrawable = RoundedBitmapDrawableFactory.create(getResources(), originalBitmap);
        float num = 2000;
        //asignamos el CornerRadius
        roundedDrawable.setCornerRadius(num);
        ImageView estado = findViewById(R.id.estado_usuario);
        estado.setImageDrawable(roundedDrawable);
    }

    //Muestra el menú inflando el layout
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Infla el menú,añade items a la barra de acción si ésta se presiona
        getMenuInflater().inflate(R.menu.menu_pantalla_inicio, menu);
        return true;
    }

    //Evento que controla qué opción del menú se ha pulsado para abrir su Activity
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        Intent intent;
        switch(item.getItemId())
        {
            case R.id.menu_sintomas:
                intent = new Intent(this, Activity_sintomas.class);
                startActivity(intent);
                break;

            case R.id.menu_informacion:
                intent = new Intent(this, Activity_informacion.class);
                startActivity(intent);
                break;

            case R.id.menu_consejos_higiene:
                intent = new Intent(this, Activity_consejos_higiene.class);
                startActivity(intent);
                break;

            case R.id.menu_ayuda:
                intent = new Intent(this, Activity_ayuda_usuario.class);
                startActivity(intent);
                break;

            case R.id.menu_ajustes:
                intent = new Intent(this, Activity_ajustes.class);
                startActivityForResult(intent, 1001);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    //evento vacío para que no vuelva a la pantalla de carga
    public void onBackPressed(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Salir");
        builder.setMessage("¿Quieres cerrar la app?");

        builder.setPositiveButton("SI", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finishAffinity();
            }
        });
        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void cambiaEstado(View v)
    {
        Intent intent = new Intent(this, Activity_cambia_estado.class);
        startActivityForResult(intent, 1001);
    }

    public void GestionBluetooth(View v)
    {
        if(adaptador == null) {
            Toast.makeText(getBaseContext(), "El dispositivo no soporta Bluetooth", Toast.LENGTH_LONG).show();
        }
        else{
            if (!adaptador.isEnabled()) {
                Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(i, ACTIVAR_BLUETOOTH);
                cambiaColorBluetooth(azul);
                // bluetooth = false;
            } else {
                adaptador.disable();
                cambiaColorBluetooth(gris);
                // bluetooth = true;
            }
        }
    }
    public void cambiaColorBluetooth(Drawable color){
        ImageButton imageButtonBluetooth = (ImageButton) findViewById(R.id.imageButton_Bluetooth);
        imageButtonBluetooth.setBackground(color);
    }

    public int getValuePreferenceColor(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_KEY, MODE_PRIVATE);
        return  preferences.getInt("color", R.drawable.verde);
    }
    public String getValuePreferenceDeviceId(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_KEY, MODE_PRIVATE);
        return  preferences.getString("device_id", "default");
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (resultCode == 2001 && requestCode == 1001)
        {
            if (data.hasExtra("nuevoEstado"))
            {
                redondear(getValuePreferenceColor(getApplicationContext()));
            }
        }
        if(resultCode == 3001 && requestCode == 1001){
            if (data.hasExtra("nuevoEstado"))
            {
                redondear(getValuePreferenceColor(getApplicationContext()));
                eliminarID(device_id);
                //Toast.makeText(getApplicationContext(), "ELIMINARID", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void eliminarID(final String id)
    {
        /* Host máquina Jorge*/
        String url = "http://192.168.100.7/pruebaServer/delete.php";
        /* Host máquina Angel */
        //String url = "http://192.168.0.36/pruebaServer/delete.php";
        /* Host Máquina Alan
        String url = "http://192.168.100.114/pruebaServer/delete.php"; */

        RequestQueue queue;

        queue = Volley.newRequestQueue(getApplicationContext());

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                //Toast.makeText(getApplicationContext(),  response, Toast.LENGTH_LONG).show();
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                //Toast.makeText(getApplicationContext(), "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        })
        {
            protected Map<String, String> getParams()
            {
                Map<String, String> info = new HashMap<String, String>();

                /* En mi ejemplo obtenía la fecha desde un campo editText, pero creo que es mejor obtener la fecha de
                "alta en el contagio" directamente según la hora del sistema en el servidor, por eso comento esa línea. */
                // info.put("fecha", editFecha.getText().toString());
                info.put("id", id);

                return info;
            }
        };

        queue.add(stringRequest);
    }

    class Conexion extends AsyncTask<String, String, String>
    {
        @Override
        protected String doInBackground(String... strings)
        {
            String result = "";

            /* Dejo comentadas estas líneas ya que son las IP's de nuestros servidores, en lugar de tener que ir escribiéndola
            o haciendo el ipconfig en el pc las descomentamos por si alguno de nosotros quiere hacer pruebas y que cada uno funcione
             con su propia IP */
            /* Host máquina Angel */
            String host = "http://192.168.100.7/pruebaServer/consulta.php";
            /* Host Máquina Alan
            String host = "http://192.168.100.114/pruebaServer/consulta.php"; */

            try
            {
                HttpClient cliente = new DefaultHttpClient();
                HttpGet request = new HttpGet();
                request.setURI(new URI(host));
                HttpResponse response = cliente.execute(request);

                BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                StringBuffer stringBuffer = new StringBuffer("");
                String linea = "";

                while ((linea = reader.readLine()) != null)
                {
                    stringBuffer.append(linea);
                }
                reader.close();
                result = stringBuffer.toString();
            } catch (Exception e)
            {
                return new String("Ha ocurrido una una excepción: " + e.getMessage());
            }

            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            // Parseamos la información del JSON aquí
            try
            {
                int contadorDispositivos = 0;
                JSONObject resultadoJson = new JSONObject(result);
                int exito = resultadoJson.getInt("exito");
                SQLiteDatabase db = baseDatos.getWritableDatabase();

                // en el php del server hay un array 'respuesta' donde el índice éxito tiene un valor de 1,
                // ese valor se pone a 1 cuándo el JSON ya posee valores y a 0 si está vacío.
                if (db != null)
                {
                    if (exito == 1)
                    {
                        // Hacer aquí el cursor
                        JSONArray dispositivos = resultadoJson.getJSONArray("dispositivos");
                        Cursor c = db.rawQuery("SELECT DEVICE_ID FROM dispositivosCercanos", null);

                        // Si el json ya tiene valores eliminamos la tabla sqlite y luego se va creando de nuevo
                        //db.execSQL(Utilidades.ELIMINAR_TABLA_USUARIO);
                        //db.execSQL(Utilidades.CREAR_TABLA_USUARIO);

                        if (c.moveToFirst())
                        {
                            for (int disp = 0; disp < dispositivos.length(); disp++)
                            {
                                for (int cur = 0; cur < c.getCount(); cur++)
                                {
                                    // Comparar el ID del JSON con el cursor, otro for hasta la longitud del cursor.
                                    JSONObject dispositivo = dispositivos.getJSONObject(disp);
                                    String id = dispositivo.getString("DEVICE_ID");
                                    String fecha = dispositivo.getString("DATE_STATUS");

                                    if (c.getString(cur).equals(id))
                                    {
                                        contadorDispositivos++;
                                        break;
                                    }
                                }
                            }
                        }

                        if (contadorDispositivos == 0)
                        {
                            if(getValuePreferenceColor(getApplicationContext()) == R.drawable.verde) {
                                saveValuePreferenceColor(getApplicationContext(), R.drawable.amarillo);
                                redondear(getValuePreferenceColor(getApplicationContext()));
                            }
                            Toast.makeText(getApplicationContext(), "Se ha encontrado una coincidencia entre los dispositivos contagiados y los dispositivos con los que has tenido contacto.", Toast.LENGTH_SHORT).show();
                        } else
                        {
                            //Toast.makeText(getApplicationContext(), "No se ha encontrado una coincidencia con ningún dispositivo infectado.", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else
                    {
                        //Toast.makeText(getApplicationContext(), "No hay dispositivos registrados", Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (JSONException jsone)
            {
                //Toast.makeText(getApplicationContext(), "Error en el JSON: " + jsone.getMessage(), Toast.LENGTH_LONG).show();
            }

            //Toast.makeText(getApplicationContext(), "Base de Datos actualizada.", Toast.LENGTH_SHORT).show();
        }
        /*protected String onPostExecute(Void result)
        {
            return null;
        }*/
    }

    public void actualizaServer(final String id)
    {
        /* Host máquina Angel */
        String url = "http://192.168.100.7/pruebaServer/post.php";
        /* Host Máquina Alan
        String url = "http://192.168.100.114/pruebaServer/post.php"; */

        RequestQueue queue;

        queue = Volley.newRequestQueue(getApplicationContext());

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                //Toast.makeText(getApplicationContext(), "Respuesta del Server: " + response, Toast.LENGTH_LONG).show();
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                //Toast.makeText(getApplicationContext(), "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        })
        {
            protected Map<String, String> getParams()
            {
                Map<String, String> info = new HashMap<String, String>();

                /* En mi ejemplo obtenía la fecha desde un campo editText, pero creo que es mejor obtener la fecha de
                "alta en el contagio" directamente según la hora del sistema en el servidor, por eso comento esa línea. */
                // info.put("fecha", editFecha.getText().toString());
                info.put("id", id);

                return info;
            }
        };

        queue.add(stringRequest);
    }

    //Broadcast para encontrar los bluetooth disponibles
    public final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            Toast.makeText(getApplicationContext(),"Buscando dispositivos...", Toast.LENGTH_SHORT).show();

            String accion = intent.getAction();
            //Si encuentra algun dispositivo entra
            if(BluetoothDevice.ACTION_FOUND.equals(accion))
            {
                //Se ha encontrado dispositivo y se obtiene la info del intent
                BluetoothDevice dispositivo = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String nomDisp = dispositivo.getName();

                //String macDis = dispositivo.getAddress();

                //pasamos el nomb del dispositivo para guardarlo en la bbdd
                insertarNombre(nomDisp);

            }
        }
    };

    //ESTO SE HACE EN SEGUNDO PLANO
    public void iniciarTemporizador()
    {
        temporizador = new Timer();
        tarea = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        //Inicia el broadcast
                        if(adaptador == null) {

                        }else {
                            if (adaptador.isDiscovering()) {
                                //Si el Bluetooth ya está en modo discover, lo cancelamos para iniciarlo de nuevo
                                adaptador.cancelDiscovery();
                            }
                            adaptador.startDiscovery();
                        }
                    }
                });
            }
        };
        temporizador.schedule(tarea,0,60000);
        //empieza desde que inicia el metodo (delay 0)
        //periodo cada 60 segundos. 10seg -> 10000

    }

    //Método para guardar el dispositivo que se le pasa en la base de datos
    public void insertarNombre(String nomb)
    {
        String fechaActual = java.text.DateFormat.getDateTimeInstance().format(new Date());

        SQLiteDatabase db = baseDatos.getReadableDatabase();

        try
        {
            //COMPROBAR QUE NO ESTE EN LA BASE DE DATOS (FUNCIONA)
            Cursor cursor = db.rawQuery("SELECT * FROM "+Utilidades.TABLA_USUARIO+" WHERE DEVICE_ID = ?",new String[]{nomb});

            //Si queremos comprobar cuantos hay
            //Cursor c = db.rawQuery("SELECT * FROM " + Utilidades.TABLA_USUARIO, null);


            //SI DEVUELVE ALGÚN VALOR SIGNIFICA QUE YA EXISTE Y NO LO INSERTA
            if(cursor.getCount()>0)
            {
                //Toast.makeText(this,nomb+ " ya existe, se actualizan las fechas", Toast.LENGTH_SHORT).show();

                String[]args = new String[]{nomb};

                //ACTUALIZAMOS DATEIN CON LA FECHA ACTUAL Y DATE OUT A NULL
                //Establecemos los campos-valores a actualizar
                ContentValues actualiza = new ContentValues();
                actualiza.put(Utilidades.CAMPO_DATE_IN,fechaActual);
                actualiza.put(Utilidades.CAMPO_DATE_OUT,"null");

                //Actualizamos el registro en la base de datos
                db.update(Utilidades.TABLA_USUARIO, actualiza, Utilidades.CAMPO_DEVICE_ID+"=?", args);
            }
            else
            {
                //Toast.makeText(this,"Inserta "+nomb, Toast.LENGTH_SHORT).show();

                //INSERTAR
                ContentValues nuevo = new ContentValues();

                //COMO NO SE COMO FUNCIONAN BIEN DATE_IN Y DATE_OUT PONGO LAS DOS CON LA FECHA ACTUAL CUANDO SE AÑADE
                nuevo.put(Utilidades.CAMPO_DEVICE_ID, nomb);
                nuevo.put(Utilidades.CAMPO_DATE_IN, fechaActual);
                nuevo.put(Utilidades.CAMPO_DATE_OUT, fechaActual);

                db.insert(Utilidades.TABLA_USUARIO, null, nuevo);
            }

        }catch (Exception e)
        {
            //Toast.makeText(this,nomb+ "Error al trabajar con la base de datos", Toast.LENGTH_SHORT).show();
        }
    }

    public void saveValuePreferenceColor(Context context, int color) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_KEY, MODE_PRIVATE);
        SharedPreferences.Editor editor;
        editor = settings.edit();
        editor.putInt("color", color);
        editor.commit();
    }
}
