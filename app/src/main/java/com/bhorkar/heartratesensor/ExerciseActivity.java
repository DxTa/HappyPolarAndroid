package com.bhorkar.heartratesensor;

import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;

public class ExerciseActivity extends ActionBarActivity implements ExerciseSelectFragment.OnFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise);
    }

    @Override
    public void onExerciseSelected(String exerciseType, Integer targetCalories) {
        
    }
}
