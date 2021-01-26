package com.globalsolutions.Tattle.UI.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.globalsolutions.Tattle.Models.User;
import com.globalsolutions.Tattle.R;
import com.globalsolutions.Tattle.Utils.App;
import com.globalsolutions.Tattle.Utils.Consts;
import com.globalsolutions.Tattle.Utils.QBResRequestExecutor;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.model.QBUser;

import java.io.File;

public class SignupActivity extends AppCompatActivity implements com.globalsolutions.Tattle.Utils.Consts {

    public static final String TAG = "SignupActivity";
    public static final String MESSAGE_STATUS = "message_status";
    public static final int PERMISSIONS_REQUEST_CODE = 1240;

    String[] appPermissions = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    private EditText emailTV, passwordTV,nametv;
    private Button regBtn;
    private TextView alreadyAccout;
    private ProgressBar progressBar;
    private ProgressBar photoUploadPB;
    User user;

    public static final int IMAGECHOOSERCODE = 1;
    Uri imageUri;
    String fileExtension;
    String imageUrl;
    boolean imageUploading = false;
    boolean photoSelected = false;
    StorageReference storageReference;

    private QBUser userForSave;
    protected QBResRequestExecutor requestExecutor;

    private CircularImageView selectPhotoBT;

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
        storageReference = FirebaseStorage.getInstance().getReference("userProfileImages");

        initializeUI();
//        LocalBroadcastManager.getInstance(this).registerReceiver(onNotice, new IntentFilter("Msg"));

        progressBar.setVisibility(View.INVISIBLE);
        photoUploadPB.setVisibility(View.INVISIBLE);

        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeKeyboard();
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
        selectPhotoBT.setClickable(false);
    /*    selectPhotoBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isStoragePermissionGranted()) {
                    openImageChooser();
                } else {
                    isStoragePermissionGranted();
                }
            }
        });*/
    }



    private void registerNewUser() {

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
        if (password.length() < 5) {
            Toast.makeText(getApplicationContext(), "Password length too short!", Toast.LENGTH_LONG).show();
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        regBtn.setClickable(false);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        task.addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                Toast.makeText(getApplicationContext(), "Registration successful!", Toast.LENGTH_LONG).show();
                                Log.i(TAG, "onComplete: " + mAuth.getUid());
                                if (photoSelected && imageUploading) {
                                    Toast.makeText(SignupActivity.this, "Wait, uploading photo!", Toast.LENGTH_SHORT).show();
                                } else {
                                    user = new User(mAuth.getUid(), name, email, imageUrl, password);
                                    signUpOnQuickblox(email, DEFAULT_QB_USER_PASSWORD);
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                                progressBar.setVisibility(View.GONE);
                                regBtn.setClickable(true);
                            }
                        });
                    }
                });
    }
    public void addToFirebase(User user) {
        databaseReference.child("users").child(user.getUserId()).setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.i(TAG, "hellllllponSuccess: "+user.getEmail()+" "+user.getUserId()+" "+user.getName()+" "+user.getImageUrl()+" "+user.getPassword());
                progressBar.setVisibility(View.GONE);
                regBtn.setClickable(true);
                startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i(TAG, "onFailure: " + e.getLocalizedMessage());
                regBtn.setClickable(true);
                Toast.makeText(SignupActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void signUpOnQuickblox(String userEmail,String userPassword) {
        userForSave = createQBUserWithCurrentData(userEmail,userPassword);
        startSignUpNewUser(userForSave);
    }
    private QBUser createQBUserWithCurrentData(String userEmail, String password) {
       /* QBUser qbUser = null;
        if (!TextUtils.isEmpty(userEmail) && !TextUtils.isEmpty(password) && !nametv.getText().toString().isEmpty()) {
            qbUser = new QBUser();
            qbUser.setLogin(userEmail);
            qbUser.setEmail(userEmail);
            qbUser.setFullName(nametv.getText().toString());
            qbUser.setPassword(DEFAULT_QB_USER_PASSWORD);
        }
        return qbUser;*/
        QBUser qbUser = null;
        if (!TextUtils.isEmpty(userEmail) && !TextUtils.isEmpty(password)) {
            qbUser = new QBUser();
//            qbUser.setLogin(userEmail);
            qbUser.setFullName(userEmail);
            qbUser.setEmail(userEmail);
//            qbUser.setFullName(password);
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
                        Log.i(TAG, "onError: "+ e.getErrors());
                        Log.i(TAG, "onError: " + e.getMessage());
                        progressBar.setVisibility(View.INVISIBLE);
                        regBtn.setClickable(true);
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
        selectPhotoBT = findViewById(R.id.addProfileImgCivRegister);
        nametv = findViewById(R.id.nameEtRegister);
        regBtn = findViewById(R.id.register);
        alreadyAccout = findViewById(R.id.alreadyAccountTVRegister);
        progressBar = findViewById(R.id.progressBar);
        photoUploadPB = findViewById(R.id.signupPhotoUploadPB);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            Log.v(TAG, "Permission: " + permissions[0] + "was " + grantResults[0]);
            //resume tasks needing this permission
            openImageChooser();
        }
    }

    public void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMAGECHOOSERCODE);
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
                        Toast.makeText(SignupActivity.this, "Failed to upload photo!", Toast.LENGTH_SHORT).show();
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
                Log.i(TAG, "onFailure: " + e.getLocalizedMessage());
                Toast.makeText(SignupActivity.this, "Failed To Upload : " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGECHOOSERCODE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            photoSelected = true;
            Glide.with(this).load(imageUri).into(selectPhotoBT);
            fileExtension = getFileExtension(imageUri);
            uploadFile(imageUri, fileExtension);
        }
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
    private void closeKeyboard()
    {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}