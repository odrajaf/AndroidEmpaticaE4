package com.example.fajardo.empaticatest;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;

import com.github.mikephil.charting.data.Entry;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

//HACER TANTAS CLASES COMO TIPOS DE VALOR HAYA E INSERTARLOS

    //UNA CON INT PARA LA CREACION GENERAL
    //OTRA PARA LOS MOVIMIENTOS
    //OTRA FLOAT PARA EL BVP
    //OTRA PARA EL IBI...
public class ManejadorSQLAsincrono extends AsyncTask<Float, Integer, Void> {

    Context contexto;
    SQLiteBackground test;

    public ManejadorSQLAsincrono(Context cont) {
        contexto = cont;
        test =  new SQLiteBackground(contexto,"bdValores",null,1);
    }

    @Override
    protected Void doInBackground(Float... params) {
        if(params[0] == EnumSensores.BVP.value()){
            test.actualizarBvp (params[1]);
        }else if(params[0] == EnumSensores.EDA.value()){
            test.actualizarEda (params[1]);
        }else if(params[0] == EnumSensores.ACELEROM.value()){
            test.actualizarMovimiento(params[1].intValue(),params[2].intValue(), params[3].intValue());
        }else if(params[0] == EnumSensores.TEMP.value()){
            test.actualizarTemperatura(params[1]);
        }else if(params[0] == EnumSensores.PULSAC.value() ){
            test.actualizarPulsaciones(params[1]);
        }
        return null;
    }

    public String obtenerIdMongo(String email){
        return test.obtenerIdMongo(email);
    }

    public long obtenerIdPaciente(String email){
        return test.obtenerIdPaciente(email);
    }

    public long insertandoPaciente(String email, String idMongo){ return test.insertandoPaciente(email,idMongo); }

    public boolean logueadoLocal(Logueado paciente){return test.logueadoLocal(paciente); }

    public Logueado obtenerLogueado(){  return test.obtenerLogueado(); }

    public boolean deslogueoLocal(){  return test.deslogueoLocal();  }

    public long  actualizarIdMongo(String email, String idMongo) {  return test.actualizarIdMongo( email,  idMongo); }

    public long  creandoSesion(){ return test.creandoSesion();}
    public void cerrandoSesion(){ test.cerrandoSesion();}

    public List<Sesion> SesionesLogueado(Date fechaFiltro){ return test.SesionesLogueado(fechaFiltro);   }

    public List<Entry> ObtenerDatosEdaSesion(long id_sesion){
        return test.ObtenerDatosEdaSesion(id_sesion);
    }

    public List<Entry> ObtenerDatosBvpSesion(long id_sesion){
        return test.ObtenerDatosBvpSesion(id_sesion);
    }

    public List<Entry> ObtenerDatosTemperaturaSesion(long id_sesion){
        return test.ObtenerDatosTemperaturaSesion(id_sesion);
    }

    public List<Entry> ObtenerDatosPulsacionesSesion(long id_sesion){
        return test.ObtenerDatosPulsacionesSesion(id_sesion);
    }

    public DataSetAcelerometro ObtenerDatosAcelerometro(long id_sesion){
        return test.ObtenerDatosAcelerometro(id_sesion);
    }
    //http://www.sgoliver.net/blog/tareas-en-segundo-plano-en-android-i-thread-y-asynctask/

    class SQLiteBackground extends SQLiteOpenHelper {

        private final long NO_HAY_SESION = -1;

        String createTable = "";
        Context context;
        String formatoFechaCompleto = "yyyy-MM-dd HH:mm:ss.SSS";
        String formatoSoloFecha = "yyyy-MM-dd";
        SimpleDateFormat formatedorFecha = new SimpleDateFormat(formatoFechaCompleto);
        SimpleDateFormat formatedorSoloFecha = new SimpleDateFormat(formatoSoloFecha);

        long idSesion = NO_HAY_SESION;

        public SQLiteBackground(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
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
            //creamos un usuario aunque sea sin id para poder seguir usando la aplicacion
            if (cuantos ==0){
                //insertandoPaciente("","");
            }
        }

