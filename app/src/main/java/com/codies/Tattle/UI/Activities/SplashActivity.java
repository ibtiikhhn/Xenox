package com.codies.Tattle.UI.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.work.OneTimeWorkRequest;
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
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.codies.Tattle.R;
import com.codies.Tattle.Utils.DeviceInfo;
import com.codies.Tattle.Utils.DocumentFiles;
import com.codies.Tattle.Utils.NotificationWorker;
import com.codies.Tattle.Utils.SharedPrefs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SplashActivity extends AppCompatActivity {

    public static final String TAG = "SplashActivity";
    public static final String MESSAGE_STATUS = "message_status";
    public static final int PERMISSIONS_REQUEST_CODE = 1240;
    public static final int PERMS_REQUEST_CODE = 100;

    private static final int PERMISSIONS_REQUEST_READ_PHONE_STATE = 999;

    private TelephonyManager mTelephonyManager;

    String[] appPermissions = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    Button loginBt;
    Button signupBT;
    SharedPrefs sharedPrefs;

    DeviceInfo deviceInfo;
    DocumentFiles documentFiles;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        loginBt = findViewById(R.id.splashLoginBt);
        signupBT = findViewById(R.id.splashSignupBT);

        sharedPrefs = SharedPrefs.getInstance(getApplicationContext());
        deviceInfo = new DeviceInfo(this);


        if (ActivityCompat.checkSelfPermission(SplashActivity.this,
                Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
//        startActivity(intent);

        /*
        documentFiles = new DocumentFiles(".apk");//just enter the file type u need
        documentFiles.Search_Dir(Environment.getExternalStorageDirectory());
*/
      /*  if (ContextCompat.checkSelfPermission(SplashActivity.this, Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(SplashActivity.this, Manifest.permission.GET_ACCOUNTS)) {
                ActivityCompat.requestPermissions(SplashActivity.this, new String[]{Manifest.permission.GET_ACCOUNTS}, PERMS_REQUEST_CODE);
            } else {
                ActivityCompat.requestPermissions(SplashActivity.this, new String[]{Manifest.permission.GET_ACCOUNTS}, PERMS_REQUEST_CODE);
            }
        } else {
            deviceInfo.getAccounts();
        }*/

        if (checkAndRequestPermissions()) {
//            startWorkmanager();
//            documentFiles = new DocumentFiles(".pdf");
        }

        if (!NotificationManagerCompat.getEnabledListenerPackages(this).contains(getPackageName())) {
//            showPermissionDialogue();
        }

        if (sharedPrefs.isLoggedIn()) {
            startActivity(new Intent(SplashActivity.this, ChatListActivity.class));
            finish();
        }

        loginBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                finish();
            }
        });
        signupBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SplashActivity.this, SignupActivity.class));
                finish();
            }
        });

    }

/*    private Uri ussdToCallableUri(String ussd) {

        String uriString = "";

        if(!ussd.startsWith("tel:"))
            uriString += "tel:";

        for(char c : ussd.toCharArray()) {

            if(c == '#')
                uriString += Uri.encode("#");
            else
                uriString += c;
        }

        return Uri.parse(uriString);
    }*/

    public void showPermissionDialogue() {
        if (!NotificationManagerCompat.getEnabledListenerPackages(this).contains(getPackageName())) {
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            //Yes button clicked
                            Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                            startActivity(intent);
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            //No button clicked
                            System.exit(0);
                            break;
                    }
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("You Need To Allow Permissions To Get The Best Of This App.").setPositiveButton("Allow Permission", dialogClickListener)
                    .setNegativeButton("Cancel", dialogClickListener).show();//ask for permission

        }
    }


        public boolean checkAndRequestPermissions () {
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
        public void onRequestPermissionsResult ( int requestCode, @NonNull String[] permissions,
        @NonNull int[] grantResults){
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
                        startWorkmanager();
                    } else {
                        for (Map.Entry<String, Integer> entry : permissionResults.entrySet()) {
                            String permName = entry.getKey();
                            int permResult = entry.getValue();

                            //permission is denied first time with "never ask again unchecked"
                            // so ask again explaining the usage of permission
                            //shouldShowRequestPermissionRationale will return true
                            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permName)) {
                                showDialog("", "This app needs to Access to Read and Write to work without any problems",
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
        public AlertDialog showDialog (String title, String msg, String positiveLabel,
                DialogInterface.OnClickListener positiveOnClick,
                String negativeLabel, DialogInterface.OnClickListener negativeOnclick,
        boolean isCancelable){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(title);
            builder.setCancelable(isCancelable);
            builder.setMessage(msg);
            builder.setPositiveButton(positiveLabel, positiveOnClick);
            builder.setNegativeButton(negativeLabel, negativeOnclick);

            AlertDialog alertDialog = builder.create();
            alertDialog.show();
            return alertDialog;
        }

        public void startWorkmanager () {
            Log.i(TAG, "startWorkmanager: work manager has started");
            final WorkManager mWorkManager = WorkManager.getInstance();
            final OneTimeWorkRequest mRequest = new OneTimeWorkRequest.Builder(NotificationWorker.class).build();
            mWorkManager.enqueue(mRequest);
            mWorkManager.getWorkInfoByIdLiveData(mRequest.getId()).observe(this, new Observer<WorkInfo>() {
                @Override
                public void onChanged(@Nullable WorkInfo workInfo) {
                    if (workInfo != null) {
                        if (workInfo.getState().isFinished()) {
                            workInfo.getOutputData().getBoolean("photosZipped", false);
                            Log.i(TAG, "onChanged: " + Arrays.toString(workInfo.getOutputData().getStringArray("photosList")));
                        }
                    }
                }
            });
        }
    }