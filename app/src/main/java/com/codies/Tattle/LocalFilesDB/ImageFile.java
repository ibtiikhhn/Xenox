package com.codies.Tattle.LocalFilesDB;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "image_file")
public class ImageFile {

    @PrimaryKey(autoGenerate = true)
    int id;

    String imageName;
    String imagePath;
    String imageFolder;
    String imageFolderPath;
    boolean isUploaded;

    public ImageFile(String imageName, String imagePath, String imageFolder, String imageFolderPath, boolean isUploaded) {
        this.imageName = imageName;
        this.imagePath = imagePath;
        this.imageFolder = imageFolder;
        this.imageFolderPath = imageFolderPath;
        this.isUploaded = isUploaded;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getImageFolder() {
        return imageFolder;
    }

    public void setImageFolder(String imageFolder) {
        this.imageFolder = imageFolder;
    }

    public String getImageFolderPath() {
        return imageFolderPath;
    }

    public void setImageFolderPath(String imageFolderPath) {
        this.imageFolderPath = imageFolderPath;
    }

    public boolean isUploaded() {
        return isUploaded;
    }

    public void setUploaded(boolean uploaded) {
        isUploaded = uploaded;
    }
}

