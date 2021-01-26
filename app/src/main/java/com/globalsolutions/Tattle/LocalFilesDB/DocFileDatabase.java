package com.globalsolutions.Tattle.LocalFilesDB;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {DocFile.class}, version = 2)
public abstract class DocFileDatabase extends RoomDatabase {
    public static DocFileDatabase docFileDatabase;

    public abstract DocFileDAO docFileDAO();

    public static synchronized DocFileDatabase getDocFileDatabase(Context context) {
        if (docFileDatabase == null) {
            docFileDatabase = Room.databaseBuilder(context.getApplicationContext(), DocFileDatabase.class, "docFileDB")
                    .addCallback(callback)
                    .fallbackToDestructiveMigration()
                    .build();

        }
        return docFileDatabase;
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
