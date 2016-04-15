package com.aalto.happypolar;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
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

import com.sromku.simple.fb.Permission;
import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.SimpleFacebookConfiguration;
import com.sromku.simple.fb.actions.Cursor;
import com.sromku.simple.fb.entities.Profile;
import com.sromku.simple.fb.listeners.OnFriendsListener;
import com.sromku.simple.fb.listeners.OnLoginListener;
import com.sromku.simple.fb.listeners.OnLogoutListener;
import com.sromku.simple.fb.listeners.OnProfileListener;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class LoginActivity extends Activity {

    private SimpleFacebook mSimpleFacebook;
    private Menu mMenu;

    private TextView tvFacebook;
    private EditText editName, editEmail, editAge, editHeight, editWeight;
    private RadioButton radioMale, radioFemale;


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

        Permission[] permissions = new Permission[] {
                Permission.EMAIL,
                Permission.USER_FRIENDS,
                Permission.PUBLIC_PROFILE
        };

        SimpleFacebookConfiguration configuration = new SimpleFacebookConfiguration.Builder()
                .setAppId("495899663951920")
                .setNamespace("happypolar")
                .setPermissions(permissions)
                .build();

        SimpleFacebook.setConfiguration(configuration);
        mSimpleFacebook = SimpleFacebook.getInstance(this);

        mSimpleFacebook.login(onLoginListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        mSimpleFacebook = SimpleFacebook.getInstance(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_login, menu);
        menu.findItem(R.id.menu_facebook).setEnabled(false);
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

    public void buttonClick (View v) {
        switch (v.getId()) {
            case R.id.login:
                mSimpleFacebook.login(onLoginListener);
                break;

            case R.id.logout:
                mSimpleFacebook.logout(onLogoutListener);
                break;

            case R.id.friends:
                mSimpleFacebook.getFriends(onFriendsListener);
                mSimpleFacebook.getProfile(onProfileListener);

        }
    }

    private OnProfileListener onProfileListener = new OnProfileListener() {
        @Override
        public void onComplete(Profile response) {
            super.onComplete(response);

        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mSimpleFacebook.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    OnLoginListener onLoginListener = new OnLoginListener() {
        @Override
        public void onLogin(String accessToken, List<Permission> acceptedPermissions, List<Permission> declinedPermissions) {
            // change the state of the button or do whatever you want
            Log.i("myapp", "Access Token: " + accessToken);
            mMenu.findItem(R.id.menu_facebook).setEnabled(false);
            Toast.makeText(LoginActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();
            tvFacebook.setText("Connected to Facebook!");

        }

        @Override
        public void onCancel() {
            // user canceled the dialog
            Log.i("myapp", "login cancel");
            mMenu.findItem(R.id.menu_facebook).setEnabled(true);
            Toast.makeText(LoginActivity.this, "Login canceled", Toast.LENGTH_SHORT).show();
            tvFacebook.setText("Not connected to Facebook");
        }

        @Override
        public void onFail(String reason) {
            // failed to login
            Log.i("myapp", "login fail");
            mMenu.findItem(R.id.menu_facebook).setEnabled(true);
            Toast.makeText(LoginActivity.this, "Login failed", Toast.LENGTH_SHORT).show();
            tvFacebook.setText("Not connected to FB. Login failed.");
        }

        @Override
        public void onException(Throwable throwable) {
            // exception from facebook
            mMenu.findItem(R.id.menu_facebook).setEnabled(true);
            Toast.makeText(LoginActivity.this, "ERROR: Facebook Login exception", Toast.LENGTH_SHORT).show();
        }

    };

    private OnFriendsListener onFriendsListener = new OnFriendsListener() {
        @Override
        public void onComplete(List<Profile> response) {
            Log.i("myapp", "No of frns: " + response.size());
        }
    };

    private OnLogoutListener onLogoutListener = new OnLogoutListener() {
        @Override
        public void onLogout() {
            Log.i("myapp", "Logout");
        }
    };
}
