package com.aalto.happypolar;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.aalto.happypolar.util.DateUtility;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Date;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;


/**
 * A simple {@link Fragment} subclass.
 */
public class ExerciseProgressFragment extends Fragment {

    private String mExerciseId;
    private String mExerciseType;

    private OnExerciseFragmentInterface mListener;

    private Integer mtargetCalories;
    private Integer mSecondsElapsed;

    private TextView tvTimer, tvCalories, tvHeartRate;
    private GraphView lineChartHR;
    private ImageView imageHeart;
    private boolean heartBeatFlag = true;

    private LineGraphSeries<DataPoint> lineDataHR;

    private Handler mHandlerTimer = new Handler();

    //TIMER DELAY
    private final int tickDelay = 1000;
    private final int step = 1; //step counter (usually should correspond to timer delay (if 2000 then 2)

    private UserProfile mUserProfile;
    private HeartRateDevice mHeartRateDevice;

    private Long mWeight;
    private Integer mAge;
    private boolean mIsMale;

    private JSONArray jsonSlot;
    private JSONObject jsonSession;
    /*Sample Session Object
        {
      user_id:  String ,
      exercise_id:  String,
      start_time: ISODate format?
      end_time: ISODate format?,
      slot: [
        {
          seconds_elapsed: Number,
          heart_rate: Number,
          location: {
            lat: Number,
            long: Number
          }
        }
      ],
      heart_rate: {
        min: Number,
        max: Number,
        average: Number
      },
      calories: Number
    }
    */

