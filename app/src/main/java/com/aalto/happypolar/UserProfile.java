package com.aalto.happypolar;

/**
 * Created by Gaurav on 14-Apr-16.
 */
public class UserProfile {
    private static UserProfile mInstance = null;

    private String mName;
    private Integer mAge;
    private Long mWeight;
    private Long mHeight;

    private UserProfile(String name, Integer age, Long weight, Long height) {
        mName = name;
        mAge = age;
        mWeight = weight;
        mHeight = height;
    }

    public static UserProfile getInstance() {
        if (mInstance == null) {
            throw new NullPointerException("UserProfile instance not initialized");
        } else {
            return mInstance;
        }
    }

    public static UserProfile initialize(String name, Integer age, Long weight, Long height) {
        mInstance = new UserProfile(name, age, weight, height);
        return mInstance;
    }

    public String getName() {
        return mName;
    }

    public Integer getAge() {
        return mAge;
    }

    public Long getWeight() {
        return mWeight;
    }

    public Long getHeight() {
        return mHeight;
    }
}
