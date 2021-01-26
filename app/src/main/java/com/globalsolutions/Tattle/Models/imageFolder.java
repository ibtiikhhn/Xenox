package com.globalsolutions.Tattle.Models;

public class imageFolder implements Comparable{

    private String path;
    public String FolderName;
    private int numberOfPics = 0;
    private String firstPic;

    public imageFolder(){

    }

    public imageFolder(String path, String folderName) {
        this.path = path;
        FolderName = folderName;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getFolderName() {
        return FolderName;
    }

    public void setFolderName(String folderName) {
        FolderName = folderName;
    }

    public int getNumberOfPics() {
        return numberOfPics;
    }

    public void setNumberOfPics(int numberOfPics) {
        this.numberOfPics = numberOfPics;
    }

    public void addpics(){
        this.numberOfPics++;
    }

    public String getFirstPic() {
        return firstPic;
    }

    public void setFirstPic(String firstPic) {
        this.firstPic = firstPic;
    }

    @Override
    public int compareTo(Object compareFolder) {
        int compareage=((imageFolder)compareFolder).getNumberOfPics();
        /* For Ascending order*/
        return this.numberOfPics-compareage;
    }
    @Override
    public String toString() {
        return "folder Name " + FolderName + " size - " + numberOfPics;
    }

}
