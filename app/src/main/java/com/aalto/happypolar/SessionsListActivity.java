package com.aalto.happypolar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class SessionsListActivity extends Activity {
    private final String ALL_TIME = "All Time";
    private final String TODAY = "Today";
    private final String YESTERDAY = "Yesterday";
    private final String LAST_7_DAYS = "Last 7 days";

    String[] timeSlots = {ALL_TIME, TODAY, YESTERDAY, LAST_7_DAYS};

    private JSONArray jsonSessions = null;
    private JSONArray jsonExercises = null;

    private Spinner mSpinnerExerciseType, mSpinnerTimeSlot;
    private ListView listSessions;

    private ArrayList<String> exerciseTypeTitle;
    private ArrayList<String> exerciseTypeId;

    private UserProfile mUserProfile;

    private ArrayList<Integer> toShowIndices;
    private SessionListAdapter sessionListAdapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sessions_list);

        listSessions = (ListView) findViewById(R.id.listSessions);
        listSessions.setOnItemClickListener(sessionListItemClickListener);

        mUserProfile = UserProfile.getInstance();

        toShowIndices = new ArrayList<Integer>();

        getSessions();
        getExercises();
        //setupSpinners() will be called by the above functions
    }


    private void getSessions() {
        AsyncHttpClient client = new AsyncHttpClient();
        client.addHeader("authorization", "Bearer " + mUserProfile.getFbAccessToken());
        client.get(MyApplication.SERVER_URL + "/users/" + mUserProfile.getId() + "/sessions", new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                super.onSuccess(statusCode, headers, response);
                jsonSessions = response;
                setupSpinners();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                Toast.makeText(SessionsListActivity.this, "Error: " + responseString, Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void getExercises() {
        AsyncHttpClient client = new AsyncHttpClient();
        client.addHeader("authorization", "Bearer " + mUserProfile.getFbAccessToken());
        client.get(MyApplication.SERVER_URL + "/exercises", new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                super.onSuccess(statusCode, headers, response);
                jsonExercises = response;
                setupSpinners();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                Toast.makeText(SessionsListActivity.this, "Error: " + responseString, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupSpinners() {
        if (jsonExercises != null && jsonSessions != null) {
            //Time Slot Spinner
            mSpinnerTimeSlot = (Spinner) findViewById(R.id.spinnerTimeSlot);
            ArrayAdapter<String> timeSlotsAdapter = new ArrayAdapter<String>(SessionsListActivity.this, android.R.layout.simple_spinner_item, timeSlots);
            timeSlotsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mSpinnerTimeSlot.setAdapter(timeSlotsAdapter);

            //Exercise Type Spinner
            mSpinnerExerciseType = (Spinner) findViewById(R.id.spinnerExerciseType);

            exerciseTypeTitle = new ArrayList<String>();
            exerciseTypeTitle.add("All Exercises");

            exerciseTypeId = new ArrayList<String>();
            exerciseTypeId.add("nothing"); //doesn't point to anyting in the jsonExercises

            for (int i = 0; i < jsonExercises.length(); i++) {
                try {
                    exerciseTypeTitle.add(jsonExercises.getJSONObject(i).getString("name"));
                    exerciseTypeId.add(jsonExercises.getJSONObject(i).getString("_id"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            ArrayAdapter<String> exerciseTypeAdapter = new ArrayAdapter<String>(SessionsListActivity.this, android.R.layout.simple_spinner_item, exerciseTypeTitle);
            exerciseTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mSpinnerExerciseType.setAdapter(exerciseTypeAdapter);

            //set onitemselected listeners
            mSpinnerTimeSlot.setOnItemSelectedListener(spinnerOnItemSelectedListener);
            mSpinnerExerciseType.setOnItemSelectedListener(spinnerOnItemSelectedListener);

            mSpinnerTimeSlot.setSelection(0);
        }
    }

    private Spinner.OnItemSelectedListener spinnerOnItemSelectedListener = new Spinner.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            //Load the Sessions into the listview here
            String exerciseId = exerciseTypeId.get(mSpinnerExerciseType.getSelectedItemPosition());
            String timeSlot = timeSlots[mSpinnerTimeSlot.getSelectedItemPosition()];

            toShowIndices.clear();

            for (int i = 0; i < jsonSessions.length(); i++) {
                try {
                    JSONObject jsonSession = jsonSessions.getJSONObject(i);

                    if (exerciseId.equals("nothing")) {
                        //add all exercises
                        toShowIndices.add(i);
                    } else if (jsonSession.getString("exercise_id").equals(exerciseId)) {
                        //add session for selected exercise. If exercise selection is ALL EXERCISE, then add all sessions
                        toShowIndices.add(i);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            if (sessionListAdapter == null) {
                sessionListAdapter = new SessionListAdapter(SessionsListActivity.this, jsonSessions, jsonExercises, toShowIndices);
                listSessions.setAdapter(sessionListAdapter);
            }

            sessionListAdapter.notifyDataSetChanged();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            //Nothing
        }
    };


    //Listener for click of session in list view
    ListView.OnItemClickListener sessionListItemClickListener = new ListView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            try {
                JSONObject jsonSession = sessionListAdapter.getSession(position);
                String exerciseType = sessionListAdapter.getExerciseTypeOfSession(position);

                Intent intentSessionView = new Intent(SessionsListActivity.this, SessionViewActivity.class);
                intentSessionView.putExtra(ExerciseActivity.JSON_SESSION, jsonSession.toString());
                intentSessionView.putExtra(ExerciseActivity.EXERCISE_TYPE, exerciseType);
                startActivity(intentSessionView);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
}
