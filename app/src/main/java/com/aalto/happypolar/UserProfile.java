package com.aalto.happypolar;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;

import com.sromku.simple.fb.entities.User;

/**
 * Created by Gaurav on 14-Apr-16.
 */
public class UserProfile {

    /* settings Keys */
    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String AGE = "age";
    public static final String GENDER = "gender";
    public static final String WEIGHT = "weight";
    public static final String HEIGHT = "height";
    public static final String EMAIL = "email";
    public static final String FB_ACCESS_TOKEN = "fb_access_token";
    public static final String FB_ID = "fb_id";

    public static final String MALE = "male";
    public static final String FEMALE = "female";

    private static UserProfile mInstance = null;

    private String mId;
    private String mName;
    private Integer mAge;
    private Long mWeight;
    private Long mHeight;
    private String mGender;
    private String mEmail;
    private String mFbAccessToken;
    private String mFbId;

    private UserProfile(String id, String name, Integer age, String gender, String email, Long weight, Long height, String fbAccessToken, String fbId) {
        mId = id;
        mName = name;
        mAge = age;
        mGender = gender;
        mEmail = email;
        mWeight = weight;
        mHeight = height;
        mFbAccessToken = fbAccessToken;
        mFbId = fbId;
    }

    public static UserProfile getInstance() {
        if (mInstance == null) {
            throw new NullPointerException("UserProfile instance not initialized");
        } else {
            return mInstance;
        }
    }

    public static UserProfile initialize(String id, String name, Integer age, String gender, String email, Long weight, Long height, String fbAccessToken, String fbId) {
        mInstance = new UserProfile(id, name, age, gender, email,  weight, height, fbAccessToken, fbId);
        return mInstance;
    }

    /*
    * Initialize from SharedPreferences
    * */
    public static UserProfile initialize(Context context) throws Settings.SettingNotFoundException {
        SharedPreferences settings = context.getSharedPreferences(MyApplication.SETTINGS_NAME, 0);
        if (settings.getString(UserProfile.FB_ID, "") == "") {
            throw new Settings.SettingNotFoundException("Setting is not initialized");
        }
        mInstance = new UserProfile(
                settings.getString(UserProfile.ID, ""),
                settings.getString(UserProfile.NAME, ""),
                settings.getInt(UserProfile.AGE, 0),
                settings.getString(UserProfile.GENDER, ""),
                settings.getString(UserProfile.EMAIL, ""),
                settings.getLong(UserProfile.WEIGHT, 0),
                settings.getLong(UserProfile.HEIGHT, 0),
                settings.getString(UserProfile.FB_ACCESS_TOKEN, ""),
                settings.getString(UserProfile.FB_ID, "")
                );
        return mInstance;
    }


    /*
    * Save UserProfile to settings
    * */
    public void save(Context context) {
        if (mInstance == null) {
            throw new NullPointerException("UserProfile instance not initialized");
        }
        SharedPreferences settings = context.getSharedPreferences(MyApplication.SETTINGS_NAME, 0);
        SharedPreferences.Editor settingsEditor = settings.edit();
        settingsEditor.putString(UserProfile.ID, mId);
        settingsEditor.putString(UserProfile.NAME, mName);
        settingsEditor.putInt(UserProfile.AGE, mAge);
        settingsEditor.putString(UserProfile.EMAIL, mEmail);
        settingsEditor.putLong(UserProfile.WEIGHT, mWeight);
        settingsEditor.putLong(UserProfile.HEIGHT, mHeight);
        settingsEditor.putString(UserProfile.GENDER, mGender);
        settingsEditor.putString(UserProfile.FB_ACCESS_TOKEN, mFbAccessToken);
        settingsEditor.putString(UserProfile.FB_ID, mFbId);
        settingsEditor.commit();
    }

    public UserProfile setFbAccessToken(String fbAccessToken) {
        mFbAccessToken = fbAccessToken;
        return this;
    }

    public String getName() {
        return mName;
    }

    public Integer getAge() {
        return mAge;
    }

    public String getEmail() {
        return mEmail;
    }

    public Long getWeight() {
        return mWeight;
    }

    public Long getHeight() {
        return mHeight;
    }

    public String getGender() {
        return mGender;
    }

    public String getFbAccessToken() {
        return mFbAccessToken;
    }

    public String getFbId() {
        return mFbId;
    }
}
