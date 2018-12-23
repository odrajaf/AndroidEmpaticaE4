package com.example.fajardo.empaticatest;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

//CAMBIAR A ASINCRONO PARA GUARDAR EN LA BASE DE DATOS
public class SQLitePersistencia extends SQLiteOpenHelper {

    private final int NO_HAY_SESION = -1;

    String createTable = "";
    Context context;

    int idSesion = NO_HAY_SESION;

    public SQLitePersistencia(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        this.context = context;
        createTable = readSQLTable(R.raw.creacion);



        //iniciandoBD();
    }

    public String readSQLTable(int resId)
    {
        InputStream inputStream = context.getResources().openRawResource(resId);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        int i;
        try {
            i = inputStream.read();
            while (i != -1)
            {
                byteArrayOutputStream.write(i);
                i = inputStream.read();
            }
            inputStream.close();
        } catch (IOException e) {
            return null;
        }
        return byteArrayOutputStream.toString();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        String[] creates = createTable.split(";");
        for(int i = 0; i < creates.length-1; i++){
            sqLiteDatabase.execSQL(creates[i]);

        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        String[] creates = createTable.split(";");
        for(int cont = 0; cont < creates.length-1; cont++){
            sqLiteDatabase.execSQL(creates[cont]);

        }
    }

    public void iniciandoBD() {
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        String query = "SELECT COUNT(*) as cuantos FROM pacientes";
        Cursor cursor = sqLiteDatabase.rawQuery(query, null);
        cursor.moveToFirst();
        int cuantos =cursor.getInt(cursor.getColumnIndex("cuantos"));
        System.out.println("CUANTOS PACIENTES HAY: " + String.valueOf(cuantos));
        if (cuantos ==0){
            //insertandoPaciente();
        }
    }
    public void insertandoPaciente() {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        String query = "INSERT INTO pacientes (nombre) VALUES ('por defecto')";
        sqLiteDatabase.execSQL(query);

    }

    public void creandoSesion(){
        System.out.println("COMPROBANDO SESION");
        if(idSesion == NO_HAY_SESION) {
            System.out.println("CREANDO SESION");
            //OBTENDRIAMOS EL ID DEL PACIENTE ACTUAL (POR DESARROLLAR)
            int idPaciente = 1;
            String strIdPaciente = String.valueOf(idPaciente);

            SQLiteDatabase dataLecturaPr = this.getReadableDatabase();
            String quer = "SELECT * FROM pacientes";
            Cursor cur = dataLecturaPr.rawQuery(quer, null);
            cur.moveToFirst();
            idPaciente =cur.getInt(cur.getColumnIndex("id"));
            System.out.println("ID PACIENTE: " + String.valueOf(idPaciente));
            /////

            SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
            String query = "INSERT INTO sesiones (fecInicio,fecFin,idPaciente) VALUES (DATETIME('now'),DATETIME('now')," + strIdPaciente + ")";
            sqLiteDatabase.execSQL(query);

            SQLiteDatabase dataLectura = this.getReadableDatabase();
            query = "SELECT last_insert_rowid() as idSesion";
            Cursor cursor = dataLectura.rawQuery(query, null);
            cursor.moveToFirst();
            idSesion = cursor.getInt(cursor.getColumnIndex("idSesion"));

            System.out.println("ID DE SESION CREADO: "+idSesion);

        }
    }

    public void actualizarBvp (float valor){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        String query = "INSERT INTO bvp (valorBvp,fecha,idSesion) VALUES ("+valor+",DATETIME('now')," + idSesion + ")";
        sqLiteDatabase.execSQL(query);
    }

    public void actualizarEda(float valor){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        String query = "INSERT INTO eda (valorEda,fecha,idSesion) VALUES ("+valor+",DATETIME('now')," + idSesion + ")";
        sqLiteDatabase.execSQL(query);
    }

    //

    public void actualizarMovimiento(int valorX, int valorY, int valorZ){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        String query = "INSERT INTO movimiento (valorX,valorY,valorZ, fecha,idSesion) VALUES ("+valorX+","+valorY+","+valorZ+",DATETIME('now')," + idSesion + ")";
        sqLiteDatabase.execSQL(query);
    }

    public void actualizarIbi(float valorIbi){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        String query = "INSERT INTO ibi (valorIbi, fecha,idSesion) VALUES ("+valorIbi+",DATETIME('now')," + idSesion + ")";
        sqLiteDatabase.execSQL(query);
    }

    public void actualizarTemperatura(float valorTemp){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        String query = "INSERT INTO temperatura (valorTemp, fecha,idSesion) VALUES ("+valorTemp+",DATETIME('now')," + idSesion + ")";
        sqLiteDatabase.execSQL(query);
    }


    public void visualizarBvp(){
        float bvp = -1;
        SQLiteDatabase dataLecturaPr = this.getReadableDatabase();
        String quer = "SELECT * FROM bvp";
        Cursor cur = dataLecturaPr.rawQuery(quer, null);
        if(cur.moveToFirst()) {
            bvp = cur.getInt(cur.getColumnIndex("valorBvp"));
            System.out.println("Valor bvp: " + String.valueOf(bvp));
        }
        while(cur.moveToNext()){
            bvp = cur.getInt(cur.getColumnIndex("valorBvp"));
            System.out.println("Valor bvp: " + String.valueOf(bvp));
        }


    }
}