    public ExerciseProgressFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_exercise_progress, container, false);
    }

    @Override
    public void onAttach(Activity context) {
        super.onAttach(context);
        if (context instanceof OnExerciseFragmentInterface) {
            mListener = (OnExerciseFragmentInterface) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }

    }

    @Override
    public void onStart() {
        super.onStart();

        imageHeart = (ImageView) getActivity().findViewById(R.id.imageHeart);

        //set onclick listeners
        Button btnStop = (Button) getActivity().findViewById(R.id.btnStop);
        Button btnCancel = (Button) getActivity().findViewById(R.id.btnCancel);
        btnStop.setOnClickListener(onButonClickListener);
        btnCancel.setOnClickListener(onButonClickListener);

        //mtargetCalories = getArguments().getInt(ExerciseActivity.TARGET_CALORIES);
        mExerciseType = getArguments().getString(ExerciseActivity.EXERCISE_TYPE);
        mExerciseId = getArguments().getString(ExerciseActivity.EXERCISE_ID);


        mUserProfile = UserProfile.getInstance();
        mHeartRateDevice = HeartRateDevice.getInstance();

        mWeight = mUserProfile.getWeight();
        mAge = mUserProfile.getAge();
        mIsMale = (mUserProfile.getGender().equals(UserProfile.MALE)) ? true : false;
        mHeartRateAvg = mHeartRateDevice.getHeartRate();  //for now just initializing

        TextView tvExerciseType = (TextView) getActivity().findViewById(R.id.tvExerciseType);
        tvExerciseType.setText(mExerciseType);

        tvTimer = (TextView) getActivity().findViewById(R.id.tvTime);
        tvCalories = (TextView) getActivity().findViewById(R.id.tvCalories);
        tvHeartRate = (TextView) getActivity().findViewById(R.id.tvHeartRate);
        lineChartHR = (GraphView) getActivity().findViewById(R.id.lineChartHeartRate);

        lineDataHR = new LineGraphSeries<DataPoint>(new DataPoint[] {
                new DataPoint(0,0)
        }); //For now an empty line chart

        lineChartHR.addSeries(lineDataHR);
        lineChartHR.getViewport().setXAxisBoundsManual(true);
        lineChartHR.getViewport().setMinX(0);
        lineChartHR.getViewport().setMaxX(60);
        lineChartHR.getGridLabelRenderer().setHorizontalAxisTitle("Seconds elapsed");
        lineChartHR.getGridLabelRenderer().setVerticalAxisTitle("Beats per min");

        /*Build the session object (to be sent to server later) */
        jsonSession = new JSONObject();
        jsonSlot = new JSONArray();
        try {
            jsonSession.put("user_id", mUserProfile.getId());
            jsonSession.put("exercise_id", mExerciseId);
            jsonSession.put("start_time", DateUtility.getISOStringFromDate(new Date()));
            // end_time, slot, heartRate, calories to be added later upon session end.
        } catch (JSONException e) {
            e.printStackTrace();
        }

        /*Start a timer for specified delay*/
        mSecondsElapsed = 0;
        mHandlerTimer.postDelayed(runnableTickTok, tickDelay);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    private Integer mheartRate;
    private Integer mHeartRateSum=0;
    private Integer mHeartRateAvg;
    private Double mCaloriesBurned = 0.0;
    private int counter = 0;
    private Runnable runnableTickTok = new Runnable() {
        @Override
        public void run() {
            //Runs every second
            mSecondsElapsed += step;  //can also act as a counter

            //Do work here
            //calculate calories, and display information.
            mheartRate = mHeartRateDevice.getHeartRate();

            //Calculate calories every 10 seconds
            if ((mSecondsElapsed % 10 == 0) && (mheartRate != 0)) {
                counter++;
                mHeartRateSum += mheartRate;
                mHeartRateAvg = mHeartRateSum / counter;
                if (mIsMale) {
                    mCaloriesBurned = ((-55.0969 + (0.6309 * mHeartRateAvg) + (0.1988 * mWeight) + (0.2017 * mAge)) / 4.184) * (mSecondsElapsed / 60);
                } else {
                    mCaloriesBurned = ((-20.4022 + (0.4472 * mHeartRateAvg) - (0.1263 * mWeight) + (0.074 * mAge)) / 4.184) * (mSecondsElapsed / 60);
                }

                //Build add slot entry
                JSONObject jsonSlotEntry = new JSONObject();
                try {
                    jsonSlotEntry.put("seconds_elapsed", mSecondsElapsed);
                    jsonSlotEntry.put("heart_rate", mheartRate);
                    //Put the entry in the slot
                    jsonSlot.put(jsonSlotEntry);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            updateUI();
            //Run again after 1 second
            mHandlerTimer.postDelayed(this, tickDelay);
        }
    };

    private void updateUI() {

        try {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvHeartRate.setText(mheartRate.toString());
                    tvCalories.setText(String.format("%.0f", mCaloriesBurned));
                    tvTimer.setText(DateUtils.formatElapsedTime(mSecondsElapsed));

                    lineDataHR.appendData(new DataPoint(mSecondsElapsed, mheartRate), true, 60);

                    //Beat the heart icon :)
                    if (heartBeatFlag == true) {
                        heartBeatFlag = false;
                        imageHeart.setImageDrawable(getResources().getDrawable(R.drawable.ic_heart));
                    } else {
                        heartBeatFlag = true;
                        imageHeart.setImageDrawable(getResources().getDrawable(R.drawable.ic_heart2));
                    }
                }
            });
        } catch (NullPointerException ex) {
            //Do nothing. Probably the UI has been destroyed.
            Log.i("myapp", "UpdateUI NullPointerException");
        }
    }


    private boolean dialogResponded = false;
    private void stopExerciseSession() {
        mHandlerTimer.removeCallbacksAndMessages(null); //stop the timer

        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setTitle("Do you want to save this exercise session?");
        dialogBuilder.setMessage("This will save the session to our server and can help you analyze your exercise progress");

        dialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialogResponded = true;
                try {
                    jsonSession.put("end_time", DateUtility.getISOStringFromDate(new Date()));
                    jsonSession.put("slot", jsonSlot);
                    jsonSession.put("calories", mCaloriesBurned);

                    //calculate min, max, avg heart rate
                    Integer minHR = 999, maxHR = 0, currHR;
                    for (int i = 0; i < jsonSlot.length(); i++) {
                        currHR = jsonSlot.getJSONObject(i).getInt("heart_rate");
                        minHR = (minHR >= currHR) ? currHR : minHR;  //Bad programming? Unreadable?
                        maxHR = (maxHR <= currHR) ? currHR : maxHR;  //conditional assignment is nice :)
                    }

                    JSONObject jsonHR = new JSONObject();
                    jsonHR.put("min", minHR);
                    jsonHR.put("max", maxHR);
                    jsonHR.put("average", mHeartRateAvg);

                    jsonSession.put("heart_rate", jsonHR);

                    //Now call api to save the exercise session
                    AsyncHttpClient client = new AsyncHttpClient();
                    client.addHeader("authorization", "Bearer " + mUserProfile.getFbAccessToken());

                    client.post(getActivity(), MyApplication.SERVER_URL + "/sessions", new StringEntity(jsonSession.toString()), "application/json",
                            new JsonHttpResponseHandler() {
                                @Override
                                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                    super.onSuccess(statusCode, headers, response);
                                    mListener.onExerciseFinished(mExerciseId, mExerciseType, mCaloriesBurned, mHeartRateAvg, tvTimer.getText().toString(), jsonSession);
                                }

                                @Override
                                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                                    super.onFailure(statusCode, headers, responseString, throwable);
                                    Toast.makeText(getActivity(), "Failed to save: " + responseString, Toast.LENGTH_SHORT).show();
                                }
                            });
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        });

        dialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialogResponded = true;
                mListener.onExerciseFinished(mExerciseId, mExerciseType, mCaloriesBurned, mHeartRateAvg, tvTimer.getText().toString(), jsonSession);            }
        });

        dialogBuilder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (!dialogResponded) {
                    dialogBuilder.create().show();
                }
            }
        });

        dialogBuilder.create().show();
    }


    public void onBtnClick(View v) {
        switch (v.getId()) {

            case R.id.btnCancel:
                mHandlerTimer.removeCallbacksAndMessages(null); //stop the timer
                getActivity().finish();
                break;

            case R.id.btnStop:
                stopExerciseSession();
                break;
        }
    }


    Button.OnClickListener onButonClickListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            onBtnClick(v);
        }
    };

}
