package com.example.fajardo.empaticatest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.opengl.Visibility;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

public class PanelDeGraficas extends AppCompatActivity {

    private LineChart mChartBvp;
    private LineChart mChartEDA;
    private LineChart mChartTemperatura;
    private LineChart mChartPulsaciones;
    private LineChart mChartAcelerometro;
    private View layoutEspera;

    TextView tituloAcelerometro;

    private Thread thread;
    private boolean plotData = true;

    final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_panel_de_graficas);

        mChartBvp = (LineChart) findViewById(R.id.chartBvp);
        mChartEDA = (LineChart) findViewById(R.id.chartEda);
        mChartTemperatura = (LineChart) findViewById(R.id.chartTemperatura);
        mChartPulsaciones = (LineChart) findViewById(R.id.chartPulsaciones);
        mChartAcelerometro = (LineChart) findViewById(R.id.chartAcelerometro);

        iniciandoGraficas(mChartBvp,-280,280);
        iniciandoGraficas(mChartEDA,0,5);
        iniciandoGraficas(mChartTemperatura,20,50);
        iniciandoGraficas(mChartPulsaciones,40,200);
        //iniciandoGraficas(mChartAcelerometro,-100,100);

        layoutEspera = (View) findViewById(R.id.layoutEspera);
        layoutEspera.setVisibility(View.GONE);

        mChartAcelerometro.setVisibility(View.GONE);
        tituloAcelerometro = (TextView) findViewById(R.id.textViewAcelerometro);
        tituloAcelerometro.setVisibility(View.GONE);


        feedMultiple();


        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter(EnumSensores.BVP.toString()));

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter(EnumSensores.EDA.toString()));

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter(EnumSensores.TEMP.toString()));

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter(EnumSensores.PULSAC.toString()));

        /*LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter(EnumSensores.ACELEROM.toString()));*/


        //login llama esta activitiy y esta a su vez a la de mainActivity
        //para dejar el activity de las graficas ya cargado
        int idSesion = getIntent().getIntExtra("idSesion",-1);

        if( idSesion == -1 ) {

            Intent escanearBiometricas = new Intent(PanelDeGraficas.this, MainActivity.class);
            if (!isTaskRoot()) {  //MainActivity to the top
                escanearBiometricas.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

            }
            startActivity(escanearBiometricas);
        }

    }


    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if(intent.getAction() == EnumSensores.BVP.toString()) {
                // Get extra data included in the Intent
                float message = intent.getFloatExtra("valor", 0);
                //Log.d("receiver", "Got message: " + message);
                addEntry(message, mChartBvp, EnumSensores.BVP.value());
            }else if (intent.getAction() == EnumSensores.EDA.toString()){
                float message = intent.getFloatExtra("valor", 0);
               // Log.d("receiver", "Got message: " + message);
                addEntry(message, mChartEDA, EnumSensores.EDA.value());

            }else if (intent.getAction() == EnumSensores.TEMP.toString()){
                float message = intent.getFloatExtra("valor", 0);
                // Log.d("receiver", "Got message: " + message);
                addEntry(message, mChartTemperatura, EnumSensores.TEMP.value());

            }else if (intent.getAction() == EnumSensores.PULSAC.toString()){
                float message = intent.getFloatExtra("valor", 0);
                // Log.d("receiver", "Got message: " + message);
                addEntry(message, mChartPulsaciones, EnumSensores.PULSAC.value());
            }/*else if (intent.getAction() == EnumSensores.ACELEROM.toString()){
                float valores[] = intent.getFloatArrayExtra("valoresAcelerometro");

                if(valores.length==3){
                    //pintaGraficaAcelerometro(valores,mChartAcelerometro);
                }


            }*/
        }
    };

    @Override
    protected void onDestroy() {
        // Unregister since the activity is about to be closed.
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onDestroy();
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

        //DayAxisValueFormatter formatoMesEjex = new DayAxisValueFormatter();
        //xlIniciado.setValueFormatter(formatoMesEjex);

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


    private void addEntry(float valor ,LineChart mchart,float tipo) {

        LineData data = mchart.getData();

        if (data != null) {

            ILineDataSet set = data.getDataSetByIndex(0);
            // set.addEntry(...); // can be called as well

            if (set == null) {

                if(tipo == EnumSensores.BVP.value()){
                    set = createSetBvp();
                }else if(tipo == EnumSensores.EDA.value()){
                    set = createSetEDA();
                }else if(tipo == EnumSensores.PULSAC.value()){
                    set = createSetPulsaciones();
                }else if(tipo == EnumSensores.TEMP.value()){
                    set = createSetTemp();
                }
                data.addDataSet(set);
            }

            //data.addEntry(new Entry(set.getEntryCount(), (float) (Math.random() * 80) + 10f), 0);
            data.addEntry(new Entry(set.getEntryCount(), valor), 0);
            data.notifyDataChanged();

            // let the chart know it's data has changed
            mchart.notifyDataSetChanged();

            // limit the number of visible entries
            mchart.setVisibleXRangeMaximum(150);
            // mChartPulsaciones.setVisibleYRange(30, AxisDependency.LEFT);

            // move to the latest entry
            mchart.moveViewToX(data.getEntryCount());

        }
    }

    private void pintaGraficaAcelerometro(float valores[] ,LineChart mchart) {

        LineData data = mchart.getData();

        if (data != null) {

            ILineDataSet setAcel = data.getDataSetByIndex(0);

            if(setAcel ==null) {

                LineDataSet setX = new LineDataSet(null, "Eje X");
                setX.setColor(Color.BLUE);
                setX = comunSet(setX);


                LineDataSet setY = new LineDataSet(null, "Eje Y");
                setY.setColor(Color.RED);
                setY = comunSet(setY);

                LineDataSet setZ = new LineDataSet(null, "Eje Z");
                setZ.setColor(Color.GREEN);
                setZ = comunSet(setZ);


                data.addDataSet(setX);
                data.addDataSet(setY);
                data.addDataSet(setZ);
                data.addEntry(new Entry(setX.getEntryCount(), valores[0]), 0);
                data.addEntry(new Entry(setY.getEntryCount(), valores[1]), 0);
                data.addEntry(new Entry(setZ.getEntryCount(), valores[2]), 0);
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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==4){

            float valor = data.getFloatExtra("name",0);
            addEntry( valor,mChartPulsaciones,EnumSensores.BVP.value());

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

        //moveTaskToBack(true);

        if (isTaskRoot()) {

            Intent retornoDatosCaptados = new Intent(PanelDeGraficas.this, MainActivity.class);
            startActivity(retornoDatosCaptados);

        } else {

            Intent retornoDatosCaptados = new Intent(PanelDeGraficas.this, MainActivity.class);
            retornoDatosCaptados.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(retornoDatosCaptados);

        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        //super.onConfigurationChanged(newConfig);


    }

    public LineDataSet createSetTemp() {

        LineDataSet set = new LineDataSet(null, "Temperatura ºC");
        set.setColor(Color.rgb(255,100,100));
        set = comunSet(set);

        return set;
    }

    public LineDataSet createSetBvp() {

        LineDataSet set = new LineDataSet(null, "Bvp");
        set.setColor(Color.RED);
        set = comunSet(set);

        return set;
    }

    public LineDataSet createSetPulsaciones() {

        LineDataSet set = new LineDataSet(null, "Pulsaciones");
        set.setColor(Color.RED);
        set = comunSet(set);

        return set;
    }

    public LineDataSet createSetEDA() {

        LineDataSet set = new LineDataSet(null, "EDA");
        set.setColor(Color.CYAN);
        set.setFillColor(Color.CYAN);
        set = comunSet(set);

        return set;
    }

    public LineDataSet comunSet(LineDataSet set) {

        set.setLineWidth(3f);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setHighlightEnabled(false);
        set.setDrawValues(false);
        set.setDrawCircles(false);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setCubicIntensity(0.2f);

        return set;
    }

}
