//SmsPanicMessageSender.java

package com.example.userinterface;

import android.content.Context;
import android.telephony.SmsManager;
import android.widget.Toast;

import java.util.Set;

public class SmsPanicMessageSender {

    private Context context;

    public SmsPanicMessageSender(Context context) {
        this.context = context;
    }

    public void sendPanicMessage(Set<String> numbers, String message) {
        // Check if the device has SMS capabilities
        if (SmsManager.getDefault() == null) {
            Toast.makeText(context, "SMS not supported on this device", Toast.LENGTH_SHORT).show();
            return;
        }

        // Iterate over the emergency contact numbers and send panic messages via SMS
        for (String number : numbers) {
            sendSms(number, message);
        }
        Toast.makeText(context, "Panic message sent to emergency contacts", Toast.LENGTH_SHORT).show();
    }

    private void sendSms(String phoneNumber, String message) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
        } catch (Exception e) {
            Toast.makeText(context, "Failed to send SMS: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
