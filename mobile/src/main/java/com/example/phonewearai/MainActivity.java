package com.example.phonewearai;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public class MainActivity extends AppCompatActivity implements MessageClient.OnMessageReceivedListener, LocationListener {


    private TextView heartView;
    private TextView tempView;
    private TextView latView;
    private TextView lngView;
    private TextView cityView;

    private LocationManager locationManager;
    private String provider;

    private float lat;
    private float lng;
    private String strLat;
    private String strLng;
    private String weathAPI = "c198627a1303756c85ea29900f1eaa7c";

    // Important CONSTANTS for JSON
    private static final String SENSOR_HEART_NAME = "Heartrate";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        heartView = findViewById(R.id.heart);
        tempView = findViewById(R.id.temp);
        latView = findViewById(R.id.lat);
        lngView = findViewById(R.id.lng);
        cityView = findViewById(R.id.city);


        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);

        // Checks for Request --> ask for permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        // Initialize the location fields
        Location location = locationManager.getLastKnownLocation(provider);

        if (location != null){
            Log.i("Provider", provider+" has been selected!");
            onLocationChanged(location);
            updateWeather();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Checks for Request --> ask for permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(provider, 400, 1, this);
        Wearable.getMessageClient(this).addListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(this);
        Wearable.getMessageClient(this).removeListener(this);
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        String msg = new String(messageEvent.getData());
        Log.i("Message received", msg);

        //https://www.py4u.net/discuss/1195822
        Map<String, String> Sensors = new Gson().fromJson(msg, new TypeToken<Map<String, String>>() {}.getType());

        String strHeartRate = Sensors.get(SENSOR_HEART_NAME);

        heartView.setText("Heartrate: "+strHeartRate);


    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        lat = (float) location.getLatitude();
        lng = (float) location.getLongitude();

        strLat = Float.toString(lat);
        strLng = Float.toString(lng);

        latView.setText("lat: "+strLat);
        lngView.setText("lng: "+strLng);

        updateWeather();

        Log.i("Location", "Location has been updated");
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        Toast.makeText(this, "Enabled new provider " + provider, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        Toast.makeText(this, "Disabled provider " + provider, Toast.LENGTH_SHORT).show();
    }

    private void updateWeather(){
        String weathURL = "https://api.openweathermap.org/data/2.5/weather?lat="+strLat+"&lon="+strLng+"&appid="+weathAPI;

        StringRequest stringRequest = new StringRequest(Request.Method.POST, weathURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    JSONArray jsonArray = jsonResponse.getJSONArray("weather");
                    JSONObject jsonObjectWeather = jsonArray.getJSONObject(0);
                    String description = jsonObjectWeather.getString("description");
                    JSONObject jsonObjectMain = jsonResponse.getJSONObject("main");
                    double temp = jsonObjectMain.getDouble("temp") - 273.15; // calvin temperature
                    double feelsLike = jsonObjectMain.getDouble("feels_like") - 273.15; // calvin temperature
                    int pressure = jsonObjectMain.getInt("pressure");
                    int humidity = jsonObjectMain.getInt("humidity");
                    JSONObject jsonObjectWind = jsonResponse.getJSONObject("wind");
                    String wind = jsonObjectWind.getString("speed");
                    JSONObject jsonObjectClouds = jsonResponse.getJSONObject("clouds");
                    String clouds = jsonObjectClouds.getString("all");
                    JSONObject jsonObjectSys = jsonResponse.getJSONObject("sys");
                    String countryName = jsonObjectSys.getString("country");
                    String cityName = jsonResponse.getString("name");

                    String strTemp = Double.toString(Math.round(temp));

                    tempView.setText("Temperature: "+strTemp);
                    cityView.setText("City: "+cityName);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                cityView.setText("Weather: API ERROR");
            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(stringRequest);
    }
}