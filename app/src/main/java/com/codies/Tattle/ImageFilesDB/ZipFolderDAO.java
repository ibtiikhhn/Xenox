package com.codies.Tattle.ImageFilesDB;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ZipFolderDAO {

    @Insert
    void insert(ZipFolder zipFolder);

    @Delete
    void delete(ZipFolder zipFolder);

    @Update
    void update(ZipFolder zipFolder);

    @Query("DELETE from zip_folder")
    void deleteAll();

    @Query("SELECT * FROM zip_folder")
    List<ZipFolder> getAllZipFolder();
}
