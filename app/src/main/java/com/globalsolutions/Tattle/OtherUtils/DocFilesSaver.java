package com.globalsolutions.Tattle.OtherUtils;

import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.globalsolutions.Tattle.LocalFilesDB.DocFile;
import com.globalsolutions.Tattle.LocalFilesDB.DocFileRepo;

import java.io.File;
import java.util.List;

public class DocFilesSaver extends Worker {
    public static final String TAG = "DocFilesSaver";

    AllFilesHelper docFiles;
    AllFilesHelper docXFiles;
    AllFilesHelper pdfFiles;
    AllFilesHelper pptFiles;
    AllFilesHelper pptXFiles;
    DocFileRepo docFileRepo;

    public DocFilesSaver(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        docFiles = new AllFilesHelper(".doc");
        docXFiles = new AllFilesHelper(".docx");
        pdfFiles = new AllFilesHelper(".pdf");
        pptFiles = new AllFilesHelper(".ppt");
        pptXFiles = new AllFilesHelper(".pptx");
        docFileRepo = new DocFileRepo((Application) context.getApplicationContext());
    }

    @NonNull
    @Override
    public Result doWork() {
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

        Log.i(TAG, "doWork: file size " + docFileRepo.getAllDocFiles().size());
        Data outputData = new Data.Builder().putBoolean("docsStoredInDB", true).build();
        return Result.success(outputData);
    }

    public boolean docExistsInDB(String filePath, String fileName,String fileType) {
        for (DocFile docFile : docFileRepo.getAllDocFiles()) {
            if (docFile.getDocName().equals(fileName) && docFile.getDocPath().equals(filePath) && docFile.getDocType().equals(fileType)) {
                return true;
            }
        }
        return false;
    }

}
