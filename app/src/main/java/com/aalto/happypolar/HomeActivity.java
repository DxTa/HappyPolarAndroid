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
import android.widget.Toast;

import com.aalto.happypolar.util.DateUtility;
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import cz.msebera.android.httpclient.Header;

public class HomeActivity extends ActionBarActivity {
    private Menu mMenu;
    private TextView tvHeartRate;
    private HeartRateDevice hrDevice;

    private UserProfile mUserProfile;

    private boolean dialogClicked = false;

    private GraphView graphDailyCalories;

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

        Button btnStartExercise = (Button) findViewById(R.id.btnStartExercise);
        btnStartExercise.setOnClickListener(btnClickListener);

        Button btnGo = (Button) findViewById(R.id.btnGo);
        btnGo.setOnClickListener(btnClickListener);

        graphDailyCalories = (GraphView) findViewById(R.id.graphDailyCalories);
        loadSummary();

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

    private void loadSummary() {
        final BarGraphSeries<DataPoint> summarySeries = new BarGraphSeries<DataPoint>();
        graphDailyCalories.addSeries(summarySeries);
        //graphDailyCalories.setTitle("Daily Summary");
        graphDailyCalories.getGridLabelRenderer().setHorizontalAxisTitle("Date");
        graphDailyCalories.getGridLabelRenderer().setVerticalAxisTitle("Calories");
        graphDailyCalories.getGridLabelRenderer().setNumHorizontalLabels(5); // only 4 because of the space

        summarySeries.setDrawValuesOnTop(true);

        graphDailyCalories.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if (isValueX) {
                    return DateUtility.getMMMd(new Date((long) value));
                } else {
                    return super.formatLabel(value, isValueX);
                }
            }
        });

        AsyncHttpClient client = new AsyncHttpClient();
        client.addHeader("authorization", "Bearer " + mUserProfile.getFbAccessToken());
        client.get(MyApplication.SERVER_URL + "/users/" + mUserProfile.getId() + "/sessions", new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                super.onSuccess(statusCode, headers, response);

                Date todayDate = new Date();

                for (int decr = 4; decr >= 0; decr--) { //last 5 days including today
                    Date currentDate = DateUtility.decrementDate(todayDate, decr);

                    double caloriesBurned = 0;
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject jsonSession = response.getJSONObject(i);
                            Date sessionDate = DateUtility.getDateFromISOString(jsonSession.getString("start_time"));
                            if (DateUtility.isSameDay(currentDate, sessionDate)) {
                                caloriesBurned = caloriesBurned + jsonSession.getDouble("calories");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    summarySeries.appendData(new DataPoint(currentDate, caloriesBurned), false, 5);
                }
                summarySeries.setSpacing(10);
                graphDailyCalories.getViewport().setXAxisBoundsManual(true);
                double xInterval=1.0;
                if (summarySeries instanceof BarGraphSeries ) {
// Shunt the viewport, per v3.1.3 to show the full width of the first and last bars.
                    graphDailyCalories.getViewport().setMinX(summarySeries.getLowestValueX() - (xInterval/2.0));
                    graphDailyCalories.getViewport().setMaxX(summarySeries.getHighestValueX() + (xInterval/2.0));
                } else {
                    graphDailyCalories.getViewport().setMinX(summarySeries.getLowestValueX() );
                    graphDailyCalories.getViewport().setMaxX(summarySeries.getHighestValueX());
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (HeartRateDevice.isConnected()) {
            HeartRateDevice hrDevice = HeartRateDevice.getInstance();
            hrDevice.addHeartRateListener(heartRateListener);
        }
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
