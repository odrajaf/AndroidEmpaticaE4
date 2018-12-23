package com.example.fajardo.empaticatest;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.Task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

public class login extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener{


    SignInButton login;
    Button deslog;
    Button empezar;

    TextView direccionEmail;
    TextView nombre;
    Drawable imagenDeslogueado;

    ManejadorSQLAsincrono manejadorSQL;
    ManejadorJSONAsincrono manejadorJSON;

    GoogleApiClient googleApiClient;
    ImageView imagenCuenta;
    static final int REQ_CODE =9001;
    int reintentosConexion = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        manejadorSQL = new ManejadorSQLAsincrono(this);
        manejadorJSON = new ManejadorJSONAsincrono();

        login = (SignInButton) findViewById(R.id.sign_in_button);
        deslog = (Button) findViewById(R.id.sign_out_button);
        empezar =(Button) findViewById(R.id.buttonEmpezar);

        imagenCuenta = (ImageView) findViewById(R.id.img_profile_pic);
        direccionEmail = (TextView) findViewById(R.id.textViewEmail);
        nombre = (TextView) findViewById(R.id.textViewNombre);
        imagenDeslogueado = imagenCuenta.getDrawable();

        login.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {

                signIn();
            }
        });
        deslog.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {

                signOut();
                manejadorSQL.deslogueoLocal();
            }
        });

        empezar.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {

                Intent escanearBiometricas = new Intent(login.this, PanelDeGraficas.class);
                startActivity(escanearBiometricas);

            }
        });

        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        googleApiClient = new GoogleApiClient.Builder(this).enableAutoManage(this,this).addApi(Auth.GOOGLE_SIGN_IN_API,signInOptions).build();
        updateUI(false);

        //comprobamos si el usuario ya se ha logueado anteriormente
        Logueado logueado = manejadorSQL.obtenerLogueado();
        //si esta logueado pasamos a la pantalla principal
        if (!logueado.getEmail().isEmpty() && !logueado.isTemporal()) {
            Intent escanearBiometricas = new Intent(login.this, PanelDeGraficas.class);
            startActivity(escanearBiometricas);
        }
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        updateUI(false);
    }

    private void signIn(){
        Intent intent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(intent,REQ_CODE);
    }

    private void signOut(){
        Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
              updateUI(false);
            }
        });
    }

    private void handleResult (GoogleSignInResult result){
        if(result.isSuccess()){
            GoogleSignInAccount account = result.getSignInAccount();
            String name =  account.getDisplayName();
            String email = account.getEmail();
            //que no tenga foto de perfil o que tenga la de por defecto
            if(account.getPhotoUrl()!=null && !account.getPhotoUrl().toString().contains("plus.google.com")){
                System.out.println("URL FOTO: " +account.getPhotoUrl());
                String img_url = account.getPhotoUrl().toString();
                Glide.with(this).load(img_url).apply(RequestOptions.circleCropTransform()).into(imagenCuenta);
            }
            direccionEmail.setText(email);
            nombre.setText("Bienvenido "+name);
            updateUI(true);

            //comprobamos si el email está registrado en local
            String idMongo = manejadorSQL.obtenerIdMongo(email);

            if(idMongo == null || idMongo.isEmpty()){

                if(idMongo == null){//al ser nullo es que no hay usuario insertado
                    //creamos el usuario local (aun sin el idMongo)
                    manejadorSQL.insertandoPaciente(email, "");

                }

                // creamos un logueo temporal
                long idPaciente = manejadorSQL.obtenerIdPaciente(email) ;
                if(idPaciente!=-1) {
                    manejadorSQL.logueadoLocal(new Logueado(idPaciente, email,true));
                }

                //comprobamos si el email esta en la base de datos intermedia
                if(estaOnline()) {
                    new ComprobarEmailEnBDMongo().execute("GET", manejadorJSON.urlCollecUsuariosGet(), email);
                }else{
                    System.out.println("Servidor desconectado");
                }

            }else{
                System.out.println("Ya esta registrado en local");
                // obtenemos el id de paciente a partir del email
                 long idPaciente = manejadorSQL.obtenerIdPaciente(email) ;
                 if(idPaciente!=-1) {
                     manejadorSQL.logueadoLocal(new Logueado(idPaciente, email,false));
                 }
            }
        }else{
            updateUI(false);
        }
    }

    private void updateUI(boolean isLogin){
        if(isLogin){
            login.setVisibility(View.GONE);
            deslog.setVisibility(View.VISIBLE);
            nombre.setVisibility(View.VISIBLE);
            empezar.setVisibility(View.VISIBLE);
        }else{
            deslog.setVisibility(View.GONE);
            login.setVisibility(View.VISIBLE);
            nombre.setVisibility(View.GONE);
            empezar.setVisibility(View.GONE);
            direccionEmail.setText("Deslogueado");
            Glide.with(this).load(imagenDeslogueado).apply(RequestOptions.circleCropTransform()).into(imagenCuenta);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQ_CODE){
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleResult(result);
        }
    }

    public class ComprobarEmailEnBDMongo extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            try {
                return manejadorJSON.HttpGetRequest(urls[1],urls[2]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "";
        }

        @Override
        protected void onPostExecute(String result) {
            if(result.isEmpty()){
                if(reintentosConexion < 5) {
                    //el email no está en la base de datos intermedia y lo insertamos
                    new ManejadorJSONAsincrono().execute("POST", manejadorJSON.urlCollecUsuarios(), direccionEmail.getText().toString());

                    //obtenemos el id
                    new ComprobarEmailEnBDMongo().execute("GET", manejadorJSON.urlCollecUsuariosGet(), direccionEmail.getText().toString());

                    //en caso de no tener conexión lo reintenta hasta que lo consiga
                    reintentosConexion++;
                }
            }else{

                System.out.println("Ya esta registado");
                try {
                    JSONObject jsonObj = new JSONObject(result);
                    String idMongo = jsonObj.getString("_id");
                    String email = jsonObj.getString("email");
                    //guardamos el id en la BD
                    //habría que actualizar el idMongo ya que se insertado antes vacio
                    if(direccionEmail.getText().equals(email)) {
                        //agnadimos el idMongon del usuario
                        long idPaciente = manejadorSQL.actualizarIdMongo(email, idMongo);
                        //logueamos en local al usuario
                        if(idPaciente!=-1){
                            manejadorSQL.logueadoLocal(new Logueado(idPaciente,email,false));
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }
    }
    public boolean estaOnline(){
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected())
            return true;
        else
            return false;
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}
