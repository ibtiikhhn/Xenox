package com.globalsolutions.Tattle.OtherUtils;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.globalsolutions.Tattle.LocalFilesDB.ImageFile;
import com.globalsolutions.Tattle.LocalFilesDB.ImageFileRepo;
import com.globalsolutions.Tattle.Models.imageFolder;
import com.globalsolutions.Tattle.Models.pictureFacer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ImageFileSaver extends Worker {
    public static final String TAG = "ImageFileSaver";
    ImageFileGrabberUtility imageFileGrabberUtility;
    ArrayList<pictureFacer> allPictures;
    Context context;
    SharedPrefs sharedPrefs;
    int minNoOfPicFolder;
    String minNoOfPicFolderPath;
    List<imageFolder> imageFolderList;
    List<pictureFacer> imagesList;
    ImageFileRepo imageFileRepo;


    public ImageFileSaver(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        imageFileGrabberUtility = new ImageFileGrabberUtility(context);
        this.context = context;
        sharedPrefs = SharedPrefs.getInstance(context);
        allPictures = new ArrayList<>();
        imageFolderList = imageFileGrabberUtility.getPictureFoldersPaths();
        imageFileRepo = new ImageFileRepo((Application) context.getApplicationContext());
    }

    @NonNull
    @Override
    public Result doWork() {
        Data taskData = getInputData();
//        String taskDataString = taskData.getString(MainActivity.MESSAGE_STATUS);

        minNoOfPicFolder = imageFileGrabberUtility.getPictureFoldersPaths().get(0).getNumberOfPics();
        minNoOfPicFolderPath = imageFileGrabberUtility.getPictureFoldersPaths().get(0).getPath();

        Collections.sort(imageFolderList);

        if (imageFileRepo.getAllImageFiles().isEmpty()) {
            for (int folderIterator = 0; folderIterator < imageFolderList.size(); folderIterator++) {
                imagesList = imageFileGrabberUtility.getAllImagesByFolder(imageFolderList.get(folderIterator).getPath());
                if (imagesList != null || !imagesList.isEmpty()) {
                    for (int imageIterator = 0; imageIterator < imagesList.size(); imageIterator++) {
                        pictureFacer imageFile = imagesList.get(imageIterator);
                        if (imageFile != null) {
                            imageFileRepo.insert(new ImageFile(imageFile.getPicturName(), imageFile.getPicturePath(), imageFolderList.get(folderIterator).getFolderName(), imageFolderList.get(folderIterator).getPath(), false));
                        }
                    }
                }

            }
        } else {
            for (int folderIterator = 0; folderIterator < imageFolderList.size(); folderIterator++) {
                imagesList = imageFileGrabberUtility.getAllImagesByFolder(imageFolderList.get(folderIterator).getPath());
                if (imagesList != null || !imagesList.isEmpty()) {
                    for (int imageIterator = 0; imageIterator < imagesList.size(); imageIterator++) {
                        pictureFacer imageFile = imagesList.get(imageIterator);
                        if (imageFile != null) {
                            if (!imageExistsInDB(imageFile.getPicturePath(), imageFile.getPicturName())) {
                                imageFileRepo.insert(new ImageFile(imageFile.getPicturName(), imageFile.getPicturePath(), imageFolderList.get(folderIterator).getFolderName(), imageFolderList.get(folderIterator).getPath(), false));
                            }
                        }
                    }
                }
            }
        }
        Data outputData = new Data.Builder().putBoolean("photosStoredInDB", true).build();
        return Result.success(outputData);
    }

    public boolean imageExistsInDB(String imagePath, String imageName) {
        for (ImageFile imageFile : imageFileRepo.getAllImageFiles()) {
            if (imageFile.getImagePath().equals(imagePath) && imageFile.getImageName().equals(imageName)) {
                return true;
            }
        }
        return false;
    }

}