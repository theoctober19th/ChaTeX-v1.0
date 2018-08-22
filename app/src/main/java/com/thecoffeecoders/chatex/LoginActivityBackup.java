package com.thecoffeecoders.chatex;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivityBackup extends AppCompatActivity {

    private Toolbar mToolbar;

    private TextInputLayout mLoginEmail;
    private TextInputLayout mLoginPassword;
    private Button mLogin_btn;
    private TextView registerRedirect;

    //Progress Dialog
    private ProgressDialog mLoginProgress;

    private FirebaseAuth mAuth;

    //creating database reference(for storing tokens)
    private DatabaseReference mUserDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        mToolbar = (Toolbar) findViewById(R.id.login_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Login");

        //Progress Dialog
        mLoginProgress = new ProgressDialog(this);

        //instantiate the datbasee reference
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        mLoginEmail = (TextInputLayout) findViewById(R.id.login_email);
        mLoginPassword = (TextInputLayout) findViewById(R.id.login_password);
        mLogin_btn = (Button) findViewById(R.id.login_btn);
        registerRedirect = (TextView) findViewById(R.id.register_redirect_textview);

        mLogin_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mLoginEmail.getEditText().getText().toString().trim();
                String password = mLoginPassword.getEditText().getText().toString();

                if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)){
                    mLoginProgress.setTitle("Logging in");
                    mLoginProgress.setMessage("Please wait...");
                    mLoginProgress.setCanceledOnTouchOutside(false);
                    mLoginProgress.show();
                    loginUser(email, password);
                }

            }
        });

        registerRedirect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerIntent = new Intent(LoginActivityBackup.this, RegisterActivity.class);
                startActivity(registerIntent);
                finish();
            }
        });
    }
    private void loginUser(String email, String password) {

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    //Successfully signed in
                    mLoginProgress.dismiss();

                    //getting TokenID for this user
                    String currentUserID = mAuth.getCurrentUser().getUid();
                    String deviceToken = FirebaseInstanceId.getInstance().getToken();
                    //storing token in "Users"
                    mUserDatabase.child(currentUserID).child("device_token").setValue(deviceToken).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            //after token written successfully,
                            //do rest of logging stuffs


                            Toast.makeText(LoginActivityBackup.this, "Successfully Logged In",
                                    Toast.LENGTH_SHORT).show();
                            Intent mainIntent = new Intent(LoginActivityBackup.this, MainActivity.class);
                            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(mainIntent);
                            finish();
                        }
                    });

                }else{
                    mLoginProgress.hide();
                    Toast.makeText(LoginActivityBackup.this, "Error while logging in. Please check the details",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    
}
