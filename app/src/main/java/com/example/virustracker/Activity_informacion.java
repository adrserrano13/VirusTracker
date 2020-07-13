package com.example.virustracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

public class Activity_informacion extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_informacion);
    }

    public void irAWeb(View v){
        String URL ="";

        switch(v.getId()){
            case R.id.imageButton_OMS:
                URL = "https://www.who.int/es/news-room/releases";
                break;
            case R.id.imageButton_Sanidad:
                URL = "https://www.mscbs.gob.es/profesionales/saludPublica/ccayes/alertasActual/nCov-China/situacionActual.htm";
                break;
            case R.id.imageButton_ElPais:
                URL = "https://elpais.com/sociedad/crisis-del-coronavirus/?rel=friso-portada";
                break;
            case R.id.imageButton_ElMundo:
                URL = "https://www.elmundo.es/ciencia-y-salud/salud/2020/05/25/5ecb6c9421efa0f1128b463c.html";
                break;
            case R.id.imageButton_ABC:
                URL = "https://www.abc.es/sociedad/abci-coronavirus-espana-directo-america-foco-pandemia-tiene-5-millones-casos-mundo-202005220630_directo.html";
                break;
            case R.id.imageButton_ElEspanol:
                URL = "https://www.elespanol.com/ciencia/salud/coronavirus/";
                break;
        }

        Uri uri = Uri.parse(URL);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }
}
