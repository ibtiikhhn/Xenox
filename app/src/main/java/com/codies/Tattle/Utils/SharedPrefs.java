package com.codies.Tattle.Utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.codies.Tattle.Models.User;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SharedPrefs {
    private static SharedPrefs sharedPrefs;
    protected Context mContext;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mSharedPreferencesEditor;

    public SharedPrefs(Context context) {
        mContext = context;
        mSharedPreferences = context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        mSharedPreferencesEditor = mSharedPreferences.edit();
    }

    public static synchronized SharedPrefs getInstance(Context context) {

        if (sharedPrefs == null) {
            sharedPrefs = new SharedPrefs(context.getApplicationContext());
        }
        return sharedPrefs;
    }

    public void loginUser(boolean value) {
        mSharedPreferencesEditor.putBoolean("loginAsUser", value);
        mSharedPreferencesEditor.commit();
    }

    public void saveUserData(User user) {
        Gson gson = new Gson();
        String json = gson.toJson(user);
        mSharedPreferencesEditor.putString("User", json);
        mSharedPreferencesEditor.commit();
    }

    public User getUser() {
        if (isLoggedIn()) {
            Gson gson = new Gson();
            String json = mSharedPreferences.getString("User", "");
            return gson.fromJson(json, User.class);
        } else {
            return null;
        }

    }

    public boolean isLoggedIn() {
        return mSharedPreferences.getBoolean("loginAsUser", false);
    }

//    public String getUserId() {
//        return mSharedPreferences.getString("userId", null);
//    }

    public void saveFolderToList(String folderName) {
        List<String> folderNames;
        if (getList() == null) {
            folderNames = new ArrayList<>();
        } else {
            folderNames = getList();
        }
        folderNames.add(folderName);
        Gson gson = new Gson();
        String json = gson.toJson(folderNames);
        mSharedPreferencesEditor.putString("folders", json);


        mSharedPreferencesEditor.commit();
    }

    public List<String> getList() {
        Gson gson = new Gson();
        String json = mSharedPreferences.getString("folders", null);
        Type type = new TypeToken<List<String>>() {}.getType();
        List<String> arrayList = gson.fromJson(json, type);
        return arrayList;
    }


    public void clearPrefrences() {
        mSharedPreferencesEditor.clear().commit();
    }

    public String getUserId() {
        return null;
    }
}
