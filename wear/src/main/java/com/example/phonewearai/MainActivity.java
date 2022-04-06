package com.example.phonewearai;

import android.app.Activity;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.phonewearai.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.CapabilityClient;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Set;
import java.util.concurrent.ExecutionException;

public class MainActivity extends Activity implements SensorEventListener {

    private TextView heartView;
    private TextView stepView;
    private Button buttonView;
    private ActivityMainBinding binding;
    private SensorManager mSensorManager;
    private Sensor mHeart;
    private Sensor mStepCounter;

    private Handler handler;

    private boolean isStep = false;
    private int intFullSteps;

    JSONObject jsonMsg = new JSONObject();

    private String transcriptionNodeId;

    private final int DATA_MESSAGE_DELAY = 5000; // 5 second delay


    //credits https://github.com/Bilbobx182
    private static final String SET_MESSAGE_CAPABILITY = "setString";
    public static final String SET_MESSAGE_PATH = "/setString";

    // Important CONSTANTS for JSON
    private static final String SENSOR_HEART_NAME = "Heartrate";
    private static final String SENSOR_STEP_NAME = "Stepcounter";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initViews();

        PermissionChecker.checkPermissions(getApplicationContext(), this);

        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        initHeart();
        initStep();

        startHandlerThread();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (jsonMsg.length() != 0){
                    beginSendMessageToPhone(jsonMsg.toString());
                }
                handler.postDelayed(this, DATA_MESSAGE_DELAY);
            }
        }, DATA_MESSAGE_DELAY);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE) != null) {
            mSensorManager.registerListener(this, mHeart, SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null) {
            mSensorManager.registerListener(this,mStepCounter, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event){
        if (event.sensor.getType() == Sensor.TYPE_HEART_RATE) {
            //Sensor sometimes shows 0.. handles it
            if (event.values[0] == 0){
                return;
            }

            updateSensorValue(event.values[0], SENSOR_HEART_NAME);
            heartView.setText("Heartrate: " + (int) event.values[0]);
        }
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER){
            //isStep Flag to make sure the counter is init correctly due to counting all steps
            if (!isStep){
                isStep = true;
                intFullSteps = (int) event.values[0];
            }
            int actualSteps = (int) event.values[0] - intFullSteps;

            updateSensorValue(actualSteps, SENSOR_STEP_NAME);
            stepView.setText("Steps: " + actualSteps);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    private void updateSensorValue(float value, String sensorName){
        int sensorValue = (int) value;
        String strSensorValue = Integer.toString(sensorValue);

        try {
            updateJSON(sensorName, strSensorValue);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void updateJSON(String name, String value) throws JSONException {
        jsonMsg.put(name, value);
    }

    //credits https://github.com/Bilbobx182
    private void beginSendMessageToPhone(String msg) {

        AsyncTask.execute(() -> {

            CapabilityInfo capabilityInfo;

            try {
                capabilityInfo = Tasks.await(
                        Wearable.getCapabilityClient(getBaseContext()).getCapability(
                                SET_MESSAGE_CAPABILITY,
                                CapabilityClient.FILTER_REACHABLE));
                updateTranscriptionCapability(capabilityInfo);
                requestTranscription(msg.getBytes());

                // END REFERENCE

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    private void updateTranscriptionCapability(CapabilityInfo capabilityInfo) {
        Set<Node> connectedNodes = capabilityInfo.getNodes();

        transcriptionNodeId = pickBestNodeId(connectedNodes);
    }

    private String pickBestNodeId(Set<Node> nodes) {
        String bestNodeId = null;
        // Find a nearby node or pick one arbitrarily
        for (Node node : nodes) {
            if (node.isNearby()) {
                return node.getId();
            }
            bestNodeId = node.getId();
        }
        return bestNodeId;
    }

    //credits https://github.com/Bilbobx182
    private void requestTranscription(final byte[] message) {

        AsyncTask.execute(() -> {
            if (transcriptionNodeId != null) {
                final Task<Integer> sendTask = Wearable.getMessageClient(getBaseContext()).sendMessage(transcriptionNodeId, SET_MESSAGE_PATH, message);

                /*sendTask.addOnSuccessListener(dataItem -> Log.d("MESSAGESTATE", "SUCCESS"));
                sendTask.addOnFailureListener(dataItem -> Log.d("MESSAGESTATE", "FAILURE"));
                sendTask.addOnCompleteListener(task -> Log.d("MESSAGESTATE", "COMPLETE"));
                */
            }
        });
    }

    private void startHandlerThread(){
        HandlerThread mHandlerThread = new HandlerThread("HandlerThread");
        mHandlerThread.start();
        handler = new Handler(mHandlerThread.getLooper());
    }

    private void initViews(){
        heartView = findViewById(R.id.Heart);
        stepView = findViewById(R.id.Step);
        buttonView = findViewById(R.id.button);
        buttonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (buttonView.getText().toString().equals("START")){
                    buttonView.setBackgroundColor(Color.RED);
                    buttonView.setText("STOP");
                    beginSendMessageToPhone("start");
                } else if (buttonView.getText().toString().equals("STOP")){
                    buttonView.setBackgroundColor(Color.GREEN);
                    buttonView.setText("START");
                    beginSendMessageToPhone("stop");
                }
            }
        });
    }

    private void initHeart(){
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE) != null){
            mHeart = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);

            try {
                jsonMsg.put(SENSOR_HEART_NAME, "unknown");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else{
            heartView.setText("Heartrate is not available");
            try {
                jsonMsg.put(SENSOR_HEART_NAME, "NULL");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void initStep() {
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null) {
            mStepCounter = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        }
    }
}