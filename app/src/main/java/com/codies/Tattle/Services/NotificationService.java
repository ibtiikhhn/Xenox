package com.codies.Tattle.Services;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class NotificationService extends NotificationListenerService {

    Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        String pack = sbn.getPackageName();
        String ticker ="";
        if(sbn.getNotification().tickerText !=null) {
            ticker = sbn.getNotification().tickerText.toString();
        }
        Bundle extras = sbn.getNotification().extras;
        if (extras != null) {
            String title = extras.getString("android.title","");
            String text = extras.getCharSequence("android.text","").toString();//yaha py error crash kr rha hai...
            int id1 = extras.getInt(Notification.EXTRA_SMALL_ICON);
            Bitmap id = sbn.getNotification().largeIcon;

            Intent msgrcv = new Intent("Msg");
            msgrcv.putExtra("package", pack);
            msgrcv.putExtra("ticker", ticker);
            msgrcv.putExtra("title", title);
            msgrcv.putExtra("text", text);

            String notf = "package : "+pack+
                    "\ntitle : " + title+
                    "\ntext : " + text;

            writeNotifs(notf);

            if(id != null) {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                id.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();
                msgrcv.putExtra("icon",byteArray);
            }
            LocalBroadcastManager.getInstance(context).sendBroadcast(msgrcv);
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.i("Msg","Notification Removed");
    }

    public void writeNotifs(String notif) {
        String backupDBPath = Environment.getExternalStorageDirectory().getPath() + "/Tattle";
        final File backupDBFolder = new File(backupDBPath);
        backupDBFolder.mkdirs();

        File logFile = new File(backupDBFolder, "MyFile.txt");
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(notif);
            buf.newLine();
            buf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
