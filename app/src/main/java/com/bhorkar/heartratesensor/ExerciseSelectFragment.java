package com.bhorkar.heartratesensor;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.Toast;

public class ExerciseSelectFragment extends Fragment {

    private OnExerciseFragmentInterface mListener;

    public static ExerciseSelectFragment newInstance() {
        return new ExerciseSelectFragment();
    }

    public ExerciseSelectFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_exercise_select, container, false);
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
        Button btnGo = (Button) getView().findViewById(R.id.btnGo);
        btnGo.setOnClickListener(btnClickListener);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    /*
    Click handler for Fragment buttons
    */
    private Button.OnClickListener btnClickListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btnGo:
                    Spinner spinnerExerciseType = (Spinner) getView().findViewById(R.id.spinnerExercises);
                    EditText numCalories = (EditText) getView().findViewById(R.id.numCalorieTarget);
                    String exerciseType = spinnerExerciseType.getSelectedItem().toString();
                    try {
                        Integer targetCalories = Integer.parseInt(numCalories.getText().toString());
                        mListener.onExerciseSelected(exerciseType, targetCalories);
                    } catch (NumberFormatException ex) {
                        Toast.makeText(getActivity(), "Wrong input", Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };

}
