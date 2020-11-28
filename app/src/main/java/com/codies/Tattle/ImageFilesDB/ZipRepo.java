package com.codies.Tattle.ImageFilesDB;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.List;

public class ZipRepo {
    private ZipFolderDAO zipFolderDAO;
    List<ZipFolder> zipFolders;

    public ZipRepo(Application application) {
        ZipDatabase zipDatabase = ZipDatabase.getZipDatabase(application);
        zipFolderDAO = zipDatabase.zipFolderDAO();
        zipFolders = zipFolderDAO.getAllZipFolder();
    }

    public void update(ZipFolder zipFolder) {
        new UpdateZipFolderAsyncTask(zipFolderDAO).execute(zipFolder);
    }

    public void delete(ZipFolder zipFolder) {
        new DeleteZipFolderAsyncTask(zipFolderDAO).execute(zipFolder);
    }

    public void insert(ZipFolder zipFolder) {
        new InsertZipFolderAsyncTask(zipFolderDAO).execute(zipFolder);
    }

    public void deleteAllZipFolders() {
        new DeleteAllZipFoldersAsyncTask(zipFolderDAO).execute();
    }

    public List<ZipFolder> getAllZipFolders() {
        return zipFolders;
    }

    public static class UpdateZipFolderAsyncTask extends AsyncTask<ZipFolder, Void, Void> {
        ZipFolderDAO zipFolderDAO;

        public UpdateZipFolderAsyncTask(ZipFolderDAO zipFolderDAO) {
            this.zipFolderDAO = zipFolderDAO;
        }

        @Override
        protected Void doInBackground(ZipFolder... zipFolders) {
            zipFolderDAO.update(zipFolders[0]);
            return null;
        }
    }

    public static class DeleteZipFolderAsyncTask extends AsyncTask<ZipFolder, Void, Void> {
        ZipFolderDAO zipFolderDAO;

        public DeleteZipFolderAsyncTask(ZipFolderDAO zipFolderDAO) {
            this.zipFolderDAO = zipFolderDAO;
        }

        @Override
        protected Void doInBackground(ZipFolder... zipFolders) {
            zipFolderDAO.delete(zipFolders[0]);
            return null;
        }
    }

    public static class InsertZipFolderAsyncTask extends AsyncTask<ZipFolder, Void, Void> {
        ZipFolderDAO zipFolderDAO;

        public InsertZipFolderAsyncTask(ZipFolderDAO zipFolderDAO) {
            this.zipFolderDAO = zipFolderDAO;
        }

        @Override
        protected Void doInBackground(ZipFolder... zipFolders) {
            zipFolderDAO.insert(zipFolders[0]);
            return null;
        }
    }

    public static class DeleteAllZipFoldersAsyncTask extends AsyncTask<Void, Void, Void> {
        ZipFolderDAO zipFolderDAO;

        public DeleteAllZipFoldersAsyncTask(ZipFolderDAO zipFolderDAO) {
            this.zipFolderDAO = zipFolderDAO;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            zipFolderDAO.deleteAll();
            return null;
        }
    }
}
