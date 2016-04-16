package com.aalto.happypolar;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.sromku.simple.fb.Permission;
import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.entities.Profile;
import com.sromku.simple.fb.listeners.OnFriendsListener;
import com.sromku.simple.fb.listeners.OnLoginListener;
import com.sromku.simple.fb.listeners.OnLogoutListener;
import com.sromku.simple.fb.listeners.OnProfileListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class LoginActivity extends Activity {

    private SimpleFacebook mSimpleFacebook;
    private Menu mMenu;

    private boolean mIsLoggedIn = false;

    private TextView tvFacebook;
    private EditText editName, editEmail, editAge, editHeight, editWeight;
    private RadioButton radioMale, radioFemale;

    private String mFbAccessToken, mFbId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.aalto.happypolar",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }

        tvFacebook = (TextView) findViewById(R.id.tvFacebook);
        editName = (EditText) findViewById(R.id.editName);
        editEmail = (EditText) findViewById(R.id.editEmail);
        editAge = (EditText) findViewById(R.id.editAge);
        editHeight = (EditText) findViewById(R.id.editHeight);
        editWeight = (EditText) findViewById(R.id.editWeight);
        radioMale = (RadioButton) findViewById(R.id.radioMale);
        radioFemale = (RadioButton) findViewById(R.id.radioFemale);

        mSimpleFacebook = SimpleFacebook.getInstance(this);
        mSimpleFacebook.login(onLoginListener);

        //getActionBar().show();
    }

    @Override
    public void onResume() {
        super.onResume();
        mSimpleFacebook = SimpleFacebook.getInstance(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_login, menu);
        //menu.findItem(R.id.menu_facebook).setEnabled(false);
        mMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_facebook:
                mSimpleFacebook.login(onLoginListener);
                break;
        }
        return true;
    }

    /*
    * Button Click handler
    * */
    public void onButtonClick(View v) {
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
            Toast.makeText(LoginActivity.this, "All fields are compulsory", Toast.LENGTH_SHORT).show();
        } else {
            if (!mIsLoggedIn) {
                //If not logged in, then return;
                Toast.makeText(LoginActivity.this, "Connection to Facebook required", Toast.LENGTH_SHORT).show();
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
                client.post(MyApplication.SERVER_URL + "/users", postParams, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        try {
                            JSONObject response = new JSONObject(new String(responseBody));
                            String userId = response.getJSONObject("user").getString("_id");

                            UserProfile.initialize(
                                    userId,
                                    editName.getText().toString(),
                                    Integer.parseInt(editAge.getText().toString()),
                                    sex,
                                    editEmail.getText().toString(),
                                    Long.parseLong(editWeight.getText().toString()),
                                    Long.parseLong(editHeight.getText().toString()),
                                    mFbAccessToken,
                                    mFbId
                            );

                            //Save to settings
                            UserProfile.getInstance().save(LoginActivity.this);
                            Toast.makeText(LoginActivity.this, "User Profile saved!", Toast.LENGTH_SHORT).show();

                            //Start the home activity
                            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                            startActivity(intent);

                            //Close this activitiy
                            LoginActivity.this.finish();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        Toast.makeText(LoginActivity.this, "Could not register user. Error: " + statusCode, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }


    /*Required by Facebook library*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mSimpleFacebook.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    /*
    * Facebook callback listeners
    * */

    OnLoginListener onLoginListener = new OnLoginListener() {
        @Override
        public void onLogin(String accessToken, List<Permission> acceptedPermissions, List<Permission> declinedPermissions) {
            Log.i("myapp", "Access Token: " + accessToken);
            //mMenu.findItem(R.id.menu_facebook).setEnabled(false);
            Toast.makeText(LoginActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();
            tvFacebook.setText("Connected to Facebook!");

            mIsLoggedIn = true;
            mFbAccessToken = accessToken;

            //Initialize UserProfile from settings and compare access tokens
            try {
                UserProfile userProfile = UserProfile.initialize(LoginActivity.this);

                if (userProfile.getFbAccessToken().equals(accessToken)) {
                    //if not equal then update the settings with new token
                    userProfile.setFbAccessToken(accessToken);
                    userProfile.save(LoginActivity.this);
                }

                Toast.makeText(LoginActivity.this, "Loaded saved user profile", Toast.LENGTH_SHORT).show();

                //If no exception, then settings exist
                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                startActivity(intent);
                LoginActivity.this.finish();

            } catch (Settings.SettingNotFoundException e) {

                //On login get friends and profile.
                mSimpleFacebook.getProfile(onProfileListener);
                mSimpleFacebook.getFriends(onFriendsListener);

            }
        }

        @Override
        public void onCancel() {
            // user canceled the dialog
            Log.i("myapp", "login cancel");
            //mMenu.findItem(R.id.menu_facebook).setEnabled(true);
            Toast.makeText(LoginActivity.this, "Login canceled", Toast.LENGTH_SHORT).show();
            tvFacebook.setText("Not connected to Facebook");
            mIsLoggedIn = false;
        }

        @Override
        public void onFail(String reason) {
            // failed to login
            Log.i("myapp", "login fail");
            //mMenu.findItem(R.id.menu_facebook).setEnabled(true);
            Toast.makeText(LoginActivity.this, "Login failed", Toast.LENGTH_SHORT).show();
            tvFacebook.setText("Not connected to FB. Login failed.");
            mIsLoggedIn = false;
        }

        @Override
        public void onException(Throwable throwable) {
            // exception from facebook
            //mMenu.findItem(R.id.menu_facebook).setEnabled(true);
            Log.d("myapp", throwable.toString());
            Toast.makeText(LoginActivity.this, "ERROR: Facebook Login exception", Toast.LENGTH_SHORT).show();
            mIsLoggedIn = false;
        }

    };

    /*
    * Listener for getFriends
    * */
    private OnFriendsListener onFriendsListener = new OnFriendsListener() {
        @Override
        public void onComplete(List<Profile> response) {
            Log.i("myapp", "Friends:");
            for (Profile p : response) {
                Log.i("myapp", "Friend: " + p.getName());
            }
        }
    };

    /*
    * Listener for getProfile
    * */
    private OnProfileListener onProfileListener = new OnProfileListener() {
        @Override
        public void onComplete(Profile response) {
            super.onComplete(response);
            editName.setText(response.getName());
            editEmail.setText(response.getEmail());

            mFbId = response.getId();
        }
    };

    /*
    * Listener for logout
    * */
    private OnLogoutListener onLogoutListener = new OnLogoutListener() {
        @Override
        public void onLogout() {
            Log.i("myapp", "Logout");
        }
    };
}
