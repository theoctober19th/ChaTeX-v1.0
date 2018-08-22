package com.thecoffeecoders.chatex;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

public class DevelopersActivity extends AppCompatActivity {

    private Toolbar mToolBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_developers);


        mToolBar = (Toolbar) findViewById(R.id.developer_app_bar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setTitle("App Developers");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
