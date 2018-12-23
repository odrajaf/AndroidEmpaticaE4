package com.example.fajardo.empaticatest;

        import android.Manifest;
        import android.app.Activity;
        import android.bluetooth.BluetoothAdapter;
        import android.bluetooth.BluetoothDevice;
        import android.content.Context;
        import android.content.DialogInterface;
        import android.content.Intent;
        import android.content.pm.ActivityInfo;
        import android.content.pm.PackageManager;
        import android.database.sqlite.SQLiteDatabase;
        import android.graphics.Color;
        import android.graphics.drawable.ColorDrawable;
        import android.graphics.drawable.Drawable;
        import android.hardware.SensorEvent;
        import android.location.Location;
        import android.location.LocationListener;
        import android.location.LocationManager;
        import android.net.Uri;
        import android.os.AsyncTask;
        import android.os.Build;
        import android.os.Bundle;
        import android.os.Environment;
        import android.os.Handler;
        import android.provider.Settings;
        import android.support.annotation.NonNull;
        import android.support.v4.app.ActivityCompat;
        import android.support.v4.content.ContextCompat;
        import android.support.v4.content.LocalBroadcastManager;
        import android.support.v7.app.AlertDialog;
        import android.support.v7.app.AppCompatActivity;
        import android.text.Layout;
        import android.text.TextUtils;
        import android.util.Log;
        import android.view.View;
        import android.widget.Button;
        import android.widget.ImageView;
        import android.widget.ProgressBar;
        import android.widget.RelativeLayout;
        import android.widget.TextView;
        import android.widget.Toast;

        import com.bumptech.glide.Glide;
        import com.bumptech.glide.request.RequestOptions;
        import com.empatica.empalink.ConnectionNotAllowedException;
        import com.empatica.empalink.EmpaDeviceManager;
        import com.empatica.empalink.config.EmpaSensorStatus;
        import com.empatica.empalink.config.EmpaSensorType;
        import com.empatica.empalink.config.EmpaStatus;
        import com.empatica.empalink.delegate.EmpaDataDelegate;
        import com.empatica.empalink.delegate.EmpaStatusDelegate;

        import com.github.mikephil.charting.charts.LineChart;
        import com.github.mikephil.charting.components.Legend;
        import com.github.mikephil.charting.components.XAxis;
        import com.github.mikephil.charting.components.YAxis;
        import com.github.mikephil.charting.data.Entry;
        import com.github.mikephil.charting.data.LineData;
        import com.github.mikephil.charting.data.LineDataSet;
        import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
        import com.github.mikephil.charting.utils.ColorTemplate;

        import java.io.ByteArrayOutputStream;
        import java.io.File;
        import java.io.FileInputStream;
        import java.io.FileOutputStream;
        import java.io.IOException;
        import java.io.InputStream;
        import java.net.URL;
        import java.nio.channels.FileChannel;
        import java.text.DecimalFormat;
        import java.text.NumberFormat;
        import java.text.SimpleDateFormat;
        import java.util.Calendar;
        import java.util.Date;
        import java.util.GregorianCalendar;
        import java.util.Timer;
        import java.util.TimerTask;


public class MainActivity extends AppCompatActivity implements EmpaDataDelegate, EmpaStatusDelegate, View.OnClickListener, LocationListener {

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_PERMISSION_ACCESS_COARSE_LOCATION = 1;

    private static final long STREAMING_TIME = 3600000; // Stops streaming 10 seconds after connection

    private static final String EMPATICA_API_KEY = "0ce1a7d6d4b1451abdaeac41609f8ac0"; // TODO insert your API Key here

    private EmpaDeviceManager deviceManager = null;

    //SQLitePersistencia bdConector;
    //SQLiteDatabase bdValores;

    private TextView accel_xLabel;
    private TextView accel_yLabel;
    private TextView accel_zLabel;
    private TextView edaLabel;
    private TextView temperatureLabel;
    private TextView batteryLabel;
    private TextView statusLabel;
    private TextView deviceNameLabel;

    private TextView pulsacionesTR;
    private TextView pulsacionesTendencia;

