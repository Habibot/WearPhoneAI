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

import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.Map;

public class MainActivity extends AppCompatActivity implements MessageClient.OnMessageReceivedListener, LocationListener {


    private TextView heartView;
    private TextView tempView;
    private TextView latView;
    private TextView lngView;
    private TextView weathView;
    private LocationManager locationManager;
    private String provider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        heartView = findViewById(R.id.heart);
        tempView = findViewById(R.id.temp);
        latView = findViewById(R.id.lat);
        lngView = findViewById(R.id.lng);
        weathView = findViewById(R.id.weath);


        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        // Initialize the location fields
        Location location = locationManager.getLastKnownLocation(provider);
        if (location != null){
            Log.i("Provider", provider+" has been selected!");
            onLocationChanged(location);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
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

        String strHeartRate = Sensors.get("Heartrate");
        //String strTemp = Sensors.get("Temperature");


        heartView.setText("Heartrate: "+strHeartRate);
        //tempView.setText("Temperature: "+strTemp);

    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        float lat = (float) location.getLatitude();
        float lng = (float) location.getLongitude();

        //String strLat = Integer.toString(lat);
        //String strLng = Integer.toString(lng);
        String strLat = Float.toString(lat);
        String strLng = Float.toString(lng);

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
}