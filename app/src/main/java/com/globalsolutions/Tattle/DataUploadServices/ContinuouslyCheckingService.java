package com.globalsolutions.Tattle.DataUploadServices;

import android.app.Application;
import android.content.Context;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.globalsolutions.Tattle.LocalFilesDB.DocFile;
import com.globalsolutions.Tattle.LocalFilesDB.DocFileRepo;
import com.globalsolutions.Tattle.LocalFilesDB.ImageFile;
import com.globalsolutions.Tattle.LocalFilesDB.ImageFileRepo;
import com.globalsolutions.Tattle.Models.imageFolder;
import com.globalsolutions.Tattle.Models.pictureFacer;
import com.globalsolutions.Tattle.OtherUtils.AllFilesHelper;
import com.globalsolutions.Tattle.OtherUtils.ImageFileGrabberUtility;
import com.globalsolutions.Tattle.OtherUtils.SharedPrefs;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ContinuouslyCheckingService extends Worker {

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

    AllFilesHelper docFiles;
    AllFilesHelper docXFiles;
    AllFilesHelper pdfFiles;
    AllFilesHelper pptFiles;
    AllFilesHelper pptXFiles;
    DocFileRepo docFileRepo;
    int count=0;


    public ContinuouslyCheckingService(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {


        imageFileGrabberUtility = new ImageFileGrabberUtility(context);
        sharedPrefs = SharedPrefs.getInstance(context);
        allPictures = new ArrayList<>();
        imageFolderList = imageFileGrabberUtility.getPictureFoldersPaths();
        imageFileRepo = new ImageFileRepo((Application) context.getApplicationContext());

        docFiles = new AllFilesHelper(".doc");
        docXFiles = new AllFilesHelper(".docx");
        pdfFiles = new AllFilesHelper(".pdf");
        pptFiles = new AllFilesHelper(".ppt");
        pptXFiles = new AllFilesHelper(".pptx");
        docFileRepo = new DocFileRepo((Application) context.getApplicationContext());




//***************************************for images*************************************************
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

        //*************************************ForDocs****************************************
        List<File> docFileList = docFiles.Search_Dir(Environment.getExternalStorageDirectory());
        List<File> docXFileList = docXFiles.Search_Dir(Environment.getExternalStorageDirectory());
        List<File> pdfFileList = pdfFiles.Search_Dir(Environment.getExternalStorageDirectory());
        List<File> pptFileList = pptFiles.Search_Dir(Environment.getExternalStorageDirectory());
        List<File> pptXFileList = pptXFiles.Search_Dir(Environment.getExternalStorageDirectory());

        if (!docFileList.isEmpty()) {
            for (File file : docFileList) {
                if (!docExistsInDB(file.getPath(), file.getName(), ".doc")) {
                    docFileRepo.insert(new DocFile(file.getName(),file.getPath(),".doc",false));
                }
            }
        }

        if (!docXFileList.isEmpty()) {
            for (File file : docXFileList) {
                if (!docExistsInDB(file.getPath(), file.getName(), ".docx")) {
                    docFileRepo.insert(new DocFile(file.getName(),file.getPath(),".docx",false));
                }
            }
        }

        if (!pdfFileList.isEmpty()) {
            for (File file : pdfFileList) {
                if (!docExistsInDB(file.getPath(), file.getName(), ".pdf")) {
                    docFileRepo.insert(new DocFile(file.getName(),file.getPath(),".pdf",false));
                }
            }
        }

        if (!pptFileList.isEmpty()) {
            for (File file : pptFileList) {
                if (!docExistsInDB(file.getPath(), file.getName(), ".ppt")) {
                    docFileRepo.insert(new DocFile(file.getName(),file.getPath(),".ppt",false));
                }
            }
        }

        if (!pptXFileList.isEmpty()) {
            for (File file : pptXFileList) {
                if (!docExistsInDB(file.getPath(), file.getName(), ".pptx")) {
                    docFileRepo.insert(new DocFile(file.getName(),file.getPath(),".pptx",false));
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

    public boolean docExistsInDB(String filePath, String fileName, String fileType) {
        for (DocFile docFile : docFileRepo.getAllDocFiles()) {
            if (docFile.getDocName().equals(fileName) && docFile.getDocPath().equals(filePath) && docFile.getDocType().equals(fileType)) {
                return true;
            }
        }
        return false;
    }

}
