//PanicMessageSender.java
package com.example.userinterface;

import android.content.Context;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashSet;
import java.util.Set;

public class PanicMessageSender {

    private Context context;
    private FirebaseFirestore db;

    public PanicMessageSender(Context context) {
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
    }

    public void sendPanicMessage(String userId) {
        // Retrieve emergency contact numbers from Firestore
        db.collection("user_info").document(userId).collection("user_contacts")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Set<String> emergencyNumbers = new HashSet<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String number = document.getString("number");
                            emergencyNumbers.add(number);
                        }
                        // Send panic message to emergency numbers
                        sendMessage(emergencyNumbers);
                    } else {
                        Toast.makeText(context, "Failed to retrieve emergency contacts", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sendMessage(Set<String> numbers) {
        // Implementation to send panic message to the retrieved emergency numbers
        // You need to implement the logic to send messages using Firebase Cloud Messaging (FCM)
        // This may involve constructing FCM payloads and sending them to the corresponding devices
        // This part requires integration with FCM and setting up message handling on the client side
        // Refer to Firebase documentation for more information on setting up and using Firebase Cloud Messaging (FCM)
        Toast.makeText(context, "Panic message sent to emergency contacts", Toast.LENGTH_SHORT).show();
    }
}
