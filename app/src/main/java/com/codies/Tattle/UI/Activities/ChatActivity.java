package com.codies.Tattle.UI.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codies.Tattle.Models.Chat;
import com.codies.Tattle.Models.User;
import com.codies.Tattle.R;
import com.codies.Tattle.Services.CallInitiaterHandler;
import com.codies.Tattle.Services.CallService;
import com.codies.Tattle.Utils.Consts;
import com.codies.Tattle.Utils.MessageAdapter;
import com.codies.Tattle.Utils.SharedPrefs;
import com.codies.Tattle.Utils.SharedPrefsHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;


import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    public static final String TAG = "HOLLO";
    RecyclerView recyclerView;
    EditText text;
    ImageButton send;
    ImageButton audioCall;
    ImageButton videoCall;
    MessageAdapter messageAdapterUser;
    List<Chat> chatList;
    SharedPrefs sharedPrefs;
    CircularImageView profileIMG;
    TextView name;
    AppCompatImageView imageView;
    DatabaseReference reference;
    User globalUser;
    String senderId;
    String receiverId;
    FirebaseAuth mAuth;
    String userEmail;
    CallInitiaterHandler callInitiaterHandler;
    QBUser opponentUser;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        send = findViewById(R.id.sendEt);
        text = findViewById(R.id.messageET);
        sharedPrefs = SharedPrefs.getInstance(this);
        profileIMG = findViewById(R.id.toolbarIMG);
        name = findViewById(R.id.toolbarName);
        imageView = findViewById(R.id.backBT);
        audioCall = findViewById(R.id.chat_audioCallBt);
        videoCall = findViewById(R.id.chat_videoCallBt);
        mAuth = FirebaseAuth.getInstance();
        Intent intent = getIntent();
        receiverId = intent.getStringExtra("userId");
        /*imageURL = intent.getStringExtra("image");
        personName = intent.getStringExtra("name");*/
//        Glide.with(getApplicationContext()).load(imageURL).into(profileIMG);
        senderId = mAuth.getCurrentUser().getUid();
        chatList = new ArrayList<>();
        messageAdapterUser = new MessageAdapter(this,senderId);
        recyclerView = findViewById(R.id.chatRV);
        reference = FirebaseDatabase.getInstance().getReference();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(messageAdapterUser);
        getCurrentUserData();
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
                    sendMessage(messagee);
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
    }

    public void sendMessage(String message) {
        Chat chat = new Chat(senderId, receiverId, message);
        reference.child("Chats").push().setValue(chat);

        reference.child("ChatList").child(senderId).child(receiverId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    reference.child("ChatList").child(senderId).child(receiverId).child("id").setValue(receiverId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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

    public void readMessages() {
        reference.child("Chats").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat != null) {
                        if (chat.getReceiver().equals(receiverId) && chat.getSender().equals(senderId) ||
                                chat.getReceiver().equals(senderId) && chat.getSender().equals(receiverId)) {
                            chatList.add(chat);
                        }
                    } else {
                        Toast.makeText(ChatActivity.this, "No Chat Exists!", Toast.LENGTH_SHORT).show();
                    }
                }
                messageAdapterUser.setList(chatList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void getCurrentUserData() {
        if (mAuth.getCurrentUser() != null) {
            reference.child("users").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        User user = dataSnapshot.getValue(User.class);
                        if (user != null) {
                            if (user.getUserId().equals(receiverId)) {
                                globalUser = user;
                                name.setText(user.getName());
                                userEmail = user.getEmail();
                                opponentUser = getUserByEmail(userEmail);
                                if (globalUser.getImageUrl() != null) {
                                    Glide.with(getApplicationContext()).load(globalUser.getImageUrl()).into(profileIMG);
                                }
                                Log.i(TAG, "onDataChange: "+user.getName());
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

    public QBUser getUserByEmail(String email) {
        Log.i(TAG, "getUserByEmail: " + email);
        final QBUser[] qbUserr = new QBUser[1];
        QBUsers.getUserByEmail(email).performAsync(new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser qbUser, Bundle bundle) {
                Log.i(TAG, "onSuccess: "+qbUser.toString());
                if (qbUser != null) {
                    opponentUser = qbUser;
                    qbUserr[0] = opponentUser;

                }
                Log.i(TAG, "onSuccess: "+"opponent user is null");
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
}