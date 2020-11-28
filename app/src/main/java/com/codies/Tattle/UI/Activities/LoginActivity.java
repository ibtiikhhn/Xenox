package com.codies.Tattle.UI.Activities;

import androidx.annotation.NonNull;

import android.app.PendingIntent;
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
import com.codies.Tattle.OtherUtils.ContactUtil;
import com.codies.Tattle.R;
import com.codies.Tattle.Services.LoginService;
import com.codies.Tattle.Utils.App;
import com.codies.Tattle.Utils.Consts;
import com.codies.Tattle.Utils.QBResRequestExecutor;
import com.codies.Tattle.OtherUtils.SharedPrefs;
import com.codies.Tattle.Utils.SharedPrefsHelper;
import com.codies.Tattle.Utils.ToastUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

public class LoginActivity extends BaseActivity implements Consts {

    public static final String TAG = "LoginActivity";
    private EditText emailTV, passwordTV;
    private Button loginBtn;
    private ProgressBar progressBar;
    private TextView dontHaveAdcount;
    private TextView forgotPassword;
    SharedPrefs sharedPrefs;
    User userFB;

    private FirebaseAuth mAuth;
    private QBUser userForSave;
    protected QBResRequestExecutor requestExecutor;

    ContactUtil contactUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        sharedPrefs = SharedPrefs.getInstance(this);

        initializeUI();

        if (sharedPrefs.isLoggedIn()) {
            startActivity(new Intent(LoginActivity.this, ChatListActivity.class));
            finish();
        }

        mAuth = FirebaseAuth.getInstance();
        requestExecutor = App.getInstance().getQbResRequestExecutor();

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUserAccount();
            }
        });

        dontHaveAdcount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        });

        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailAddress = emailTV.getText().toString();
                if (emailAddress.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Enter an email first!", Toast.LENGTH_SHORT).show();
                } else {
                    mAuth.sendPasswordResetEmail(emailAddress)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(LoginActivity.this, "Password Reset Link has been sent to your email!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(LoginActivity.this, "Something wrong happened!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    private void loginUserAccount() {
        progressBar.setVisibility(View.VISIBLE);

        String email, password;
        email = emailTV.getText().toString();
        password = passwordTV.getText().toString();

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

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
//                            Toast.makeText(getApplicationContext(), "Login successful!", Toast.LENGTH_LONG).show();
                            userFB = new User();
                            userFB.setEmail(email);
                            userForSave = createQBUserWithCurrentData(email, password);
                            Log.i(TAG, "onComplete: " + userForSave.getEmail());
                            Log.i(TAG, "onComplete: "+userForSave.getPassword());
                            startSignUpNewUser(userForSave);

                        } else {
                            Log.i(TAG, "onComplete: " + task.getException().getMessage());
                            Toast.makeText(getApplicationContext(), "Login failed! Please try again later", Toast.LENGTH_LONG).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });
    }

    private void signInCreatedUser(final QBUser qbUser) {
        Log.d(TAG, "SignIn Started");
        requestExecutor.signInUser(qbUser, new QBEntityCallbackImpl<QBUser>() {
            @Override
            public void onSuccess(QBUser user, Bundle params) {
                Log.d(TAG, "SignIn Successful");
                Log.i(TAG, "onSuccess: "+user.toString());
                sharedPrefsHelper.saveQbUser(userForSave);
//                progressBar.setVisibility(View.GONE);
                updateUserOnServer(user);


                //dont know what this method does, if doesn't login, check this with the sample app
//                updateUserOnServer(qbUser);
            }

            @Override
            public void onError(QBResponseException responseException) {
                Log.d(TAG, "Error SignIn" + responseException.getMessage());
           /*     hideProgressDialog();
                ToastUtils.longToast(R.string.sign_in_error);*/
                Toast.makeText(LoginActivity.this, "Error Signing up! Try again later", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startSignUpNewUser(final QBUser newUser) {
        Log.d(TAG, "SignUp New User");
//        showProgressDialog(R.string.dlg_creating_new_user);
        requestExecutor.signUpNewUser(newUser, new QBEntityCallback<QBUser>() {
                    @Override
                    public void onSuccess(QBUser result, Bundle params) {
                        Log.d(TAG, "SignUp Successful");
                        saveUserData(newUser);
                        loginToChat(result);
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        Log.d(TAG, "Error SignUp" + e.getMessage());
                        if (e.getHttpStatusCode() == Consts.ERR_LOGIN_ALREADY_TAKEN_HTTP_STATUS) {
                            signInCreatedUser(newUser);
                        } else {
//                            hideProgressDialog();
                            ToastUtils.longToast(R.string.sign_up_error);
                        }
                    }
                }
        );
    }

    private QBUser createQBUserWithCurrentData(String userEmail, String password) {
        QBUser qbUser = null;
        if (!TextUtils.isEmpty(userEmail) && !TextUtils.isEmpty(password)) {
            qbUser = new QBUser();
//            qbUser.setLogin(userEmail);
            qbUser.setEmail(userEmail);
//            qbUser.setFullName(password);
            qbUser.setPassword(DEFAULT_QB_USER_PASSWORD);
        }
        return qbUser;
    }

    private void updateUserOnServer(QBUser user) {
        user.setPassword(null);
        QBUsers.updateUser(user).performAsync(new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser qbUser, Bundle bundle) {
//                OpponentsActivity.start(LoginActivity.this);
                sharedPrefs.saveUserData(userFB);
                sharedPrefs.loginUser(true);
                saveUserData(user);
                Intent intent = new Intent(LoginActivity.this, ChatListActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(QBResponseException e) {
                ToastUtils.longToast(R.string.update_user_error);
            }
        });
    }

    private void loginToChat(final QBUser qbUser) {
        qbUser.setPassword(App.USER_DEFAULT_PASSWORD);
        userForSave = qbUser;
        startLoginService(qbUser);
    }

    private void startLoginService(QBUser qbUser) {
        Intent tempIntent = new Intent(this, LoginService.class);
        PendingIntent pendingIntent = createPendingResult(Consts.EXTRA_LOGIN_RESULT_CODE, tempIntent, 0);
        LoginService.start(this, qbUser, pendingIntent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Consts.EXTRA_LOGIN_RESULT_CODE) {
            boolean isLoginSuccess = data.getBooleanExtra(Consts.EXTRA_LOGIN_RESULT, false);
            String errorMessage = data.getStringExtra(Consts.EXTRA_LOGIN_ERROR_MESSAGE);

            if (isLoginSuccess) {
                saveUserData(userForSave);
                signInCreatedUser(userForSave);
            } else {
                ToastUtils.longToast(getString(R.string.login_chat_login_error) + errorMessage);
            }
        }
    }

    private void saveUserData(QBUser qbUser) {
        SharedPrefsHelper sharedPrefsHelper = SharedPrefsHelper.getInstance();
        sharedPrefsHelper.saveQbUser(qbUser);
    }

    private void initializeUI() {
        emailTV = findViewById(R.id.email);
        passwordTV = findViewById(R.id.password);
        dontHaveAdcount = findViewById(R.id.dontHaveAccountTVLogin);
        loginBtn = findViewById(R.id.login);
        forgotPassword = findViewById(R.id.forgotPassword);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);
    }
}