        public String obtenerIdMongo(String email){
            String idMongo = null;

            SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
            String query = "SELECT idMongo FROM pacientes WHERE email LIKE \""+DatabaseUtils.sqlEscapeString(email)+"\"";
            Cursor cursor = sqLiteDatabase.rawQuery(query, null);
            int exito = cursor.getCount();
            if(exito !=0) {
                cursor.moveToFirst();
                idMongo = cursor.getString(cursor.getColumnIndex("idMongo"));
            }
            return idMongo;
        }

        public long obtenerIdPaciente(String email){
            long idPaciente = -1;
            SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
            String query = "SELECT id FROM pacientes WHERE email LIKE \""+DatabaseUtils.sqlEscapeString(email)+"\"";
            Cursor cursor = sqLiteDatabase.rawQuery(query, null);
            int exito = cursor.getCount();
            if(exito !=0) {
                cursor.moveToFirst();
                idPaciente = cursor.getLong(cursor.getColumnIndex("id"));
            }
            return idPaciente;
        }

        public long insertandoPaciente(String email, String idMongo) {

            SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("email",DatabaseUtils.sqlEscapeString(email));
            values.put("idMongo",idMongo);
            long idInsertado = -1;
            try{
                idInsertado = sqLiteDatabase.insert("pacientes","",values);

            }catch (SQLException ex){
                System.out.println("Error al insertar el id del usuario en la bd local");
            }
            return idInsertado;
        }

        public long actualizarIdMongo(String email, String idMongo) {
            SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
            //String query = "INSERT INTO pacientes (email, idMongo) VALUES ("+ DatabaseUtils.sqlEscapeString(email)+",'"+idMongo+"')";
            ContentValues values = new ContentValues();
            values.put("idMongo",idMongo);
            String[] args = new String[]{DatabaseUtils.sqlEscapeString(email)};
            long idActualizado = -1;
            try{
                idActualizado = sqLiteDatabase.update("pacientes",values,"email=? ",args);

            }catch (SQLException ex){
                System.out.println("Error al actualizar el idMongo del usuario en la bd local");
            }
            return idActualizado;
        }

        public boolean logueadoLocal(Logueado paciente) {
            SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
            //String query = "INSERT INTO pacientes (email, idMongo) VALUES ("+ DatabaseUtils.sqlEscapeString(email)+",'"+idMongo+"')";
            ContentValues values = new ContentValues();
            values.put("idLogueado",paciente.getIdPaciente());
            values.put("emailLogueado",DatabaseUtils.sqlEscapeString(paciente.getEmail()));
            values.put("temporal",(paciente.isTemporal())?1:0);
            try{
                sqLiteDatabase.delete("logeoActual","",new String[0]);
                sqLiteDatabase.insert("logeoActual","",values);

            }catch (SQLException ex){
                System.out.println("Error al insertar el usuario que se logueara en la bd local");
                return false;
            }
            return true;
        }

        public boolean deslogueoLocal() {
            SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

            try{
                sqLiteDatabase.delete("logeoActual","",new String[0]);


            }catch (SQLException ex){
                System.out.println("Error al borrar el usuario logueado localmente");
                return false;
            }
            return true;
        }

        public Logueado obtenerLogueado() {
            String emailLogueado = "";
            long idPaciente = -1;
            boolean temporal = true;
            String idMongo = "";

            SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
            String query = "SELECT * FROM logeoActual";
            Cursor cursor = sqLiteDatabase.rawQuery(query, null);
            int exito = cursor.getCount();
            if(exito !=0) {
                cursor.moveToFirst();
                idPaciente = cursor.getLong(cursor.getColumnIndex("idLogueado"));
                emailLogueado = cursor.getString(cursor.getColumnIndex("emailLogueado"));
                temporal = (0!= cursor.getInt(cursor.getColumnIndex("temporal")));
                idMongo = obtenerIdMongo(emailLogueado.replace("'",""));
            }
            return new Logueado(idPaciente,emailLogueado,temporal,idMongo);
        }

        public long creandoSesion(){
            Logueado usuarioActual = obtenerLogueado();

            //System.out.println("COMPROBANDO SESION");
            if(idSesion == NO_HAY_SESION) {

                //System.out.println("CREANDO SESION");
                if(usuarioActual.getIdPaciente()!=-1) {

                    SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

                    String fechaActual = formatedorFecha.format(new java.util.Date());

                    ContentValues values = new ContentValues();
                    values.put("fecInicio",fechaActual);
                    values.put("fecFin",fechaActual);
                    values.put("idPaciente",usuarioActual.getIdPaciente());

                    try{
                        idSesion = sqLiteDatabase.insert("sesiones","",values);
                        return idSesion;

                    }catch (SQLException ex){
                        System.out.println("Error al agnadir la sesion");
                        return -1;
                    }
                    //System.out.println("ID DE SESION CREADO: " + idSesion);
                }
            }
            return -1;
        }

