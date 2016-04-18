package com.aalto.happypolar;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;


public class ExerciseSummaryFragment extends Fragment {

    private OnExerciseFragmentInterface mListener;

    public ExerciseSummaryFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_exercise_summary, container, false);
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


    private Button.OnClickListener onButtonClickLister = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btnClose:
                    mListener.onSummaryClose();
                    break;
            }
        }
    };

    public void onStart() {
        super.onStart();
        Integer targetCalories = getArguments().getInt(ExerciseActivity.TARGET_CALORIES);
        String exerciseType = getArguments().getString(ExerciseActivity.EXERCISE_TYPE);
        String exerciseId = getArguments().getString(ExerciseActivity.EXERCISE_ID);
        Double caloriesBurned = getArguments().getDouble(ExerciseActivity.CALORIES_BURNED);
        Integer heartRateAvg = getArguments().getInt(ExerciseActivity.HEART_RATE_AVG);
        String timeElapsed = getArguments().getString(ExerciseActivity.TIME_ELAPSED);
        JSONObject jsonSession;
        try {
            jsonSession = new JSONObject(getArguments().getString(ExerciseActivity.JSON_SESSION));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        TextView tvTimer, tvCalories, tvHeartRate, tvExerciseType;

        tvTimer = (TextView) getActivity().findViewById(R.id.tvTime);
        tvCalories = (TextView) getActivity().findViewById(R.id.tvCalories);
        tvHeartRate = (TextView) getActivity().findViewById(R.id.tvHeartRate);
        tvExerciseType = (TextView) getActivity().findViewById(R.id.tvExerciseType);

        tvTimer.setText(timeElapsed);
        tvCalories.setText(String.format("%.0f", caloriesBurned) + " / " + targetCalories.toString());
        tvHeartRate.setText(heartRateAvg.toString());
        tvExerciseType.setText(exerciseType);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

}
