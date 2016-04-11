package com.bhorkar.heartratesensor;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;
import android.widget.Spinner;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ExerciseSelectFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class ExerciseSelectFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

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
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    /*
    Click handler for Fragment buttons
    */
    public void onButtonClick (View v) {
        switch (v.getId()) {
            case R.id.btnGo:
                Spinner spinnerExerciseType = (Spinner) getView().findViewById(R.id.spinnerExercises);
                NumberPicker numCalories = (NumberPicker) getView().findViewById(R.id.numCalorieTarget);
                String exerciseType = spinnerExerciseType.getSelectedItem().toString();
                Integer targetCalories = numCalories.getValue();
                mListener.onExerciseSelected(exerciseType, targetCalories);
                break;
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onExerciseSelected(String exerciseType, Integer targetCalories);
    }
}
