package com.example.phonewearai;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.util.Log;
import android.widget.TextView;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

public class BluetoothChecker {

    private static BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    public static void checkBluetooth(List<String> allDevices, TextView deviceView) {
        if (bluetoothAdapter.isEnabled() && bluetoothAdapter != null) {
            Set<BluetoothDevice> pairedDevices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();

            if (allDevices.isEmpty()) {
                deviceView.setText("No Bluetooth Devices Connected");
            }

            if (pairedDevices.size() > 0) {
                allDevices.clear();
                allDevices.add("Show all Devices");

                for (BluetoothDevice device : pairedDevices) {
                    if (isConnected(device)) {
                        Log.i("NAME", device.getName());
                        String deviceName = device.getName();
                        allDevices.add(deviceName);
                    }
                }

                if (allDevices.size() > 0) {
                    deviceView.setText("Bluetooth Devices Connected");
                }
            }
        }
        else{
            deviceView.setText("Bluetooth Deactivated");
            allDevices.clear();
            allDevices.add("Show all Devices");
        }
    }

    private static boolean isConnected(BluetoothDevice device) {
        try {
            Method m = device.getClass().getMethod("isConnected", (Class[]) null);
            boolean connected = (boolean) m.invoke(device, (Object[]) null);
            return connected;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
