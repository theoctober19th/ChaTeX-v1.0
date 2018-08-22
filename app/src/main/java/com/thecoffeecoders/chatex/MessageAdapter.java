package com.thecoffeecoders.chatex;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.support.constraint.solver.SolverVariable;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mobapphome.mahencryptorlib.MAHEncryptor;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;
import io.github.kexanie.library.MathView;
import static com.thecoffeecoders.chatex.R.id.parent;
//import io.github.kexanie.library.MathView;

/**
 * Created by AkshayeJH on 24/07/17.
 */

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    public static final int VIEW_TYPE_FRIEND_MESSAGE = 1;
    public static final int VIEW_TYPE_USER_MESSAGE = 0;
    private List<Messages> mMessageList;
    private Context mContext;
    private DatabaseReference mUserDatabase;
    private final String currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
    Typeface nunito;
    Typeface helvetica;
    public MessageAdapter(Context context, List<Messages> mMessageList) {
        this.mMessageList = mMessageList;
        this.mContext = context;

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        //DatabaseReference  = FirebaseDatabase.getInstance().getReference();
        if (viewType == MessageAdapter.VIEW_TYPE_FRIEND_MESSAGE) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.message_single_layout_friend, parent, false);

            return new ItemMessageFriendHolder(v);
        }else if(viewType == MessageAdapter.VIEW_TYPE_USER_MESSAGE){
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.message_single_layout_user, parent, false);

            return new ItemMessageUserHolder(v);
        }
        return null;

    }

    public class ItemMessageUserHolder extends RecyclerView.ViewHolder {

        public TextView messageText;
        public CircleImageView profileImage;
        //public TextView displayName;
        public ImageView messageImage;
        public TextView messageTimeText;

        public MathView messageLatex;

        public ItemMessageUserHolder(View view) {
            super(view);

            messageText = (TextView) view.findViewById(R.id.message_user);
            profileImage = (CircleImageView) view.findViewById(R.id.message_profile_user);
            //displayName = (TextView) view.findViewById(R.id.display_name_user);
            messageImage = (ImageView) view.findViewById(R.id.message_image_user);

            messageLatex = (MathView) view.findViewById(R.id.message_latex_user);
            messageTimeText = (TextView) view.findViewById(R.id.time_text_user);
        }
    }

    public class ItemMessageFriendHolder extends RecyclerView.ViewHolder {

        public TextView messageText;
        public CircleImageView profileImage;
        //public TextView displayName;
        public ImageView messageImage;
        public TextView messageTimeText;

        public MathView messageLatex;

        public ItemMessageFriendHolder(View view) {
            super(view);

            messageText = (TextView) view.findViewById(R.id.message_friend);
            profileImage = (CircleImageView) view.findViewById(R.id.message_profile_friend);
            //displayName = (TextView) view.findViewById(R.id.display_name_friend);
            messageImage = (ImageView) view.findViewById(R.id.message_image_friend);

            messageLatex = (MathView) view.findViewById(R.id.message_latex_friend);
            messageTimeText = (TextView) view.findViewById(R.id.time_text_friend);
        }
    }




    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, int i) {
         boolean isImageFitToScreen = false;

        if(viewHolder instanceof ItemMessageFriendHolder) {
            final Messages c = mMessageList.get(i);

            String from_user = c.getFrom();
            String message_type = c.getType();
            long messageTime = c.getTime();
            String date = getMessageDateToDisplay(messageTime);

            mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(from_user);
            mUserDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    //String name = dataSnapshot.child("name").getValue().toString();
                    String image = dataSnapshot.child("thumb_image").getValue().toString();

                    //((ItemMessageFriendHolder)viewHolder).displayName.setText(name);
                    Picasso.with(((ItemMessageFriendHolder)viewHolder).profileImage.getContext()).load(image)
                            .placeholder(R.drawable.default_avatar).into(((ItemMessageFriendHolder)viewHolder).profileImage);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            if (message_type.equals("text")) {
                String text = c.getMessage();
                try {
                    MAHEncryptor mahEncryptor = MAHEncryptor.newInstance("IzeKVH8DlBfBe2uHSf6fYJbVuFSmS31n");
                    String decrypted = mahEncryptor.decode(text);
                    text = decrypted;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (hasLatex(text)) {
                    ((ItemMessageFriendHolder)viewHolder).messageLatex.setVisibility(View.VISIBLE);
                    ((ItemMessageFriendHolder)viewHolder).messageLatex.setText(text);
                    ((ItemMessageFriendHolder)viewHolder).messageImage.setVisibility(View.GONE);
                    ((ItemMessageFriendHolder)viewHolder).messageText.setVisibility(View.GONE);
                } else {
                    //setting typeface

                    Context context = ((ItemMessageFriendHolder)viewHolder).messageText.getContext();
                    helvetica = Typeface.createFromAsset(context.getAssets(), "fonts/HelveticaNeue.ttf");
                    ((ItemMessageFriendHolder)viewHolder).messageText.setTypeface(helvetica);


                    ((ItemMessageFriendHolder)viewHolder).messageText.setText(text);
                    ((ItemMessageFriendHolder)viewHolder).messageText.setVisibility(View.VISIBLE);
                    ((ItemMessageFriendHolder)viewHolder).messageImage.setVisibility(View.GONE);
                    ((ItemMessageFriendHolder)viewHolder).messageLatex.setVisibility(View.GONE);
                }
                ((ItemMessageFriendHolder)viewHolder).messageTimeText.setText(date);
            } else {
                ((ItemMessageFriendHolder)viewHolder).messageImage.setVisibility(View.VISIBLE);
                ((ItemMessageFriendHolder)viewHolder).messageImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent fullScreenIntent = new Intent(mContext, FullScreenActivity.class);
                        fullScreenIntent.putExtra("image", c.getMessage());
                        mContext.startActivity(fullScreenIntent);


                    }
                });
                ((ItemMessageFriendHolder)viewHolder).messageText.setVisibility(View.GONE);
                ((ItemMessageFriendHolder)viewHolder).messageLatex.setVisibility(View.GONE);

                Glide.with(((ItemMessageFriendHolder)viewHolder).profileImage.getContext())
                        .load(c.getMessage())
                        .override(200, 200)
                        .centerCrop() // scale to fill the ImageView and crop any extra
                        .into(((ItemMessageFriendHolder)viewHolder).messageImage);


              /*  Picasso.with(((ItemMessageFriendHolder)viewHolder).profileImage.getContext()).load(c.getMessage())
                        .placeholder(R.drawable.loading_icon).into(((ItemMessageFriendHolder)viewHolder).messageImage);
               */
                ((ItemMessageFriendHolder)viewHolder).messageTimeText.setText(date);
            }
        }else if(viewHolder instanceof ItemMessageUserHolder){
            final Messages c = mMessageList.get(i);

            String from_user = c.getFrom();
            String message_type = c.getType();
            long messageTime = c.getTime();
            String date = getMessageDateToDisplay(messageTime);

            mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(from_user);
            mUserDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String name = dataSnapshot.child("name").getValue().toString();
                    String image = dataSnapshot.child("thumb_image").getValue().toString();

                    //((ItemMessageUserHolder)viewHolder).displayName.setText(name);
                    Picasso.with(((ItemMessageUserHolder)viewHolder).profileImage.getContext()).load(image)
                            .placeholder(R.drawable.default_avatar).into(((ItemMessageUserHolder)viewHolder).profileImage);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            if (message_type.equals("text")) {
                String text = c.getMessage();
                try {
                    MAHEncryptor mahEncryptor = MAHEncryptor.newInstance("IzeKVH8DlBfBe2uHSf6fYJbVuFSmS31n");
                    String decrypted = mahEncryptor.decode(text);
                    text = decrypted;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (hasLatex(text)) {
                    ((ItemMessageUserHolder)viewHolder).messageLatex.setVisibility(View.VISIBLE);

                    /*((ItemMessageUserHolder)viewHolder).messageLatex.config("MathJax.Hub.Config({\n" +
                            "jax: [\"input/TeX\",\"output/HTML-CSS\"],\n" +
                            "displayAlign: \"right\"});"
                    );*/

                    ((ItemMessageUserHolder)viewHolder).messageLatex.setText(text);
                    ((ItemMessageUserHolder)viewHolder).messageImage.setVisibility(View.GONE);
                    ((ItemMessageUserHolder)viewHolder).messageText.setVisibility(View.GONE);
                } else {

                    Context context = ((ItemMessageUserHolder)viewHolder).messageText.getContext();
                    helvetica = Typeface.createFromAsset(context.getAssets(), "fonts/HelveticaNeue.ttf");
                    ((ItemMessageUserHolder)viewHolder).messageText.setTypeface(helvetica);


                    ((ItemMessageUserHolder)viewHolder).messageText.setText(text);
                    ((ItemMessageUserHolder)viewHolder).messageText.setVisibility(View.VISIBLE);
                    ((ItemMessageUserHolder)viewHolder).messageImage.setVisibility(View.GONE);
                    ((ItemMessageUserHolder)viewHolder).messageLatex.setVisibility(View.GONE);
                }
                ((ItemMessageUserHolder)viewHolder).messageTimeText.setText(date);
            } else {
                ((ItemMessageUserHolder)viewHolder).messageImage.setVisibility(View.VISIBLE);

                ((ItemMessageUserHolder)viewHolder).messageImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent fullScreenIntent = new Intent(mContext, FullScreenActivity.class);
                        fullScreenIntent.putExtra("image", c.getMessage());
                        mContext.startActivity(fullScreenIntent);


                    }
                });
                ((ItemMessageUserHolder)viewHolder).messageText.setVisibility(View.GONE);
                ((ItemMessageUserHolder)viewHolder).messageLatex.setVisibility(View.GONE);
                Glide.with(((ItemMessageUserHolder)viewHolder).profileImage.getContext())
                        .load(c.getMessage())
                        .override(200, 200)
                        .centerCrop() // scale to fill the Imag
                        // eView and crop any extra
                        .placeholder(R.drawable.loading_icon)
                        .into(((ItemMessageUserHolder)viewHolder).messageImage);
