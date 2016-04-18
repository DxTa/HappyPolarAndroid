package com.aalto.happypolar;

import org.json.JSONObject;

import java.util.Date;

/**
 * Created by Gaurav on 12-Apr-16.
 * Interface for Exercise Fragments
 */
public interface OnExerciseFragmentInterface {
    void onExerciseSelected(String exerciseId, String exerciseType, Integer targetCalories);

    void onExerciseFinished(
            String exerciseId,
            String exerciseType,
            Integer targetCalories,
            Double caloriesBurned,
            Integer heartRateAvg,

            String timeElapsed,
            JSONObject jsonSession
            );

    void onSummaryClose();
}