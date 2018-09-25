package com.example.classifieds.Account;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.classifieds.Home.SearchActivity;
import com.example.classifieds.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private TextView mSignup;
    private Context mContext;
    private EditText mEmail, mPassword;
    private ProgressBar mProgressBar;
    private Button mBtnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

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

    private void hideSoftKeyboard(){
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    private void initWidgets() {
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mEmail = (EditText) findViewById(R.id.email);
        mPassword = (EditText) findViewById(R.id.password);
        mContext = LoginActivity.this;
        mSignup = (TextView) findViewById(R.id.signup);
        mBtnLogin = (Button) findViewById(R.id.loginBtn);


        mSignup.setText(Html.fromHtml(getString(R.string.not_registered)));
        mProgressBar.setVisibility(View.GONE);
    }

    private void init() {

        mBtnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mEmail.getText().toString().trim();
                String password = mPassword.getText().toString();

                if(validateFields(email)) {
                    toastService(getString(R.string.email_empty));
                } else if (validateFields(password)){
                    toastService(getString(R.string.password_empty));
                } else {
                    showProgressBar();

                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information
                                        hideProgressBar();
                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Log.d(TAG, "signInWithEmail:failure");
                                        toastService(getString(R.string.authentication_failure));
                                        hideProgressBar();
                                    }
                                }
                            });
                }
            }
        });

        mSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, RegisterActivity.class);
                startActivity(intent);
                finish();
            }
        });

        hideSoftKeyboard();
    }

    private void setupFireBaseAuth() {
        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if(user != null) {
                    Log.d(TAG, "onAuthStateChanged: signed_in: " + user.getUid());
                    if(user.isEmailVerified()){
                        Log.d(TAG, "onAuthStateChanged: signed_in: " + user.getUid());
                        toastService("Authenticated with " + user.getEmail());
                        Intent intent = new Intent(LoginActivity.this, SearchActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }else{
                        toastService(getString(R.string.email_not_verified));
                        mAuth.signOut();
                    }
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
