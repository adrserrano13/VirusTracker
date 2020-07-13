package com.example.virustracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class Activity_cambia_estado extends AppCompatActivity {
    private String PREFS_KEY = "mispreferencias";

    // Un simple String que represente el ID del dispositivo hasta que sepamos como obtener o generar el ID.
    String device_id = MainActivity.device_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cambia_estado);
        redondear(getValuePreferenceColor(getApplicationContext()));
        crearComponente();
        //device_id = MainActivity.device_id;
    }

    public void crearComponente()
    {
        LinearLayout linearExterno = findViewById(R.id.linearExterno);
        LinearLayout linearInterno = new LinearLayout(this);

        linearExterno.setOrientation(LinearLayout.VERTICAL);
        linearInterno.setOrientation(LinearLayout.HORIZONTAL);

        //cargar la sharedPreference para saber el color
        int id_preference = 0;
        String desc_rojo = "  Estado rojo                ";
        String desc_verde = "  Estado verde             ";
        //posibles cambios
        if(getValuePreferenceColor(getApplicationContext()) == R.drawable.verde)
        {
            linearExterno.addView(generarEstado(desc_rojo, R.drawable.rojo, "rojo"));
        }
        else if(getValuePreferenceColor(getApplicationContext()) == R.drawable.amarillo)
        {
            linearExterno.addView(generarEstado(desc_verde, R.drawable.verde, "verde"));
            linearExterno.addView(generarEstado(desc_rojo, R.drawable.rojo, "rojo"));
        }
        linearExterno.addView(linearInterno);
    }


    public LinearLayout generarEstado(String desc, int color, String tag)
    {
        final LinearLayout linearInterno = new LinearLayout(this);
        linearInterno.setOrientation(LinearLayout.HORIZONTAL);
        TextView texto = new TextView(this);
        texto.setText(desc);

        ImageView colorEstado = new ImageView(this);
        colorEstado.setImageDrawable(getDrawable(color));
        colorEstado.setLayoutParams(new LinearLayout.LayoutParams(400,400));

        redondear2(color, colorEstado);
        //constraint a los bordes


        linearInterno.addView(texto);
        linearInterno.addView(colorEstado);

        linearInterno.setTag(tag);

        linearInterno.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(linearInterno.getTag().equals("verde"))
                {
                    Toast.makeText(getApplicationContext(), "Se ha cambia tu estado a verde", Toast.LENGTH_SHORT).show();
                    saveValuePreferenceColor(getApplicationContext(), R.drawable.verde);
                    devolverValor(linearInterno);
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "Se ha cambia tu estado a rojo", Toast.LENGTH_SHORT).show();
                    saveValuePreferenceColor(getApplicationContext(), R.drawable.rojo);
                    devolverValor(linearInterno);

                    /* Cuando se cambia a estado rojo, se pasa el device_id al servidor para que este lo almacene en la tabla
                    de infectados */
                    actualizaServer(device_id);
                }
            }
        });

        return linearInterno;
    }

    public void devolverValor(LinearLayout linearInterno) {
        Intent i = new Intent();
        i.putExtra("nuevoEstado", (String) linearInterno.getTag());
        setResult(2001, i);
        finish();
    }

    public void saveValuePreferenceColor(Context context, int color) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_KEY, MODE_PRIVATE);
        SharedPreferences.Editor editor;
        editor = settings.edit();
        editor.putInt("color", color);
        editor.commit();
    }

    public int getValuePreferenceColor(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_KEY, MODE_PRIVATE);
        return  preferences.getInt("color", R.drawable.verde);
    }

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
        ImageView estado = findViewById(R.id.estado_actual);
        estado.setImageDrawable(roundedDrawable);
    }

    public void redondear2(int id, ImageView imageView)
    {
        //obtenemos el drawable original
        Drawable originalDrawable = getResources().getDrawable(id);
        Bitmap originalBitmap = ((BitmapDrawable) originalDrawable).getBitmap();

        //creamos el drawable redondeado
        float num = 2000;
        RoundedBitmapDrawable roundedDrawable = RoundedBitmapDrawableFactory.create(getResources(), originalBitmap);
        //asignamos el CornerRadius
        roundedDrawable.setCornerRadius(num);
        imageView.setImageDrawable(roundedDrawable);
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
                Toast.makeText(getApplicationContext(), "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
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

}
