package com.globalsolutions.Tattle.LocalFilesDB;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface DocFileDAO {
    @Insert
    void insert(DocFile docFile);

    @Delete
    void delete(DocFile docFile);

    @Update
    void update(DocFile docFile);

    @Query("DELETE from doc_file")
    void deleteAll();

    @Query("SELECT * FROM doc_file")
    List<DocFile> getAllDocFiles();
}
