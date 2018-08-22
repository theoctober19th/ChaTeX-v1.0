package com.thecoffeecoders.chatex;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity {

    //Firebase Connection parameters
    private DatabaseReference mUserDatabase;
    private FirebaseUser mCurrentUser;
    private StorageReference mImageStorage; //for storing thumbnail and actual image later on.

    //Android layout contents
    private CircleImageView mDisplayImage;
    private TextView mName;
    private TextView mStatus;
    private Button mStatusBtn;
    private Button mImageBtn;

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

        //Firebase storage for profile pictures and thumbnail storage
        mImageStorage = FirebaseStorage.getInstance().getReference();

        //make mUserDatabasee point to the entry under "Users" which corresponds to the id of current user
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String current_uid = mCurrentUser.getUid();

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);
        mUserDatabase.keepSynced(true); //for Firebase Offline Capabilities

        //this listener will listen to any changes that may happen in the values of the
        //user status, name or the image
        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //if any data is changed, we will get the changed data in string variables
                //make string variable for all the details.
                String name = dataSnapshot.child("name").getValue().toString();
                final String image = dataSnapshot.child("image").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

                //now set the values of elements to these updated values
                mName.setText(name);  //update the name
                mStatus.setText(status); //update the status
                if(!image.equals("default")){  //only if the image is not the default placeholder image

                    //Picasso.with(SettingsActivity.this).load(image).placeholder(R.drawable.default_avatar).into(mDisplayImage); //update the image
                    //load image with offline capability
                    Picasso.with(SettingsActivity.this).load(image).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.default_avatar).into(mDisplayImage, new Callback() {
                        @Override
                        public void onSuccess() {
                            //do nothing. thing is already done :P
                        }

                        @Override
                        public void onError() { //if not available in offline, then make only download from the internet
                            Picasso.with(SettingsActivity.this).load(image).placeholder(R.drawable.default_avatar).into(mDisplayImage);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //what to do when data is cancelled? may be error handling codes here :)
            }
        });

        //now set the action of "Change Status" Button. We will send the user to StatusActivity
        mStatusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String status_value = mStatus.getText().toString();
                Intent statusIntent = new Intent(SettingsActivity.this, StatusActivity.class);
                statusIntent.putExtra("status_value", status_value);
                startActivity(statusIntent);
            }
        });

        mImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                This was used by AKshye in his video tutorial. But this will only allow us to chose image from the gallery.
                The code I wrote below will allow user to chose image from any compatible app, as well as camera.
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galleryIntent, "Select Image"), GALLERY_PICK);
                */

               CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1,1) //we want only square images
                        .setMinCropWindowSize(500,500)//but not less than 500x500
                        .start(SettingsActivity.this);
            }
        });
    }
    //End of OnCreate() method------------



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);


            if (resultCode == RESULT_OK) { //if image is successfully cropped

                //Start Progress Dialog
                mProgressDialog = new ProgressDialog(SettingsActivity.this);
                mProgressDialog.setTitle("Uploading Image");
                mProgressDialog.setMessage("Please wait...");
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.show();

                //Create a file path of that URI that will now be used to generate thumbnail
                File croppedImageFilePath = new File(result.getUri().getPath());

                Bitmap main_image_bitmap = new Compressor(this)
                        .setMaxHeight(250)  //thumbnail height
                        .setMaxWidth(250)   //thumbnail width
                        .setQuality(100)
                        .compressToBitmap(croppedImageFilePath);
                ByteArrayOutputStream baosMain = new ByteArrayOutputStream();
                main_image_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baosMain);
                final byte[] main_image_byte = baosMain.toByteArray();

                //Compress the image provided by the thumb_filePath file, and then store the compressed image in a bitmap.
                //then create a byte array, that will hold our compressed size image.
                    Bitmap thumb_bitmap = new Compressor(this)
                            .setMaxHeight(100)  //thumbnail height
                            .setMaxWidth(100)   //thumbnail width
                            .setQuality(100)     //thumbnail quality
                            .compressToBitmap(croppedImageFilePath);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    final byte[] thumb_byte = baos.toByteArray(); //this is the compressed image information


                //now create the path for storing these images.
                //The path will be created as per the current user id.
                //First go the the folder with that user ID, then inside that:
                //profile_images: will store the original size file
                //profile_images/thumbs: will store the thumbnail file
                String currentUserId = mCurrentUser.getUid();
                StorageReference filepath = mImageStorage.child("profile_images").child(currentUserId+".jpg");
                final StorageReference thumb_filepath = mImageStorage.child("profile_images").child("thumbs").child(currentUserId+".jpg");

                //Put the original image file to "profile_images" folder
                UploadTask mainImageUploadTask = filepath.putBytes(main_image_byte);
                mainImageUploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            //Image uploaded successfully
                            // but the url of that image is still not entered into the database till now
                            // we will do that later
                            // for now proceed to upload thumbnail too in the database
                            //but first lets store the download URL of the original size image in a string. we will use it later.
                            @SuppressWarnings("VisibleForTests") //this was done to remove one error. IDK what is it.
                            final String download_url = task.getResult().getDownloadUrl().toString(); //store download URL of the original size image

                            //upload the thumbnail image to the "thumbs" folder
                            UploadTask thumbImageUploadTask = thumb_filepath.putBytes(thumb_byte);
                            thumbImageUploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {

                                    if(thumb_task.isSuccessful()){
                                        //If the Thumbnail was uploaded successfully we will come here
                                        //but still we are not writing the URL to find these files in the database.

                                        @SuppressWarnings("VisibleForTests")
                                        String thumb_downloadUrl = thumb_task.getResult().getDownloadUrl().toString(); //store download URL of the thumbnail image file

                                        //Now we will update the database to store the updated URLs of the newly uploaded image and thumbnail files
                                        //For that first create a Map to store the URLs
                                        Map update_hashMap = new HashMap<String, String>();
                                        update_hashMap.put("image", download_url);
                                        update_hashMap.put("thumb_image", thumb_downloadUrl);

                                        //and then add that Map to database by 'updateChildren()'. This will update the previous contents in those "image" and "thumb_image" field
                                        mUserDatabase.updateChildren(update_hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){//if the image and thumbnail added to database successfully

                                                    //if image and thumbnail were successfully updated, then dismiss the progress dialog, and print 'Successful!' message
                                                    mProgressDialog.dismiss();
                                                    Toast.makeText(SettingsActivity.this, "Successful!", Toast.LENGTH_LONG).show();
                                                }else{//The update of URLs couldnt be completed successfully

                                                    //Make Toast notification of 'Image couldn't be added to database'
                                                    Toast.makeText(SettingsActivity.this, "Image couldn't be added to the database.", Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        });

                                    }else{ //task of uploading the thumbnail was failed.
                                        mProgressDialog.dismiss();
                                        Toast.makeText(SettingsActivity.this, "There was some error updating the thumbnails", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });

                        }else{//If there is error in uploading the original image
                            mProgressDialog.dismiss();
                            Toast.makeText(SettingsActivity.this, "Error in uploading image...", Toast.LENGTH_LONG).show();
                        }
                    }
                });


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) { //if image couldn't be cropped successfully
                Exception error = result.getError();
            }
        }
    }
    //End of onActivityResult() ---------------------------
}
