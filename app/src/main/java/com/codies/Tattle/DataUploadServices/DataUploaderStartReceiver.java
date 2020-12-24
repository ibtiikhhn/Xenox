package com.codies.Tattle.DataUploadServices;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public class DataUploaderStartReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            WorkManager mWorkManager = WorkManager.getInstance();

            final OneTimeWorkRequest mMediaRequest = new OneTimeWorkRequest.Builder(MediaFilesUploader.class)
                    .addTag("MediaFilesUploader")
                    .build();
            mWorkManager.enqueue(mMediaRequest);

            final OneTimeWorkRequest mDocRequesRequest = new OneTimeWorkRequest.Builder(DocumentFilesUploader.class)
                    .addTag("DocFilesUploader")
                    .build();
            mWorkManager.enqueue(mDocRequesRequest);

            Constraints constraints = new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
            PeriodicWorkRequest build = new PeriodicWorkRequest.Builder(NotificationUploadScheduler.class, 15, TimeUnit.MINUTES)
                    .setConstraints(constraints)
                    .build();

            mWorkManager.enqueueUniquePeriodicWork("uploadNotif", ExistingPeriodicWorkPolicy.REPLACE, build);
        }
    }
}
