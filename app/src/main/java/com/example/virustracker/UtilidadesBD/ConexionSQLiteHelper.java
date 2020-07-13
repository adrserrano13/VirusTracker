package com.example.virustracker.UtilidadesBD;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class ConexionSQLiteHelper extends SQLiteOpenHelper {

    // Quizá también debamos hacer una tabla con los dispositivos de los que hemos estado cerca
    // pero eso es algo que le preguntaremos a Paco y si eso ya lo haremos.

    public ConexionSQLiteHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(Utilidades.CREAR_TABLA_USUARIO);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int versionAnterior, int versionNueva) {
        db.execSQL(Utilidades.ELIMINAR_TABLA_USUARIO);
        onCreate(db);
    }
}