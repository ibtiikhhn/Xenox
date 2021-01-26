package com.globalsolutions.Tattle.UI.Activities;

import androidx.annotation.NonNull;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.globalsolutions.Tattle.Models.User;
import com.globalsolutions.Tattle.OtherUtils.ContactUtil;
import com.globalsolutions.Tattle.R;
import com.globalsolutions.Tattle.Services.LoginService;
import com.globalsolutions.Tattle.Utils.App;
import com.globalsolutions.Tattle.Utils.Consts;
import com.globalsolutions.Tattle.Utils.QBResRequestExecutor;
import com.globalsolutions.Tattle.OtherUtils.SharedPrefs;
import com.globalsolutions.Tattle.Utils.SharedPrefsHelper;
import com.globalsolutions.Tattle.Utils.ToastUtils;
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
                closeKeyboard();
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
        if (password.length() < 5) {
            Toast.makeText(getApplicationContext(), "Password length too short!", Toast.LENGTH_LONG).show();
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        loginBtn.setClickable(false);
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
//                            Toast.makeText(getApplicationContext(), "Login successful!", Toast.LENGTH_LONG).show();
                            userFB = new User();
                            userFB.setEmail(email);
                            userForSave = createQBUserWithCurrentData(email, DEFAULT_QB_USER_PASSWORD);
                            Log.i(TAG, "onComplete: email " + userForSave.getEmail());
                            Log.i(TAG, "onComplete: password "+userForSave.getPassword());
                            startSignUpNewUser(userForSave);

                        } else {
                            Log.i(TAG, "onComplete: " + task.getException().getMessage());
                            Toast.makeText(getApplicationContext(), "Login failed! Please try again later", Toast.LENGTH_LONG).show();
                            loginBtn.setClickable(true);
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
                Log.i(TAG, "SignIn Successful success py aya hai");
                sharedPrefsHelper.saveQbUser(userForSave);
//                progressBar.setVisibility(View.GONE);
                loginToChat(userForSave);
//                updateUserOnServer(user);


                //dont know what this method does, if doesn't login, check this with the sample app
//                updateUserOnServer(qbUser);
            }

            @Override
            public void onError(QBResponseException responseException) {
                Log.i(TAG, "Error SignIn error py aya hai" + responseException.getMessage());
           /*     hideProgressDialog();
                ToastUtils.longToast(R.string.sign_in_error);*/
                loginBtn.setClickable(true);
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
                        Log.i(TAG, "SignUp new user py success aya ");
//                        saveUserData(newUser);
                        loginToChat(result);
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        loginBtn.setClickable(true);
                        Log.i(TAG, "Error py agya hai" + e.getMessage());
                        if (e.getHttpStatusCode() == Consts.ERR_LOGIN_ALREADY_TAKEN_HTTP_STATUS) {
                            Log.i(TAG, "onError: if py aya hai");
                            signInCreatedUser(newUser);
                        } else {
//                            hideProgressDialog();
                            Log.i(TAG, "onError: else py aya hai");
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
            qbUser.setFullName(userEmail);
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
                /*sharedPrefs.saveUserData(userFB);
                sharedPrefs.loginUser(true);
                saveUserData(user);
                Intent intent = new Intent(LoginActivity.this, ChatListActivity.class);
                startActivity(intent);
                finish();*/
            }

            @Override
            public void onError(QBResponseException e) {
                Log.i(TAG, "onError:update useronserver wala error hai = "+e.getLocalizedMessage());
                ToastUtils.longToast(R.string.update_user_error);
            }
        });
    }

    private void loginToChat(final QBUser qbUser) {
        Log.i(TAG, "loginToChat: login to chat py agya");
//        qbUser.setPassword(App.USER_DEFAULT_PASSWORD);
        userForSave = qbUser;
        startLoginService(qbUser);
    }

    private void startLoginService(QBUser qbUser) {
        Log.i(TAG, "startLoginService login service py agya");
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
            Log.i(TAG, "onActivityResult:User for save = " + userForSave.getEmail());
            loginBtn.setClickable(true);

            if (isLoginSuccess) {
                sharedPrefs.saveUserData(userFB);
                sharedPrefs.loginUser(true);
                saveUserData(userForSave);
                Intent intent = new Intent(LoginActivity.this, ChatListActivity.class);
                startActivity(intent);
                finish();
//                signInCreatedUser(userForSave);
            } else {
                Log.i(TAG, "onActivityResult: " + errorMessage);
                ToastUtils.longToast(getString(R.string.login_chat_login_error) + errorMessage);
            }
        }
    }

    private void saveUserData(QBUser qbUser) {
        Log.i(TAG, "saveUserData: "+qbUser.toString());
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

    private void closeKeyboard()
    {
        // this will give us the view
        // which is currently focus
        // in this layout
        View view = this.getCurrentFocus();
        // if nothing is currently
        // focus then this will protect
        // the app from crash
        if (view != null) {
            // now assign the system
            // service to InputMethodManager
            InputMethodManager manager
                    = (InputMethodManager)
                    getSystemService(
                            Context.INPUT_METHOD_SERVICE);
            manager
                    .hideSoftInputFromWindow(
                            view.getWindowToken(), 0);
        }
    }
}