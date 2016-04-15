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

    private final int tickDelay = 1000;

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

        TextView tvExerciseType = (TextView) getActivity().findViewById(R.id.tvExerciseType);
        tvExerciseType.setText(exerciseType);

        tvTimer = (TextView) getActivity().findViewById(R.id.tvTime);
        tvCalories = (TextView) getActivity().findViewById(R.id.tvCalories);
        tvHeartRate = (TextView) getActivity().findViewById(R.id.tvHeartRate);

        /*Start a timer for every second*/
        mSecondsElapsed = 0;
        mHandlerTimer.postDelayed(runnableTickTok, tickDelay);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    private Runnable runnableTickTok = new Runnable() {
        @Override
        public void run() {
            //Runs every second

            //Do work here
            //Increment timer, calculate calories, and display information.
            mSecondsElapsed++;
            tvHeartRate.setText(HeartRateDevice.getInstance().getHeartRate().toString());

            tvTimer.setText(DateUtils.formatElapsedTime(mSecondsElapsed));

            //Run again after 1 second
            mHandlerTimer.postDelayed(this, tickDelay);
        }
    };

}
