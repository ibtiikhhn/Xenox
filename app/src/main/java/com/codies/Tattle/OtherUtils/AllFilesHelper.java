package com.codies.Tattle.OtherUtils;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AllFilesHelper {

    String fileType;
    List<File> myFiles;

    // Constructor
    public AllFilesHelper(String fileType) {
        this.fileType = fileType;
        myFiles = new ArrayList<>();
    }

    public List<File> Search_Dir(File dir) {
//        String pdfPattern = ".pdf";


        File[] FileList = dir.listFiles();

        if (FileList != null) {
            for (int i = 0; i < FileList.length; i++) {

                if (FileList[i].isDirectory()) {
                    Search_Dir(FileList[i]);
                } else {
                    if (FileList[i].getName().endsWith(fileType)){
                        //here you have that file.
                        myFiles.add(FileList[i]);

                    }
                }
            }
        }
        return myFiles;
    }
}
