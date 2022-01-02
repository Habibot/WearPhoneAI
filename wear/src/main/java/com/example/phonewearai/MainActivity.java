package com.example.phonewearai;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
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
    private ActivityMainBinding binding;
    private SensorManager mSensorManager;
    private Sensor mHeart;

    JSONObject jsonMsg = new JSONObject();

    private String transcriptionNodeId;

    //credits https://github.com/Bilbobx182
    private static final String SET_MESSAGE_CAPABILITY = "setString";
    public static final String SET_MESSAGE_PATH = "/setString";

    // Important CONSTANTS for JSON
    private static final String SENSOR_HEART_NAME = "Heartrate";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        heartView = findViewById(R.id.Heart);

        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);

        // HEARTRATE INITIATION
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

    @Override
    protected void onResume() {
        super.onResume();
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE) != null) {
            mSensorManager.registerListener(this, mHeart, SensorManager.SENSOR_DELAY_NORMAL);
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
            String strHeartRate = updateSensorValue(event, SENSOR_HEART_NAME);

            beginSendMessageToPhone(jsonMsg.toString());

            heartView.setText("Heartrate: " + strHeartRate);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }


    private String updateSensorValue(SensorEvent event, String sensorName){
        float sensorValue = event.values[0];
        String strSensorValue = Float.toString(sensorValue);

        try {
            updateJSON(sensorName, strSensorValue);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return strSensorValue;
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
                        Wearable.getCapabilityClient(getBaseContext()).getCapability(SET_MESSAGE_CAPABILITY, CapabilityClient.FILTER_REACHABLE));
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

}