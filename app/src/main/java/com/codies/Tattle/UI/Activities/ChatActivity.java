package com.codies.Tattle.UI.Activities;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codies.Tattle.Models.Chat;
import com.codies.Tattle.Models.ChatList;
import com.codies.Tattle.Models.User;
import com.codies.Tattle.R;
import com.codies.Tattle.Services.CallInitiaterHandler;
import com.codies.Tattle.Services.CallService;
import com.codies.Tattle.Utils.Consts;
import com.codies.Tattle.Adapters.MessageAdapter;
import com.codies.Tattle.OtherUtils.SharedPrefs;
import com.codies.Tattle.Utils.SharedPrefsHelper;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
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
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.codies.Tattle.UI.Activities.ChatListActivity.IMAGECHOOSERCODE;

public class ChatActivity extends AppCompatActivity {

    public static final String TAG = "HOLLO";
    RecyclerView recyclerView;
    EditText text;
    AppCompatImageView send;
    ImageButton audioCall;
    ImageButton videoCall;
    ImageView imageChooser;
    MessageAdapter messageAdapterUser;
    List<Chat> chatList;
    SharedPrefs sharedPrefs;
    CircularImageView profileIMG;
    TextView receiverName;
    AppCompatImageView imageView;
    DatabaseReference reference;
    User receiverUser;
    User senderUser;

    String senderId;
    String receiverId;
    FirebaseAuth mAuth;
    String combinedId;
    String userEmail;
    String uploadImgURL = "";
    CallInitiaterHandler callInitiaterHandler;
    QBUser opponentUser;
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        send = findViewById(R.id.sendEt);
        text = findViewById(R.id.messageET);
        sharedPrefs = SharedPrefs.getInstance(this);
        profileIMG = findViewById(R.id.toolbarIMG);
        receiverName = findViewById(R.id.toolbarName);
        imageView = findViewById(R.id.backBT);
        audioCall = findViewById(R.id.chat_audioCallBt);
        videoCall = findViewById(R.id.chat_videoCallBt);
        imageChooser = findViewById(R.id.imageChooserIV);

        storageReference = FirebaseStorage.getInstance().getReference("ChatImages");
        mAuth = FirebaseAuth.getInstance();
        senderId = mAuth.getCurrentUser().getUid();
        Intent intent = getIntent();
        if (intent.getBooleanExtra("fromSearch", false)) {
            receiverId = intent.getStringExtra("userId");
            combinedId = setOneToOneChat(senderId, receiverId);
        } else {
            receiverId = intent.getStringExtra("receiverId");
            combinedId = intent.getStringExtra("combinedId");
        }
        chatList = new ArrayList<>();
        messageAdapterUser = new MessageAdapter(this, senderId, chatList);
        recyclerView = findViewById(R.id.chatRV);
        reference = FirebaseDatabase.getInstance().getReference();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(messageAdapterUser);
        getReceiverUserData(receiverId);
        getSenderUserData();
        readMessages();
        callInitiaterHandler = new CallInitiaterHandler(getApplicationContext(), ChatActivity.this);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messagee = text.getText().toString();
                if (!messagee.isEmpty()) {
                    sendMessage(messagee, false);
                } else {
                    Toast.makeText(ChatActivity.this, "Can't send empty message", Toast.LENGTH_SHORT).show();
                }
                text.setText("");
            }
        });

        audioCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            /*    Intent intent = new Intent(ChatActivity.this, OpponentsActivity.class);
                intent.putExtra("userEmail", userEmail);
                intent.putExtra("isAudioCall", true);
                startActivity(intent);*/
                callInitiaterHandler.setCallType(true);
                callInitiaterHandler.startCall(opponentUser);
            }
        });

        videoCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
         /*       Intent intent = new Intent(ChatActivity.this, OpponentsActivity.class);
                intent.putExtra("userEmail", userEmail);
                intent.putExtra("isAudioCall", false);
                startActivity(intent);*/
                callInitiaterHandler.setCallType(false);
                callInitiaterHandler.startCall(opponentUser);
            }
        });

        imageChooser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isStoragePermissionGranted()) {
                    openImageChooser();
                } else {
                    isStoragePermissionGranted();
                }
            }
        });
    }

    public void sendMessage(String message, boolean isImage) {
        Chat chat = new Chat(senderId, receiverId, message, isImage);
        Log.i(TAG, "sendMessage: sender id = " + senderId);
        Log.i(TAG, "sendMessage: receiver id = " + receiverId);
        reference.child("Messages").child(combinedId).push().setValue(chat);
        ChatList currentUserChatlist = new ChatList(receiverUser.getName(), receiverUser.getImageUrl(), senderId, receiverId, combinedId,message);
        ChatList opponentUserChatlist = new ChatList(senderUser.getName(), senderUser.getImageUrl(), senderId, receiverId, combinedId,message);

//            ChatList chatList = new ChatList(senderUser.getName(), senderUser.getImageUrl(), senderUser.getUserId(), receiverUser.getName(), receiverUser.getImageUrl(), receiverUser.getUserId(), combinedId, message);
        reference.child("UserChatList").child(mAuth.getCurrentUser().getUid()).child(combinedId).setValue(currentUserChatlist).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                reference.child("UserChatList").child(receiverId).child(combinedId).setValue(opponentUserChatlist).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "onSuccess: image msg ma b send ho gye ");
                    }
                });
            }
        });


    }


    public void readMessages() {
        if (combinedId != null && !combinedId.isEmpty()) {
            reference.child("Messages").child(combinedId).addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    Chat chat = snapshot.getValue(Chat.class);
                    chatList.add(chat);
                    messageAdapterUser.notifyDataSetChanged();
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } else {
            String myCombinedId = setOneToOneChat(senderId, receiverId);
            reference.child("Messages").child(myCombinedId).addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    Chat chat = snapshot.getValue(Chat.class);
                    chatList.add(chat);
                    messageAdapterUser.notifyDataSetChanged();
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean isIncomingCall = SharedPrefsHelper.getInstance().get(Consts.EXTRA_IS_INCOMING_CALL, false);
        if (callInitiaterHandler.isCallServiceRunning(CallService.class)) {
            Log.d(TAG, "CallService is running now");
            CallActivity.start(this, isIncomingCall);
        }
        callInitiaterHandler.clearAppNotifications();
    }

    public QBUser getUserByEmail(String email) {
        Log.i(TAG, "getUserByEmail: " + email);
        final QBUser[] qbUserr = new QBUser[1];
        QBUsers.getUserByEmail(email).performAsync(new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser qbUser, Bundle bundle) {
                Log.i(TAG, "onSuccess: " + qbUser.toString());
                if (qbUser != null) {
                    opponentUser = qbUser;
                    qbUserr[0] = opponentUser;
                }
                Log.i(TAG, "onSuccess: " + "opponent user is null");
            }

            @Override
            public void onError(QBResponseException e) {
                Log.i(TAG, "onError: " + e.getMessage());
                Log.i(TAG, "onError: " + e.getErrors());
                Log.i(TAG, "onError: " + e.getHttpStatusCode());
                Log.i(TAG, "onError: " + e.getLocalizedMessage());
//                Toast.makeText(this, "User not found!!", Toast.LENGTH_SHORT).show();
            }
        });
        return qbUserr[0];
    }

    private String setOneToOneChat(String senderId, String receiverId) {
        int compare = senderId.compareTo(receiverId);
        if (compare < 0) {
            //a is smaller
            return senderId + "_" + receiverId;
        } else /*if (compare > 0) */ {
            //a is larger
            return receiverId + "_" + senderId;
        } /*else return null;*/
    }

    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission is granted");
