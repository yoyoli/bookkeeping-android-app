package edu.northeastern.finalproject_group12;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LocationService {

    private static final String CHANNEL_ID = "location_notification_channel";
    public static final int NOTIFICATION_REQUEST_CODE = 2000;
    public static final int LOCATION_REQUEST_CODE = 1000;

    private final Context context;
    private final FusedLocationProviderClient fusedLocationClient;
    private final SQLiteControllerHelper dbHelper;
    private final Set<String> notifiedLocations;

    public LocationService(Context context) {
        this.context = context;
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        dbHelper = new SQLiteControllerHelper(context, "transactions.db", null, 1);
        createNotificationChannel();
        notifiedLocations = new HashSet<>(); // Initialize the set
    }

    public void startLocationUpdates() {
        Log.d("Location Service", "In startLocationUpdates");
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Request location permissions if not granted
            ActivityCompat.requestPermissions((MainActivity) context, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_REQUEST_CODE);
            return;
        }

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000); // 10 seconds interval
        locationRequest.setFastestInterval(5000); // 5 seconds fastest interval
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    private final LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (locationResult == null) {
                return;
            }
            for (Location location : locationResult.getLocations()) {
                double currentLatitude = location.getLatitude();
                double currentLongitude = location.getLongitude();
                checkProximity(currentLatitude, currentLongitude);
            }
        }
    };

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Location Notification Channel";
            String description = "Channel for location proximity alerts";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void sendNotification(String message) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Check notification permission (Android 13+)
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                showPermissionDialog("Notification permission is required for alerts. Please enable it in settings.");
                return;
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_active)
                .setContentTitle("Location-Based Transaction Reminder")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(0, builder.build());
    }

    private void showPermissionDialog(String message) {
        new AlertDialog.Builder(context)
                .setTitle("Enable Notifications")
                .setMessage(message)
                .setPositiveButton("Go to Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.setData(android.net.Uri.parse("package:" + context.getPackageName()));
                        context.startActivity(intent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void checkProximity(double currentLatitude, double currentLongitude) {
        List<Location> locations = dbHelper.getFrequentLocations();

        for (Location location : locations) {
            float[] results = new float[1];

            Location.distanceBetween(currentLatitude, currentLongitude, location.getLongitude(),  location.getLatitude(), results);
            float distanceInMeters = results[0];

            Log.d("LocationService", "Current location: (" + currentLatitude + ", " + currentLongitude + ")");
            Log.d("LocationService", "Saved location: (" + location.getLatitude() + ", " + location.getLongitude() + ")");
            Log.d("LocationService", "Distance to location: " + distanceInMeters + " meters");

            String locationKey = location.getLatitude() + "," + location.getLongitude();

            if (distanceInMeters < 100 && !notifiedLocations.contains(locationKey)) {
                sendNotification("You are close to a previous transaction. Would you like to add a new transaction?");
                notifiedLocations.add(locationKey);
                Log.d("LocationService", "contains key: " + !notifiedLocations.contains(locationKey));
                break;
            }
        }
    }

}