        public void cerrandoSesion(){

            //fecha de finalizacion de sesion
            String fechaActual = formatedorFecha.format(new java.util.Date());

            SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("fecFin",fechaActual);
            String[] args = new String[]{String.valueOf(idSesion)};

            try{
                sqLiteDatabase.update("sesiones",values,"id=? ",args);

            }catch (SQLException ex){
                System.out.println("Error al actualizar el idMongo del usuario en la bd local");
            }


            idSesion = NO_HAY_SESION;
        }

        public List<Sesion> SesionesLogueado(Date fechaFiltro){
            List<Sesion> sesionesUsuario = new ArrayList<>();
            Logueado usuario = obtenerLogueado();


            if(usuario.getIdPaciente() !=-1){

                SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
                String query = "SELECT * FROM sesiones WHERE idPaciente = "+String.valueOf(usuario.getIdPaciente());

                if(fechaFiltro!=null){
                    Calendar diaSiguienteCalendar = Calendar.getInstance();
                    diaSiguienteCalendar.setTime(fechaFiltro);
                    diaSiguienteCalendar.add(Calendar.DATE, 1);
                    Date diaSiguienteFiltro = diaSiguienteCalendar.getTime();

                    query = query + " AND fecInicio > \""+formatedorSoloFecha.format(fechaFiltro)+"\"";
                    query = query + " AND fecInicio < \""+formatedorSoloFecha.format(diaSiguienteFiltro)+"\"";
                }
                Cursor cursor = sqLiteDatabase.rawQuery(query, null);
                int cuantos = cursor.getCount();
                if(cuantos !=0) {
                    cursor.moveToFirst();
                    for(int i=0; i< cuantos;i++) {

                        long idSesion = cursor.getLong(cursor.getColumnIndex("id"));
                        String strFechaInicio = cursor.getString(cursor.getColumnIndex("fecInicio"));
                        String strFechaFin = cursor.getString(cursor.getColumnIndex("fecFin"));

                        try {
                            sesionesUsuario.add(new Sesion(idSesion,formatedorFecha.parse(strFechaInicio),formatedorFecha.parse(strFechaFin),usuario.getIdPaciente()));
                        } catch (ParseException e) {
                            System.out.println("Error al parsear fecha sesion");
                        }
                        cursor.moveToNext();
                    }
                }
            }


            return sesionesUsuario;
        }

        public List<Entry> ObtenerDatosEdaSesion(long id_sesion){

            List<Entry> conjuntoEda = ObtenerDatosGenerico( id_sesion,"valorEda", "eda");
            return conjuntoEda;
        }

        public List<Entry> ObtenerDatosBvpSesion(long id_sesion){

            List<Entry> conjuntoBVP = ObtenerDatosGenerico( id_sesion,"valorBvp", "bvp");
            return conjuntoBVP;
        }

        public List<Entry> ObtenerDatosTemperaturaSesion(long id_sesion){

            List<Entry> conjuntoTemp = ObtenerDatosGenerico( id_sesion,"valorTemp", "temperatura");
            return conjuntoTemp;
        }

        public List<Entry> ObtenerDatosPulsacionesSesion(long id_sesion){

            List<Entry> conjuntoTemp = ObtenerDatosGenerico( id_sesion,"valorPulsaciones", "pulsaciones");
            return conjuntoTemp;
        }

