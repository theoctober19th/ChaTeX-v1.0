package com.thecoffeecoders.chatex;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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
//import io.github.kexanie.library.MathView;

/**
 * Created by AkshayeJH on 24/07/17.
 */

public class MessageAdapterBackup extends RecyclerView.Adapter<MessageAdapterBackup.MessageViewHolder>{


    private List<Messages> mMessageList;
    private DatabaseReference mUserDatabase;

    public MessageAdapterBackup(List<Messages> mMessageList) {
        this.mMessageList = mMessageList;
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_single_layout_friend,parent, false);

        return new MessageViewHolder(v);

    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        public TextView messageText;
        public CircleImageView profileImage;
        public TextView displayName;
        public ImageView messageImage;
        public TextView messageTimeText;

        public MathView messageLatex;

        public MessageViewHolder(View view) {
            super(view);

            messageText = (TextView) view.findViewById(R.id.message_friend);
            profileImage = (CircleImageView) view.findViewById(R.id.message_profile_friend);
            displayName = (TextView) view.findViewById(R.id.display_name_friend);
            messageImage = (ImageView) view.findViewById(R.id.message_image_friend);

            messageLatex = (MathView) view.findViewById(R.id.message_latex_friend);
            messageTimeText = (TextView) view.findViewById(R.id.time_text_friend);
        }
    }

    @Override
    public void onBindViewHolder(final MessageViewHolder viewHolder, int i) {
        Messages c = mMessageList.get(i);

        String from_user = c.getFrom();
        String message_type = c.getType();
        long messageTime  = c.getTime();
        String date = getMessageDateToDisplay(messageTime);

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(from_user);
        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                String image = dataSnapshot.child("thumb_image").getValue().toString();

                viewHolder.displayName.setText(name);
                Picasso.with(viewHolder.profileImage.getContext()).load(image)
                        .placeholder(R.drawable.default_avatar).into(viewHolder.profileImage);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if(message_type.equals("text")) {
            String text = c.getMessage();
            try {
                MAHEncryptor mahEncryptor = MAHEncryptor.newInstance("IzeKVH8DlBfBe2uHSf6fYJbVuFSmS31n");
                String decrypted = mahEncryptor.decode(text);
                text = decrypted;
            } catch (Exception e) {
                e.printStackTrace();
            }
            if(hasLatex(text)){
                viewHolder.messageLatex.setVisibility(View.VISIBLE);
                viewHolder.messageLatex.setText(text);
                viewHolder.messageImage.setVisibility(View.GONE);
                viewHolder.messageText.setVisibility(View.GONE);
            } else {
                viewHolder.messageText.setText(text);
                viewHolder.messageText.setVisibility(View.VISIBLE);
                viewHolder.messageImage.setVisibility(View.GONE);
                viewHolder.messageLatex.setVisibility(View.GONE);
            }
            viewHolder.messageTimeText.setText(date);
        } else {
            viewHolder.messageText.setVisibility(View.INVISIBLE);
            Picasso.with(viewHolder.profileImage.getContext()).load(c.getMessage())
                    .placeholder(R.drawable.loading_icon).into(viewHolder.messageImage);
            viewHolder.messageTimeText.setText(String.valueOf(messageTime));
        }
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
