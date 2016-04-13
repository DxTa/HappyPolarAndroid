package com.bhorkar.heartratesensor;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import java.util.List;
import java.util.UUID;

public class PairDeviceActivity extends ActionBarActivity {

    private String TAG = "mytag";

    private BluetoothAdapter mBtAdapter;
    //private BluetoothGatt mBtGatt;
    private LeDeviceListAdapter mLeDeviceListAdapter;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pair_device);

        BluetoothManager btManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBtAdapter = btManager.getAdapter();

        mHandler = new Handler();
        mLeDeviceListAdapter = new LeDeviceListAdapter(this);

        ListView lvScanResults = (ListView) findViewById(R.id.lvScanResults);
        lvScanResults.setAdapter(mLeDeviceListAdapter);
        lvScanResults.setOnItemClickListener(mListViewClickListener);
    }

    public void onBtnClick (View view) {
        //if BT is not turned on, ask for it.
        if (mBtAdapter == null || !mBtAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBtIntent);
            return;
        }

        Button button = (Button) view;
        switch (view.getId()) {
            case R.id.btnScan:
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mBtAdapter.stopLeScan(mLeScanCallback);
                    }
                }, 30000);

                mBtAdapter.startLeScan(mLeScanCallback);
                break;
        }
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            mLeDeviceListAdapter.addDevice(device);
            mLeDeviceListAdapter.notifyDataSetChanged();
        }
    };

    private ListView.OnItemClickListener mListViewClickListener = new ListView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            HeartRateDevice heartRateDevice = HeartRateDevice.initializeInstance(getApplicationContext(), mLeDeviceListAdapter.getDevice(position));
            /*
            if (mBtGatt == null) {
                BluetoothDevice device = mLeDeviceListAdapter.getDevice(position);
                mBtGatt = device.connectGatt(PairDeviceActivity.this, true, mBluetoothGattCallback);
                mBtAdapter.stopLeScan(mLeScanCallback);
            }
            */
        }
    };

    /*
    private BluetoothGattCallback mBluetoothGattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    Log.i(TAG, "Device Connected");
                    gatt.discoverServices();
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.i(TAG, "Device Disconnected");
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
            Log.i(TAG, characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 1).toString());
        }
    };
    */
}
