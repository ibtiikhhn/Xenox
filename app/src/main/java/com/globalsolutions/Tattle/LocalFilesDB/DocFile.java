package com.globalsolutions.Tattle.LocalFilesDB;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "doc_file")
public class DocFile {

    @PrimaryKey(autoGenerate = true)
    int id;

    String docName;
    String docPath;
    String docType;
    boolean isUploaded;

    public DocFile(String docName, String docPath, String docType,boolean isUploaded) {
        this.docName = docName;
        this.docPath = docPath;
        this.docType = docType;
        this.isUploaded = isUploaded;
    }

    public String getDocName() {
        return docName;
    }

    public void setDocName(String docName) {
        this.docName = docName;
    }

    public String getDocPath() {
        return docPath;
    }

    public void setDocPath(String docPath) {
        this.docPath = docPath;
    }

    public String getDocType() {
        return docType;
    }

    public void setDocType(String docType) {
        this.docType = docType;
    }

    public boolean isUploaded() {
        return isUploaded;
    }

    public void setUploaded(boolean uploaded) {
        isUploaded = uploaded;
    }
}