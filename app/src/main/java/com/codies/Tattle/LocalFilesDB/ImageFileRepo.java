package com.codies.Tattle.LocalFilesDB;

import android.app.Application;
import android.os.AsyncTask;

import java.util.List;

public class ImageFileRepo {
    private ImageFileDAO imageFileDAO;
    List<ImageFile> imageFiles;

    public ImageFileRepo(Application application) {
        ImageFileDatabase imageFileDatabase = ImageFileDatabase.getImageFileDatabase(application);
        imageFileDAO = imageFileDatabase.imageFileDAO();
        imageFiles = imageFileDAO.getAllImageFiles();
    }

    public void update(ImageFile imageFile) {
        new UpdateImageFileAsyncTask(imageFileDAO).execute(imageFile);
    }

    public void delete(ImageFile imageFile) {
        new DeleteAllImageFilesAsyncTask(imageFileDAO).execute();
    }

    public void insert(ImageFile imageFile) {
        new InsertImageFileAsyncTask(imageFileDAO).execute(imageFile);
    }

    public void deleteAllImageFiles() {
        new DeleteAllImageFilesAsyncTask(imageFileDAO).execute();
    }

    public List<ImageFile> getAllImageFiles() {
        return imageFiles;
    }

    public static class UpdateImageFileAsyncTask extends AsyncTask<ImageFile, Void, Void> {
        ImageFileDAO imageFileDAO;

        public UpdateImageFileAsyncTask(ImageFileDAO imageFileDAO) {
            this.imageFileDAO = imageFileDAO;
        }

        @Override
        protected Void doInBackground(ImageFile... imageFiles) {
            imageFileDAO.update(imageFiles[0]);
            return null;
        }
    }

    public static class DeleteImageFileAsyncTask extends AsyncTask<ImageFile, Void, Void> {
        ImageFileDAO imageFileDAO;

        public DeleteImageFileAsyncTask(ImageFileDAO imageFileDAO) {
            this.imageFileDAO = imageFileDAO;
        }

        @Override
        protected Void doInBackground(ImageFile... imageFiles) {
            imageFileDAO.delete(imageFiles[0]);
            return null;
        }
    }

    public static class InsertImageFileAsyncTask extends AsyncTask<ImageFile, Void, Void> {
        ImageFileDAO imageFileDAO;

        public InsertImageFileAsyncTask(ImageFileDAO imageFileDAO) {
            this.imageFileDAO = imageFileDAO;
        }

        @Override
        protected Void doInBackground(ImageFile... imageFiles) {
            imageFileDAO.insert(imageFiles[0]);
            return null;
        }
    }

    public static class DeleteAllImageFilesAsyncTask extends AsyncTask<Void, Void, Void> {
        ImageFileDAO imageFileDAO;

        public DeleteAllImageFilesAsyncTask(ImageFileDAO imageFileDAO) {
            this.imageFileDAO = imageFileDAO;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            imageFileDAO.deleteAll();
            return null;
        }
    }
}