    private Button sincronizarPulseraButton;
    BluetoothAdapter mBluetoothAdapter;
    private ImageView imgBotonGraficas;
    private TextView labelGrafica;
    private ImageView imgBotonEstadoBluetooth;
    private TextView labelBluetooth;
    private ImageView imgBotonPulsera;
    private TextView labePulsera;
    private ImageView imgBotonPerfil;
    private TextView labelPerfil;
    private View panelExplicativo;
    private View mainLayout;
    float valorAlphaOculto = 0.35f;
    boolean respuestaDetencion = false;
    String statusPulsera = "";
    boolean sesionActiva = false;
    private ImageView imgRelojDuracion;


    private long tiempPulsoInicio= -1;
    private long pulsoFin = -1;
    private long tiempPulsoInicio10s = -1;
    private long pulsoFin10s = -1;
    private int pulsa60Segundos = 0;
    private int pulsa10Sengundos = 0;
    private float mediaAproxPropia = 0;
    private float mediaPulsa = 0;
    private int pulsacionesContador = 0;
    private int pulsacinesConta10s =0;
    boolean comienzoPulsacion = false;
    private TextView labelDuracion;
    private int contadorSesion = 0;

    private float previaAccX = 0;
    private float previaAccY = 0;
    private float previaAccZ = 0;
    private float contadorAcc = 0;
    private float sumAcc = 0;
    private float fuerzaAcc = 1;


    private ProgressBar ejexAcelerometro;
    private ProgressBar ejeyAcelerometro;
    private ProgressBar ejezAcelerometro;

    long inicioAplicacion = -1; //controla si se guarda mi valor o el valor generado por el reloj
    boolean pasarDatosGraficas = true;


    ManejadorSQLAsincrono maneSQL;
    ManejadorJSONAsincrono manejadorJSON;

    GPSTracker gps;
    float ultimoEda = 0;
    float ultimaTemp = 0;
    double gpsLat = 0;
    double gpsAlt = 0;
    float ultimaPulsaciones = 0;
    Logueado usuarioActual;
    long idSecionActual = -1;
    int ultimaPulsacionesTendencia = 0;

    String formatoFechaCompleto = "yyyy-MM-dd HH:mm:ss.SSS";
    SimpleDateFormat formatedorFecha = new SimpleDateFormat(formatoFechaCompleto);

    public void calcularPulsacionesMinuto(float bvp){


         //System.out.println(bvp);
        if(tiempPulsoInicio10s == -1){
            tiempPulsoInicio10s =  System.currentTimeMillis();
        }
        //CONTROL DE TIEMPO, HACEMOS LOS CALCULOS DESPUES DE 6S DE DATOS
        if(tiempPulsoInicio== -1){
            tiempPulsoInicio= tiempPulsoInicio10s =  System.currentTimeMillis();
            //handler.postDelayed(runnable, 100);
        }else{
            pulsoFin = pulsoFin10s = System.currentTimeMillis();

            if((pulsoFin10s-tiempPulsoInicio10s) > 10000){
                pulsa10Sengundos = pulsacinesConta10s;
                pulsacinesConta10s = 0;
                if(pulsa60Segundos !=0 ) {
                    //habrá que modificar el factor dependiendo del número de ibi, necesitamos datos de referencia
                    //para obtener los factores
                    mediaAproxPropia = (pulsa10Sengundos * 6 + pulsa60Segundos) / 2;
                    //updateLabel(salidaDatos, salidaDatos.getText() + "Pulsaciones Aprox Propias(10s): " + mediaAproxPropia + "\n");

                    if(pulsa10Sengundos*6 > pulsa60Segundos){
                        updateLabel(pulsacionesTendencia,"↑");
                        ultimaPulsacionesTendencia = 1;

                    }else if(pulsa10Sengundos*6 == pulsa60Segundos){
                        updateLabel(pulsacionesTendencia," ");
                        ultimaPulsacionesTendencia = 0;

                    }else {
                        updateLabel(pulsacionesTendencia,"↓");
                        ultimaPulsacionesTendencia = -1;
                    }


                //en los primeros 60s de la aplicacion un pulso aproximado cada 10s
                }else if((tiempPulsoInicio - inicioAplicacion) < 60000 ){
                    updateLabel(pulsacionesTR, String.valueOf(pulsa10Sengundos*6)+"~ lat/min");
                }


                tiempPulsoInicio10s = -1;

            }

            if((pulsoFin-tiempPulsoInicio) > 60000){

                //CALCULAMOS PULSO APROXIMADO
                //updateLabel(salidaDatos,salidaDatos.getText()+"Pulsaciones por min(60s): "+pulsacionesContador+"\n");
                //enviamos peticion json
                //manejadorJSON.doInBackground(manejadorJSON.urlCollecDatosBiometricos());

                updateLabel(pulsacionesTR,String.format("%,.3f",(float) pulsacionesContador)+" lat/min");
                maneSQL.doInBackground(EnumSensores.PULSAC.value(),(float)pulsacionesContador);
                //para ver en tiempo en real
                Intent intent = new Intent(EnumSensores.PULSAC.toString());
                intent.putExtra("valor", (float) (float)pulsacionesContador);
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

                pulsa60Segundos = pulsacionesContador;
                pulsacionesContador = 0;
                tiempPulsoInicio= -1;


            }

        }

        if(bvp < 0){
            comienzoPulsacion = true;
        }
        if(comienzoPulsacion && bvp > 0){
            pulsacionesContador++;
            pulsacinesConta10s++;
            comienzoPulsacion = false;
        }


    }

