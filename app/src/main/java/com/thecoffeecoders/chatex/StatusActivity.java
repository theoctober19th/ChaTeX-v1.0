package com.thecoffeecoders.chatex;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private TextInputLayout mStatus;
    private Button mSavebtn;

    //For firebase
    private DatabaseReference mStatusDatabase;
    private FirebaseUser mCurrentUser;

    //Progress Dialog
    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        //Firebase instantiation
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String current_uid = mCurrentUser.getUid();
        mStatusDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);


        mToolbar = (Toolbar) findViewById(R.id.status_appBar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Edit your status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        //layout elements instantiation
        mSavebtn = (Button) findViewById(R.id.status_save_btn);
        mStatus = (TextInputLayout) findViewById(R.id.status_input);

        //get the previous value of the status here. we will put default value of the status edit box to this.
        String status_value = getIntent().getStringExtra("status_value");
        mStatus.getEditText().setText(status_value);

        mSavebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Progress Dialog
                mProgress = new ProgressDialog(StatusActivity.this);
                mProgress.setTitle("Saving Changes");
                mProgress.setMessage("Please wait...");
                mProgress.show();

                String status = mStatus.getEditText().getText().toString();
                mStatusDatabase.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            mProgress.dismiss();

                            /*
                            This part is not in the actual code, but I have khas khas... and want to write anyway :)
                           */
                            Intent settingsIntent = new Intent(StatusActivity.this, SettingsActivity.class);
                            startActivity(settingsIntent);
                            finish();
                            //UPTO HERE
                        } else{
                            Toast.makeText(StatusActivity.this, "Error on saving changes...", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }
}
