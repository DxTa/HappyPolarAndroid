package com.bhorkar.heartratesensor;

import android.app.FragmentTransaction;
import android.net.Uri;

import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;

public class ExerciseActivity extends FragmentActivity implements OnExerciseFragmentInterface {

    public static final String EXERCISE_TYPE = "EXERCISE_TYPE";
    public static final String TARGET_CALORIES = "TARGET_CALORIES";

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
    }

    /*
    * Event fired by the Exercise Selection fragment
    * Action - Detach the fragment, and start the exercise fragment
    * */
    @Override
    public void onExerciseSelected(String exerciseType, Integer targetCalories) {
        /* Starting the exercise progress fragment */
        ExerciseProgressFragment exerciseProgressFragment = new ExerciseProgressFragment();
        Bundle args = new Bundle();
        args.putString(EXERCISE_TYPE, exerciseType);
        args.putInt(TARGET_CALORIES, targetCalories);
        exerciseProgressFragment.setArguments(args);

        android.support.v4.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, exerciseProgressFragment);
        //transaction.addToBackStack(null);
        transaction.commit();
    }
}
