package com.codies.Tattle.UI.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.codies.Tattle.Models.User;
import com.codies.Tattle.R;
import com.codies.Tattle.Utils.App;
import com.codies.Tattle.Utils.Consts;
import com.codies.Tattle.Utils.QBResRequestExecutor;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.model.QBUser;

public class SignupActivity extends AppCompatActivity implements com.codies.Tattle.Utils.Consts {

    public static final String TAG = "SignUpActivity";
    private EditText emailTV, passwordTV,nametv;
    private Button regBtn;
    private TextView alreadyAccout;
    private ProgressBar progressBar;
    User user;

    private QBUser userForSave;
    protected QBResRequestExecutor requestExecutor;

    FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        requestExecutor = App.getInstance().getQbResRequestExecutor();

        mAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();

        initializeUI();
        progressBar.setVisibility(View.INVISIBLE);

        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerNewUser();
            }
        });

        alreadyAccout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    private void registerNewUser() {
        progressBar.setVisibility(View.VISIBLE);

        String email, password, name;
        name = nametv.getText().toString();
        email = emailTV.getText().toString();
        password = passwordTV.getText().toString();

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(getApplicationContext(), "Please enter name...", Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getApplicationContext(), "Please enter email...", Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(getApplicationContext(), "Please enter password!", Toast.LENGTH_LONG).show();
            return;
        }
        if (password.length() < 8) {
            Toast.makeText(getApplicationContext(), "Password length too short!", Toast.LENGTH_LONG).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.i(TAG, "onComplete: "+task.getResult());
                        Log.i(TAG, "onComplete: "+task.getException());
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "Registration successful!", Toast.LENGTH_LONG).show();
                            Log.i(TAG, "onComplete: "+mAuth.getUid());;
                            user = new User(mAuth.getUid(), name, email, null);
                            signUpOnQuickblox(email,password);
                          /*  Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                            startActivity(intent);*/
                        }
                        else {

                            Toast.makeText(getApplicationContext(), "Registration failed! Please try again later", Toast.LENGTH_LONG).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });
    }
    public void addToFirebase(User user) {
        databaseReference.child("users").child(user.getUserId()).setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                progressBar.setVisibility(View.GONE);
                startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i(TAG, "onFailure: " + e.getLocalizedMessage());
                Toast.makeText(SignupActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void signUpOnQuickblox(String userEmail,String userPassword) {
        userForSave = createQBUserWithCurrentData(userEmail,userPassword);
        startSignUpNewUser(userForSave);
    }
    private QBUser createQBUserWithCurrentData(String userEmail, String password) {
        QBUser qbUser = null;
        if (!TextUtils.isEmpty(userEmail) && !TextUtils.isEmpty(password) && !nametv.getText().toString().isEmpty()) {
            qbUser = new QBUser();
            qbUser.setLogin(userEmail);
            qbUser.setEmail(userEmail);
            qbUser.setFullName(nametv.getText().toString());
            qbUser.setPassword(DEFAULT_QB_USER_PASSWORD);
        }
        return qbUser;
    }

    private void startSignUpNewUser(final QBUser newUser) {
        Log.d(TAG, "SignUp New User");
        requestExecutor.signUpNewUser(newUser, new QBEntityCallback<QBUser>() {
                    @Override
                    public void onSuccess(QBUser result, Bundle params) {
                        Log.d(TAG, "SignUp Successful");
                        /*saveUserData(newUser);
                        loginToChat(result);*/
                        addToFirebase(user);
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        Log.d(TAG, "Error SignUp" + e.getMessage());
                        Log.i(TAG, "onError: "+ e.getErrors());
                        Log.i(TAG, "onError: " + e.getMessage());
                        if (e.getHttpStatusCode() == Consts.ERR_LOGIN_ALREADY_TAKEN_HTTP_STATUS) {
//                            signInCreatedUser(newUser);
                            Toast.makeText(SignupActivity.this, "User Already Exists!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                        } else {
                            Toast.makeText(SignupActivity.this, "An error occurred! Try again later.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

    private void initializeUI() {
        emailTV = findViewById(R.id.email);
        passwordTV = findViewById(R.id.password);
        nametv = findViewById(R.id.nameEtRegister);
        regBtn = findViewById(R.id.register);
        alreadyAccout = findViewById(R.id.alreadyAccountTVRegister);
        progressBar = findViewById(R.id.progressBar);
    }
}