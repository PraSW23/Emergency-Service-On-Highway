// DriveModeFragment.java

package com.example.userinterface;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class DriveModeFragment extends Fragment implements OnMapReadyCallback,  UserResponseListener {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int PERMISSION_REQUEST_CODE = 2;

    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private Marker currentLocationMarker;
    private TextView speedTextView;
    private SupportMapFragment mapFragment;
    private static final long NEAR_ZERO_SPEED_DURATION = 30000; // 30 seconds in milliseconds
    private static final long USER_RESPONSE_DURATION = 30000; // 30 seconds in milliseconds

    private boolean isAccidentDetected = false;
    private long nearZeroSpeedStartTime = 0;
    private TextView countdownTextView;

    private static final int notificationId = 1;


    // Constants for notification channel
    private static final String CHANNEL_ID = "your_channel_id";
    private static final String CHANNEL_NAME = "Your Channel Name";
    private static final String CHANNEL_DESCRIPTION = "Your Channel Description";

    private SmsPanicMessageSender smsPanicMessageSender;

    // Method to create notification channel
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = CHANNEL_NAME;
            String description = CHANNEL_DESCRIPTION;
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system
            NotificationManager notificationManager = requireActivity().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_drive_mode, container, false);

        speedTextView = view.findViewById(R.id.speedTextView);
        TextView driveModeTextView = view.findViewById(R.id.driveModeTextView);
        Button exitDriveModeButton = view.findViewById(R.id.exitDriveModeButton);
        countdownTextView = view.findViewById(R.id.countdownTextView);
        smsPanicMessageSender = new SmsPanicMessageSender(requireContext());

        exitDriveModeButton.setOnClickListener(v -> {
            // Handling exit drive mode action
            // You can add code here to handle what happens when the button is clicked
        });

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    updateCurrentLocation(location);
                }
            }
        };

        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapContainer);
        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
            getChildFragmentManager().beginTransaction().add(R.id.mapContainer, mapFragment).commit();
        }
        mapFragment.getMapAsync(this);
        // Create the notification channel
        createNotificationChannel();


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        startLocationUpdates();
        IntentFilter filter = new IntentFilter("handle_user_response");
        requireActivity().registerReceiver(handleUserResponseReceiver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        stopLocationUpdates();
        requireActivity().unregisterReceiver(handleUserResponseReceiver);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        enableMyLocation();
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
        } else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
            } else {
                Toast.makeText(requireContext(), "Location permission denied.", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == PERMISSION_REQUEST_CODE) {
            // Check if the permission is granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted, proceed with your actions
                createNotificationWithActions();
            } else {
                // Permission is denied, show a message or take appropriate action
                Toast.makeText(requireContext(), "Permission denied to post notifications", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(2000)
                .setFastestInterval(1000);

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }
    }

    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    private void updateCurrentLocation(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        if (currentLocationMarker == null) {
            MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("Current Location");
            currentLocationMarker = googleMap.addMarker(markerOptions);
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f));
        } else {
            currentLocationMarker.setPosition(latLng);
        }

        float speed = location.getSpeed();
        speed = (float) (speed * 3.6);
        speedTextView.setText(String.format("Speed: %.2f km/h", speed));

        // Check if speed is near zero
        if (speed < 1000) {
            if (!isAccidentDetected) {
                if (nearZeroSpeedStartTime == 0) {
                    nearZeroSpeedStartTime = System.currentTimeMillis();
                } else {
                    long elapsedTime = System.currentTimeMillis() - nearZeroSpeedStartTime;
                    long remainingTime = NEAR_ZERO_SPEED_DURATION - elapsedTime;
                    if (remainingTime > 0) {
                        // Update countdown timer
                        updateCountdownTimer(remainingTime);
                    } else {
                        // Accident detected
                        isAccidentDetected = true;
                        triggerAlarmAndNotification();

                    }
                }
            }
        } else {
            // Reset if speed is not near zero
            nearZeroSpeedStartTime = 0;
            isAccidentDetected = false;
        }
    }

    // Method to create and show notification with actions
    // Method to create and show notification with actions
    // Method to create and show notification with actions
    private void createNotificationWithActions() {
        // Create an Intent for the notification action buttons
        Intent yesIntent = new Intent(requireContext(), YesReceiver.class);
        yesIntent.putExtra("notificationId", notificationId);
        Intent noIntent = new Intent(requireContext(), NoReceiver.class);
        noIntent.putExtra("notificationId", notificationId);

        // Create PendingIntent for each action
        PendingIntent yesPendingIntent = PendingIntent.getBroadcast(requireContext(), 0, yesIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        PendingIntent noPendingIntent = PendingIntent.getBroadcast(requireContext(), 0, noIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext(), CHANNEL_ID)
                .setContentTitle("Are you SAFE?")
                .setContentText("Please respond to confirm your safety.")
                .setSmallIcon(R.drawable.notification_icon)
                .addAction(R.drawable.ic_yes, "Yes", yesPendingIntent)
                .addAction(R.drawable.ic_no, "No", noPendingIntent);

        // Show the notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(requireContext());
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, so request it
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.POST_NOTIFICATIONS}, PERMISSION_REQUEST_CODE);
        } else {
            // Permission is already granted, proceed with your actions
            notificationManager.notify(notificationId, builder.build());
            // Trigger alarms (LED flash, sound, vibration)
            TriggerManager.triggerAlarms(requireContext());
        }
    }



    private void updateCountdownTimer(long remainingTime) {
        int seconds = (int) (remainingTime / 1000);
        int minutes = seconds / 60;
        seconds %= 60;
        String countdownText = String.format("Time left: %02d:%02d", minutes, seconds);
        countdownTextView.setText(countdownText);
    }

    private void triggerAlarmAndNotification() {
        // Trigger alarms (sound, vibration)
        // Show notification asking if the user is safe

        // You can use AlarmManager for scheduling alarms and NotificationManager for showing notifications.
        // For simplicity, let's assume you have methods to trigger alarms and show notifications.
        triggerAlarm();
        // Create and show notification with actions (Accident detection confirmation)
        createNotificationWithActions();
        showNotification();
    }

    private void triggerAlarm() {
        // Implement your code to trigger alarms (e.g., sound, vibration)
        Toast.makeText(requireContext(), "Possible accident detected! Check if you are safe.", Toast.LENGTH_LONG).show();
    }

    private void showNotification() {
        // Implement your code to show a notification asking if the user is safe
        // You can use NotificationManager to show notifications

        // For simplicity, let's just display a Toast message as a notification
        Toast.makeText(requireContext(), "Are you safe? Please respond.", Toast.LENGTH_LONG).show();

        // You can also use NotificationCompat.Builder to create and show notifications
    }

    private void handleUserResponse(boolean isSafe) {
        if (isSafe) {
            // User confirmed safe
            resetAccidentDetection();
            Toast.makeText(requireContext(), "You are safe. Mechanism reset.", Toast.LENGTH_SHORT).show();
        } else {
            // User indicated not safe, send panic message
            sendPanicMessage();
        }
    }


    private void resetAccidentDetection() {
        isAccidentDetected = false;
        nearZeroSpeedStartTime = 0;
    }

    private void sendPanicMessage() {
        // Implement your code to send panic message to added contacts
        // You can use the PanicMessageSender class or integrate your panic message sending logic here
        // For simplicity, let's just display a Toast message indicating panic message sending
        Toast.makeText(requireContext(), "Panic message sent to emergency contacts.", Toast.LENGTH_LONG).show();
    }

    private BroadcastReceiver handleUserResponseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra("response")) {
                boolean isSafe = intent.getBooleanExtra("response", false);
                handleUserResponse(isSafe);
            }
        }
    };

    @Override
    public void onUserResponse(boolean isSafe) {
        handleUserResponse(isSafe);
    }
}
