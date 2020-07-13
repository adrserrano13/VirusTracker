package com.example.virustracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

public class Activity_encuesta_positivo extends AppCompatActivity {
    private String PREFS_KEY = "mispreferencias";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encuesta_positivo);
    }

    public void respuesta(View v)
    {
        if(v.getId() == R.id.boton_si) {
            saveValuePreference(getApplicationContext(), R.drawable.rojo);
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }else{
            Intent intent = new Intent(this, Activity_encuesta.class);
            startActivity(intent);
        }
    }

    public void saveValuePreference(Context context, int color) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_KEY, MODE_PRIVATE);
        SharedPreferences.Editor editor;
        editor = settings.edit();
        editor.putInt("color", color);
        editor.commit();
    }
}
