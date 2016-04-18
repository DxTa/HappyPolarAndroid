package com.aalto.happypolar;

import android.app.FragmentTransaction;
import android.os.PowerManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import org.json.JSONObject;

public class ExerciseActivity extends FragmentActivity implements OnExerciseFragmentInterface {

    public static final String EXERCISE_TYPE = "EXERCISE_TYPE";
    public static final String TARGET_CALORIES = "TARGET_CALORIES";
    public static final String EXERCISE_ID = "EXERCISE_ID";
    public static final String CALORIES_BURNED = "CALORIES_BURNED";
    public static final String HEART_RATE_AVG = "HEART_RATE_AVG";
    public static final String TIME_ELAPSED = "TIME_ELAPSED";
    public static final String JSON_SESSION = "JSON_SESSION";

    PowerManager.WakeLock mWakeLock;
    ExerciseProgressFragment exerciseProgressFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise);

        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        if (findViewById(R.id.fragment_container) != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }

            // Create a new Fragment to be placed in the activity layout
            ExerciseSelectFragment exerciseSelectFragment = new ExerciseSelectFragment();

            // Add the fragment to the 'fragment_container' FrameLayout
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, exerciseSelectFragment).commit();
        }

        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "HappyPolarWakeLockTag");
        mWakeLock.acquire();
    }

    @Override
    protected void onDestroy () {
        mWakeLock.release();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (exerciseProgressFragment != null) {
            exerciseProgressFragment.onButtonClick(findViewById(R.id.btnCancel));
        }
    }

    /*
    * Event fired by the Exercise Selection fragment
    * Action - Detach the fragment, and start the exercise fragment
    * */
    @Override
    public void onExerciseSelected(String exerciseId, String exerciseType, Integer targetCalories) {
        /* Starting the exercise progress fragment */
        exerciseProgressFragment = new ExerciseProgressFragment();
        Bundle args = new Bundle();
        args.putString(EXERCISE_ID, exerciseId);
        args.putString(EXERCISE_TYPE, exerciseType);
        args.putInt(TARGET_CALORIES, targetCalories);
        exerciseProgressFragment.setArguments(args);

        android.support.v4.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, exerciseProgressFragment);
        //transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onExerciseFinished(String exerciseId, String exerciseType, Integer targetCalories, Double caloriesBurned, Integer heartRateAvg, String timeElapsed, JSONObject jsonSession) {
        ExerciseSummaryFragment exerciseSummaryFragment = new ExerciseSummaryFragment();
        Bundle args = new Bundle();
        args.putString(EXERCISE_ID, exerciseId);
        args.putString(EXERCISE_TYPE, exerciseType);
        args.putInt(TARGET_CALORIES, targetCalories);
        args.putDouble(CALORIES_BURNED, caloriesBurned);
        args.putInt(HEART_RATE_AVG, heartRateAvg);
        args.putString(TIME_ELAPSED, timeElapsed);
        args.putString(JSON_SESSION, jsonSession.toString());
        exerciseSummaryFragment.setArguments(args);

        android.support.v4.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, exerciseSummaryFragment);
        transaction.commit();
    }

    @Override
    public void onSummaryClose() {
        this.finish();
    }
}
