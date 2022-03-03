package com.example.phonewearai;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    private List<String> allDevices = new ArrayList<String>();

    private LocationManager locationManager;
    private String provider;
    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    private float lat;
    private float lng;
    private double elevation;
    private String strLat;
    private String strLng;
    private String strElevation;
    private String weathAPI = "c198627a1303756c85ea29900f1eaa7c";

    // Important CONSTANTS for JSON
    private static final String SENSOR_HEART_NAME = "Heartrate";
    private static final String SENSOR_STEP_NAME = "Stepcounter";

    private final int REQUEST_LOCATION_PERMISSION = 1;
    private final int REQUEST_BLUETOOTH_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Checks for Request --> ask for permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_BLUETOOTH_PERMISSION);
            return;
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
            return;
        }

        // displays all connected bluetooth devices
        if (bluetoothAdapter.isEnabled() && bluetoothAdapter != null){
            checkBluetooth();
        }

        // Initialize the location fields
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);

        Location location = locationManager.getLastKnownLocation(provider);
        if (location != null){
            Log.i("Provider", provider+" has been selected!");
            onLocationChanged(location);
            updateWeather();
            updateElevation();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Checks for Request --> ask for permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
            return;
        }

        locationManager.requestLocationUpdates(provider, 500, 10, this);
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

        String strHeartRate = Sensors.get(SENSOR_HEART_NAME);
        String strStepCounter = Sensors.get(SENSOR_STEP_NAME);

        heartView.setText("Heartrate: "+strHeartRate);
        stepView.setText("Steps: " +strStepCounter);

    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        lat = (float) location.getLatitude();
        lng = (float) location.getLongitude();
        strLat = Float.toString(lat);
        strLng = Float.toString(lng);
        latView.setText("lat: "+strLat);
        lngView.setText("lng: "+strLng);

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
        spinView = findViewById(R.id.spinner);
        ArrayAdapter<String> myAdapter = new ArrayAdapter<String>(MainActivity.this,
                android.R.layout.simple_list_item_1, allDevices);
        myAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinView.setAdapter(myAdapter);
        refView = findViewById(R.id.blueref);
        refView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bluetoothAdapter.isEnabled() && bluetoothAdapter != null){
                    checkBluetooth();
                } else {
                    deviceView.setText("No Bluetooth Devices Connected");
                    Toast.makeText(MainActivity.this, "Bluetooth Not Available",
                    Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateWeather(){
        String weathURL = "https://api.openweathermap.org/data/2.5/weather?lat="+strLat+"&lon="+strLng+"&appid="+weathAPI;
        Log.i("weathURL", weathURL);

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

    private void updateElevation(){
        String altURL = "https://api.opentopodata.org/v1/aster30m?locations="+strLat+","+strLng;
        Log.i("altURL", altURL);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, altURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    JSONArray jsonArray = jsonResponse.getJSONArray("results");
                    JSONObject jsonObjectElevation = jsonArray.getJSONObject(0);
                    elevation = jsonObjectElevation.getDouble("elevation");
                    strElevation = Double.toString(elevation);
                    eleView.setText("elevation: "+strElevation);

                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                eleView.setText("elevation: API ERROR");
            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(stringRequest);
    }

    public void checkBluetooth(){
        Set<BluetoothDevice> pairedDevices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
        if (allDevices.isEmpty()){
            deviceView.setText("No Bluetooth Devices Connected");
        }
        if (pairedDevices.size() > 0){
            allDevices.clear();

            for (BluetoothDevice device : pairedDevices){
                if (isConnected(device)){
                    Log.i("NAME", device.getName());
                    String deviceName = device.getName();
                    allDevices.add(deviceName);
                }
            }

            if (allDevices.size() > 0){
                deviceView.setText("Bluetooth Devices Connected");
            }
        }
    }

    public boolean isConnected(BluetoothDevice device) {
        try {
            Method m = device.getClass().getMethod("isConnected", (Class[]) null);
            boolean connected = (boolean) m.invoke(device, (Object[]) null);
            return connected;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

}