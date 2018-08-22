package com.thecoffeecoders.chatex;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.w3c.dom.Text;

public class EmailVerificationActivity extends AppCompatActivity {

    FirebaseUser mCurrentUser;
    FirebaseAuth mAuth;
    Button loginButton;
    Button resendVerificationButton;
    TextView verificationMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_verification);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        loginButton = (Button) findViewById(R.id.verification_button);
        verificationMessage = (TextView) findViewById(R.id.verification_message);
        resendVerificationButton = (Button) findViewById(R.id.resend_verfication_btn);

        resendVerificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentUser.sendEmailVerification();
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();

                Intent loginIntent = new Intent(EmailVerificationActivity.this, LoginActivity.class);
                loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(loginIntent);
                finish();
            }
        });

    }
}