//                openImageChooser();
                return true;
            } else {
                Log.v(TAG, "Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Permission is granted");
//            openImageChooser();
            return true;
        }
    }

    public void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMAGECHOOSERCODE);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGECHOOSERCODE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
//            Glide.with(this).load(imageUri).into(dialogueProfileImg);
            String fileExtension = getFileExtension(imageUri);
            Log.i(TAG, "onActivityResult: image agye ");
            uploadFile(imageUri, fileExtension);
        }
    }

    public void uploadFile(Uri imageUri, String extension) {
        Log.i(TAG, "uploadFile: ye call hua ");
        final StorageReference storageReference = this.storageReference.child(System.currentTimeMillis() + "." + extension);
        storageReference.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Log.i(TAG, "onSuccess: photo upload ho k url b agya");
                        uploadImgURL = uri.toString();
                        sendMessage(uploadImgURL, true);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ChatActivity.this, "Failed to upload photo!", Toast.LENGTH_SHORT).show();
                        Log.i(TAG, "onFailure: " + e.getLocalizedMessage());
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //tell user that uploading failed
                Toast.makeText(ChatActivity.this, "Failed To Upload : " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                //keep the user updated about this task
                double val = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
            }
        });
    }

    public void getSenderUserData() {
        if (mAuth.getCurrentUser() != null) {
            reference.child("users").child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        senderUser = user;
                        /*if (user.getImageUrl() != null) {
                            Glide.with(ChatListActivity.this).load(user.getImageUrl()).into(profileBt);
                        }*/
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


    public void getReceiverUserData(String receiverId) {
        if (mAuth.getCurrentUser() != null) {
            reference.child("users").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        User user = dataSnapshot.getValue(User.class);
                        if (user != null) {
                            if (user.getUserId().equals(receiverId)) {
                                receiverUser = user;
                                receiverName.setText(user.getName());
                                userEmail = user.getEmail();
                                opponentUser = getUserByEmail(userEmail);
                                if (receiverUser.getImageUrl() != null) {
                                    Glide.with(getApplicationContext()).load(receiverUser.getImageUrl()).into(profileIMG);
                                }
                                Log.i(TAG, "onDataChange: " + user.getName());
                                //user ki image agr set krni ho to yaha py kro
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }
}