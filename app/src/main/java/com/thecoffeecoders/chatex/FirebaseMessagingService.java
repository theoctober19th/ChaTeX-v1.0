package com.thecoffeecoders.chatex;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.view.View;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by bikalpa on 1/7/2018.
 */

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    //when a message is received(in other words, notification is received)
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String notificationTitle = remoteMessage.getNotification().getTitle();
        String notificationMessage = remoteMessage.getNotification().getBody();
        String click_action = remoteMessage.getNotification().getClickAction();
        String from_user_id = remoteMessage.getData().get("from_user_id");
        String from_user_name = remoteMessage.getData().get("from_user_name");
        String from_thumb_image = remoteMessage.getData().get("from_thumb_image");


        //pushing a notification to the user's mobile
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(notificationTitle)
                        .setContentText(notificationMessage);

        Intent resultIntent = new Intent(click_action);
        //
        if(click_action.equals("com.thecoffeecoders.chatex_TARGET_NOTIFICATION")){
            resultIntent.putExtra("user_id", from_user_id);
            resultIntent.putExtra("user_state", "req_received");
        } else {
            resultIntent.putExtra("user_id", from_user_id);
            resultIntent.putExtra("user_name", from_user_name);
            resultIntent.putExtra("thumb_image", from_thumb_image);
        }
        //

        //resultIntent.putExtra("user_id", from_user_id);
        //resultIntent.putExtra("user_state", "req_received");

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        mBuilder.setContentIntent(resultPendingIntent);



        int mNotificationId = (int) System.currentTimeMillis();
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.notify(mNotificationId, mBuilder.build());

    }
}
