// NoReceiver.java
package com.example.userinterface;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationManagerCompat;

public class NoReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Broadcast user response to the DriveModeFragment
        boolean isSafe = intent.getBooleanExtra("response", false);
        int notificationId = intent.getIntExtra("notificationId", -1);
        Intent responseIntent = new Intent("handle_user_response");
        responseIntent.putExtra("response", isSafe);
        context.sendBroadcast(responseIntent);

        // Cancel the notification if notificationId is valid
        if (notificationId != -1) {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.cancel(notificationId);
        }
    }
}