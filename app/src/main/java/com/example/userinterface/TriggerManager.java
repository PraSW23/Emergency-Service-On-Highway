// TriggerManager.java
package com.example.userinterface;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Vibrator;

public class TriggerManager {

    public static void triggerAlarms(Context context) {
        playBeepSound(context);
        triggerHardVibration(context);
        flashMobileLED(context);
    }

    private static void playBeepSound(Context context) {
        try {
            // Create a new MediaPlayer instance
            MediaPlayer mediaPlayer = new MediaPlayer();

            // Set the beep sound as the data source
            mediaPlayer.setDataSource(context, Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.beep_sound));

            // Prepare the MediaPlayer asynchronously
            mediaPlayer.prepareAsync();

            // Set a listener for when the MediaPlayer is prepared
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    // Once prepared, set the volume to the maximum level
                    AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                    int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, 0);

                    // Start playing the beep sound
                    mp.start();
                }
            });

            // Set a listener for when the MediaPlayer completes playback
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    // Release the MediaPlayer resources after the sound finishes playing
                    mp.release();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void triggerHardVibration(Context context) {
        try {
            // Get system vibrator service
            Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

            // Check if the device has a vibrator
            if (vibrator != null && vibrator.hasVibrator()) {
                // Vibrate for 500 milliseconds (5 seconds)
                vibrator.vibrate(5000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void flashMobileLED(Context context) {
        try {
            CameraManager cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
            String cameraId = null;
            if (cameraManager != null) {
                cameraId = cameraManager.getCameraIdList()[0]; // Use the first available camera
            }

            // Check if the device has a camera with flash
            if (cameraManager != null && cameraId != null && context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
                for (int i = 0; i < 15; i++) {
                    // Turn on the flashlight
                    cameraManager.setTorchMode(cameraId, true);

                    // Wait for 100 milliseconds (0.08 seconds)
                    Thread.sleep(80);

                    // Turn off the flashlight
                    cameraManager.setTorchMode(cameraId, false);

                    // Wait for 100 milliseconds (0.08 seconds)
                    Thread.sleep(80);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
