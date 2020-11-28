package com.codies.Tattle.ImageFilesDB;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "zip_folder")
public class ZipFolder {

    @PrimaryKey(autoGenerate = true)
    int id;

    String folderName;

    String folderPath;

    boolean isUploaded;

    public ZipFolder(String folderName, String folderPath,boolean isUploaded) {
        this.folderName = folderName;
        this.folderPath = folderPath;
        this.isUploaded = isUploaded;
    }

    public boolean isUploaded() {
        return isUploaded;
    }

    public void setUploaded(boolean uploaded) {
        isUploaded = uploaded;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public String getFolderPath() {
        return folderPath;
    }

    public void setFolderPath(String folderPath) {
        this.folderPath = folderPath;
    }
}