//                Picasso.with(((ItemMessageUserHolder)viewHolder).profileImage.getContext()).load(c.getMessage())
//                        .placeholder(R.drawable.loading_icon).into(((ItemMessageUserHolder)viewHolder).messageImage);
                ((ItemMessageUserHolder)viewHolder).messageTimeText.setText(date);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return mMessageList.get(position).getFrom().equals(currentUserID) ? MessageAdapter.VIEW_TYPE_USER_MESSAGE : MessageAdapter.VIEW_TYPE_FRIEND_MESSAGE;
    }

    private boolean hasLatex(String text) {
        if(Pattern.matches(".*\\(.*\\).*", text) || Pattern.matches(".*\\$\\$.*\\$\\$.*", text)){
            return true;
        }else{
            return false;
        }
    }

    private String getMessageDateToDisplay(long messageTime) {
        long currentTime = System.currentTimeMillis();
        String date;
        if(currentTime - messageTime >= 43200000){
            date = new java.text.SimpleDateFormat("MMM dd hh:mm a").format(new java.util.Date (messageTime));
        }else{
            date = new java.text.SimpleDateFormat("hh:mm a").format(new java.util.Date (messageTime));
        }
        return date;
    }
    @Override
    public int getItemCount() {
        return mMessageList.size();
    }
}