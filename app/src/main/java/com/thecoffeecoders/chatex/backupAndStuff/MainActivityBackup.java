/*package com.thecoffeecoders.chatex.backupAndStuff;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.thecoffeecoders.chatex.R;
import com.thecoffeecoders.chatex.SectionsPagerAdapter;
import com.thecoffeecoders.chatex.SettingsActivity;
import com.thecoffeecoders.chatex.StartActivity;
import com.thecoffeecoders.chatex.UsersActivity;

public class MainActivityBackup extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private String TAG="ChaTeX";
    private Toolbar mToolBar;

    //For three tabs in the main page
    private ViewPager mViewPager;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private TabLayout mTablayout;

    //points to the "User" table later
    private DatabaseReference mUserRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();

        mToolBar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setTitle("ChaTeX");

        //Tabs in the main page
        mViewPager = (ViewPager) findViewById(R.id.main_tabPager);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mTablayout = (TabLayout) findViewById(R.id.main_tabs);
        mTablayout.setupWithViewPager(mViewPager);

        if (mAuth.getCurrentUser() != null) {
            mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
        }

        //The new firebase version made me to do this.. dont know this will be fine.
        //************************************************************************
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    mUserRef.child("online").setValue("true");
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Intent startIntent = new Intent(MainActivityBackup.this, StartActivity.class);
                    startActivity(startIntent);
                    finish();
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };

        //---------------------------------------------------------------------------
    }

    @Override
    public void onStart(){
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.main_menu, menu);



        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()){
            case R.id.main_logout_button: FirebaseAuth.getInstance().signOut(); break;
            case R.id.main_settings_btn:
                Intent settingsIntent = new Intent(MainActivityBackup.this, SettingsActivity.class);
                startActivity(settingsIntent);
                break;
            case R.id.main_all_btn:
                Intent allUsersIntent = new Intent(MainActivityBackup.this, UsersActivity.class);
                startActivity(allUsersIntent);
                break;
        }
        return true;
    }

    @Override
    public void onStop() {
        super.onStop();

        mUserRef.child("online").setValue(ServerValue.TIMESTAMP);
        //mUserRef.child("lastSeen").setValue(ServerValue.TIMESTAMP);

        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}
*/