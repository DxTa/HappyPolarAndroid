package com.aalto.happypolar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class EditProfileActivity extends Activity {

    private EditText editName, editEmail, editAge, editHeight, editWeight;
    private RadioButton radioMale, radioFemale;

    UserProfile userProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        editName = (EditText) findViewById(R.id.editName);
        editEmail = (EditText) findViewById(R.id.editEmail);
        editAge = (EditText) findViewById(R.id.editAge);
        editHeight = (EditText) findViewById(R.id.editHeight);
        editWeight = (EditText) findViewById(R.id.editWeight);
        radioMale = (RadioButton) findViewById(R.id.radioMale);
        radioFemale = (RadioButton) findViewById(R.id.radioFemale);

        userProfile = UserProfile.getInstance();

        editName.setText(userProfile.getName());
        editEmail.setText(userProfile.getEmail());
        editAge.setText(userProfile.getAge().toString());
        editHeight.setText(userProfile.getHeight().toString());
        editWeight.setText(userProfile.getWeight().toString());

        if (userProfile.getGender().equals(UserProfile.MALE)) {
            radioMale.setChecked(true);
            radioFemale.setChecked(false);
        } else {
            radioFemale.setChecked(true);
            radioMale.setChecked(false);
        }
    }


    //Click handler for all buttons
    public void onButtonClick(View v) {
        switch(v.getId()) {
            case R.id.btnSave:
                saveUser();
                break;

            case R.id.btnClose:
                EditProfileActivity.this.finish();
                break;
        }
    }


    private void saveUser() {
        boolean isOk = true;
        if (editName.getText().toString() == "") {
            isOk = false;
        }
        if (editEmail.getText().toString() == "") {
            isOk = false;
        }
        if (editAge.getText().toString() == "") {
            isOk = false;
        }
        if (editWeight.getText().toString() == "") {
            isOk = false;
        }
        if (editHeight.getText().toString() == "") {
            isOk = false;
        }

        if (isOk == false) {
            Toast.makeText(EditProfileActivity.this, "All fields are compulsory", Toast.LENGTH_SHORT).show();
        } else {
            String gender;
            if (radioMale.isChecked()) {
                gender = UserProfile.MALE;
            } else {
                gender = UserProfile.FEMALE;
            }

            // Posting user creation request to the server
            AsyncHttpClient client = new AsyncHttpClient();
            RequestParams postParams = new RequestParams();
            postParams.add("name", editName.getText().toString());
            postParams.add("age", editAge.getText().toString());
            postParams.add("height", editHeight.getText().toString());
            postParams.add("weight", editWeight.getText().toString());
            postParams.add("gender", gender);
            //TODO Add more after API is updated by backend team

            final String sex = gender;
            client.addHeader("authorization", "Bearer " + userProfile.getFbAccessToken());
            client.put(MyApplication.SERVER_URL + "/users/" + userProfile.getId(), postParams, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    super.onSuccess(statusCode, headers, response);
                    try {
                        UserProfile.initialize(
                                response.getString("_id"),
                                editName.getText().toString(),
                                Integer.parseInt(editAge.getText().toString()),
                                sex,
                                editEmail.getText().toString(),
                                Long.parseLong(editWeight.getText().toString()),
                                Long.parseLong(editHeight.getText().toString()),
                                userProfile.getFbAccessToken(),
                                userProfile.getFbId()
                        );

                        //Save to settings
                        UserProfile.getInstance().save(EditProfileActivity.this);
                        Toast.makeText(EditProfileActivity.this, "User Profile updated!", Toast.LENGTH_SHORT).show();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    super.onFailure(statusCode, headers, responseString, throwable);
                    Toast.makeText(EditProfileActivity.this, "Could not update user. Error: " + responseString, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
