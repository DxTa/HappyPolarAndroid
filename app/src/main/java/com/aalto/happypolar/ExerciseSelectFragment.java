package com.aalto.happypolar;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class ExerciseSelectFragment extends Fragment {

    private OnExerciseFragmentInterface mListener;

    private JSONArray jsonExercises;

    private Spinner spinnerExercises;
    private TextView tvDescription;
    private ImageView imageExercise;

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

        spinnerExercises = (Spinner)getView().findViewById(R.id.spinnerExercises);
        spinnerExercises.setOnItemSelectedListener(spinnerSelectionListener);

        tvDescription = (TextView) getView().findViewById(R.id.tvDescription);
        imageExercise = (ImageView) getView().findViewById(R.id.imageExercise);

        AsyncHttpClient client = new AsyncHttpClient();
        client.addHeader("authorization", "Bearer " + UserProfile.getInstance().getFbAccessToken());
        client.get(MyApplication.SERVER_URL + "/exercises", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    jsonExercises = new JSONArray(new String(responseBody));
                    ArrayList<String> exerciseTitles = new ArrayList();
                    for (int i = 0; i < jsonExercises.length(); i++) {
                        JSONObject jsonEx = jsonExercises.getJSONObject(i);
                        exerciseTitles.add(jsonEx.getString("name"));
                    }

                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_spinner_item, exerciseTitles);
                    arrayAdapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );
                    spinnerExercises.setAdapter(arrayAdapter);
                    spinnerExercises.setSelection(0);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(getActivity(), "Could not load exercises", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    /*
    * Spinner Item Click listener
    * */
    Spinner.OnItemSelectedListener spinnerSelectionListener = new Spinner.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            try {
                JSONObject jsonEx = jsonExercises.getJSONObject(position);
                tvDescription.setText(jsonEx.getString("description"));

                imageExercise.setImageResource(android.R.color.transparent);
                //Download the image now
                AsyncHttpClient client = new AsyncHttpClient();
                client.get(jsonEx.getString("image"), new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        Bitmap image = BitmapFactory.decodeByteArray(responseBody, 0, responseBody.length);
                        imageExercise.setImageBitmap(image);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            //nothing
        }
    };


    /*
    Click handler for Fragment buttons
    */
    private Button.OnClickListener btnClickListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btnGo:
                    Spinner spinnerExerciseType = (Spinner) getView().findViewById(R.id.spinnerExercises);
                    //EditText numCalories = (EditText) getView().findViewById(R.id.numCalorieTarget);
                    String exerciseType = spinnerExerciseType.getSelectedItem().toString();
                    try {
                        //Integer targetCalories = Integer.parseInt(numCalories.getText().toString());
                        String exerciseId = jsonExercises.getJSONObject(spinnerExerciseType.getSelectedItemPosition()).getString("_id");
                        mListener.onExerciseSelected(exerciseId, exerciseType);
                    } catch (NumberFormatException ex) {
                        Toast.makeText(getActivity(), "Wrong input", Toast.LENGTH_SHORT).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    };

}
