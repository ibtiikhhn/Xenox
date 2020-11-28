package com.codies.Tattle.UI.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.codies.Tattle.Models.User;
import com.codies.Tattle.R;
import com.codies.Tattle.Utils.App;
import com.codies.Tattle.Utils.Consts;
import com.codies.Tattle.Utils.QBResRequestExecutor;
import com.codies.Tattle.OtherUtils.SaveNotifications;
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

public class SignupActivity extends AppCompatActivity implements com.codies.Tattle.Utils.Consts {

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

    SaveNotifications saveNotifications;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        requestExecutor = App.getInstance().getQbResRequestExecutor();
        mAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
        storageReference = FirebaseStorage.getInstance().getReference("userProfileImages");
        saveNotifications = new SaveNotifications();

        initializeUI();
//        LocalBroadcastManager.getInstance(this).registerReceiver(onNotice, new IntentFilter("Msg"));

        progressBar.setVisibility(View.INVISIBLE);
        photoUploadPB.setVisibility(View.INVISIBLE);

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
        selectPhotoBT.setOnClickListener(new View.OnClickListener() {
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
                            Log.i(TAG, "onComplete: "+mAuth.getUid());
                            if (photoSelected && imageUploading) {
                                Toast.makeText(SignupActivity.this, "Wait, uploading photo!", Toast.LENGTH_SHORT).show();
                            } else {
                                user = new User(mAuth.getUid(), name, email, imageUrl);
                                signUpOnQuickblox(email,password);
                            }
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



/*    private BroadcastReceiver onNotice = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String pack = intent.getStringExtra("package");
            String title = intent.getStringExtra("title");
            String text = intent.getStringExtra("text");

*//*            Log.i(TAG, "onReceive: " + pack);
            Log.i(TAG, "onReceive: " + title);
            Log.i(TAG, "onReceive: " + text);*//*
            String notf = "package : "+pack+
                    "\ntitle : " + title+
                    "\ntext : " + text;

            Log.i(TAG, "onReceive: " + notf);
            saveNotifications.writeNotifs(notf);

            //int id = intent.getIntExtra("icon",0);

       *//*     Log.i(TAG, "onReceive: " + title);
            Log.i(TAG, "onReceive: " + text);*//*
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
    };*/

}