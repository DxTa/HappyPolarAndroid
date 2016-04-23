package com.aalto.happypolar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.aalto.happypolar.util.DateUtility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Gaurav on 22-Apr-16.
 */
public class SessionListAdapter extends ArrayAdapter<JSONArray> {

    JSONArray jsonSessions;
    JSONArray jsonExercises;
    ArrayList<Integer> toShowIndices;
    Context context;

    public SessionListAdapter(Context context, JSONArray jsonSessions, JSONArray jsonExercises, ArrayList<Integer> toShowIndices) {
        super(context, R.layout.listitem_session);
        this.jsonSessions = jsonSessions;
        this.jsonExercises = jsonExercises;
        this.toShowIndices = toShowIndices;
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.listitem_session, parent, false);
        TextView txtExerciseName = (TextView) rowView.findViewById(R.id.txtExerciseName);
        TextView txtSessionDate = (TextView) rowView.findViewById(R.id.txtSessionDate);
        TextView txtCalories = (TextView) rowView.findViewById(R.id.txtCaloriesBurned);

        try {
            JSONObject jsonSession = jsonSessions.getJSONObject(toShowIndices.get(position));

            for (int i = 0; i < jsonExercises.length(); i++) {
                if (jsonSession.getString("exercise_id").equals(jsonExercises.getJSONObject(i).getString("_id"))) {
                    txtExerciseName.setText(jsonExercises.getJSONObject(i).getString("name"));
                    break;
                }
            }

            txtSessionDate.setText(DateUtility.getDateFromISOString(jsonSession.getString("start_time")).toString());
            txtCalories.setText(String.format("%.0f calories burned", jsonSession.getDouble("calories")));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return rowView;
    }

    @Override
    public int getCount() {
        return toShowIndices.size();
    }

    public JSONObject getSession(int index) throws JSONException {
        JSONObject jsonSess = jsonSessions.getJSONObject(toShowIndices.get(index));
        return jsonSess;
    }

    public String getExerciseTypeOfSession(int index) {
        JSONObject jsonSess;
        String exerciseType = null;
        try {
            jsonSess = jsonSessions.getJSONObject(toShowIndices.get(index));
            String exerciseId = jsonSess.getString("exercise_id");

            for (int i = 0; i < jsonExercises.length(); i++) {
                if (exerciseId.equals(jsonExercises.getJSONObject(i).getString("_id"))) {
                    exerciseType = jsonExercises.getJSONObject(i).getString("name");
                    break;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return exerciseType;
    }
}
