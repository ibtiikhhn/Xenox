package com.codies.Tattle.DataUploadServices;

import android.app.Application;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.codies.Tattle.LocalFilesDB.DocFile;
import com.codies.Tattle.LocalFilesDB.DocFileRepo;
import com.codies.Tattle.OtherUtils.SharedPrefs;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.io.File;
import java.nio.file.Files;
import java.util.List;

public class DocumentFilesUploader extends Worker {

    public static final String TAG = "DocumentFilesUploader";

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    FirebaseAuth firebaseAuth;
    SharedPrefs sharedPrefs;
    StorageReference storageReference;
    List<DocFile> allFiles;
    DocFileRepo docFileRepo;
    boolean uploaded = false;



    public DocumentFilesUploader(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        docFileRepo = new DocFileRepo((Application) context.getApplicationContext());
        allFiles = docFileRepo.getAllDocFiles();
        sharedPrefs = SharedPrefs.getInstance(this.getApplicationContext());
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = firebaseDatabase.getReference();
        storageReference = FirebaseStorage.getInstance().getReference(firebaseAuth.getCurrentUser().getUid());
    }

    @NonNull
    @Override
    public Result doWork() {
        Data taskData = getInputData();

        Log.i(TAG, "doWork: files size : " + allFiles.size());
        if (allFiles != null && !allFiles.isEmpty()) {
            for (int i = 0; i < allFiles.size(); i++) {
                File file = getFileFromPath(allFiles.get(i).getDocPath());
                DocFile docFile = allFiles.get(i);
                if (file != null&& !docFile.isUploaded()) {
                    uploaded = false;
                    uploadFile(file, docFile);
                    while (!uploaded) {

                    }
                }
            }
        }

        Data outputData = new Data.Builder().putBoolean("filesSyncedWithServer", true).build();
        return Result.success(outputData);
    }

    public void uploadFile(File file,DocFile docFile) {
        Log.i(TAG, "uploadFile: file name "+file.getName());
        StorageReference mStorageRef = this.storageReference.child("UserFiles").child(file.getName());

        Uri uri = Uri.fromFile(file);
        mStorageRef.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.i(TAG, "onSuccess: file uploaded"+taskSnapshot.getUploadSessionUri().toString());
                mStorageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        databaseReference.child("users").child(firebaseAuth.getCurrentUser().getUid()).child("Documents").push().setValue(uri.toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                docFile.setUploaded(true);
                                docFileRepo.update(docFile);
                                uploaded = true;
                                Log.i(TAG, "onSuccess: Successfuly upload doc files and saved the urls");
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                uploaded = true;
                                Log.i(TAG, "onFailure: errorsaving urls " + e.getMessage());
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        uploaded = true;
                        Log.i(TAG, "onFailure: error getting zip url " + e.getMessage());
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                uploaded = true;
                Log.i(TAG, "onFailure: error uploading zip file : " + e.getMessage());
            }
        });
    }

    public File getFileFromPath(String path) {
        File file = new File(path);
        if (file.exists()) {
            return file;
        }
        return null;
    }
}
