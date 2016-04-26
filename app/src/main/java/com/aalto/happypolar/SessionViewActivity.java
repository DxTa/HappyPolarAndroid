package com.aalto.happypolar;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.format.DateUtils;
import android.widget.Toast;

import com.aalto.happypolar.util.DateUtility;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class SessionViewActivity extends FragmentActivity implements OnExerciseFragmentInterface {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_view);

        //get the arguments
        JSONObject jsonSession = null;
        try {
            jsonSession = new JSONObject(getIntent().getStringExtra(ExerciseActivity.JSON_SESSION));
            String exerciseType = getIntent().getStringExtra(ExerciseActivity.EXERCISE_TYPE);

            String exerciseId = jsonSession.getString("exercise_id");
            Double caloriesBurned = jsonSession.getDouble("calories");
            Integer heartRateAvg = jsonSession.getJSONObject("heart_rate").getInt("average");

            Date timeStart = DateUtility.getDateFromISOString(jsonSession.getString("start_time"));
            Date timeEnd = DateUtility.getDateFromISOString(jsonSession.getString("end_time"));

            Long dateDiff = (timeEnd.getTime() - timeStart.getTime()) / 1000; //get time difference in seconds
            String timeElapsed = DateUtils.formatElapsedTime(dateDiff);

            ExerciseSummaryFragment exerciseSummaryFragment = new ExerciseSummaryFragment();
            Bundle args = new Bundle();
            args.putString(ExerciseActivity.EXERCISE_ID, exerciseId);
            args.putString(ExerciseActivity.EXERCISE_TYPE, exerciseType);
            //args.putInt(TARGET_CALORIES, targetCalories);
            args.putDouble(ExerciseActivity.CALORIES_BURNED, caloriesBurned);
            args.putInt(ExerciseActivity.HEART_RATE_AVG, heartRateAvg);
            args.putString(ExerciseActivity.TIME_ELAPSED, timeElapsed);
            args.putString(ExerciseActivity.JSON_SESSION, jsonSession.toString());
            exerciseSummaryFragment.setArguments(args);

            android.support.v4.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, exerciseSummaryFragment);
            transaction.commit();
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(SessionViewActivity.this, "There was a problem loading the exercise data", Toast.LENGTH_SHORT).show();
            this.finish();
        }
    }

    @Override
    public void onExerciseSelected(String exerciseId, String exerciseType) {
        //nothing
    }

    @Override
    public void onExerciseFinished(String exerciseId, String exerciseType, Double caloriesBurned, Integer heartRateAvg, String timeElapsed, JSONObject jsonSession) {
        //nothing
    }

    @Override
    public void onSummaryClose() {
        //nothing
        SessionViewActivity.this.finish();
    }
}