        public DataSetAcelerometro ObtenerDatosAcelerometro(long id_sesion){

            DataSetAcelerometro conjuntoAcelerometro;

            List<Entry> acelerometroX = new ArrayList<>();
            List<Entry> acelerometroY = new ArrayList<>();
            List<Entry> acelerometroZ = new ArrayList<>();
            //podriamos hacer tres llamadas pero tardaría tres veces mas
            //conjuntoAcelerometro = ObtenerDatosGenerico( id_sesion,"valorX, valorY, valorZ", "movimiento");
            SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
            String query = "SELECT * FROM movimiento WHERE idSesion = "+String.valueOf(id_sesion);

            Cursor cursor = sqLiteDatabase.rawQuery(query, null);
            int cuantos = cursor.getCount();
            if(cuantos !=0) {
                cursor.moveToFirst();
                for(int i=0; i< cuantos;i++) {

                    float valorX = cursor.getFloat(cursor.getColumnIndex("valorX"));
                    float valorY = cursor.getFloat(cursor.getColumnIndex("valorY"));
                    float valorZ = cursor.getFloat(cursor.getColumnIndex("valorZ"));
                    String strFecha = cursor.getString(cursor.getColumnIndex("fecha"));

                    try {
                        acelerometroX.add(new Entry(horaToFloat(formatedorFecha.parse(strFecha)),valorX));
                        acelerometroY.add(new Entry(horaToFloat(formatedorFecha.parse(strFecha)),valorY));
                        acelerometroZ.add(new Entry(horaToFloat(formatedorFecha.parse(strFecha)),valorZ));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    cursor.moveToNext();
                }


            }

            return new DataSetAcelerometro(acelerometroX,acelerometroY,acelerometroZ);
        }

        public List<Entry> ObtenerDatosGenerico(long id_sesion,String campo, String tabla){

            List<Entry> conjuntoDatos = new ArrayList<>();

            SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
            String query = "SELECT * FROM "+tabla+" WHERE idSesion = "+String.valueOf(id_sesion);


            Cursor cursor = sqLiteDatabase.rawQuery(query, null);
            int cuantos = cursor.getCount();
            if(cuantos !=0) {
                cursor.moveToFirst();
                for(int i=0; i< cuantos;i++) {


                    float valor = cursor.getFloat(cursor.getColumnIndex(campo));
                    String strFecha = cursor.getString(cursor.getColumnIndex("fecha"));

                    try {
                        conjuntoDatos.add(new Entry(horaToFloat(formatedorFecha.parse(strFecha)),valor));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    cursor.moveToNext();
                }
            }
            return conjuntoDatos;
        }

        public float horaToFloat(Date hora){

            SimpleDateFormat formatoHora = new SimpleDateFormat("HHmmssSSS");
            String horaStr = formatoHora.format(hora.getTime());

            int horas = Integer.parseInt(horaStr.substring(0,2))*3600;
            int minutos =  Integer.parseInt(horaStr.substring(2,4))*60;
            int segundos =  Integer.parseInt(horaStr.substring(4,6));
            float milisegundos =  Float.parseFloat("0."+horaStr.substring(6,9));
            float segundosTodales = horas+ minutos +segundos+milisegundos;

            return segundosTodales;
        }

        public void actualizarBvp (float valor){
            SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
            String query = "INSERT INTO bvp (valorBvp,fecha,idSesion) VALUES ("+valor+",strftime('%Y-%m-%d %H:%M:%f', 'now')," + idSesion + ")";
            sqLiteDatabase.execSQL(query);
        }

        public void actualizarEda(float valor){
            SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
            String query = "INSERT INTO eda (valorEda,fecha,idSesion) VALUES ("+valor+",strftime('%Y-%m-%d %H:%M:%f', 'now')," + idSesion + ")";
            sqLiteDatabase.execSQL(query);
        }


        public void actualizarMovimiento(int valorX, int valorY, int valorZ){
            SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
            String query = "INSERT INTO movimiento (valorX,valorY,valorZ, fecha,idSesion) VALUES ("+valorX+","+valorY+","+valorZ+",strftime('%Y-%m-%d %H:%M:%f', 'now')," + idSesion + ")";
            sqLiteDatabase.execSQL(query);
        }

        public void actualizarTemperatura(float valorTemp){
            SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
            String query = "INSERT INTO temperatura (valorTemp, fecha,idSesion) VALUES ("+valorTemp+",strftime('%Y-%m-%d %H:%M:%f', 'now')," + idSesion + ")";
            sqLiteDatabase.execSQL(query);
        }


        public void actualizarPulsaciones(float valorPulsa){
            System.out.println("Inserta: "+valorPulsa);
            SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
            String query = "INSERT INTO pulsaciones (valorPulsaciones, fecha,idSesion) VALUES ("+valorPulsa+",strftime('%Y-%m-%d %H:%M:%f', 'now')," + idSesion + ")";
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
}
