package com.aalto.happypolar;


import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;


/**
 * A simple {@link Fragment} subclass.
 */
public class ExerciseProgressFragment extends Fragment {

    private OnExerciseFragmentInterface mListener;

    private Integer mtargetCalories;
    private Integer mSecondsElapsed;

    private TextView tvTimer, tvCalories, tvHeartRate;
    private LineChart lineChartHeartRate;

    private Handler mHandlerTimer = new Handler();

    //TIMER DELAY
    private final int tickDelay = 2000;
    private final int step = 2; //step counter (usually should correspond to timer delay (if 2000 then 2)

    private UserProfile mUserProfile;
    private HeartRateDevice mHeartRateDevice;

    private Long mWeight;
    private Integer mAge;
    private boolean mIsMale;

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
        mtargetCalories = getArguments().getInt(ExerciseActivity.TARGET_CALORIES);
        String exerciseType = getArguments().getString(ExerciseActivity.EXERCISE_TYPE);

        mUserProfile = UserProfile.getInstance();
        mHeartRateDevice = HeartRateDevice.getInstance();

        mWeight = mUserProfile.getWeight();
        mAge = mUserProfile.getAge();
        mIsMale = (mUserProfile.getGender().equals(UserProfile.MALE)) ? true : false;

        TextView tvExerciseType = (TextView) getActivity().findViewById(R.id.tvExerciseType);
        tvExerciseType.setText(exerciseType);

        tvTimer = (TextView) getActivity().findViewById(R.id.tvTime);
        tvCalories = (TextView) getActivity().findViewById(R.id.tvCalories);
        tvHeartRate = (TextView) getActivity().findViewById(R.id.tvHeartRate);

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
    private Runnable runnableTickTok = new Runnable() {
        @Override
        public void run() {
            //Runs every second
            mSecondsElapsed += step;  //can also act as a counter

            //Do work here
            //calculate calories, and display information.
            mheartRate = mHeartRateDevice.getHeartRate();
            mHeartRateSum += mheartRate;
            mHeartRateAvg = mHeartRateSum / mSecondsElapsed;

            //Calculate calories every 10 seconds
            if (mSecondsElapsed % 10 == 0) {
                if (mIsMale) {
                    mCaloriesBurned = ((-55.0969 + (0.6309 * mHeartRateAvg) + (0.1988 * mWeight) + (0.2017 * mAge)) / 4.184) * (mSecondsElapsed / 60);
                } else {
                    mCaloriesBurned = ((-20.4022 + (0.4472 * mHeartRateAvg) - (0.1263 * mWeight) + (0.074 * mAge)) / 4.184) * (mSecondsElapsed / 60);
                }
            }

            updateUI();
            //Run again after 1 second
            mHandlerTimer.postDelayed(this, tickDelay);
        }
    };

    private void updateUI() {

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvHeartRate.setText(mheartRate.toString());
                tvCalories.setText(mCaloriesBurned.toString());
                tvTimer.setText(DateUtils.formatElapsedTime(mSecondsElapsed));
            }
        });
    }

}
