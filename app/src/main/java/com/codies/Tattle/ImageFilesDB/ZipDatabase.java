package com.codies.Tattle.ImageFilesDB;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {ZipFolder.class}, version = 1)
public abstract class ZipDatabase extends RoomDatabase{
    public static ZipDatabase zipDatabase;

    public abstract ZipFolderDAO zipFolderDAO();

    public static synchronized ZipDatabase getZipDatabase(Context context) {
        if (zipDatabase == null) {
            zipDatabase = Room.databaseBuilder(context.getApplicationContext(), ZipDatabase.class, "zipDB")
                    .addCallback(callback)
                    .fallbackToDestructiveMigration()
                    .build();

        }
        return zipDatabase;
    }

    private static RoomDatabase.Callback callback = new RoomDatabase.Callback(){
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
