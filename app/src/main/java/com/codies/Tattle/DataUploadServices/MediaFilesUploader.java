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
import com.codies.Tattle.LocalFilesDB.ImageFile;
import com.codies.Tattle.LocalFilesDB.ImageFileRepo;
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
import java.util.List;

public class MediaFilesUploader extends Worker {

    private static final String TAG = "MediaFilesUploader";
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    FirebaseAuth firebaseAuth;
    SharedPrefs sharedPrefs;
    StorageReference storageReference;
    ImageFileRepo imageFileRepo;
    Context context;
    List<ImageFile> imageFiles;
    boolean uploaded = false;

    public MediaFilesUploader(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        sharedPrefs = SharedPrefs.getInstance(this.getApplicationContext());
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null) {
            databaseReference = firebaseDatabase.getReference();
            storageReference = FirebaseStorage.getInstance().getReference(firebaseAuth.getCurrentUser().getUid());
            imageFileRepo = new ImageFileRepo((Application) context.getApplicationContext());
            imageFiles = imageFileRepo.getAllImageFiles();

            if (imageFiles != null && !imageFiles.isEmpty()) {
                for (int i = 0; i < imageFiles.size(); i++) {
                    File file = getFileFromPath(imageFiles.get(i).getImagePath());
                    ImageFile imageFile = imageFiles.get(i);
                    if (file != null &&  !imageFile.isUploaded() ) {
                        uploaded = false;
                        uploadFile(file, imageFile);
                        Log.i(TAG, "doWork: "+"now uploaded");
                        while (!uploaded) {

                        }
                    }
                }
            }

        }

        Data outputData = new Data.Builder().putBoolean("photosSyncedWithServer", true).build();
        return Result.success(outputData);
    }


    public void uploadFile(File file,ImageFile imageFile) {
        Log.i(TAG, "uploadFile: file name " + file.getName());
        StorageReference mStorageRef = this.storageReference.child("UserImages").child(imageFile.getImageFolder()).child(file.getName());

        Uri uri = Uri.fromFile(file);
        mStorageRef.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.i(TAG, "onSuccess: image uploaded" + taskSnapshot.getUploadSessionUri().toString());
                mStorageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        databaseReference.child("UserRetrievedData").child(firebaseAuth.getCurrentUser().getUid()).child("Images").child(imageFile.getImageFolder()).push().setValue(uri.toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                imageFile.setUploaded(true);
                                imageFileRepo.update(imageFile);
                                uploaded = true;
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                uploaded = true;
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        uploaded = true;
                        Log.i(TAG, "onFailure: error getting image url " + e.getMessage());
                        Log.i(TAG, "onFailure: " + e.getLocalizedMessage());
                        Log.i(TAG, "onFailure: " + e.getCause().getMessage());
                        Log.i(TAG, "onFailure: " + e.getStackTrace().toString());
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
