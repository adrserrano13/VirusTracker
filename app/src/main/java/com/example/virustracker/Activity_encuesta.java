package com.example.virustracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

public class Activity_encuesta extends AppCompatActivity {
    private String PREFS_KEY = "mispreferencias";
    CheckBox diarrea, tos, fiebre, garganta, gusto, respirar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encuesta);

        diarrea = (CheckBox) findViewById(R.id.checkBox_diarrea);
        tos = (CheckBox) findViewById(R.id.checkBox_tos);
        fiebre = (CheckBox) findViewById(R.id.checkBox_fiebre);
        garganta = (CheckBox) findViewById(R.id.checkBox_garganta);
        gusto = (CheckBox) findViewById(R.id.checkBox_gusto);
        respirar = (CheckBox) findViewById(R.id.checkBox_respirar);
    }

    public void enviar(View v)
    {
        int resPositivas = 0;

        if(diarrea.isChecked())
            resPositivas++;
        if(tos.isChecked())
            resPositivas++;
        if(fiebre.isChecked())
            resPositivas++;
        if(garganta.isChecked())
            resPositivas++;
        if(gusto.isChecked())
            resPositivas++;
        if(respirar.isChecked())
            resPositivas++;



        if(resPositivas >= 4){
            //Color amarillo
            saveValuePreference(getApplicationContext(), R.drawable.amarillo);
        }

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void saveValuePreference(Context context, int color) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_KEY, MODE_PRIVATE);
        SharedPreferences.Editor editor;
        editor = settings.edit();
        editor.putInt("color", color);
        editor.commit();
    }
}
