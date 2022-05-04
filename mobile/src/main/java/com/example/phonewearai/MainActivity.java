package com.example.phonewearai;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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
    private ImageButton reportView;
    private Chronometer timerView;
    private Button settresView;
    private TextView heartTresView;
    private TextView cadenceTresView;
    private TextView speedTresView;
    private TextView tresTextView;

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
    private int intHeartStart;
    private int intCadenceStart;
    private int intSpeedStart;
    private int intHeartEnd;
    private int intCadenceEnd;
    private int intSpeedEnd;

    private boolean runStarted = false;
    private boolean tresholdSet = false;

    // Important CONSTANTS for JSON
    private static final String SENSOR_HEART_NAME = "Heartrate";
    private static final String SENSOR_STEP_NAME = "Stepcounter";

    private static final int REQUEST_LOCATION_PERMISSION = 1;

    private final ArrayList<Integer> heartList = new ArrayList<>();
    private final ArrayList<Integer> cadenceList = new ArrayList<>();
    private final ArrayList<Integer> stepsList = new ArrayList<>();
    private final ArrayList<Float> speedList = new ArrayList<>();
    private final ArrayList<Float> latList = new ArrayList<>();
    private final ArrayList<Float> lngList = new ArrayList<>();


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

        if(msg.equals("start")){
            runStarted = true;
            timerView.setBase(SystemClock.elapsedRealtime());
            timerView.start();
            return;
        }
        if(msg.equals("stop")){
            runStarted = false;
            timerView.stop();
            return;
        }
        if(tresholdSet){
            checkTresholdViolation();
        }
        //https://www.py4u.net/discuss/1195822
        Map<String, String> Sensors = new Gson().fromJson(msg, new TypeToken<Map<String, String>>() {}.getType());

        // it happens that heart value may be unknown
        if (!Sensors.get(SENSOR_HEART_NAME).equals("unknown")){
            strHeartRate = Sensors.get(SENSOR_HEART_NAME);
            // can happen that wear json object has no steps yet.
            if (Sensors.get(SENSOR_STEP_NAME) != null){
                // if makes sure that it wont be overwritten when heartrate is getting send
                if(!Sensors.get(SENSOR_STEP_NAME).equals(strStepCounter)){
                    strOldStepCounter = strStepCounter;
                    strStepCounter = Sensors.get(SENSOR_STEP_NAME);
                }
            }

            // cadence can jump way too high so in case it is, get max 300
            cadence = Math.min(MovementChecker.updateCadence(strStepCounter, strOldStepCounter), 300);
            if(Integer.parseInt(strStepCounter) != 0) {
                if(runStarted){
                    cadenceList.add(cadence);
                }
                cadenceView.setText("Cadence: " + cadence);
            }
            if(runStarted){
                heartList.add(Integer.parseInt(strHeartRate));
                stepsList.add(Integer.parseInt(strStepCounter));
            }

            heartView.setText("Heartrate: "+strHeartRate);
            stepView.setText("Steps: " +strStepCounter);
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        float lat = (float) location.getLatitude();
        float lng = (float) location.getLongitude();
        strLat = Float.toString(lat);
        strLng = Float.toString(lng);
        strSpeed = Float.toString(location.getSpeed());

        if(runStarted){
            speedList.add(location.getSpeed());
            latList.add((float) location.getLatitude());
            lngList.add((float) location.getLongitude());
        }

        latView.setText("lat: "+strLat);
        lngView.setText("lng: "+strLng);
        speedView.setText("Speed: "+strSpeed);
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
                // if request is cancelled result arrays empty
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //permission granted
                    Criteria criteria = new Criteria();
                    provider = locationManager.getBestProvider(criteria, false);

                } else {
                    // permission denied
                }
            }
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
        reportView = findViewById(R.id.report);
        timerView = findViewById(R.id.timer);
        settresView = findViewById(R.id.settreshold);
        heartTresView = findViewById(R.id.hearttres);
        cadenceTresView = findViewById(R.id.cadencetres);
        speedTresView = findViewById(R.id.speedtres);
        tresTextView = findViewById(R.id.trestext);

        allDevices.add("Show all Devices");
        ArrayAdapter<String> myAdapter = new ArrayAdapter<String>(getApplicationContext(),
                R.layout.spinner_item, allDevices);
        myAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinView.setAdapter(myAdapter);

        refView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BluetoothChecker.checkBluetooth(allDevices, deviceView);
                spinView.setSelection(0);
            }
        });

        reportView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(MainActivity.this, ChartActivity.class);
                myIntent.putExtra("heart", heartList);
                myIntent.putExtra("cadence", cadenceList);
                myIntent.putExtra("speed", speedList);
                MainActivity.this.startActivity(myIntent);
            }
        });

        settresView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater factory = LayoutInflater.from(MainActivity.this);
                final View textEntryView = factory.inflate(R.layout.treshold_entry, null);

                final EditText heartStart = (EditText) textEntryView.findViewById(R.id.heartstart);
                final EditText heartEnd = (EditText) textEntryView.findViewById(R.id.heartend);
                final EditText cadenceStart = (EditText) textEntryView.findViewById(R.id.cadencestart);
                final EditText cadenceEnd = (EditText) textEntryView.findViewById(R.id.cadenceend);
                final EditText speedStart = (EditText) textEntryView.findViewById(R.id.speedstart);
                final EditText speedEnd = (EditText) textEntryView.findViewById(R.id.speedend);

                final AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                alert.setTitle("Set Treshold")
                        .setView(textEntryView).setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // to prevent errors in call of violation check
                        tresholdSet = true;

                        // set UI treshold text to the input amount
                        heartTresView.setText("Treshold: "+ heartStart.getText().toString() + " - " + heartEnd.getText().toString());
                        cadenceTresView.setText("Treshold: "+ cadenceStart.getText().toString()+ " - " + cadenceEnd.getText().toString());
                        speedTresView.setText("Treshold: "+ speedStart.getText().toString()+ " - " + speedEnd.getText().toString());

                        // initialisation to integer from EDITABLE
                        intHeartStart = Integer.parseInt(heartStart.getText().toString());
                        intHeartEnd = Integer.parseInt(heartEnd.getText().toString());
                        intCadenceStart = Integer.parseInt(cadenceStart.getText().toString());
                        intCadenceEnd = Integer.parseInt(cadenceEnd.getText().toString());
                        intSpeedStart = Integer.parseInt(speedStart.getText().toString());
                        intSpeedEnd = Integer.parseInt(speedEnd.getText().toString());
                    }
                }).setNegativeButton("Clear", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        tresholdSet = false;
                        heartTresView.setText("Treshold: none");
                        cadenceTresView.setText("Treshold: none");
                        speedTresView.setText("Treshold: none");
                        tresTextView.setText("No Treshold Set");
                    }
                });
                alert.show();
            }
        });
    }

    private void checkTresholdViolation(){
        String fullText = "";
        if (Integer.parseInt(strHeartRate) < intHeartStart || intHeartEnd < Integer.parseInt(strHeartRate)) {
            // heartbeat too slow
            if (Integer.parseInt(strHeartRate) < intHeartStart){
                fullText += "Careful! Heartbeat is low\n";
            }
            // heartbeat too fast
            if (intHeartEnd < Integer.parseInt(strHeartRate)){
                fullText += "Careful! Heartbeat is high\n";
            }
        } else {
            fullText += "Heartbeat is good!\n";
        }
        if (cadence < intCadenceStart || intCadenceEnd < cadence){
            // cadence too low
            if (cadence < intCadenceStart){
                fullText += "Careful! Cadence is low\n";
            }
            // cadence too fast
            if (intCadenceEnd < cadence){
                fullText += "Careful! Cadence is high\n";
            }
        } else {
            fullText += "Cadence is Good!\n";
        }
        if (Float.parseFloat(strSpeed) < intSpeedStart || intSpeedEnd < Float.parseFloat(strSpeed)){
            // speed too slow
            if (Float.parseFloat(strSpeed) < intSpeedStart){
                fullText += "Careful! Speed is low\n";
            }
            // speed too fast
            if (intSpeedEnd < Float.parseFloat(strSpeed)){
                fullText += "Careful! Speed is high\n";
            }
        } else {
            fullText += "Speed is Good!\n";
        }
        tresTextView.setText(fullText);
    }
}