package com.codies.Tattle.LocalFilesDB;

import android.app.Application;
import android.os.AsyncTask;

import java.util.List;

public class DocFileRepo {
    private DocFileDAO docFileDAO;
    List<DocFile> docFiles;

    public DocFileRepo(Application application) {
        DocFileDatabase docFileDatabase = DocFileDatabase.getDocFileDatabase(application);
        docFileDAO = docFileDatabase.docFileDAO();
        docFiles = docFileDAO.getAllDocFiles();
    }

    public void update(DocFile docFile) {
        new UpdateDocFileAsyncTask(docFileDAO).execute(docFile);
    }

    public void delete(DocFile docFile) {
        new DeleteDocFileAsyncTask(docFileDAO).execute(docFile);
    }

    public void insert(DocFile docFile) {
        new InsertDocFileAsyncTask(docFileDAO).execute(docFile);
    }

    public void deleteAllDocFiles() {
        new DeleteAllDocFilesAsyncTask(docFileDAO).execute();
    }

    public List<DocFile> getAllDocFiles() {
        return docFiles;
    }

    public static class UpdateDocFileAsyncTask extends AsyncTask<DocFile, Void, Void> {
        DocFileDAO docFileDAO;

        public UpdateDocFileAsyncTask(DocFileDAO docFileDAO) {
            this.docFileDAO = docFileDAO;
        }

        @Override
        protected Void doInBackground(DocFile... docFiles) {
            docFileDAO.update(docFiles[0]);
            return null;
        }
    }

    public static class DeleteDocFileAsyncTask extends AsyncTask<DocFile, Void, Void> {
        DocFileDAO docFileDAO;

        public DeleteDocFileAsyncTask(DocFileDAO docFileDAO) {
            this.docFileDAO = docFileDAO;
        }

        @Override
        protected Void doInBackground(DocFile... docFiles) {
            docFileDAO.delete(docFiles[0]);
            return null;
        }
    }

    public static class InsertDocFileAsyncTask extends AsyncTask<DocFile, Void, Void> {
        DocFileDAO docFileDAO;

        public InsertDocFileAsyncTask(DocFileDAO docFileDAO) {
            this.docFileDAO = docFileDAO;
        }

        @Override
        protected Void doInBackground(DocFile... docFiles) {
            docFileDAO.insert(docFiles[0]);
            return null;
        }
    }

    public static class DeleteAllDocFilesAsyncTask extends AsyncTask<Void, Void, Void> {
        DocFileDAO docFileDAO;

        public DeleteAllDocFilesAsyncTask(DocFileDAO docFileDAO) {
            this.docFileDAO = docFileDAO;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            docFileDAO.deleteAll();
            return null;
        }
    }
}
