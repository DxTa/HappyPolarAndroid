package com.aalto.happypolar;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

public class PairDeviceActivity extends ActionBarActivity {

    private String TAG = "mytag";

    private BluetoothAdapter mBtAdapter;
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

    @Override
    protected void onResume() {
        super.onResume();
        refreshScanDevices();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_pair_device, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_refresh:
                refreshScanDevices();
                break;
        }
        return true;
    }

    private void refreshScanDevices () {
        //if BT is not turned on, ask for it.
        if (mBtAdapter == null || !mBtAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBtIntent);
            return;
        }
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mBtAdapter.stopLeScan(mLeScanCallback);
            }
        }, 10000);

        Toast.makeText(PairDeviceActivity.this, "Scanning...", Toast.LENGTH_SHORT).show();
        mBtAdapter.startLeScan(mLeScanCallback);
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
            final BluetoothDevice btDevice = mLeDeviceListAdapter.getDevice(position);

            mBtAdapter.stopLeScan(mLeScanCallback); //stop scanning as well

            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(PairDeviceActivity.this);
            dialogBuilder.setTitle("Connect to this device?");
            dialogBuilder.setMessage(btDevice.getName() + "\n" + "Address - " + btDevice.getAddress());

            dialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    HeartRateDevice.initializeInstance(getApplicationContext(), btDevice, connectionListener);
                }
            });

            dialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //Do nothing if clicked no
                }
            });

            dialogBuilder.create().show();
        }
    };

    private HeartRateDevice.DeviceConnectionListener connectionListener = new HeartRateDevice.DeviceConnectionListener() {
        @Override
        public void deviceConnected(BluetoothDevice device) {
            //Toast.makeText(PairDeviceActivity.this, "Connected to: " + device.getName(), Toast.LENGTH_LONG).show();
            PairDeviceActivity.this.finish();
        }
    };
}
