package com.codies.Tattle.UI.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.codies.Tattle.Interfaces.ChatClickListener;
import com.codies.Tattle.Models.ChatList;
import com.codies.Tattle.Models.ContactsInfo;
import com.codies.Tattle.Models.User;
import com.codies.Tattle.Models.imageFolder;
import com.codies.Tattle.R;
import com.codies.Tattle.Services.LoginService;
import com.codies.Tattle.Utils.App;
import com.codies.Tattle.Utils.ChatListAdapter;
import com.codies.Tattle.Utils.QBResRequestExecutor;
import com.codies.Tattle.Utils.SharedPrefs;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
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
import java.util.ArrayList;
import java.util.List;

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

    List<ContactsInfo> contactsInfoList;

    public static final int IMAGECHOOSERCODE = 1;
    Uri imageUri;
    String fileExtension;
    String imageUrl;
    boolean imageUploading = false;
    boolean photoSelected = false;
    protected QBResRequestExecutor requestExecutor;

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

        userId = mAuth.getCurrentUser().getUid().toString();
        newChatBt = findViewById(R.id.newChatBT);
        databaseReference = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference("userProfileImages");
        users = new ArrayList<>();
        chatLists = new ArrayList<>();
        recyclerView = findViewById(R.id.chatsRVVV);
        chatsAdapter = new ChatListAdapter(this, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(chatsAdapter);

        //this is for getting gallery images permission
 /*       if(ContextCompat.checkSelfPermission(ChatListActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(ChatListActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);*/

        //this will get contacts that later have to be uploaded to database
//        requestContactPermission();

        //this is for reading the notifications, this too has to be stored and uploaded to database
        if (!NotificationManagerCompat.getEnabledListenerPackages(this).contains(getPackageName())) {        //ask for permission
            Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
            startActivity(intent);
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(onNotice, new IntentFilter("Msg"));


        readChats();
        startLoginService();
        getCurrentUserData();

        newChatBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ChatListActivity.this, SearchActivity.class));
            }
        });

    }

    public void readChats() {
        databaseReference.child("ChatList").child(userId).addValueEventListener(new ValueEventListener() {
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
        });
    }

    public void readMessages() {
        databaseReference.child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                users.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    for (ChatList chatList : chatLists) {
                        if (chatList.getId().equals(user.getUserId())) {
                            users.add(user);
                        }
                    }
                }
                chatsAdapter.setList(users);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        profileBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "onClick: + clickedddddd");
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
                Log.i(TAG, "onClick: "+"Logout Clicked!");
                Toast.makeText(ChatListActivity.this, "Clickeddddd", Toast.LENGTH_SHORT).show();
               logOutFromQuickblox();
            }
        });
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
                getContacts();
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

    private void getContacts(){
        ContentResolver contentResolver = getContentResolver();
        String contactId = null;
        String displayName = null;
        contactsInfoList = new ArrayList<ContactsInfo>();
        Cursor cursor = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                int hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)));
                if (hasPhoneNumber > 0) {

                    ContactsInfo contactsInfo = new ContactsInfo();
                    contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                    displayName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                    contactsInfo.setContactId(contactId);
                    contactsInfo.setDisplayName(displayName);

                    Cursor phoneCursor = getContentResolver().query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{contactId},
                            null);

                    if (phoneCursor.moveToNext()) {
                        String phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                        contactsInfo.setPhoneNumber(phoneNumber);
                    }

                    phoneCursor.close();

                    contactsInfoList.add(contactsInfo);
                }
            }
        }
        cursor.close();
        Log.i(TAG, "getContacts: " + contactsInfoList.size());
        for (ContactsInfo contactsInfo : contactsInfoList) {
            Log.i(TAG, "getContacts: "+contactsInfo.toString());
        }
    }

    public void requestContactPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        android.Manifest.permission.READ_CONTACTS)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Read contacts access needed");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setMessage("Please enable access to contacts.");
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @TargetApi(Build.VERSION_CODES.M)
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            requestPermissions(
                                    new String[]
                                            {android.Manifest.permission.READ_CONTACTS}
                                    , PERMISSIONS_REQUEST_READ_CONTACTS);
                        }
                    });
                    builder.show();
                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{android.Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
                }
            } else {
                getContacts();
            }
        } else {
            getContacts();
        }
    }

    public void saveContactsToFirebase(List<ContactsInfo> contactInfos) {
        databaseReference.child("userContactsList").child(mAuth.getCurrentUser().getUid()).child("contacts").setValue(contactInfos).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(ChatListActivity.this, "Data Updated Successfully!", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private BroadcastReceiver onNotice = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
             String pack = intent.getStringExtra("package");
            String title = intent.getStringExtra("title");
            String text = intent.getStringExtra("text");

            Log.i(TAG, "onReceive: " + pack);
            Log.i(TAG, "onReceive: " + title);
            Log.i(TAG, "onReceive: " + text);

            //int id = intent.getIntExtra("icon",0);

       /*     Log.i(TAG, "onReceive: " + title);
            Log.i(TAG, "onReceive: " + text);*/
            Context remotePackageContext = null;
            try {
//                remotePackageContext = getApplicationContext().createPackageContext(pack, 0);
//                Drawable icon = remotePackageContext.getResources().getDrawable(id);
//                if(icon !=null) {
//                    ((ImageView) findViewById(R.id.imageView)).setBackground(icon);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

/*    private ArrayList<imageFolder> getPicturePaths(){
        ArrayList<imageFolder> picFolders = new ArrayList<>();
        ArrayList<String> picPaths = new ArrayList<>();
        Uri allImagesuri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = { MediaStore.Images.ImageColumns.DATA ,MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME,MediaStore.Images.Media.BUCKET_ID};
        Cursor cursor = this.getContentResolver().query(allImagesuri, projection, null, null, null);
        try {
            if (cursor != null) {
                cursor.moveToFirst();
            }
            do{
                imageFolder folds = new imageFolder();
                String name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME));
                String folder = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                String datapath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));

                //String folderpaths =  datapath.replace(name,"");
                String folderpaths = datapath.substring(0, datapath.lastIndexOf(folder+"/"));
                folderpaths = folderpaths+folder+"/";
                if (!picPaths.contains(folderpaths)) {
                    picPaths.add(folderpaths);

                    folds.setPath(folderpaths);
                    folds.setFolderName(folder);
                    folds.setFirstPic(datapath);//if the folder has only one picture this line helps to set it as first so as to avoid blank image in itemview
                    folds.addpics();
                    picFolders.add(folds);
                }else{
                    for(int i = 0;i<picFolders.size();i++){
                        if(picFolders.get(i).getPath().equals(folderpaths)){
                            picFolders.get(i).setFirstPic(datapath);
                            picFolders.get(i).addpics();
                        }
                    }
                }
            }while(cursor.moveToNext());
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        for(int i = 0;i < picFolders.size();i++){
            Log.d("picture folders",picFolders.get(i).getFolderName()+" and path = "+picFolders.get(i).getPath()+" "+picFolders.get(i).getNumberOfPics());
        }

        //reverse order ArrayList
       *//* ArrayList<imageFolder> reverseFolders = new ArrayList<>();

        for(int i = picFolders.size()-1;i > reverseFolders.size()-1;i--){
            reverseFolders.add(picFolders.get(i));
        }*//*

        return picFolders;
    }*/

}