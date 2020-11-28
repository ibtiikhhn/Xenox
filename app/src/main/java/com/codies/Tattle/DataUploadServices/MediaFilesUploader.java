package com.codies.Tattle.DataUploadServices;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.codies.Tattle.ImageFilesDB.ZipFolder;
import com.codies.Tattle.ImageFilesDB.ZipRepo;
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

public class MediaFilesUploader extends Worker {

    private static final String TAG = "ContactsUploadService";

    public MediaFilesUploader(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    FirebaseAuth firebaseAuth;
    SharedPrefs sharedPrefs;
    StorageReference storageReference;
    ZipRepo zipRepo;
    ZipFolder currentFolder;

    @NonNull
    @Override
    public Result doWork() {
        zipRepo = new ZipRepo((Application) this.getApplicationContext());
        sharedPrefs = SharedPrefs.getInstance(this.getApplicationContext());
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = firebaseDatabase.getReference();
        storageReference = FirebaseStorage.getInstance().getReference("UserFolders");

        if (!zipRepo.getAllZipFolders().isEmpty()) {
            for (ZipFolder zipFolder : zipRepo.getAllZipFolders()) {
                if (!zipFolder.isUploaded()) {
                    currentFolder = zipFolder;
                    getFIle(zipFolder);
                }
            }
        }
        return null;
    }

    public void getFIle(ZipFolder zipFolder) {
        String backupDBPath = Environment.getExternalStorageDirectory().getPath() + "/Tattle";
        final File backupDBFolder = new File(backupDBPath);
        backupDBFolder.mkdirs();

        File filee = new File(backupDBFolder, zipFolder.getFolderName()+".zip");
        if (filee.exists()) {
            Log.i(TAG, "getFileToUpload: ");
            uploadFile(filee);
        } else {
            Log.i(TAG, "file doesnt exist: ");
        }
    }

    public void uploadFile(File file) {
        final StorageReference storageReference = this.storageReference.child(file.getName());
        Uri uri = Uri.fromFile(file);
        storageReference.child(firebaseAuth.getCurrentUser().getUid()).putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        databaseReference.child("RetrievedData").child(firebaseAuth.getCurrentUser().getUid()).child("ImageFolders").push().setValue(uri.toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                currentFolder.setUploaded(true);
                                zipRepo.update(currentFolder);
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
                        Log.i(TAG, "onFailure: " + e.getMessage());
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i(TAG, "onFailure: " + e.getMessage());
            }
        });
    }

}
