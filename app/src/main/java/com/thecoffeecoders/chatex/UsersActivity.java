package com.thecoffeecoders.chatex;

import android.content.Context;
import android.content.Intent;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView mUsersList;

    //Database Referecnce declaration
    private DatabaseReference mUsersDatabase;
    private String mCurrentUserID;

        private EditText mSearchEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        //Database Reference Instantiation. We will point the database pointer to the "Users" field
        //so that we can retrieve all the result of that field to the RecyclerView


        mCurrentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        mToolbar = (Toolbar)findViewById(R.id.users_appBar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        //
        mUsersDatabase.child(mCurrentUserID).child("online").setValue("true");
        //


        mUsersList = (RecyclerView)findViewById(R.id.users_list);
        mUsersList.setHasFixedSize(true);
        mUsersList.setLayoutManager(new LinearLayoutManager(this));



        mSearchEditText = (EditText) findViewById(R.id.search_field_text);

        searchAndSetData(mUsersDatabase.orderByChild("name"));
        mSearchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String searchText = mSearchEditText.getText().toString();
                Query search = mUsersDatabase.orderByChild("name").startAt(searchText).endAt(searchText+"\uf8ff");
                searchAndSetData(search);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }



   /* protected void onStart() {
        super.onStart();

        searchQuery = mUsersDatabase.orderByChild("name");
        searchAndSetData(searchQuery);
    }*/

    public void searchAndSetData(Query search){

        FirebaseRecyclerAdapter<Users, UsersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, UsersViewHolder>(
                Users.class,
                R.layout.users_single_layout,
                UsersViewHolder.class,
                //mUsersDatabase
                search

        ) {
            @Override
            protected void populateViewHolder(UsersViewHolder viewHolder, Users model, int position) {
                viewHolder.setName(model.getName());
                viewHolder.setStatus(model.getStatus());
                viewHolder.setThumbImage(model.getThumb_image(), getApplicationContext());

                final String user_id = getRef(position).getKey();

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent profileIntent = new Intent(UsersActivity.this, ProfileActivity.class);
                        profileIntent.putExtra("user_id", user_id);
                        profileIntent.putExtra("user_state", "not_friends");
                        startActivity(profileIntent);
                    }
                });
            }
        };

        mUsersList.setAdapter(firebaseRecyclerAdapter);
    }

    @Override
    protected void onStop() {
        super.onStop();

        //
        mUsersDatabase.child(mCurrentUserID).child("online").setValue(ServerValue.TIMESTAMP);
        //
    }

    public static class UsersViewHolder extends RecyclerView.ViewHolder{
        View mView;

        public UsersViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setName(String name){
            TextView userNameView = (TextView)mView.findViewById(R.id.user_single_name);
            userNameView.setText(name);
        }

        public void setStatus(String status){
            TextView statusView = (TextView)mView.findViewById(R.id.user_single_status);
            statusView.setText(status);
        }

        public void setThumbImage(String thumbImage, Context appContext){
            CircleImageView userImageView = (CircleImageView)mView.findViewById(R.id.user_single_image);
            Picasso.with(appContext).load(thumbImage).placeholder(R.drawable.default_avatar).into(userImageView);
        }


    }
}
