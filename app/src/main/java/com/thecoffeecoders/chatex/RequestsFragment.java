package com.thecoffeecoders.chatex;


import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestsFragment extends Fragment {

    private RecyclerView mRequestsList;

    private DatabaseReference mFriendReqDatabase;
    private DatabaseReference mUsersDatabase;

    private FirebaseAuth mAuth;

    private String mCurrent_user_id;

    private View mMainView;

    public RequestsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mMainView = inflater.inflate(R.layout.fragment_requests, container, false);

        mRequestsList = (RecyclerView) mMainView.findViewById(R.id.friend_req_list);
        mAuth = FirebaseAuth.getInstance();

        if(mAuth.getCurrentUser() != null) {
            mCurrent_user_id = mAuth.getCurrentUser().getUid();
            mFriendReqDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req").child(mCurrent_user_id);
            mFriendReqDatabase.keepSynced(true);
            mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
            mUsersDatabase.keepSynced(true);
        }

        mRequestsList.setHasFixedSize(true);
        mRequestsList.setLayoutManager(new LinearLayoutManager(getContext()));

        // Inflate the layout for this fragment
        return mMainView;
    }

    public void onStart() {
        super.onStart();

        if(mAuth.getCurrentUser() != null) {

            FirebaseRecyclerAdapter<Requests, RequestsFragment.RequestsViewHolder> friendsRecyclerViewAdapter = new FirebaseRecyclerAdapter<Requests, RequestsFragment.RequestsViewHolder>(

                    Requests.class,
                    R.layout.users_single_layout,
                    RequestsFragment.RequestsViewHolder.class,
                    mFriendReqDatabase
            ) {
                @Override
                protected void populateViewHolder(final RequestsFragment.RequestsViewHolder requestsViewHolder, Requests requests, int i) {

                    final String request_type = requests.getRequest_type();
                    final String user_state;
                    if(request_type.equals("sent")) {
                        user_state = "req_sent";
                        requestsViewHolder.setStatus("SENT");
                    }else{
                         user_state = "req_received";
                        requestsViewHolder.setStatus("RECEIVED");
                    }

                    final String list_user_id = getRef(i).getKey();

                    mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            final String userName = dataSnapshot.child("name").getValue().toString();
                            final String userThumb = dataSnapshot.child("thumb_image").getValue().toString();

                            requestsViewHolder.setName(userName);
                            requestsViewHolder.setUserImage(userThumb, getContext());
                            requestsViewHolder.setUserOffline();
                            requestsViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                    Intent profileIntent = new Intent(getContext(), ProfileActivity.class);
                                    profileIntent.putExtra("user_id", list_user_id);
                                    profileIntent.putExtra("user_state", user_state);
                                    startActivity(profileIntent);

                                }
                            });


                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }
            };

            mRequestsList.setAdapter(friendsRecyclerViewAdapter);
        }


    }

    public static class RequestsViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public RequestsViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

        }

        public void setStatus(String status){

            TextView userStatusView = (TextView) mView.findViewById(R.id.user_single_status);
            userStatusView.setText(status);
            if(status.equals("RECEIVED")){
                userStatusView.setTypeface(userStatusView.getTypeface(), Typeface.BOLD);
            }

        }

        public void setName(String name){

            TextView userNameView = (TextView) mView.findViewById(R.id.user_single_name);
            userNameView.setText(name);

        }

        public void setUserImage(String thumb_image, Context ctx){

            CircleImageView userImageView = (CircleImageView) mView.findViewById(R.id.user_single_image);
            Picasso.with(ctx).load(thumb_image).placeholder(R.drawable.default_avatar).into(userImageView);

        }

        public void setUserOffline() {
            ImageView userOnlineView = (ImageView) mView.findViewById(R.id.user_single_online_icon);
            userOnlineView.setVisibility(View.INVISIBLE);
        }


    }


}
