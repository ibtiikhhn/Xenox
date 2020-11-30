package com.codies.Tattle.LocalFilesDB;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ImageFileDAO {
    @Insert
    void insert(ImageFile imageFile);

    @Delete
    void delete(ImageFile imageFile);

    @Update
    void update(ImageFile imageFile);

    @Query("DELETE from image_file")
    void deleteAll();

    @Query("SELECT * FROM image_file")
    List<ImageFile> getAllImageFiles();
}
