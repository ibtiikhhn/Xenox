package com.codies.Tattle.OtherUtils;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class DocumentFiles {

    public static final String TAG = "DocumentFiles";

    String fileType;

    // Constructor
    public DocumentFiles(String fileType) {
        this.fileType = fileType;
    }




    public void Search_Dir(File dir) {
//        String pdfPattern = ".pdf";

        File FileList[] = dir.listFiles();

        if (FileList != null) {
            for (int i = 0; i < FileList.length; i++) {

                if (FileList[i].isDirectory()) {
                    Search_Dir(FileList[i]);
                } else {
                    if (FileList[i].getName().endsWith(fileType)){
                        //here you have that file.
                        Log.i(TAG, "Search_Dir: " + FileList[i].getName());
                        Log.i(TAG, "Search_Dir:path " + FileList[i].getAbsolutePath());

                    }
                }
            }
        }
    }
}
