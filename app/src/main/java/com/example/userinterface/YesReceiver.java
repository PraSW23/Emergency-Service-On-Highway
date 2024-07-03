// YesReceiver.java
package com.example.userinterface;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationManagerCompat;

public class YesReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        boolean isSafe = true; // Yes means safe

        // Assuming you're passing the notification ID as an extra in the intent
        int notificationId = intent.getIntExtra("notificationId", -1);

        // Broadcast user response to the DriveModeFragment
        Intent responseIntent = new Intent("handle_user_response");
        responseIntent.putExtra("response", isSafe);
        context.sendBroadcast(responseIntent);

        // Cancel the notification
        if (notificationId != -1) {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.cancel(notificationId);
        }
    }
}
