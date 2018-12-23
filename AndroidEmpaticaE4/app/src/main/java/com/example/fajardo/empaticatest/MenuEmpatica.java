package com.example.fajardo.empaticatest;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.Event;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MenuEmpatica extends AppCompatActivity implements  View.OnClickListener  {

    Button botonVolarDB;
    Button botonDesloguear;
    ManejadorSQLAsincrono manejadorSQL;
    TextView indicadorMes;
    LinearLayout panelBotones;

    CompactCalendarView compactCalendar;

    private SimpleDateFormat dateFormatMonth = new SimpleDateFormat("MMMM - yyyy", Locale.getDefault());
    SimpleDateFormat formatoHora = new SimpleDateFormat("HH:mm:ss",Locale.getDefault());


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_empatica);
        manejadorSQL = new ManejadorSQLAsincrono(this);

        indicadorMes = (TextView) findViewById(R.id.textViewMes);
        indicadorMes.setText("   Sesiones en "+dateFormatMonth.format(new Date()));


        compactCalendar = (CompactCalendarView) findViewById(R.id.compactcalendar);
        compactCalendar.setUseThreeLetterAbbreviation(true);
        //insertamos los botones en el calendario
        List<Sesion> sesiones =  manejadorSQL.SesionesLogueado(null);
        panelBotones = (LinearLayout) findViewById(R.id.layoutBotonesSesiones);

        for(int i =0; i < sesiones.size();i++){

            Event ev1 = new Event(Color.RED, sesiones.get(i).getFechaInicio().getTime(), String.valueOf(sesiones.get(i).getFechaInicio()));
            compactCalendar.addEvent(ev1);
        }
        //mostramos las sesiones del dia de hoy
        mostrarBotonesSesiones(new Date());

        compactCalendar.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked) {
                mostrarBotonesSesiones(dateClicked);
            }

            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
                indicadorMes.setText("   Sesiones en "+dateFormatMonth.format(firstDayOfNewMonth));
            }
        });





        //comprobamos si el usuario ya se ha logueado anteriormente
        Logueado logueado = manejadorSQL.obtenerLogueado();
        if(!logueado.getEmail().isEmpty()){

            this.setTitle(logueado.getEmail().replace('\'', Character.MIN_VALUE));
        }

        botonVolarDB = (Button) findViewById(R.id.buttonVolcarBD);
        botonVolarDB.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {

                //voldando base de datos
                try {
                    File sd = Environment.getExternalStorageDirectory();

                    if (sd.canWrite()) {
                        String currentDBPath = "/data/data/" + getPackageName() + "/databases/bdValores";
                        String backupDBPath = "backupname.db";
                        File currentDB = new File(currentDBPath);
                        File backupDB = new File(sd, backupDBPath);

                        if (currentDB.exists()) {
                            FileChannel src = new FileInputStream(currentDB).getChannel();
                            FileChannel dst = new FileOutputStream(backupDB).getChannel();
                            dst.transferFrom(src, 0, src.size());
                            src.close();
                            dst.close();
                        }
                    }
                    System.out.println("volcado completado");
                    Toast.makeText(getApplicationContext(), "Volcado completado a la memoria del dispositivo", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    System.out.println("Error en el volcado");
                }

            }
        });

        botonDesloguear = (Button) findViewById(R.id.buttonDeslogueo);
        botonDesloguear.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                manejadorSQL.deslogueoLocal();

                Intent pantallaLogin = new Intent(MenuEmpatica.this, login.class);
                startActivity(pantallaLogin);
            }
        });

    }

    public void mostrarBotonesSesiones(Date fechaDia){
        //limpiamos el panel
        panelBotones.removeAllViews();
        Context context = getApplicationContext();
        getViewModelStore();

        //obtenemos las sesiones de ese dia clicado
        List<Sesion> sesiones =  manejadorSQL.SesionesLogueado(fechaDia);
        TableRow.LayoutParams tr = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT);

        //se obtendría del sql
        if (sesiones.size()>0) {
            for(int i =0; i < sesiones.size();i++){

                Button myButton = new Button(context);
                myButton.setOnClickListener(MenuEmpatica.this);
                myButton.setAllCaps(false);

                myButton.setText(formatoHora.format(sesiones.get(i).getFechaInicio())
                        + ", Duración: " +sesiones.get(i).duracionSesion()+"s");
                myButton.setId((int)sesiones.get(i).getIdSesion());
                panelBotones.addView(myButton, tr);
            }

        }else {
            Toast.makeText(context, "No hay sesiones este día", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View view) {

        Intent menusPerfil = new Intent(MenuEmpatica.this, PanelDeGraficasSesiones.class);
        menusPerfil.putExtra("idSesion",view.getId());
        startActivity(menusPerfil);
    }

    @Override
    public void onBackPressed() {
        Intent menusPerfil = new Intent(MenuEmpatica.this, MainActivity.class);
        if (!isTaskRoot()) {
            menusPerfil.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        }
        startActivity(menusPerfil);
    }
}
