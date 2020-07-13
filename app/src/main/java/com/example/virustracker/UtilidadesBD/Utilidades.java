package com.example.virustracker.UtilidadesBD;

public class Utilidades
{
    //Constantes campos de la tabla usuario

    public static final String TABLA_USUARIO = "dispositivosCercanos";

    public static final String CAMPO_DEVICE_ID = "DEVICE_ID";
    //public static final String CAMPO_DATE_STATUS = "DATE_STATUS";
    public static final String CAMPO_DATE_IN = "DATE_IN";
    public static final String CAMPO_DATE_OUT = "DATE_OUT";


    //public static final String CREAR_TABLA_USUARIO = "CREATE TABLE "+TABLA_USUARIO+" ("+CAMPO_DEVICE_ID+" INTEGER, "+CAMPO_DATE_IN+" TEXT, "+CAMPO_DATE_OUT+" TEXT)";
    public static final String CREAR_TABLA_USUARIO = "CREATE TABLE "+TABLA_USUARIO+" ("+CAMPO_DEVICE_ID+" TEXT, "+CAMPO_DATE_IN+" TEXT, "+CAMPO_DATE_OUT+" TEXT)";
    public static final String ELIMINAR_TABLA_USUARIO = "DROP TABLE IF EXISTS "+TABLA_USUARIO+"";
}