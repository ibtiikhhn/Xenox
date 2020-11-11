package com.codies.Tattle.UI.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.codies.Tattle.Models.User;
import com.codies.Tattle.R;
import com.codies.Tattle.Services.CallInitiaterHandler;
import com.codies.Tattle.Services.CallService;
import com.codies.Tattle.Utils.Consts;
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

public class SearchActivity extends AppCompatActivity {

    public static final String TAG = "SearchActivity";

    EditText searchEt;
    ImageButton exitBt;
    View includeView;
    CircularImageView userImg;
    TextView userName;
    TextView userEmail;
    ImageButton chatBt;
    ImageButton audioCallBt;
    ImageButton videoCallBt;
    ImageButton searchBt;
    ProgressBar searchProgress;

    DatabaseReference databaseReference;
    User searchedUser;
    User currentUser;
    FirebaseAuth mAuth;
    CallInitiaterHandler callInitiaterHandler;
    QBUser opponentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        initViews();
        callInitiaterHandler = new CallInitiaterHandler(getApplicationContext(), SearchActivity.this);
        includeView.setVisibility(View.INVISIBLE);
        searchProgress.setVisibility(View.INVISIBLE);

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        exitBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        searchBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query = searchEt.getText().toString();
                searchUser(query);
                searchProgress.setVisibility(View.VISIBLE);
            }
        });

        chatBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SearchActivity.this, ChatActivity.class);
                intent.putExtra("userId", searchedUser.getUserId());
                startActivity(intent);
                finish();

            }
        });

        audioCallBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callInitiaterHandler.setCallType(true);
                callInitiaterHandler.startCall(opponentUser);
            }
        });

        videoCallBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callInitiaterHandler.setCallType(false);
                callInitiaterHandler.startCall(opponentUser);
            }
        });
    }

    public String getCurrentUserEmail() {
        if (mAuth.getCurrentUser() != null) {
            return mAuth.getCurrentUser().getEmail();
        } else {
            return null;
        }
    }


    public void searchUser(String query) {
        searchProgress.setVisibility(View.VISIBLE);
        databaseReference.child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        if (!getCurrentUserEmail().equals(query)) {
                            if (user.getEmail().equals(query)) {
                                searchedUser = user;
                                opponentUser = getUserByEmail(searchedUser.getEmail());
                                displayUser(searchedUser);
                            }
                        } else {
                            Toast.makeText(SearchActivity.this, "You Exist!", Toast.LENGTH_SHORT).show();
                        }
                        searchProgress.setVisibility(View.INVISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(SearchActivity.this, "An Error Occurred..", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void displayUser(User user) {
        includeView.setVisibility(View.VISIBLE);
        userName.setText(user.getName().toString());
        userEmail.setText(user.getEmail().toString());
        if (user.getImageUrl() != null) {
            Glide.with(getApplicationContext()).load(user.getImageUrl()).into(userImg);
        }
        /*if (!user.getImageUrl().equals("")) {
            Glide.with(this).load(user.getImageUrl()).into(userImg);
        }*/
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

    public void initViews() {
        searchEt = findViewById(R.id.searchEmailET);
        exitBt = findViewById(R.id.searchExitBt);
        includeView = findViewById(R.id.searchId);
        searchProgress = findViewById(R.id.searchPB);
        userImg = includeView.findViewById(R.id.searchImage);
        userName = includeView.findViewById(R.id.searchNametv);
        userEmail = includeView.findViewById(R.id.searchEmailtv);
        searchBt = findViewById(R.id.searchBT);
        chatBt = includeView.findViewById(R.id.searchChatBT);
        audioCallBt = includeView.findViewById(R.id.searchAudioCallBT);
        videoCallBt = includeView.findViewById(R.id.searchVideoCallBT);
    }
}