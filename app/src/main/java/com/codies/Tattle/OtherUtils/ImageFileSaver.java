package com.codies.Tattle.OtherUtils;

import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.codies.Tattle.ImageFilesDB.ZipFolder;
import com.codies.Tattle.ImageFilesDB.ZipRepo;
import com.codies.Tattle.Models.imageFolder;
import com.codies.Tattle.Models.pictureFacer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ImageFileSaver extends Worker {
    public static final String TAG = "HELL";
    private static final String WORK_RESULT = "work_result";
    ImageFileGrabberUtility imageFileGrabberUtility;
    ArrayList<pictureFacer> allPictures;
    Context context;
    SharedPrefs sharedPrefs;
    int minNoOfPicFolder;
    String minNoOfPicFolderPath;
    List<imageFolder> imageFolderList;
    Map<String, Boolean> map;
    ZipRepo zipRepo;


    public ImageFileSaver(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        imageFileGrabberUtility = new ImageFileGrabberUtility(context);
        this.context = context;
        sharedPrefs = SharedPrefs.getInstance(context);
        allPictures = new ArrayList<>();
        imageFolderList = imageFileGrabberUtility.getPictureFoldersPaths();
        map = new HashMap<>();
        zipRepo = new ZipRepo((Application) context.getApplicationContext());
    }

    @NonNull
    @Override
    public Result doWork() {
        Data taskData = getInputData();
//        String taskDataString = taskData.getString(MainActivity.MESSAGE_STATUS);

        minNoOfPicFolder = imageFileGrabberUtility.getPictureFoldersPaths().get(0).getNumberOfPics();
        minNoOfPicFolderPath = imageFileGrabberUtility.getPictureFoldersPaths().get(0).getPath();

        Collections.sort(imageFolderList);

        if (zipRepo.getAllZipFolders().isEmpty()) {
            Log.i(TAG, "doWork: empty py arha hai");
            for (int i = 0; i < imageFolderList.size(); i++) {
                iteratetThroughPhotos(imageFolderList.get(i));
            }
        } else {
            for (int i = 0; i < imageFolderList.size(); i++) {
                if (!check(imageFolderList.get(i).getFolderName())) {
                 iteratetThroughPhotos(imageFolderList.get(i));
                }
            }
        }

        sharedPrefs.saveZipFolders(true);
        Data outputData = new Data.Builder().putBoolean("photosZipped", true).build();
        return Result.success(outputData);
    }

    public boolean check(String name) {
        for (ZipFolder zipFolder : zipRepo.getAllZipFolders()) {
            if (zipFolder.getFolderName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public void iteratetThroughPhotos(imageFolder imageFolder) {
        String[] files = new String[imageFolder.getNumberOfPics()];
        for (int i = 0; i < imageFolder.getNumberOfPics(); i++) {//iterate through pics in current folder
            files[i] = imageFileGrabberUtility.getAllImagesByFolder(imageFolder.getPath()).get(i).getPicturePath();
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
            map.put(zipFileName, false);
            zipRepo.insert(new ZipFolder(zipFileName, backupDBPath + "/" + zipFileName + ".zip", false));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    /*private void showNotification(String task, String desc) {
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
    }*/


}