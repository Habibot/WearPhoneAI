package com.example.phonewearai;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements MessageClient.OnMessageReceivedListener, LocationListener {

    private TextView heartView;
    private TextView tempView;
    private TextView latView;
    private TextView lngView;
    private TextView cityView;
    private TextView eleView;
    private TextView stepView;
    private TextView deviceView;
    private Spinner spinView;
    private ImageButton refView;
    private TextView cadenceView;
    private TextView speedView;

    private List<String> allDevices = new ArrayList<>();

    private LocationManager locationManager;
    private String provider;

    private String strLat;
    private String strLng;
    private String strHeartRate;
    private String strStepCounter = "0";
    private String strOldStepCounter = "0";
    private String strSpeed;
    private int cadence;

    private boolean isHandler = true;

    // Important CONSTANTS for JSON
    private static final String SENSOR_HEART_NAME = "Heartrate";
    private static final String SENSOR_STEP_NAME = "Stepcounter";

    private static final int REQUEST_LOCATION_PERMISSION = 1;

    private final ArrayList<Integer> heartList = new ArrayList<>();
    private final ArrayList<Integer> cadenceList = new ArrayList<>();
    private final ArrayList<Integer> stepsList = new ArrayList<>();
    private final ArrayList<Float> speedList = new ArrayList<Float>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();


        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Checks for Request --> ask for permission
        PermissionChecker.checkPermission(getApplicationContext(),this);

        // Initialize the location fields
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);

        Location location = locationManager.getLastKnownLocation(provider);
        if (location != null){
            Log.i("Provider", provider+" has been selected!");
            onLocationChanged(location);
            WeatherChecker.callWeather(
                    strLat,
                    strLng,
                    getApplicationContext(),
                    tempView,
                    cityView,
                    eleView);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Checks for Request --> ask for permission
        PermissionChecker.checkPermission(getApplicationContext(), this);

        locationManager.requestLocationUpdates(provider, 1000, 5, this);
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
        Log.i("TAG", "");


        //https://www.py4u.net/discuss/1195822
        Map<String, String> Sensors = new Gson().fromJson(msg, new TypeToken<Map<String, String>>() {}.getType());

        strHeartRate = Sensors.get(SENSOR_HEART_NAME);
        // if makes sure that it wont be overwritten when heartrate is getting send
        if(!Sensors.get(SENSOR_STEP_NAME).equals(strStepCounter)){
            strOldStepCounter = strStepCounter;
            strStepCounter = Sensors.get(SENSOR_STEP_NAME);
        }

        Log.i("TAG", strStepCounter + " oder " + strOldStepCounter);

        cadence = MovementChecker.updateCadence(strStepCounter, strOldStepCounter);
        if(Integer.parseInt(strStepCounter) != 0) {
            cadenceList.add(cadence);
            cadenceView.setText("Cadence: " + cadence);
        }
        heartList.add(Integer.parseInt(strHeartRate));
        stepsList.add(Integer.parseInt(strStepCounter));

        heartView.setText("Heartrate: "+strHeartRate);
        stepView.setText("Steps: " +strStepCounter);

    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        float lat = (float) location.getLatitude();
        float lng = (float) location.getLongitude();
        strLat = Float.toString(lat);
        strLng = Float.toString(lng);
        strSpeed = Float.toString(location.getSpeed());
        speedList.add(location.getSpeed());
        latView.setText("lat: "+strLat);
        lngView.setText("lng: "+strLng);
        speedView.setText("Speed: "+String.format("%.2f", strSpeed));


        Log.i("Location", "Location has been updated");
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        Toast.makeText(getApplicationContext(), "GPS Activated", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        Toast.makeText(getApplicationContext(), "GPS Deactivated ", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_LOCATION_PERMISSION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    Criteria criteria = new Criteria();
                    provider = locationManager.getBestProvider(criteria, false);

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void initViews(){
        heartView = findViewById(R.id.heart);
        tempView = findViewById(R.id.temp);
        latView = findViewById(R.id.lat);
        lngView = findViewById(R.id.lng);
        cityView = findViewById(R.id.city);
        eleView = findViewById(R.id.ele);
        stepView = findViewById(R.id.step);
        deviceView = findViewById(R.id.device);
        refView = findViewById(R.id.blueref);
        cadenceView = findViewById(R.id.cadence);
        spinView = findViewById(R.id.spinner);
        speedView = findViewById(R.id.speed);

        ArrayAdapter<String> myAdapter = new ArrayAdapter<String>(getApplicationContext(),
                android.R.layout.simple_list_item_1, allDevices);
        myAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinView.setAdapter(myAdapter);
        refView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BluetoothChecker.checkBluetooth(allDevices, deviceView);
            }
        });

    }

//    private void updateWeather(){
//        String weathURL = "https://api.openweathermap.org/data/2.5/weather?lat="+strLat+"&lon="+strLng+"&appid="+weathAPI;
//        Log.i("weathURL", weathURL);
//
//        StringRequest stringRequest = new StringRequest(Request.Method.POST, weathURL, new Response.Listener<String>() {
//            @Override
//            public void onResponse(String response) {
//                try {
//                    JSONObject jsonResponse = new JSONObject(response);
//                    JSONArray jsonArray = jsonResponse.getJSONArray("weather");
//                    JSONObject jsonObjectWeather = jsonArray.getJSONObject(0);
//                    String description = jsonObjectWeather.getString("description");
//                    JSONObject jsonObjectMain = jsonResponse.getJSONObject("main");
//                    double temp = jsonObjectMain.getDouble("temp") - 273.15; // calvin temperature
//                    double feelsLike = jsonObjectMain.getDouble("feels_like") - 273.15; // calvin temperature
//                    int pressure = jsonObjectMain.getInt("pressure");
//                    int humidity = jsonObjectMain.getInt("humidity");
//                    JSONObject jsonObjectWind = jsonResponse.getJSONObject("wind");
//                    String wind = jsonObjectWind.getString("speed");
//                    JSONObject jsonObjectClouds = jsonResponse.getJSONObject("clouds");
//                    String clouds = jsonObjectClouds.getString("all");
//                    JSONObject jsonObjectSys = jsonResponse.getJSONObject("sys");
//                    String countryName = jsonObjectSys.getString("country");
//                    String cityName = jsonResponse.getString("name");
//
//                    String strTemp = Double.toString(Math.round(temp));
//
//                    tempView.setText("Temperature: "+strTemp);
//                    cityView.setText("City: "+cityName);
//
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//
//            }
//        }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                cityView.setText("Weather: API ERROR");
//            }
//        });
//
//        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
//        requestQueue.add(stringRequest);
//    }
//
//    private void updateElevation(){
//        String altURL = "https://api.opentopodata.org/v1/aster30m?locations="+strLat+","+strLng;
//        Log.i("altURL", altURL);
//
//        StringRequest stringRequest = new StringRequest(Request.Method.GET, altURL, new Response.Listener<String>() {
//            @Override
//            public void onResponse(String response) {
//                try {
//                    JSONObject jsonResponse = new JSONObject(response);
//                    JSONArray jsonArray = jsonResponse.getJSONArray("results");
//                    JSONObject jsonObjectElevation = jsonArray.getJSONObject(0);
//                    elevation = jsonObjectElevation.getDouble("elevation");
//                    strElevation = Double.toString(elevation);
//                    eleView.setText("elevation: "+strElevation);
//
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//
//
//            }
//        }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                eleView.setText("elevation: API ERROR");
//            }
//        });
//
//        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
//        requestQueue.add(stringRequest);
//    }

}