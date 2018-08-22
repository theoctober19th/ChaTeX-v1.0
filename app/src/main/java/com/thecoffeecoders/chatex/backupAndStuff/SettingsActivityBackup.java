package com.thecoffeecoders.chatex.backupAndStuff;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.thecoffeecoders.chatex.R;
import com.thecoffeecoders.chatex.StatusActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingsActivityBackup extends AppCompatActivity {

    //Firebase Connection
    private DatabaseReference mUserDatabase;
    private FirebaseUser mCurrentUser;

    //Android layout contents
    private CircleImageView mDisplayImage;
    private TextView mName;
    private TextView mStatus;
    private Button mStatusBtn;
    private Button mImageBtn;

    private static final int GALLERY_PICK = 1;


    //Firebase Storage Reference for the profile pictures storage.
    private StorageReference mImageStorage;

    //Progress Dialog
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //instantiate the layout elements
        mDisplayImage = (CircleImageView)findViewById(R.id.settings_image);
        mName = (TextView) findViewById(R.id.settings_display_name);
        mStatus = (TextView) findViewById(R.id.settings_status);
        mStatusBtn = (Button) findViewById(R.id.settings_status_btn);
        mImageBtn = (Button) findViewById(R.id.settings_image_btn);

        //Firebase storage for profile pictures storage
        mImageStorage = FirebaseStorage.getInstance().getReference();

        //Firebase (Go to users table for current user ID)
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String current_uid = mCurrentUser.getUid();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);

        //for retrieving the account setting values for the current user
        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //make string variable for all the details.
                String name = dataSnapshot.child("name").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

                //now set the values of elements to these values
                mName.setText(name);  //update the name
                mStatus.setText(status); //update the status
                if(!image.equals("default")){
                    Picasso.with(SettingsActivityBackup.this).load(image).placeholder(R.drawable.default_avatar).into(mDisplayImage); //update the image
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mStatusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String status_value = mStatus.getText().toString();
                Intent statusIntent = new Intent(SettingsActivityBackup.this, StatusActivity.class);
                statusIntent.putExtra("status_value", status_value);
                startActivity(statusIntent);
            }
        });

        mImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //This intent is for taking image from gallery
                /*Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galleryIntent, "Select Image"), GALLERY_PICK);
                */


               //This will add camera as well as gallery feature... instead of Akshye's meethod i used this
               CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON).setAspectRatio(1,1)
                        .setMinCropWindowSize(500,500)
                        .start(SettingsActivityBackup.this);

            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                //Start Progress Dialog
                mProgressDialog = new ProgressDialog(SettingsActivityBackup.this);
                mProgressDialog.setTitle("Uploading Image");
                mProgressDialog.setMessage("Please wait...");
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.show();
                Uri resultUri = result.getUri();
                File thumb_filePath = new File(resultUri.getPath());

                //Storage code for storing the image in firebase storage
                    Bitmap thumb_bitmap = new Compressor(this)
                            .setMaxHeight(200)
                            .setMaxWidth(200)
                            .setQuality(75)
                            .compressToBitmap(thumb_filePath);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    final byte[] thumb_byte = baos.toByteArray();

                String currentUserId = mCurrentUser.getUid();
                StorageReference filepath = mImageStorage.child("profile_images").child(currentUserId+".jpg");
                final StorageReference thumb_filepath = mImageStorage.child("profile_images").child("thumbs").child(currentUserId+".jpg");
                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            //If image Uploaded Successfully, now proceed to update the thumnail in the database
                            @SuppressWarnings("VisibleForTests")
                            final String download_url = task.getResult().getDownloadUrl().toString();
                            UploadTask uploadTask = thumb_filepath.putBytes(thumb_byte);
                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {

                                    if(thumb_task.isSuccessful()){
                                        @SuppressWarnings("VisibleForTests")
                                        String thumb_downloadUrl = thumb_task.getResult().getDownloadUrl().toString();
                                        Map update_hashMap = new HashMap<String, String>();
                                        update_hashMap.put("image", download_url);
                                        update_hashMap.put("thumb_image", thumb_downloadUrl);

                                        //now add this url to the database "image" field
                                        mUserDatabase.updateChildren(update_hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    //if the image added to database successfully
                                                    mProgressDialog.dismiss();
                                                    Toast.makeText(SettingsActivityBackup.this, "Successful!", Toast.LENGTH_LONG).show();
                                                }else{
                                                    //the image was uploaded but it can't be addded to the database
                                                    Toast.makeText(SettingsActivityBackup.this, "Image couldn't be added to the database.", Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        });
                                    }else{

                                        mProgressDialog.dismiss();
                                        Toast.makeText(SettingsActivityBackup.this, "There was some error updating the thumbnails", Toast.LENGTH_LONG).show();

                                    }
                                }
                            });

                        }else{
                            //If there is error in uploading the image
                            mProgressDialog.dismiss();
                            Toast.makeText(SettingsActivityBackup.this, "Error in uploading", Toast.LENGTH_LONG).show();
                        }
                    }
                });


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    public static String random() {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(10);
        char tempChar;
        for (int i = 0; i < randomLength; i++){
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }
}
