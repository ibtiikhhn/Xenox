package com.codies.Tattle.UI.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.codies.Tattle.Adapters.SearchAdapter;
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

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends BaseActivity {

    public static final String TAG = "SearchActivity";

    EditText searchEt;
    ImageButton exitBt;
    ImageButton searchBt;
    ProgressBar searchProgress;

    DatabaseReference databaseReference;
    FirebaseAuth mAuth;
    CallInitiaterHandler callInitiaterHandler;
    QBUser opponentUser;
    List<User> searchUserList;

    RecyclerView searchRv;
    SearchAdapter searchAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        initViews();

        callInitiaterHandler = new CallInitiaterHandler(getApplicationContext(), SearchActivity.this);
        searchUserList = new ArrayList<>();
        searchAdapter = new SearchAdapter(this);
        searchRv.setLayoutManager(new LinearLayoutManager(this));
        searchRv.setAdapter(searchAdapter);

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
    }

    public String getCurrentUserEmail() {
        if (mAuth.getCurrentUser() != null) {
            return mAuth.getCurrentUser().getEmail();
        } else {
            return null;
        }
    }


    public void searchUser(String query) {
        databaseReference.child("users").getRef().orderByChild("name").startAt(query).endAt(query + "\uf8ff").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                searchUserList.clear();
                if (snapshot.getChildrenCount() > 0) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        User user = dataSnapshot.getValue(User.class);
                        if (user != null) {
                            if (!user.getEmail().equals(getCurrentUserEmail())) {
                                searchUserList.add(user);
                            } else {
                                Toast.makeText(SearchActivity.this, "You Searched Your Own Name!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                    searchProgress.setVisibility(View.INVISIBLE);
                    searchAdapter.setList(searchUserList);
                } else {
                    Toast.makeText(SearchActivity.this, "User Not Found!!", Toast.LENGTH_SHORT).show();
                    searchProgress.setVisibility(View.INVISIBLE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SearchActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    public QBUser getUserByEmail(String email) {
        final QBUser[] qbUserr = new QBUser[1];
        QBUsers.getUserByEmail(email).performAsync(new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser qbUser, Bundle bundle) {
                if (qbUser != null) {
                    opponentUser = qbUser;
                    qbUserr[0] = opponentUser;
                }
            }

            @Override
            public void onError(QBResponseException e) {
                Log.i(TAG, "onError: " + e.getMessage());
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
            CallActivity.start(this, isIncomingCall);
        }
        callInitiaterHandler.clearAppNotifications();
    }

    public void initViews() {
        searchEt = findViewById(R.id.searchEmailET);
        exitBt = findViewById(R.id.searchExitBt);
        searchProgress = findViewById(R.id.searchPB);
        searchRv = findViewById(R.id.searchRV);
        searchBt = findViewById(R.id.searchBT);
        searchProgress.setVisibility(View.INVISIBLE);
    }

    public void initAudioCall(String email) {
        QBUsers.getUserByEmail(email).performAsync(new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser qbUser, Bundle bundle) {
                if (qbUser != null) {
                    callInitiaterHandler.setCallType(true);
                    callInitiaterHandler.startCall(qbUser);
                }
            }

            @Override
            public void onError(QBResponseException e) {
                Log.i(TAG, "onError: " + e.getMessage());
//                Toast.makeText(this, "User not found!!", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void initVideoCall(String email) {
        QBUsers.getUserByEmail(email).performAsync(new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser qbUser, Bundle bundle) {
                if (qbUser != null) {
                    callInitiaterHandler.setCallType(false);
                    callInitiaterHandler.startCall(qbUser);
                }
            }

            @Override
            public void onError(QBResponseException e) {
                Log.i(TAG, "onError: " + e.getMessage());
//                Toast.makeText(this, "User not found!!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void initChat(String userId) {
        Intent intent = new Intent(SearchActivity.this, ChatActivity.class);
        intent.putExtra("userId", userId);
        intent.putExtra("fromSearch", true);
        startActivity(intent);
        finish();
    }

}