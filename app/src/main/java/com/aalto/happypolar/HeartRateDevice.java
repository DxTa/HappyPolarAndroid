package com.aalto.happypolar;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Gaurav on 14-Apr-16.
 */
public class HeartRateDevice {

    private String TAG = "myapp";

    private static HeartRateDevice mInstance = null;

    private BluetoothDevice mBtDevice;
    private BluetoothGatt mBtGatt;
    private Boolean isConnected = false;

    private Integer mHeartRate = null;

    private DeviceConnectionListener connectionListener;
    private ArrayList<HeartRateListener> heartRateListeners = new ArrayList();

    private HeartRateDevice(Context context, BluetoothDevice btDevice, DeviceConnectionListener callback) {
        connectionListener = callback;
        mBtDevice = btDevice;
        mBtGatt = btDevice.connectGatt(context, true, mBluetoothGattCallback);
    }

    public static HeartRateDevice getInstance() {
        if (mInstance == null) {
            throw new NullPointerException("Instance not initialized");
        } else {
            return mInstance;
        }
    }

    public static HeartRateDevice initializeInstance(Context context, BluetoothDevice btDevice, DeviceConnectionListener callback) {
        mInstance = new HeartRateDevice(context, btDevice, callback);
        return mInstance;
    }

    public Integer getHeartRate() {
        return mHeartRate;
    }

    public static boolean isConnected() {
        if (mInstance != null) {
            return mInstance.isConnected;
        } else {
            return false;
        }
    }

    public BluetoothDevice getBluetoothDevice () {
        return mBtDevice;
    }

    public void disconnect() {
        mBtGatt.disconnect();
    }

    public void connect() {
        mBtGatt.connect();
    }

    public void close() {
        mBtGatt.close();
    }

    public void addHeartRateListener (HeartRateListener hrListener) {
        if (!heartRateListeners.contains(hrListener)) {
            heartRateListeners.add(hrListener);
        }
    }

    public void removeHeartRateListener (HeartRateListener hrListener) {
        heartRateListeners.remove(hrListener);
    }

    private BluetoothGattCallback mBluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    Log.i(TAG, "Device Connected");

                    isConnected = true;

                    mBtDevice = gatt.getDevice(); //update the device just in case
                    gatt.discoverServices();

                    //callback to notify
                    if (connectionListener != null) {
                        connectionListener.deviceConnected(gatt.getDevice());
                        connectionListener = null;
                    }
                    break;

                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.i(TAG, "Device Disconnected");
                    isConnected = false;
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            List<BluetoothGattService> services = gatt.getServices();
            Log.i(TAG, "Services discovered");
            for (BluetoothGattService s : services) {
                Log.i(TAG, s.getUuid().toString());
            }
            Log.i(TAG, "Reading heart rate continuously");
            BluetoothGattService btGattServiceHeartRate = gatt.getService(UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb"));

            isConnected = true;

            Log.i(TAG, "Heart Rate Measurement Characteristics");
            List<BluetoothGattCharacteristic> characteristics = btGattServiceHeartRate.getCharacteristics();
            for (BluetoothGattCharacteristic ch : characteristics) {
                Log.i(TAG, ch.getUuid().toString());
            }
            //gatt.readCharacteristic(btGattServiceHeartRate.getCharacteristic(UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb")));
            gatt.setCharacteristicNotification(btGattServiceHeartRate.getCharacteristic(UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb")), true);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            //Log.i(TAG, characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 1).toString());
            mHeartRate = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 1);

            //Notify the HR listeners
            for (HeartRateListener hrListener : heartRateListeners) {
                hrListener.HeartRateUpdated(mHeartRate);
            }
        }
    };


    /*
    * Interfaces for callers
    */
    public interface DeviceConnectionListener {
        void deviceConnected(BluetoothDevice device);
    }

    public interface HeartRateListener {
        void HeartRateUpdated (Integer heartRate);
    }
}
