package com.codies.Tattle.Utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.codies.Tattle.Models.imageFolder;
import com.codies.Tattle.Models.pictureFacer;
import com.codies.Tattle.R;
import com.codies.Tattle.UI.Activities.MainActivity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NotificationWorker extends Worker {
    public static final String TAG = "HELL";
    private static final String WORK_RESULT = "work_result";
    PictureUtility pictureUtility;
    ArrayList<pictureFacer> allPictures;
    Context context;
    SharedPrefs sharedPrefs;
    int minNoOfPicFolder;
    String minNoOfPicFolderPath;
    List<imageFolder> imageFolderList;


    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        pictureUtility = new PictureUtility(context);
        this.context = context;
        sharedPrefs = SharedPrefs.getInstance(context);
        allPictures = new ArrayList<>();
        imageFolderList = pictureUtility.getPictureFoldersPaths();
    }

    @NonNull
    @Override
    public Result doWork() {
        Data taskData = getInputData();
//        String taskDataString = taskData.getString(MainActivity.MESSAGE_STATUS);

        minNoOfPicFolder = pictureUtility.getPictureFoldersPaths().get(0).getNumberOfPics();
        minNoOfPicFolderPath = pictureUtility.getPictureFoldersPaths().get(0).getPath();

        Collections.sort(imageFolderList);
        if (sharedPrefs.getList() == null || sharedPrefs.getList().isEmpty()) {
            iteratetThroughPhotos(imageFolderList.get(0));
        } else {
            for (int i = 0; i < imageFolderList.size(); i++) {
                if (!sharedPrefs.getList().contains(imageFolderList.get(i).getFolderName())) {
                    Log.i(TAG, "doWork: sharedPrefs doesnt contain");
                    iteratetThroughPhotos(imageFolderList.get(i));
                }
            }
            Log.i(TAG, "doWork: "+sharedPrefs.getList().size());
        }

        Data outputData = new Data.Builder().putBoolean("photosZipped", true).build();
        return Result.success(outputData);
    }

    public void iteratetThroughPhotos(imageFolder imageFolder) {
        String[] files = new String[imageFolder.getNumberOfPics()];
        for (int i = 0; i < imageFolder.getNumberOfPics(); i++) {//iterate through pics in current folder
            files[i] = pictureUtility.getAllImagesByFolder(imageFolder.getPath()).get(i).getPicturePath();
        }
        saveFile(files, imageFolder.getFolderName());
    }

    public void saveFile(String[] files, String zipFileName) {
        String backupDBPath = Environment.getExternalStorageDirectory().getPath() + "/Tattle";
        final File backupDBFolder = new File(backupDBPath);
        backupDBFolder.mkdirs();
        final File backupDB = new File(backupDBFolder, "/db_pos.db");
        String[] s = new String[1];
        s[0] = backupDB.getAbsolutePath();
        Compress compress = new Compress();
        try {
            compress.zip(files, backupDBPath + "/" + zipFileName + ".zip");
            uploadFileToServer(backupDBPath + "/" + zipFileName + ".zip", zipFileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void uploadFileToServer(String filePath, String folderName) {
            sharedPrefs.saveFolderToList(folderName);
    }


    private void showNotification(String task, String desc) {
        NotificationManager manager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "task_channel";
        String channelName = "task_name";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new
                    NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(channel);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelId)
                .setContentTitle(task)
                .setContentText(desc)
                .setSmallIcon(R.mipmap.ic_launcher);
        manager.notify(1, builder.build());
    }

}