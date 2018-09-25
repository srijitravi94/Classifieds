package com.example.classifieds.Account;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.example.classifieds.Models.User;
import com.example.classifieds.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private TextView mSignin;
    private EditText mFirstName, mLastName, mEmail, mPassword;
    private Context mContext;
    private ProgressBar mProgressBar;
    private Button mRegisterBtn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        setupFireBaseAuth();
        initWidgets();
        init();
    }

    private void toastService(String message) {
        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
    }

    private boolean validateFields(String field) {
        return field.equals("");
    }

    private void showProgressBar(){
        mProgressBar.setVisibility(View.VISIBLE);

    }

    private void hideProgressBar(){
        if(mProgressBar.getVisibility() == View.VISIBLE){
            mProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void handleFirebaseAuthenticationError(Exception e) {
        if (e instanceof FirebaseAuthWeakPasswordException) {
            toastService(getString(R.string.weak_password));
        } else if (e instanceof FirebaseAuthUserCollisionException) {
            toastService(getString(R.string.email_exists));
        } else if(e instanceof FirebaseAuthInvalidCredentialsException) {
            toastService(getString(R.string.invalid_email));
        } else {
            toastService(getString(R.string.something_wrong));
        }
    }

    public void sendVerificationEmail() {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                toastService(getString(R.string.check_email));
                            }
                            else{
                                toastService(getString(R.string.something_wrong));
                            }
                        }
                    });
        }

    }

    private void redirectLoginScreen(){
        Log.d(TAG, "redirectLoginScreen: redirecting to login screen.");

        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void addNewUser(String firstName, String lastName, String email) {

        String userId = mAuth.getCurrentUser().getUid();

        User user = new User(userId, firstName, lastName, email);

        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();

        myRef.child(getString(R.string.node_users))
                .child(userId)
                .setValue(user);

        mAuth.signOut();
        redirectLoginScreen();
    }

    private void hideSoftKeyboard(){
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    private void initWidgets() {
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mFirstName = (EditText) findViewById(R.id.firstName);
        mLastName = (EditText) findViewById(R.id.lastName);
        mEmail = (EditText) findViewById(R.id.email);
        mPassword = (EditText) findViewById(R.id.password);
        mContext = RegisterActivity.this;
        mSignin = (TextView) findViewById(R.id.signin);
        mRegisterBtn = (Button) findViewById(R.id.registerBtn);


        mSignin.setText(Html.fromHtml(getString(R.string.already_registered)));
        mProgressBar.setVisibility(View.GONE);
    }

    private void init() {

        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String firstName = mFirstName.getText().toString().trim();
                String lastName = mLastName.getText().toString().trim();
                String email = mEmail.getText().toString().trim();
                String password = mPassword.getText().toString();

                if(validateFields(firstName)) {
                    toastService(getString(R.string.first_name_empty));
                } else if (validateFields(lastName)){
                    toastService(getString(R.string.last_name_empty));
                } else if(validateFields(email)) {
                    toastService(getString(R.string.email_empty));
                } else if (validateFields(password)){
                    toastService(getString(R.string.password_empty));
                } else {
                    registerNewUser(firstName, lastName, email, password);
                }
            }
        });


        mSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        hideSoftKeyboard();
    }

    private void registerNewUser(final String firstName, final String lastName, final String email, final String password) {

        showProgressBar();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            sendVerificationEmail();
                            addNewUser(firstName, lastName, email);
                            hideProgressBar();
                        } else {
                            // If sign in fails, display a message to the user.
                            Exception e = task.getException();
                            Log.d(TAG, "createUserWithEmail:failure");
                            handleFirebaseAuthenticationError(e);
                            hideProgressBar();
                        }
                    }
                });
    }

    private void setupFireBaseAuth() {
        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if(user != null) {
                    Log.d(TAG, "onAuthStateChanged: signed_in: " + user.getUid());
                } else {
                    Log.d(TAG, "onAuthStateChanged: signed_out");
                }
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}
