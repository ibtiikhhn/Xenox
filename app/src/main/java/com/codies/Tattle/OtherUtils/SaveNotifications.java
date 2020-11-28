package com.codies.Tattle.OtherUtils;

import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class SaveNotifications {

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
