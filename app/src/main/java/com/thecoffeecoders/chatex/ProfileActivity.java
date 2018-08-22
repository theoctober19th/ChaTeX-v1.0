package com.thecoffeecoders.chatex;


import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private ImageView mProfileImage;
    private TextView mProfileName, mProfileStatus;
    private TextView profileFirstButtonText;
    private TextView profileSecondButtonText;
    //private TextView profileMessage;
    private Button mProfileSendReqBtn, mDeclineBtn;

    private DatabaseReference mUsersDatabase;

    private ProgressDialog mProgressDialog;

    private DatabaseReference mFriendReqDatabase;
    private DatabaseReference mFriendDatabase;
    private DatabaseReference mNotificationDatabase;

    private DatabaseReference mRootRef;

    private FirebaseUser mCurrent_user;

    private String mCurrent_state;
    private String thisUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final String current_state = getIntent().getStringExtra("user_state");

        final String user_id = getIntent().getStringExtra("user_id");
        thisUserID = user_id;

        mRootRef = FirebaseDatabase.getInstance().getReference();

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mFriendReqDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        mNotificationDatabase = FirebaseDatabase.getInstance().getReference().child("notifications");
        mCurrent_user = FirebaseAuth.getInstance().getCurrentUser();

        mProfileImage = (ImageView) findViewById(R.id.profile_image);
        mProfileName = (TextView) findViewById(R.id.profile_displayName);
        mProfileStatus = (TextView) findViewById(R.id.profile_status);
        //mProfileFriendsCount = (TextView) findViewById(R.id.profile_totalFriends);
        mProfileSendReqBtn = (Button) findViewById(R.id.profile_send_req_btn);
        mDeclineBtn = (Button) findViewById(R.id.profile_decline_btn);
        profileFirstButtonText = (TextView) findViewById(R.id.profile_first_button_text);
        profileSecondButtonText = (TextView) findViewById(R.id.profile_second_button_text);
        //profileMessage = (TextView) findViewById(R.id.profile_message);



        //mCurrent_state = "not_friends";
        mCurrent_state = current_state;

        mDeclineBtn.setVisibility(View.INVISIBLE);
        profileSecondButtonText.setVisibility(View.INVISIBLE);
        mDeclineBtn.setEnabled(false);


        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Loading User Data");
        mProgressDialog.setMessage("Please wait while we load the user data.");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();

        mUsersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                final String display_name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();

                mProfileName.setText(display_name);
                mProfileStatus.setText(status);

                Picasso.with(ProfileActivity.this).load(image).placeholder(R.drawable.default_avatar).into(mProfileImage);

                if(mCurrent_user.getUid().equals(user_id)){


                    mDeclineBtn.setEnabled(false);
                    //profileMessage.setVisibility(View.INVISIBLE);
                    profileSecondButtonText.setVisibility(View.INVISIBLE);
                    mDeclineBtn.setVisibility(View.INVISIBLE);

                    mProfileSendReqBtn.setVisibility(View.VISIBLE);
                    mProfileSendReqBtn.setEnabled(true);
                    profileFirstButtonText.setVisibility(View.VISIBLE);

                    profileFirstButtonText.setText("Go to my Profile");
                    mProfileSendReqBtn.setBackgroundResource(R.drawable.my_profile_icon);
                    mCurrent_state = "own_profile";
                }


                //--------------- FRIENDS LIST / REQUEST FEATURE -----

                mFriendReqDatabase.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if(dataSnapshot.hasChild(user_id)){

                            String req_type = dataSnapshot.child(user_id).child("request_type").getValue().toString();

                            if(req_type.equals("received") || current_state.equals("req_received")){

                                mCurrent_state = "req_received";

                                //profileMessage.setText(display_name + " has sent you a friend request.");
                                mProfileSendReqBtn.setBackgroundResource(R.drawable.tick_icon);
                                profileFirstButtonText.setText("Accept Friend Request");

                                //profileMessage.setVisibility(View.VISIBLE);
                                mProfileSendReqBtn.setVisibility(View.VISIBLE);
                                mProfileSendReqBtn.setEnabled(true);
                                profileFirstButtonText.setVisibility(View.VISIBLE);

                                mDeclineBtn.setVisibility(View.VISIBLE);
                                profileSecondButtonText.setVisibility(View.VISIBLE);
                                mDeclineBtn.setEnabled(true);


                            } else if(req_type.equals("sent")) {

                                mCurrent_state = "req_sent";


                                //profileMessage.setText("You have sent request to " + display_name);
                                mProfileSendReqBtn.setBackgroundResource(R.drawable.cross_icon);
                                profileFirstButtonText.setText("Cancel Friend Request");

                                //profileMessage.setVisibility(View.VISIBLE);
                                mProfileSendReqBtn.setVisibility(View.VISIBLE);
                                mProfileSendReqBtn.setEnabled(true);
                                profileFirstButtonText.setVisibility(View.VISIBLE);

                                mDeclineBtn.setVisibility(View.INVISIBLE);
                                profileSecondButtonText.setVisibility(View.INVISIBLE);
                                mDeclineBtn.setEnabled(false);

                            }

                            mProgressDialog.dismiss();


                        } else {


                            mFriendDatabase.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    if(dataSnapshot.hasChild(user_id)){

                                        mCurrent_state = "friends";


                                        //profileMessage.setText("You are friends with " + display_name);
                                        mProfileSendReqBtn.setBackgroundResource(R.drawable.cross_icon);
                                        profileFirstButtonText.setText("Unfriend");

                                        //profileMessage.setVisibility(View.VISIBLE);
                                        mProfileSendReqBtn.setVisibility(View.VISIBLE);
                                        mProfileSendReqBtn.setEnabled(true);
                                        profileFirstButtonText.setVisibility(View.VISIBLE);

                                        mDeclineBtn.setVisibility(View.INVISIBLE);
                                        profileSecondButtonText.setVisibility(View.INVISIBLE);
                                        mDeclineBtn.setEnabled(false);

                                    }

                                    mProgressDialog.dismiss();

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                    mProgressDialog.dismiss();

                                }
                            });

                        }



                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        mProfileSendReqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mProfileSendReqBtn.setEnabled(false);

                // --------------- NOT FRIENDS STATE ------------

                if(mCurrent_state.equals("not_friends")){


                    DatabaseReference newNotificationref = mRootRef.child("notifications").child(user_id).push();
                    String newNotificationId = newNotificationref.getKey();

                    HashMap<String, String> notificationData = new HashMap<>();
                    notificationData.put("from", mCurrent_user.getUid());
                    notificationData.put("type", "request");

                    Map requestMap = new HashMap();
                    requestMap.put("Friend_req/" + mCurrent_user.getUid() + "/" + user_id + "/request_type", "sent");
                    requestMap.put("Friend_req/" + user_id + "/" + mCurrent_user.getUid() + "/request_type", "received");
                    requestMap.put("notifications/" + user_id + "/" + newNotificationId, notificationData);

                    mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if(databaseError != null){

                                Toast.makeText(ProfileActivity.this, "There was some error in sending request", Toast.LENGTH_SHORT).show();

                            } else {
                                mCurrent_state = "req_sent";
                                //profileMessage.setText("You have sent request to " + mProfileName.getText());
                                mProfileSendReqBtn.setBackgroundResource(R.drawable.cross_icon);
                                profileFirstButtonText.setText("Cancel Friend Request");

                                //profileMessage.setVisibility(View.VISIBLE);
                                mProfileSendReqBtn.setVisibility(View.VISIBLE);
                                mProfileSendReqBtn.setEnabled(true);
                                profileFirstButtonText.setVisibility(View.VISIBLE);
                            }

                            mProfileSendReqBtn.setEnabled(true);


                        }
                    });

                }


                // - -------------- CANCEL REQUEST STATE ------------

                if(mCurrent_state.equals("req_sent")){

                    mFriendReqDatabase.child(mCurrent_user.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            mFriendReqDatabase.child(user_id).child(mCurrent_user.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {


                                    mProfileSendReqBtn.setEnabled(true);
                                    mCurrent_state = "not_friends";
                                    mProfileSendReqBtn.setBackgroundResource(R.drawable.add_friend_icon);
                                    //profileMessage.setText("");
                                    profileFirstButtonText.setText("Send Friend Request");

                                    //profileMessage.setVisibility(View.VISIBLE);
                                    mProfileSendReqBtn.setVisibility(View.VISIBLE);
                                    mProfileSendReqBtn.setEnabled(true);
                                    profileFirstButtonText.setVisibility(View.VISIBLE);

                                    mDeclineBtn.setVisibility(View.INVISIBLE);
                                    mDeclineBtn.setEnabled(false);
                                    profileSecondButtonText.setVisibility(View.INVISIBLE);


                                }
                            });

                        }
                    });

                }


                // - -------------- MY OWN PROFILE CASE ------------

                if(mCurrent_state.equals("own_profile")){

                    Intent settingsIntent = new Intent(ProfileActivity.this, SettingsActivity.class);
                    startActivity(settingsIntent);

                    mDeclineBtn.setVisibility(View.INVISIBLE);
                    mDeclineBtn.setEnabled(false);
                    profileSecondButtonText.setVisibility(View.INVISIBLE);
                }


                // ------------ REQ RECEIVED STATE ----------

                if(mCurrent_state.equals("req_received")){

                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());

                    Map friendsMap = new HashMap();
                    friendsMap.put("Friends/" + mCurrent_user.getUid() + "/" + user_id + "/date", currentDate);
                    friendsMap.put("Friends/" + user_id + "/"  + mCurrent_user.getUid() + "/date", currentDate);


                    friendsMap.put("Friend_req/" + mCurrent_user.getUid() + "/" + user_id, null);
                    friendsMap.put("Friend_req/" + user_id + "/" + mCurrent_user.getUid(), null);


                    mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {


                            if(databaseError == null){

                                mProfileSendReqBtn.setEnabled(true);
                                mCurrent_state = "friends";
                                mProfileSendReqBtn.setBackgroundResource(R.drawable.cross_icon);
                                //profileMessage.setText("You are frineds with " + mProfileName.getText());
                                profileFirstButtonText.setText("Unfriend");

                               // profileMessage.setVisibility(View.VISIBLE);
                                mProfileSendReqBtn.setVisibility(View.VISIBLE);
                                mProfileSendReqBtn.setEnabled(true);
                                profileFirstButtonText.setVisibility(View.VISIBLE);

                                mDeclineBtn.setVisibility(View.INVISIBLE);
                                profileSecondButtonText.setVisibility(View.INVISIBLE);
                                mDeclineBtn.setEnabled(false);

                            } else {

                                String error = databaseError.getMessage();

                                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();


                            }

                        }
                    });

                }


                // ------------ UNFRIENDS ---------

                if(mCurrent_state.equals("friends")){

                    Map unfriendMap = new HashMap();
                    unfriendMap.put("Friends/" + mCurrent_user.getUid() + "/" + user_id, null);
                    unfriendMap.put("Friends/" + user_id + "/" + mCurrent_user.getUid(), null);

                    mRootRef.updateChildren(unfriendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {


                            if(databaseError == null){

                                mCurrent_state = "not_friends";
                                mProfileSendReqBtn.setBackgroundResource(R.drawable.add_friend_icon);
                                //profileMessage.setText("Send friend request to chat with " + mProfileName.getText());
                                //profileMessage.setText("");
                                profileFirstButtonText.setText("Send Friend Request");

                                //profileMessage.setVisibility(View.VISIBLE);
                                mProfileSendReqBtn.setVisibility(View.VISIBLE);
                                mProfileSendReqBtn.setEnabled(true);
                                profileFirstButtonText.setVisibility(View.VISIBLE);

                                mDeclineBtn.setVisibility(View.INVISIBLE);
                                profileSecondButtonText.setVisibility(View.INVISIBLE);
                                mDeclineBtn.setEnabled(false);

                            } else {

                                String error = databaseError.getMessage();

                                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();


                            }

                            mProfileSendReqBtn.setEnabled(true);

                        }
                    });

                }


            }
        });

        mDeclineBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mCurrent_state == "req_received"){

                    Map declineRequestMap = new HashMap();
                    declineRequestMap.put("Friend_req/" + mCurrent_user.getUid() + "/" + user_id, null);
                    declineRequestMap.put("Friend_req/" + user_id + "/" + mCurrent_user.getUid(), null);

                    mRootRef.updateChildren(declineRequestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if(databaseError == null){

                                mCurrent_state = "not_friends";
                                mProfileSendReqBtn.setBackgroundResource(R.drawable.add_friend_icon);
                                //profileMessage.setText("Send friend request to chat with " + mProfileName.getText());
                                //profileMessage.setText("");
                                profileFirstButtonText.setText("Send Friend Request");

                                //profileMessage.setVisibility(View.VISIBLE);
                                mProfileSendReqBtn.setVisibility(View.VISIBLE);
                                mProfileSendReqBtn.setEnabled(true);
                                profileFirstButtonText.setVisibility(View.VISIBLE);

                                mDeclineBtn.setVisibility(View.INVISIBLE);
                                profileSecondButtonText.setVisibility(View.INVISIBLE);
                                mDeclineBtn.setEnabled(false);

                            } else {

                                String error = databaseError.getMessage();

                                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();

                            }

                            mProfileSendReqBtn.setEnabled(true);

                        }
                    });

                }
            }
        });

        //profileMessage.setVisibility(View.VISIBLE);
        mProfileSendReqBtn.setVisibility(View.VISIBLE);
        mProfileSendReqBtn.setEnabled(true);
        profileFirstButtonText.setVisibility(View.VISIBLE);

    }

    @Override
    protected void onStart() {
        super.onStart();

        if(mCurrent_user.getUid().equals(thisUserID)){

            mDeclineBtn.setEnabled(false);
            //profileMessage.setVisibility(View.INVISIBLE);
            profileSecondButtonText.setVisibility(View.INVISIBLE);
            mDeclineBtn.setVisibility(View.INVISIBLE);

            mProfileSendReqBtn.setVisibility(View.VISIBLE);
            mProfileSendReqBtn.setEnabled(true);
            profileFirstButtonText.setVisibility(View.VISIBLE);

            profileFirstButtonText.setText("Go to my Profile");
            mProfileSendReqBtn.setBackgroundResource(R.drawable.my_profile_icon);
            mCurrent_state = "own_profile";
        }
    }
}