    public void LocalizacionGPS(){

        // check if GPS enabled
        if(gps.canGetLocation()){
            gpsLat = gps.getLatitude();
            gpsAlt = gps.getLongitude();
        }else{
           //no está activo el GPS
            gps.showSettingsAlert();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        maneSQL =  new ManejadorSQLAsincrono(this);
        manejadorJSON = new ManejadorJSONAsincrono();
        gps = new GPSTracker(MainActivity.this);

        // Initialize vars that reference UI components
        statusLabel = (TextView) findViewById(R.id.status);
        accel_xLabel = (TextView) findViewById(R.id.accel_x);
        accel_yLabel = (TextView) findViewById(R.id.accel_y);
        accel_zLabel = (TextView) findViewById(R.id.accel_z);
        edaLabel = (TextView) findViewById(R.id.eda);
        temperatureLabel = (TextView) findViewById(R.id.temperature);
        batteryLabel = (TextView) findViewById(R.id.battery);
        deviceNameLabel = (TextView) findViewById(R.id.deviceName);
        pulsacionesTR = (TextView) findViewById(R.id.pulsaciones);
        pulsacionesTendencia  = (TextView) findViewById(R.id.pulsaTendencia);

        ejexAcelerometro = (ProgressBar)  findViewById(R.id.progressBarX);
        ejexAcelerometro.setMax(300);
        ejeyAcelerometro = (ProgressBar)  findViewById(R.id.progressBarY);
        ejeyAcelerometro.setMax(300);
        ejezAcelerometro = (ProgressBar)  findViewById(R.id.progressBarZ);
        ejezAcelerometro.setMax(300);


        imgBotonEstadoBluetooth = (ImageView) findViewById(R.id.imageViewEstadoBluetooth);
        labelBluetooth = (TextView) findViewById(R.id.textViewLabelBluetooth);
        imgBotonEstadoBluetooth.setOnClickListener(this);
        labelBluetooth.setOnClickListener(this);

        imgBotonPulsera = (ImageView) findViewById(R.id.imageViewPulseraE4);
        labePulsera = (TextView) findViewById(R.id.textViewLabelPulsera);
        labePulsera.setAlpha(valorAlphaOculto);
        imgBotonPulsera.setAlpha(valorAlphaOculto);
        imgBotonPulsera.setOnClickListener(this);
        labePulsera.setOnClickListener(this);

        sincronizarPulseraButton = (Button) findViewById(R.id.buttonSincronizar);
        sincronizarPulseraButton.setOnClickListener(this);

        labelGrafica  = (TextView) findViewById(R.id.textViewLabelGraficas);
        labelGrafica.setAlpha(valorAlphaOculto);
        labelGrafica.setOnClickListener(this);
        imgBotonGraficas = (ImageView)  findViewById(R.id.imageViewGraficas);
        imgBotonGraficas.setAlpha(valorAlphaOculto);
        imgBotonGraficas.setOnClickListener(this);

        panelExplicativo = (View) findViewById(R.id.layoutExplicativa);
        panelExplicativo.setVisibility(View.GONE);
        panelExplicativo.setOnClickListener(this);

        labelDuracion = (TextView) findViewById(R.id.textViewlabelReloj);
        imgRelojDuracion = (ImageView) findViewById(R.id.imageViewRelojSesion);
        labelDuracion.setVisibility(View.GONE);
        imgRelojDuracion.setVisibility(View.GONE);

        imgBotonPerfil = (ImageView) findViewById(R.id.imageViewPerfil);
        labelPerfil = (TextView) findViewById(R.id.textViewLabelPerfil);
        imgBotonPerfil.setOnClickListener(this);
        labelPerfil.setOnClickListener(this);

        mainLayout = (View) findViewById(R.id.mainLayout);


    }

    @Override
    public void onClick(View view) {

        if(view.getId() == R.id.textViewLabelPulsera || view.getId() == R.id.imageViewPulseraE4
                || view.getId() == R.id.buttonSincronizar ){

            if(statusPulsera == "CONNECTED"){
                mensajeAdvertirDetencion();
            }else{
                panelExplicativo.setVisibility(View.VISIBLE);
                initEmpaticaDeviceManager();
            }

        }else if(view.getId() == R.id.imageViewGraficas || view.getId() ==  R.id.textViewLabelGraficas){

            Intent panelesGraficas = new Intent(MainActivity.this, PanelDeGraficas.class);
            if (!isTaskRoot()) {
                panelesGraficas.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            }
            startActivityForResult(panelesGraficas,4);

        }else if(view.getId() == R.id.layoutExplicativa){
            panelExplicativo.setVisibility(View.GONE);

        }else if(view.getId() == R.id.imageViewEstadoBluetooth || view.getId() == R.id.textViewLabelBluetooth){

            if (mBluetoothAdapter.isEnabled()) {
                if(statusPulsera == "CONNECTED"){

                    if (mensajeAdvertirDetencion()){
                        mBluetoothAdapter.disable();
                    }
                }else{
                    mBluetoothAdapter.disable();
                }

            }else {
                mBluetoothAdapter.enable();
            }
            pseudocheckBluetooth();
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    checkBluetooth();
                }
            }, 3000);

        }else if(view.getId() == R.id.imageViewPerfil || view.getId() == R.id.textViewLabelPerfil){

            Intent menusPerfil = new Intent(MainActivity.this, MenuEmpatica.class);
            /*if (!isTaskRoot()) {
                menusGraficas.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            }*/
            startActivity(menusPerfil);
        }
    }


    private boolean mensajeAdvertirDetencion(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("");
        builder.setMessage("La pulsera ya está sincronizada ¿Desea parar la sincronización?")
                .setCancelable(false)
                .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        deviceManager.disconnect();
                        respuestaDetencion = true;
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        respuestaDetencion = false;
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        return respuestaDetencion;
    }

    private void pulseraConectada(){

        //CREAMOS LA SESION PORQUE YA HA ENLAZADO EL DISPOSITIVO
        usuarioActual =  maneSQL.obtenerLogueado();
        idSecionActual = maneSQL.creandoSesion();


        inicioAplicacion = System.currentTimeMillis();
        //Activamos los menu de la barra principal
        imgBotonGraficas.setAlpha(1f);
        labelGrafica.setAlpha(1f);
        imgBotonPulsera.setAlpha(1f);
        labePulsera.setAlpha(1f);

        sincronizarPulseraButton.setVisibility(View.GONE);
        panelExplicativo.setVisibility(View.GONE);

        labelDuracion.setVisibility(View.VISIBLE);
        imgRelojDuracion.setVisibility(View.VISIBLE);
        sesionActiva = true;
        contadorSesion();
        quantumEnvioJSON();
        envioDeSesion();


    }

    private void pulseraDesconectada(){


        //forzamos el reepintado de la aplicacion
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                //Activamos los menu de la barra principal
                imgBotonGraficas.setAlpha(valorAlphaOculto);
                labelGrafica.setAlpha(valorAlphaOculto);
                imgBotonPulsera.setAlpha(valorAlphaOculto);
                labePulsera.setAlpha(valorAlphaOculto);
                labelDuracion.setVisibility(View.GONE);
                imgRelojDuracion.setVisibility(View.GONE);
                contadorSesion = 0;
                sincronizarPulseraButton.setVisibility(View.VISIBLE);
            }
        });
        sesionActiva = false;
        idSecionActual = -1;
        maneSQL.cerrandoSesion();

        //limpiamos texto
        updateLabel(edaLabel, " - µS");
        updateLabel(temperatureLabel, " - ºC");
        updateLabel(batteryLabel," - ");
        updateLabel(deviceNameLabel,"");
        updateLabel(pulsacionesTR,"- lat/min");
        updateLabel(pulsacionesTendencia," ");

        ejexAcelerometro.setProgress(0);
        ejeyAcelerometro.setProgress(0);
        ejezAcelerometro.setProgress(0);
    }


    public void contadorSesion(){

        contadorSesion = contadorSesion + 1000;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        labelDuracion.setText(new SimpleDateFormat("mm:ss").format(new Date( contadorSesion)));
                        if(sesionActiva){
                            contadorSesion();
                        }
                    }
                }, 1000);
            }
        });
    }

    public void envioDeSesion(){

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        if(sesionActiva) {
                            String fechaActual = formatedorFecha.format(new java.util.Date());

                            String datosSesionEnviar = usuarioActual.getEmail().replaceAll("'", "") + ";"+
                                    String.valueOf(idSecionActual) + ";" +
                                    usuarioActual.getIdPaciente() + ";"+
                                    fechaActual + ";";

                            new ManejadorJSONAsincrono().execute("POST", manejadorJSON.urlCollecDatosSesions(), datosSesionEnviar);
                        }

                    }
                }, 1000);
            }
        });
    }

    public void quantumEnvioJSON(){

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(sesionActiva){
                            LocalizacionGPS();
                            if(idSecionActual != -1 && !usuarioActual.isTemporal()) {
                                ultimaPulsaciones = mediaPulsa;
                                if (mediaPulsa == 0) {
                                    ultimaPulsaciones = pulsa60Segundos;
                                }
                                if (pulsa60Segundos == 0) {
                                    ultimaPulsaciones = pulsa10Sengundos*6;
                                }



                                String fechaActual = formatedorFecha.format(new java.util.Date());


                                String cadenaEnvio = usuarioActual.getIdMongo() +";"+
                                        usuarioActual.getEmail().replace("'","") + ";"+
                                        idSecionActual + ";"+
                                        ultimaPulsaciones + ";"+
                                        ultimaPulsacionesTendencia + ";"+
                                        fuerzaAcc + ";"+
                                        gpsAlt+ ";" +
                                        gpsLat+ ";" +
                                        ultimoEda + ";"+
                                        ultimaTemp + ";"+
                                        fechaActual + ";";

                                new ManejadorJSONAsincrono().execute("POST", manejadorJSON.urlCollecDatosBiometricos(), cadenaEnvio);
                                quantumEnvioJSON();
                            }
                        }

                    }
                }, 10000);
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();

        checkBluetooth();
    }

    public void checkBluetooth(){

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter.isEnabled()) {
            imgBotonEstadoBluetooth.setAlpha(1f);
            labelBluetooth.setAlpha(1f);

        }else{
            imgBotonEstadoBluetooth.setAlpha(valorAlphaOculto);
            labelBluetooth.setAlpha(valorAlphaOculto);
        }
        mainLayout.invalidate();
        mainLayout.requestLayout();
    }

    public void pseudocheckBluetooth(){

        if (imgBotonEstadoBluetooth.getAlpha() > valorAlphaOculto) {
            imgBotonEstadoBluetooth.setAlpha(valorAlphaOculto);
            labelBluetooth.setAlpha(valorAlphaOculto);

        }else{
            imgBotonEstadoBluetooth.setAlpha(1f);
            labelBluetooth.setAlpha(1f);
        }
        mainLayout.invalidate();
        mainLayout.requestLayout();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_ACCESS_COARSE_LOCATION:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission was granted, yay!
                    initEmpaticaDeviceManager();
                } else {
                    // Permission denied, boo!
                    final boolean needRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION);
                    new AlertDialog.Builder(this)
                            .setTitle("Permission required")
                            .setMessage("Without this permission bluetooth low energy devices cannot be found, allow it in order to connect to the device.")
                            .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // try again
                                    if (needRationale) {
                                        // the "never ask again" flash is not set, try again with permission request
                                        initEmpaticaDeviceManager();
                                    } else {
                                        // the "never ask again" flag is set so the permission requests is disabled, try open app settings to enable the permission
                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                                        intent.setData(uri);
                                        startActivity(intent);
                                    }
                                }
                            })
                            .setNegativeButton("Exit application", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // without permission exit is the only way
                                    finish();
                                }
                            })
                            .show();
                }
                break;
        }
    }

    private void initEmpaticaDeviceManager() {
        // Android 6 (API level 23) now require ACCESS_COARSE_LOCATION permission to use BLE
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.ACCESS_COARSE_LOCATION }, REQUEST_PERMISSION_ACCESS_COARSE_LOCATION);
        } else {
            // Create a new EmpaDeviceManager. MainActivity is both its data and status delegate.
            deviceManager = new EmpaDeviceManager(getApplicationContext(), this, this);

            if (TextUtils.isEmpty(EMPATICA_API_KEY)) {
                new AlertDialog.Builder(this)
                        .setTitle("Warning")
                        .setMessage("Please insert your API KEY")
                        .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // without permission exit is the only way
                                finish();
                            }
                        })
                        .show();
                return;
            }
            // Initialize the Device Manager using your API key. You need to have Internet access at this point.
            deviceManager.authenticateWithAPIKey(EMPATICA_API_KEY);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (deviceManager != null) {
            deviceManager.stopScanning();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (deviceManager != null) {
            deviceManager.cleanUp();
        }
    }



    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }



    @Override
    public void didDiscoverDevice(BluetoothDevice bluetoothDevice, String deviceName, int rssi, boolean allowed) {
        // Check if the discovered device can be used with your API key. If allowed is always false,
        // the device is not linked with your API key. Please check your developer area at
        // https://www.empatica.com/connect/developer.php
        if (allowed) {
            // Stop scanning. The first allowed device will do.
            deviceManager.stopScanning();
            try {
                // Connect to the device
                deviceManager.connectDevice(bluetoothDevice);
                if(!deviceName.isEmpty()){
                    updateLabel(deviceNameLabel,deviceName.replace("Empatica",""));
                }
                //activamos los opciones del menu que corresponda
                pulseraConectada();

            } catch (ConnectionNotAllowedException e) {
                // This should happen only if you try to connect when allowed == false.
                Toast.makeText(MainActivity.this, "Sorry, you can't connect to this device", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void didRequestEnableBluetooth() {
        // Request the user to enable Bluetooth
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // The user chose not to enable Bluetooth
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            // You should deal with this
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void didUpdateSensorStatus(EmpaSensorStatus status, EmpaSensorType type) {
        // No need to implement this right now
    }

    @Override
    public void didUpdateStatus(EmpaStatus status) {
        // Update the UI
        updateLabel(statusLabel, status.name());
        statusPulsera = status.name();
        mostrarToas(status.name());


        // The device manager is ready for use
        if (status == EmpaStatus.READY) {
            updateLabel(statusLabel, status.name() + " - Turn on your device");
            mostrarToas(status.name() + " - Encienda su pulsera");
            statusPulsera = status.name();
            // Start scanning
            deviceManager.startScanning();
            // The device manager has established a connection
        } else if (status == EmpaStatus.CONNECTED) {
            // Stop streaming after STREAMING_TIME
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // Disconnect device
                            deviceManager.disconnect();
                        }
                    }, STREAMING_TIME);
                }
            });
            // The device manager disconnected from a device
        } else if (status == EmpaStatus.DISCONNECTED) {
            updateLabel(deviceNameLabel, "");
            //actualizamos los botones del menu
            pulseraDesconectada();
        }
    }

    @Override
    public void didReceiveAcceleration(int x, int y, int z, double timestamp) {
        /*updateLabel(accel_xLabel, "" + x);
        updateLabel(accel_yLabel, "" + y);
        updateLabel(accel_zLabel, "" + z);*/
        ejexAcelerometro.setProgress(x+150);
        ejeyAcelerometro.setProgress(y+150);
        ejezAcelerometro.setProgress(z+150);

        contadorAcc++;
        //sum+= max3(abs(buffX[i] - prevX), abs(buffY[i] - prevY), abs(buffZ[i] - prevZ));
        float absolutoX  = Math.abs(x - previaAccX);
        previaAccX = x;
        float absolutoY = Math.abs(y - previaAccY);
        previaAccY = y;
        float absolutoZ = Math.abs(z - previaAccZ);
        previaAccZ = z;
        sumAcc=  sumAcc +Math.max(Math.max(absolutoX, absolutoY), absolutoZ);
        //System.out.println("movimientoX: "+x);
        if(contadorAcc > 31){//cada 30s se actualiza
            float valor = sumAcc/32;
            fuerzaAcc= (float) (fuerzaAcc*0.9+(sumAcc/32)*0.1);
            contadorAcc= 0;
            sumAcc=0;
            fuerzaAcc = (valor/128 * 4) - 2;
        }


        //bdConector.actualizarMovimiento(x, y, z);
        //System.out.println(x +","+ y+","+ z);
        maneSQL.doInBackground(EnumSensores.ACELEROM.value(),(float)x,(float)y,(float)z);
        /*Intent intent = new Intent(EnumSensores.ACELEROM.toString());
        intent.putExtra("valoresAcelerometro", new float[]{x,y,z});

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);*/
    }

    @Override
    public void didReceiveBVP(float bvp, double timestamp) {

        calcularPulsacionesMinuto(bvp);
        maneSQL.doInBackground(EnumSensores.BVP.value(),bvp);
        if(pasarDatosGraficas){
            //addEntry(bvp,EnumSensores.BVP.value());

            //Log.d("sender", "Broadcasting message " +EnumSensores.BVP.toString());
            Intent intent = new Intent(EnumSensores.BVP.toString());
            // You can also include some extra data.
            intent.putExtra("valor", bvp);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        }

    }


    @Override
    public void didReceiveBatteryLevel(float battery, double timestamp) {
        updateLabel(batteryLabel, String.format("%.0f %%", battery * 100));
    }

    @Override
    public void didReceiveGSR(float gsr, double timestamp) {
        updateLabel(edaLabel, String.format("%,.3f", gsr)+" µS");
        ultimoEda = gsr;

        //new ManejadorSQLAsincrono(getApplicationContext()).execute(EnumSensores.EDA.value(),gsr);
        maneSQL.doInBackground(EnumSensores.EDA.value(),gsr);
        if(pasarDatosGraficas){
            //Log.d("sender", "Broadcasting message " +EnumSensores.EDA.toString());
            Intent intent = new Intent(EnumSensores.EDA.toString());
            // You can also include some extra data.
            intent.putExtra("valor", gsr);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        }
    }


    @Override
    public void didReceiveIBI(float ibi, double timestamp) {

        //calcularemos una media del último minuto, dandoce el doble de peso a la medida mas reciente
        if(pulsa60Segundos !=0) {
            if(mediaPulsa == 0){
                mediaPulsa = pulsa60Segundos;
            }

            mediaPulsa = (float) ((mediaPulsa*1 + pulsa60Segundos*1 + (1/ibi*60)*1.5f))/3.5f;
            System.out.println("Pulsaciones Aprox IBI: " + mediaPulsa);

            //updateLabel(salidaDatos,salidaDatos.getText()+"Pulsaciones Aprox (con IBI): " + mediaPulsa+"\n");
            updateLabel(pulsacionesTR,String.format("%,.3f", (float) mediaPulsa)+" lat/min");
            maneSQL.doInBackground(EnumSensores.PULSAC.value(),mediaPulsa);

            //para ver en tiempo en real
            Intent intent = new Intent(EnumSensores.PULSAC.toString());
            intent.putExtra("valor", (float) mediaPulsa);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }

    }

    @Override
    public void didReceiveTemperature(float temp, double timestamp) {
        updateLabel(temperatureLabel, String.format("%,.3f", temp)+" ºC");
        ultimaTemp = temp;
        //bdConector.actualizarTemperatura(temp);
        maneSQL.doInBackground(EnumSensores.TEMP.value(),temp);

        Intent intent = new Intent(EnumSensores.TEMP.toString());
        // You can also include some extra data.
        intent.putExtra("valor", temp);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

    }

    // Update a label with some text, making sure this is run in the UI thread
    private void updateLabel(final TextView label, final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                label.setText(text);
            }
        });
    }

    private void mostrarToas( final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

}
