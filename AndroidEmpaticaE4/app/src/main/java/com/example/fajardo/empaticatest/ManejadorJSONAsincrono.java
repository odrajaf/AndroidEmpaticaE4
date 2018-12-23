package com.example.fajardo.empaticatest;

import android.app.Activity;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;


public class ManejadorJSONAsincrono extends AsyncTask<String, Void, String> {

    static String tokenPass = "password123456";
    //static String direccionIp = "http://192.168.0.28";
    static String direccionIp = "http://192.168.43.240";
    //static String direccionIp = "http://192.168.1.54";

    @Override
    protected String doInBackground(String... urls) {
            // params comes from the execute() call: params[0] is the url.

        try {
                if(urls[0] == "GET" ){
                    return HttpGetRequest(urls[1],urls[2]);
                }else{
                    return HttpPost(urls[1],urls[2]);
                }

        } catch (JSONException e) {
                //e.printStackTrace();
                return "Error al parsear el JSON";
        }catch (IOException e) {
                //e.printStackTrace();
                return "Direccion web no disponible";
        }
    }

    public String HttpGetRequest( String url, String dato)throws IOException {

        String urlString = "";

        if(url.equals(urlCollecUsuariosGet())){
            urlString = urlCollecUsuariosGet() +"?email="+URLEncoder.encode(dato, "utf-8");
        }else{
           //seguramente para obtener datos de la bd
        }
        System.out.println(urlString);

        InputStream inputStream = null;
        String result = "";
        try {

            // create HttpClient
            HttpClient httpclient = new DefaultHttpClient();
            HttpGet getRequest = new HttpGet(urlString);
            getRequest.setHeader("token", tokenPass);

            // make GET request to the given URL
            HttpResponse httpResponse = httpclient.execute(getRequest);

            // receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();

            // convert inputstream to string
            if(inputStream != null)
                result = convertInputStreamToString(inputStream);
            else
                result = "Did not work!";

        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }

        return result;
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }


    // onPostExecute displays the results of the AsyncTask.
    @Override
    protected void onPostExecute(String result) {
        System.out.println(result);
    }


    //metodos estaticos para las diferentes colecciones
    public String urlCollecDatosBiometricos(){
        return direccionIp+":8080/api/datosBiometricos";
    }

    public String urlCollecDatosSesions(){
        return direccionIp+":8080/api/sesionesUsuarios";
    }

    public String urlCollecUsuarios(){
        return direccionIp+":8080/api/usuariosEmpatica";
    }

    public String urlCollecUsuariosGet(){
        return direccionIp+":8080/api/usuariosEmpatica/email";
    }




    public String HttpPost(String myUrl, String datos) throws IOException, JSONException {
        String result = "";

        URL url = new URL(myUrl);

        // 1. create HttpURLConnection
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        conn.setRequestProperty("token", tokenPass);

        JSONObject jsonObject;
        // 2. build JSON object
        if( urlCollecDatosBiometricos().compareTo(myUrl) ==0){
            jsonObject = buidJsonObjectDatos(datos);

        }else if(urlCollecDatosSesions().compareTo(myUrl) ==0){
            jsonObject = buidJsonObjectSesiones(datos);
        }else{
            jsonObject = buidJsonObjectUsuario(datos);
        }

        // 3. add JSON content to POST request body
        setPostRequestContent(conn, jsonObject);

        // 4. make POST request to the given URL
        conn.connect();

        // 5. return response message
        return conn.getResponseMessage()+"";

    }

    private JSONObject buidJsonObjectDatos(String datos) throws JSONException {

        String datosArray[] = datos.split(";");
        JSONObject jsonLocalizacion = new JSONObject();
        jsonLocalizacion.accumulate("pais","");
        jsonLocalizacion.accumulate("direccion","");

        JSONObject jsonObject = new JSONObject();
        jsonObject.accumulate("idPaciente", datosArray[0]);
        jsonObject.accumulate("email",  datosArray[1]);
        jsonObject.accumulate("idSesion", datosArray[2]);
        jsonObject.accumulate("pulsaciones", datosArray[3]);
        jsonObject.accumulate("tendenciaPulsaciones", datosArray[4]);
        jsonObject.accumulate("movimientoAcc", datosArray[5]);

        JSONObject jsonCoordenada = new JSONObject();

        jsonCoordenada.accumulate("longitude", Double.parseDouble(datosArray[6]));
        jsonCoordenada.accumulate("latitude", Double.parseDouble(datosArray[7]));

        jsonLocalizacion.accumulate("coordenada",jsonCoordenada);

        jsonObject.accumulate("localizacion",jsonLocalizacion);

        jsonObject.accumulate("eda", datosArray[8]);
        jsonObject.accumulate("temperatura", datosArray[9]);
        jsonObject.accumulate("fecha", datosArray[10]);
        System.out.println(jsonObject.toString());
        return jsonObject;
    }

    private JSONObject buidJsonObjectSesiones(String datos) throws JSONException {

        JSONObject jsonObject = new JSONObject();
        String datosArray[] = datos.split(";");
        jsonObject.accumulate("email",  datosArray[0]);
        jsonObject.accumulate("idSesion", datosArray[1]);
        jsonObject.accumulate("idPaciente", datosArray[2]);
        jsonObject.accumulate("fecha", datosArray[3]);
        return jsonObject;
    }

    private JSONObject buidJsonObjectUsuario(String datos) throws JSONException {

        JSONObject jsonObject = new JSONObject();
        jsonObject.accumulate("email", datos);
        return jsonObject;
    }

    private void setPostRequestContent(HttpURLConnection conn, JSONObject jsonObject) throws IOException {

        OutputStream os = conn.getOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
        writer.write(jsonObject.toString());
        Log.i(MainActivity.class.toString(), jsonObject.toString());
        writer.flush();
        writer.close();
        os.close();
    }


}




