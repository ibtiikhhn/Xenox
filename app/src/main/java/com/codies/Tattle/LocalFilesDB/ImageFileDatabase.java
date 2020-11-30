package com.codies.Tattle.LocalFilesDB;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {ImageFile.class}, version = 1)
public abstract class ImageFileDatabase extends RoomDatabase {
    public static ImageFileDatabase imageFileDatabase;

    public abstract ImageFileDAO imageFileDAO();

    public static synchronized ImageFileDatabase getImageFileDatabase(Context context) {
        if (imageFileDatabase == null) {
            imageFileDatabase = Room.databaseBuilder(context.getApplicationContext(), ImageFileDatabase.class, "imageFileDB")
                    .addCallback(callback)
                    .fallbackToDestructiveMigration()
                    .build();

        }
        return imageFileDatabase;
    }

    private static Callback callback = new Callback(){
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
//            new PopulateDatabase(studentDatabase).execute();
        }

        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
//            new PopulateDatabase(studentDatabase).execute();
        }
    };
}
