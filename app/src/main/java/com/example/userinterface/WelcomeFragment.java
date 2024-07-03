// WelcomeFragment.java
package com.example.userinterface;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import java.util.HashSet;
import java.util.Set;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.vonage.client.VonageClient;
import com.vonage.client.sms.MessageStatus;
import com.vonage.client.sms.SmsSubmissionResponse;
import com.vonage.client.sms.messages.TextMessage;

public class WelcomeFragment extends Fragment {

    private TextView textViewWelcome, textViewEmail, textViewUsername;
    private Button btnDriveMode, btnPanic;
    private FirebaseFirestore db;
    private static final int SMS_PERMISSION_REQUEST_CODE = 100;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 101;
    private Context context;
    private FirebaseUser currentUser;

    public WelcomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_welcome, container, false);
        context = requireContext();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        textViewWelcome = view.findViewById(R.id.textViewWelcome);
        textViewEmail = view.findViewById(R.id.textViewEmail);
        btnDriveMode = view.findViewById(R.id.btnDriveMode);
        btnPanic = view.findViewById(R.id.btnPanic);
        textViewUsername = view.findViewById(R.id.textViewUsername);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Retrieve user information from Firestore
        retrieveUserInfo();

        // Set up button click listeners
        btnPanic.setOnClickListener(v -> {
            // Check for SMS permission before sending panic message
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                // Request SMS permission if not granted
                ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_REQUEST_CODE);
            } else {
                // Permission already granted, proceed with sending panic message
                sendPanicMessage();
            }
        });

        // Set up Drive Mode button click listener
        btnDriveMode.setOnClickListener(v -> {
            // Switch to DriveModeFragment
            switchToDriveModeFragment();
        });

        return view;
    }

    // Method to retrieve user information from Firestore
    private void retrieveUserInfo() {
        if (currentUser != null) {
            String userId = currentUser.getUid();
            db.collection("user_info").document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // User document exists, retrieve name and email
                            String name = documentSnapshot.getString("name");
                            String email = documentSnapshot.getString("email");

                            // Display name and email in TextViews
                            textViewUsername.setText(name);
                            textViewEmail.setText(email);
                        } else {
                            // User document does not exist
                            Toast.makeText(context, "User document does not exist", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Failed to retrieve user information
                        Toast.makeText(context, "Failed to retrieve user information: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            // User is not signed in
            // Handle the case where the user is not authenticated
        }
    }

    // Method to send panic message using SMS
    private void sendPanicMessage() {
        if (currentUser != null) {
            String userId = currentUser.getUid();
            db.collection("user_info").document(userId).collection("user_contacts")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        Set<String> savedNumbers = new HashSet<>();
                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            String number = document.getString("number");
                            savedNumbers.add(number);
                        }

                        if (savedNumbers.isEmpty()) {
                            // No emergency contacts found
                            Toast.makeText(context, "No emergency contacts found", Toast.LENGTH_SHORT).show();
                        } else {
                            // Show confirmation dialog before sending panic message
                            showConfirmationDialog(savedNumbers);
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Failed to retrieve emergency contacts
                        Toast.makeText(context, "Failed to retrieve emergency contacts: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            // User is not signed in
            // Handle the case where the user is not authenticated
        }
    }

    // Method to show confirmation dialog before sending panic message
    private void showConfirmationDialog(Set<String> numbers) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Confirmation");
        StringBuilder messageBuilder = new StringBuilder("Send panic message to:");
        for (String number : numbers) {
            messageBuilder.append("\n").append(number);
        }
        builder.setMessage(messageBuilder.toString());
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Get current location and format message with location coordinates
                getLocationAndSendMessage(numbers);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User clicked "Cancel", do nothing
            }
        });
        builder.show();
    }

    private void getLocationAndSendMessage(Set<String> numbers) {
        TriggerManager.triggerAlarms(context);
    }

    // Request permission result handler
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with sending panic message
                sendPanicMessage();
            } else {
                // Permission denied, show a message or handle accordingly
                Toast.makeText(context, "SMS permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Method to switch to DriveModeFragment
    private void switchToDriveModeFragment() {
        // Show confirmation dialog before switching to DriveModeFragment
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Confirmation");
        builder.setMessage("Switch to Drive Mode?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User confirmed, replace fragment with DriveModeFragment
                getParentFragmentManager().beginTransaction()
                        .replace(((ViewGroup) requireView().getParent()).getId(), new DriveModeFragment())
                        .addToBackStack(null)
                        .commit();
                // Trigger alarms (LED flash, sound, vibration)
                TriggerManager.triggerAlarms(context);
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User declined, do nothing
            }
        });
        builder.show();
    }


}
