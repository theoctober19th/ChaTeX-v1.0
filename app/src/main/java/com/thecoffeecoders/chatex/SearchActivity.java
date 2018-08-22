package com.thecoffeecoders.chatex;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class SearchActivity extends AppCompatActivity {

    private EditText mSearchField;
    private ImageButton mSearchBtn;

    private RecyclerView mResultList;

    private DatabaseReference mUserDatabase;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        mUserDatabase = FirebaseDatabase.getInstance().getReference("Users");

        mSearchBtn = (ImageButton) findViewById(R.id.searchBtn);
        mSearchField = (EditText) findViewById(R.id.search_field_text);

        mResultList = (RecyclerView) findViewById(R.id.result_list);
        mResultList.setHasFixedSize(true);
        mResultList.setLayoutManager(new LinearLayoutManager(this));

        mSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchText = mSearchField.getText().toString();
                firebaseUserSearch(searchText);
            }
        });
    }

    private void firebaseUserSearch(String searchText) {

        //Query simpleQuery = mUserDatabase;//.orderByChild("name");
        Query firebaseSearchQuery = mUserDatabase.orderByChild("name").startAt(searchText).endAt(searchText+"\uf8ff");
        FirebaseRecyclerAdapter<Users, UsersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, UsersViewHolder>(
                Users.class,
                R.layout.users_single_layout,
                UsersViewHolder.class,
                firebaseSearchQuery
        ) {
            @Override
            protected void populateViewHolder(UsersViewHolder viewHolder, Users model, int position) {

                viewHolder.setDetails(getApplicationContext(), model.getName(), model.getStatus(), model.getImage());

                final String user_id = getRef(position).getKey();

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent profileIntent = new Intent(SearchActivity.this, ProfileActivity.class);
                        profileIntent.putExtra("user_id", user_id);
                        profileIntent.putExtra("user_state", "not_friends");
                        startActivity(profileIntent);
                    }
                });
            }
        };

        mResultList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class UsersViewHolder extends RecyclerView.ViewHolder{
        View mView;

        public UsersViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }

        public void setDetails(Context ctx, String username, String status, String thumbnail){
            TextView userName = (TextView) mView.findViewById(R.id.user_single_name);
            TextView userStatus = (TextView) mView.findViewById(R.id.user_single_status);
            CircleImageView thumbImage = (CircleImageView) mView.findViewById(R.id.user_single_image);

            userName.setText(username);
            userStatus.setText(status);
            Picasso.with(ctx).load(thumbnail).placeholder(R.drawable.default_avatar).into(thumbImage);
        }
    }
}
