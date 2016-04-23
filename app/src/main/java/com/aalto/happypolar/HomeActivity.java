package com.aalto.happypolar;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

public class HomeActivity extends ActionBarActivity {
    private Menu mMenu;
    private TextView tvHeartRate;
    private HeartRateDevice hrDevice;

    private UserProfile mUserProfile;

    private boolean dialogClicked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("myapp", "Home activity onCreate");
        setContentView(R.layout.activity_home);
        tvHeartRate = (TextView)findViewById(R.id.tvHeartRate);
        tvHeartRate.setText("~");

        mUserProfile = UserProfile.getInstance();

        TextView tvGreeting = (TextView) findViewById(R.id.tvGreeting);
        tvGreeting.setText("Hello, " + mUserProfile.getName().split(" ")[0]); //only display the first name

        if (!HeartRateDevice.isConnected()) {
            pairHeartRateDevice();
        }

    }

    private void pairHeartRateDevice () {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(HomeActivity.this);
        dialogBuilder.setTitle("Connect to a Heart Rate Sensor");
        dialogBuilder.setMessage("This application requires a connection to a Bluetooth Heart Rate Sensor");

        dialogBuilder.setPositiveButton("Connect", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialogClicked = true;
                Intent intent = new Intent(HomeActivity.this, PairDeviceActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });

        dialogBuilder.setNegativeButton("Exit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                HomeActivity.this.finish();
            }
        });

        dialogBuilder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (!dialogClicked) {
                    //dialogBuilder.create().show();
                }
            }
        });

        dialogBuilder.create().show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (HeartRateDevice.isConnected()) {
            HeartRateDevice hrDevice = HeartRateDevice.getInstance();
            hrDevice.addHeartRateListener(heartRateListener);
        }

        Button btnStartExercise = (Button) findViewById(R.id.btnStartExercise);
        btnStartExercise.setOnClickListener(btnClickListener);

        Button btnGo = (Button) findViewById(R.id.btnGo);
        btnGo.setOnClickListener(btnClickListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (HeartRateDevice.isConnected()) {
            HeartRateDevice.getInstance().removeHeartRateListener(heartRateListener);
        }
    }

    @Override
    protected void onDestroy () {
        super.onDestroy();
        Log.i("myapp", "Destroyed");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_home, menu);
        mMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_profile:
                //Open edit profile activity
                Intent intentEditProfile = new Intent(HomeActivity.this, EditProfileActivity.class);
                startActivity(intentEditProfile);
                break;

            case R.id.menu_pair_device:
                //Open pair device activity
                Intent intentPairDevice = new Intent(HomeActivity.this, PairDeviceActivity.class);
                startActivity(intentPairDevice);
                break;
        }
        return true;
    }

    private Button.OnClickListener btnClickListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent;
            switch (v.getId()) {

                case R.id.btnStartExercise:
                    if (!HeartRateDevice.isConnected()) {
                        pairHeartRateDevice();
                    } else {
                        intent = new Intent(HomeActivity.this, ExerciseActivity.class);
                        startActivity(intent);
                    }
                    break;


                case R.id.btnGo:
                    Spinner spinnerMenu = (Spinner) findViewById(R.id.spinnerMenu);
                    String selectedMenuItem = spinnerMenu.getSelectedItem().toString();

                    if(selectedMenuItem.equals(getResources().getString(R.string.exercise_history))) {
                        intent = new Intent(HomeActivity.this, SessionsListActivity.class);
                        startActivity(intent);
                    } else if(selectedMenuItem.equals(getResources().getString(R.string.start_exercise))) {
                        Button btnStartEx = (Button) findViewById(R.id.btnStartExercise);
                        btnStartEx.performClick();
                    } else if(selectedMenuItem.equals(getResources().getString(R.string.edit_profile))) {
                        intent = new Intent(HomeActivity.this, EditProfileActivity.class);
                        startActivity(intent);
                    } else if(selectedMenuItem.equals(getResources().getString(R.string.pair_device))) {
                        intent = new Intent(HomeActivity.this, PairDeviceActivity.class);
                        startActivity(intent);
                    }
                    break;
            }
        }
    };

    public HeartRateDevice.HeartRateListener heartRateListener = new HeartRateDevice.HeartRateListener() {
        @Override
        public void HeartRateUpdated(final Integer heartRate) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvHeartRate.setText(heartRate.toString());
                    tvHeartRate.invalidate();
                }
            });
            Log.i("myapp", "Heart Rate " + heartRate.toString());
        }
    };
}
