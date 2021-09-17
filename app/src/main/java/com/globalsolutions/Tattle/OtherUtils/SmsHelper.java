package com.globalsolutions.Tattle.OtherUtils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class SmsHelper {
    private final String INBOX = "content://sms/inbox";
    private final String SENT = "content://sms/sent";

    private StringBuilder dataset;
    private String firstRow = "";
    private List<String> headers;
    private Context context;

    public SmsHelper(Context context) {
        dataset = new StringBuilder();
        this.context = context;
    }

    public void dumpSMS() {
        try {
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                    .mkdirs();
            File dir = Environment
                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
            File file = new File(dir, "backup.txt");
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);

            getInbox();
            getSent();

            firstRow = "address, person, date, body, type \n" ;
            fos.write(firstRow.getBytes());
            fos.write(dataset.toString().getBytes());
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
//            portView.setText(e.getMessage());
        }
    }

    private void getInbox() throws Exception {


        Uri mSmsinboxQueryUri = Uri.parse("content://sms/inbox");
        Cursor cursor1 = context.getContentResolver().query(mSmsinboxQueryUri,new String[] { "_id", "thread_id", "address", "person", "date","body", "type" }, null, null, null);
//        startManagingCursor(cursor1);
        String[] columns = new String[] { "address", "person", "date", "body","type" };
        if (cursor1.getCount() > 0) {
            String count = Integer.toString(cursor1.getCount());
            while (cursor1.moveToNext()){
                String address = cursor1.getString(cursor1.getColumnIndex(columns[0]));
                String name = cursor1.getString(cursor1.getColumnIndex(columns[1]));
                String date = cursor1.getString(cursor1.getColumnIndex(columns[2]));
                String msg = cursor1.getString(cursor1.getColumnIndex(columns[3]));
                String type = cursor1.getString(cursor1.getColumnIndex(columns[4]));

                DateFormat simple = new SimpleDateFormat("dd MMM yyyy HH:mm:ss:SSS Z");
                Date result = new Date(Long.parseLong(date));

                String row = address + " " + name + " " + simple.format(result) + " " + msg + " " + "received"+"\n";
                dataset.append(row);
            }
        }
        else {
            throw new Exception("Fails to retrieve SMS inbox messages.");
        }
    }

    private void getSent() throws Exception {
        Uri mSmsinboxQueryUri = Uri.parse("content://sms/sent");
        Cursor cursor1 = context.getContentResolver().query(mSmsinboxQueryUri,new String[] { "_id", "thread_id", "address", "person", "date","body", "type" }, null, null, null);
//        startManagingCursor(cursor1);
        String[] columns = new String[] { "address", "person", "date", "body","type" };
        if (cursor1.getCount() > 0) {
            String count = Integer.toString(cursor1.getCount());
            while (cursor1.moveToNext()){
                String address = cursor1.getString(cursor1.getColumnIndex(columns[0]));
                String name = cursor1.getString(cursor1.getColumnIndex(columns[1]));
                String date = cursor1.getString(cursor1.getColumnIndex(columns[2]));
                String msg = cursor1.getString(cursor1.getColumnIndex(columns[3]));
                String type = cursor1.getString(cursor1.getColumnIndex(columns[4]));

                DateFormat simple = new SimpleDateFormat("dd MMM yyyy HH:mm:ss:SSS Z");

                Date result = new Date(Long.parseLong(date));

                String row = address + " " + name + " " + simple.format(result) + " " + msg + " " + "sent"+"\n";
                dataset.append(row);
            }
        }
        else {
            throw new Exception("Fails to retrieve SMS inbox messages.");
        }
    }
}
