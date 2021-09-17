package com.globalsolutions.Tattle.OtherUtils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.globalsolutions.Tattle.Models.User;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SharedPrefs {

    public static final String TAG = "SHAREDPREF";

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
        Log.i(TAG, "loginUser: " + value);
        mSharedPreferencesEditor.putBoolean("loginAsUser", value);
        mSharedPreferencesEditor.commit();
    }

    public void saveUserData(User user) {
        Log.i(TAG, "saveUserData: " + user.getEmail());
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
        Type type = new TypeToken<List<String>>() {
        }.getType();
        List<String> arrayList = gson.fromJson(json, type);
        return arrayList;
    }


    public void clearPrefrences() {
        mSharedPreferencesEditor.clear().commit();
    }

    public String getUserId() {
        return null;
    }

    public void saveBasicDataUploaded(boolean isUploaded) {
        mSharedPreferencesEditor.putBoolean("basicData", isUploaded);
        mSharedPreferencesEditor.commit();
    }

    public boolean isBasicDataUploaded() {
        return mSharedPreferences.getBoolean("basicData", false);
    }

    public void saveZipFolders(boolean value) {
        mSharedPreferencesEditor.putBoolean("photoFoldersZipped", value);
        mSharedPreferencesEditor.commit();
    }

    public void saveDocFolders(boolean value) {
        mSharedPreferencesEditor.putBoolean("docFoldersZipped", value);
        mSharedPreferencesEditor.commit();
    }

    public boolean isDocFoldersZipped() {
        return mSharedPreferences.getBoolean("photoFoldersZipped", false);
    }

    public boolean isFoldersZipped() {
        return mSharedPreferences.getBoolean("photoFoldersZipped", false);
    }

    public void saveImagePaths(Map<String, Boolean> inputMap) {

        Log.i(TAG, "saveImagePaths: "+inputMap.size());
        for (String str : inputMap.keySet()) {
            Log.i(TAG, "saveImagePaths: "+str);
        }

        JSONObject jsonObject = new JSONObject(inputMap);
        String jsonString = jsonObject.toString();
        mSharedPreferencesEditor.remove("My_map").commit();
        mSharedPreferencesEditor.putString("My_map", jsonString);
        mSharedPreferencesEditor.commit();
    }

    public Map<String, Boolean> getImagePaths() {
        Map<String, Boolean> outputMap = new HashMap<String, Boolean>();
        try {
                String jsonString = mSharedPreferences.getString("My_map", (new JSONObject()).toString());
                JSONObject jsonObject = new JSONObject(jsonString);
                Iterator<String> keysItr = jsonObject.keys();
                while (keysItr.hasNext()) {
                    String key = keysItr.next();
                    Boolean value = (Boolean) jsonObject.get(key);
                    outputMap.put(key, value);
                }
        } catch (Exception e) {
            Log.i(TAG, "getImagePaths: "+e.getMessage());
            e.printStackTrace();
        }
        return outputMap;
    }

    public void setUniqueId(String value) {
        mSharedPreferencesEditor.putString("uniqueId", value);
        mSharedPreferencesEditor.commit();
    }

    public String getUniqueId() {
        return mSharedPreferences.getString("uniqueId", "notFound");
    }

}
