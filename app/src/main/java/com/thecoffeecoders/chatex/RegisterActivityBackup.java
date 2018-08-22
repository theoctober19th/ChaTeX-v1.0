package com.thecoffeecoders.chatex;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

public class RegisterActivityBackup extends AppCompatActivity {

    private TextInputLayout mDisplayName;
    private TextInputLayout mEmail;
    private TextInputLayout mPassword;
    private TextInputLayout mConfirmPassword;
    private Button mCreateBtn;
    private TextView loginRedirect;

    private Toolbar mToolbar;

    //ProgressDialog
    private ProgressDialog mRegProgress;

    //Firebase authentication
    private FirebaseAuth mAuth;

    //Firebase Database
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //Toolbar set
        mToolbar = (Toolbar) findViewById(R.id.register_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Register");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Progress Dialog
        mRegProgress = new ProgressDialog(this);



        //Firebase Authentication
        mAuth = FirebaseAuth.getInstance();

        mDisplayName = (TextInputLayout)findViewById(R.id.reg_display_name);
        mEmail = (TextInputLayout) findViewById(R.id.login_email);
        mPassword = (TextInputLayout) findViewById(R.id.login_password);
        mConfirmPassword = (TextInputLayout) findViewById(R.id.login_confirm_password);
        mCreateBtn = (Button) findViewById(R.id.reg_create_btn);
        loginRedirect = (TextView) findViewById(R.id.login_redirect_textview);

        mCreateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String display_name = mDisplayName.getEditText().getText().toString();
                String email = mEmail.getEditText().getText().toString().trim();
                String password = mPassword.getEditText().getText().toString();
                String confirmPassword = mConfirmPassword.getEditText().getText().toString();

                if(password.equals(confirmPassword)){
                    if(!TextUtils.isEmpty(display_name) && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)){
                        mRegProgress.setTitle("Registering User");
                        mRegProgress.setMessage("Please wait..");
                        mRegProgress.setCanceledOnTouchOutside(false);
                        mRegProgress.show();
                        register_user(display_name, email, password);
                    }
                } else{
                    Toast.makeText(RegisterActivityBackup.this, "Password and Confirm Password fields do not match",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        loginRedirect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginIntent = new Intent(RegisterActivityBackup.this, LoginActivity.class);
                startActivity(loginIntent);
                finish();
            }
        });
    }

    private void register_user(final String display_name, String email, String password) {

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {

                            //For getting UserID of the current user
                            FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();

                            current_user.sendEmailVerification()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Log.d("RegisterActivity", "Verification Email Sent");


                                                Intent verificationIntent = new Intent(RegisterActivityBackup.this, EmailVerificationActivity.class);
                                                verificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(verificationIntent);
                                                finish();


                                            }
                                        }
                                    });




                            String uid = current_user.getUid();

                            //get device token for current session
                            String deviceToken = FirebaseInstanceId.getInstance().getToken();

                            //writing user information to database
                            mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
                            HashMap<String, String> userMap = new HashMap<String, String>();
                            userMap.put("device_token",deviceToken); //store the device token too
                            userMap.put("name", display_name);
                            userMap.put("status", "ChaTeX is awesome!"); //default status to store to database
                            userMap.put("image", "default");
                            userMap.put("thumb_image", "default");
                            mDatabase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){ //do this only after writing to database
                                        mRegProgress.dismiss();
                                        Toast.makeText(RegisterActivityBackup.this, "Registered and logged in successfully.",
                                                Toast.LENGTH_SHORT).show();
                                        Intent mainIntent = new Intent(RegisterActivityBackup.this, MainActivity.class);
                                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(mainIntent);
                                        finish();
                                    }
                                }
                            });
                        }

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            mRegProgress.hide();
                            Toast.makeText(RegisterActivityBackup.this, "Error on registering the user. Please check all details are correct.",
                                    Toast.LENGTH_SHORT).show();
                            Intent startIntent = new Intent(RegisterActivityBackup.this, MainActivity.class);
                            startActivity(startIntent);
                            finish();
                        }
                    }
                });
    }
}
