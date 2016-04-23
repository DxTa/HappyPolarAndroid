package com.aalto.happypolar;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.util.Base64;
import android.util.Log;

import com.sromku.simple.fb.Permission;
import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.SimpleFacebookConfiguration;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Gaurav on 15-Apr-16.
 */
public class MyApplication extends Application {
    //public static final String SERVER_URL = "http://happypolar.fi:3000/api";
    public static final String SERVER_URL = "http://82.130.13.85:3000/api";
    public static final String SETTINGS_NAME = "HappyPolarSettings";
    private static SharedPreferences mSettings;

    @Override
    public void onCreate() {
        super.onCreate();

        Permission[] permissions = new Permission[] {
                Permission.EMAIL //,
                //Permission.USER_FRIENDS,
                //Permission.PUBLIC_PROFILE
        };

        SimpleFacebookConfiguration configuration = new SimpleFacebookConfiguration.Builder()
                .setAppId("495899663951920")
                .setNamespace("happypolar")
                .setPermissions(permissions)
                .build();

        SimpleFacebook.setConfiguration(configuration);


        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.aalto.happypolar",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("myapp:", "KeyHash: " + Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }
    }
}
