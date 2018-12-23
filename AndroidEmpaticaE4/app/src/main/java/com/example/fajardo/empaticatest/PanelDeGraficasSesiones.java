package com.example.fajardo.empaticatest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class PanelDeGraficasSesiones extends AppCompatActivity {

    private LineChart mChartBvp;
    private LineChart mChartEDA;
    private LineChart mChartTemperatura;
    private LineChart mChartPulsaciones;
    private LineChart mChartAcelerometro;
    int idSesion = -1;
    private View layoutEspera;


    private Thread thread;
    private boolean plotData = true;
    GraficasPersistencia graficasPersis;
    final Handler handler = new Handler();
    ManejadorSQLAsincrono manejadorSQL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_panel_de_graficas);
        mChartBvp = (LineChart) findViewById(R.id.chartBvp);
        mChartEDA = (LineChart) findViewById(R.id.chartEda);
        mChartTemperatura = (LineChart) findViewById(R.id.chartTemperatura);
        mChartPulsaciones = (LineChart) findViewById(R.id.chartPulsaciones);
        mChartAcelerometro = (LineChart) findViewById(R.id.chartAcelerometro);

        manejadorSQL = new ManejadorSQLAsincrono(this);

        iniciandoGraficas(mChartBvp,-280,280);
        iniciandoGraficas(mChartEDA,0,5);
        iniciandoGraficas(mChartTemperatura,20,50);
        iniciandoGraficas(mChartPulsaciones,40,200);
        iniciandoGraficas(mChartAcelerometro,-100,100);
        layoutEspera = (View) findViewById(R.id.layoutEspera);
        layoutEspera.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                layoutEspera.setVisibility(View.GONE);
            }
        });



        feedMultiple();

        idSesion = getIntent().getIntExtra("idSesion",-1);

        if( idSesion != -1 ) {

            System.out.println("IdSesion obtenido: "+idSesion);
            //cargamos los valores en las graficas

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    cargarEdaDatos();

                }
            }, 0);



            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    cargarBvp();

                }
                }, 250);


            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            cargarTemperatura();
                        }
                    }, 500);
                }
            });

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            cargarPulsaciones();
                        }
                    }, 750);
                }
            });

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            cargarAcelerometro();
                        }
                    }, 1000);
                }
            });

        }

        mChartBvp.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {

            @Override
            public void onValueSelected(Entry e, Highlight h) {
                if (e == null)
                    return;
                Toast.makeText(getApplicationContext(), " used " + e.getX() , Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onNothingSelected() {

            }
        });
    }



    public void cargarEdaDatos(){
        //EDA
        List<Entry> datosEdaSesion = manejadorSQL.ObtenerDatosEdaSesion(idSesion);
        addEntry(datosEdaSesion, mChartEDA, EnumSensores.EDA.value());

    }

    public void cargarBvp(){
        //BVP
        List<Entry> datosBvpSesion = manejadorSQL.ObtenerDatosBvpSesion(idSesion);
        addEntry(datosBvpSesion, mChartBvp, EnumSensores.BVP.value());
    }
    public  void cargarTemperatura(){
        //temperatura
        List<Entry> datosTemperaturaSesion = manejadorSQL.ObtenerDatosTemperaturaSesion(idSesion);
        addEntry(datosTemperaturaSesion, mChartTemperatura, EnumSensores.TEMP.value());
    }

    public  void cargarPulsaciones(){
        //Pulsaciones
        List<Entry> datosPulsaciones = manejadorSQL.ObtenerDatosPulsacionesSesion(idSesion);
        if(datosPulsaciones.size() > 0)
            addEntry(datosPulsaciones, mChartPulsaciones, EnumSensores.PULSAC.value());
    }

    public void cargarAcelerometro(){
        //acelerometro
        DataSetAcelerometro datosAcelerometro = manejadorSQL.ObtenerDatosAcelerometro(idSesion);
        pintaGraficaAcelerometro(datosAcelerometro,mChartAcelerometro);
    }

    private void iniciandoGraficas(LineChart mChartIniciado,float minY, float maxY){

        // enable description text
        mChartIniciado.getDescription().setEnabled(false);

        // enable touch gestures
        mChartIniciado.setTouchEnabled(true);

        // enable scaling and dragging
        mChartIniciado.setDragEnabled(true);
        mChartIniciado.setScaleEnabled(true);
        mChartIniciado.setDrawGridBackground(false);

        // if disabled, scaling can be done on x- and y-axis separately
        mChartIniciado.setPinchZoom(true);

        // set an alternative background color
        mChartIniciado.setBackgroundColor(Color.WHITE);

        LineData data = new LineData();
        data.setValueTextColor(Color.BLACK);

        // add empty data
        mChartIniciado.setData(data);

        // get the legend (only possible after setting data)
        Legend lopciones = mChartIniciado.getLegend();

        // modify the legend ...
        lopciones.setForm(Legend.LegendForm.LINE);
        lopciones.setTextColor(Color.BLACK);

        XAxis xlIniciado = mChartIniciado.getXAxis();

        xlIniciado.setTextColor(Color.BLACK);
        xlIniciado.setDrawGridLines(true);
        xlIniciado.setAvoidFirstLastClipping(true);
        xlIniciado.setEnabled(true);

        DayAxisValueFormatter formatoMesEjex = new DayAxisValueFormatter();
        xlIniciado.setValueFormatter(formatoMesEjex);

        YAxis leftAxis = mChartIniciado.getAxisLeft();
        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setAxisMaximum(maxY);
        leftAxis.setAxisMinimum(minY);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = mChartIniciado.getAxisRight();
        rightAxis.setEnabled(false);

        mChartIniciado.getAxisLeft().setDrawGridLines(false);
        mChartIniciado.getXAxis().setDrawGridLines(false);
        mChartIniciado.setDrawBorders(false);
    }


    private void addEntry(List<Entry> valores ,LineChart mchart,float tipo) {

        LineData data = mchart.getData();

        if (data != null) {

            ILineDataSet set = data.getDataSetByIndex(0);
            // set.addEntry(...); // can be called as well

            if (set == null) {

                if(tipo == EnumSensores.BVP.value()){
                    set = createSetBvpciones(valores);
                }else if(tipo == EnumSensores.EDA.value()){
                    set = createSetEDA(valores);
                }else if(tipo == EnumSensores.TEMP.value()){
                    set = createSetTemp(valores);
                }else if(tipo == EnumSensores.PULSAC.value()){
                    set = createSetPulsaciones(valores);
                }
                data.addDataSet(set);
            }
            data.notifyDataChanged();

            // let the chart know it's data has changed
            mchart.notifyDataSetChanged();

            // limit the number of visible entries
            if(tipo == EnumSensores.BVP.value()){
                mchart.setVisibleXRange(1,10);
            }else if(tipo == EnumSensores.EDA.value()){
                mchart.setVisibleXRange(1,15);
            }else if(tipo == EnumSensores.ACELEROM.value()){
                mchart.setVisibleXRange(1,15);
            }else if(tipo == EnumSensores.TEMP.value()){
                mchart.setVisibleXRange(1,17);
            }else if(tipo == EnumSensores.PULSAC.value()){
                mchart.setVisibleXRange(1,10);
            }


            // move to the latest entry
            mchart.moveViewToX(data.getEntryCount());

        }
    }

    private void pintaGraficaAcelerometro(DataSetAcelerometro datosAcelerom ,LineChart mchart) {

        LineData data = mchart.getData();

        if (data != null) {

            ILineDataSet setAcel = data.getDataSetByIndex(0);

            if(setAcel ==null) {

                LineDataSet setX = new LineDataSet(datosAcelerom.getValoresEjeX(), "Eje X");
                setX.setColor(Color.BLUE);
                setX = comunSet(setX);


                LineDataSet setY = new LineDataSet(datosAcelerom.getValoresEjeY(), "Eje Y");
                setY.setColor(Color.RED);
                setY = comunSet(setY);

                LineDataSet setZ = new LineDataSet(datosAcelerom.getValoresEjeZ(), "Eje Z");
                setZ.setColor(Color.GREEN);
                setZ = comunSet(setZ);


                data.addDataSet(setX);
                data.addDataSet(setY);
                data.addDataSet(setZ);
            }
            data.notifyDataChanged();

            // let the chart know it's data has changed
            mchart.notifyDataSetChanged();

            // limit the number of visible entries
            mchart.setVisibleXRange(1,10);

            // move to the latest entry
            mchart.moveViewToX(data.getEntryCount());

        }
    }



    private void feedMultiple() {

        if (thread != null){
            thread.interrupt();
        }

        thread = new Thread(new Runnable() {

            @Override
            public void run() {
                while (true){
                    plotData = true;
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        });

        thread.start();
    }

    @Override
    public void onBackPressed() {

        Intent menuEmpatica = new Intent(PanelDeGraficasSesiones.this, MenuEmpatica.class);
        if (!isTaskRoot()) {
            menuEmpatica.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        }
        startActivity(menuEmpatica);
    }




    public LineDataSet createSetPulsaciones(List<Entry> valores) {

        LineDataSet set = new LineDataSet(valores, "Pulsaciones");
        set.setColor(Color.RED);
        set = comunSet(set);

        return set;
    }

    public LineDataSet createSetBvpciones(List<Entry> valores) {

        LineDataSet set = new LineDataSet(valores, "BVP");
        set.setColor(Color.RED);
        set = comunSet(set);

        return set;
    }

    public LineDataSet createSetTemp(List<Entry> valores) {

        LineDataSet set = new LineDataSet(valores, "Temperatura ºC");
        set.setColor(Color.rgb(255,100,100));
        set = comunSet(set);

        return set;
    }

    public LineDataSet createSetEDA(List<Entry> valores) {

        LineDataSet set = new LineDataSet(valores, "EDA \u00B5s");
        set.setColor(Color.CYAN);
        set.setFillColor(Color.CYAN);
        set = comunSet(set);

        return set;
    }

    public LineDataSet comunSet(LineDataSet set) {

        set.setLineWidth(2f);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);

        set.setHighlightEnabled(false);
        set.setDrawValues(false);
        set.setDrawCircles(false);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setCubicIntensity(0.2f);

        return set;
    }

}
