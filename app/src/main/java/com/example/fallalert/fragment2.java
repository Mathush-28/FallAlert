
package com.example.fallalert;



import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.Manifest;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.Locale;

public class fragment2 extends Fragment implements SensorEventListener {


    private TextView textViewAccel;
    private TextView textViewGyro;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor gyroscope;


    private float[] accelerationValues = new float[3];
    private float[] gyroscopeValues = new float[3];


    //for handling permission requests
    private ActivityResultLauncher<String> requestPermissionLauncher;


    //threshold values for fall detection

    private static final float MIN_FALL_THRESHOLD = 12.0f;
    private static final float MAX_FALL_THRESHOLD = 16.0f;
    private static final float DEFAULT_FALL_THRESHOLD = 14.0f;
    private static final float GYRO_THRESHOLD = 10.0f;
    private static final float STOP_THRESHOLD = 9.9f; // Threshold for detecting a sudden stop
    private static final int STOP_TIME_WINDOW_MS = 1000; // Time window to detect a stop in milliseconds
    private float fallThreshold = DEFAULT_FALL_THRESHOLD; // Default sensitivity value

    private long lastFallTime = 0;
    private boolean potentialFallDetected = false;

    private TextView textViewStatus;
    private ProgressBar progressBar;

    private Handler handler = new Handler(Looper.getMainLooper());
    private DatabaseHelper databaseHelper;

    // Declare EditText fields for contact name and phone number
    private EditText editTextName;
    private EditText editTextPhone;
    private Button buttonSaveContact;


    private SharedPreferences sharedPreferences;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_fragment2, container, false);

        // Initialize SharedPreferences
        sharedPreferences = requireActivity().getSharedPreferences("Settings", Context.MODE_PRIVATE);


        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            if (isGranted) {
                Toast.makeText(getContext(), "SMS permission granted.", Toast.LENGTH_SHORT).show();
                editor.putBoolean("sms_permission_granted", true);
                editor.putBoolean("sms_alert_enabled", true);
            } else {
                Toast.makeText(getContext(), "SMS permission denied. Alerts will not be sent.", Toast.LENGTH_SHORT).show();
                disableContactInput();
                editor.putBoolean("sms_permission_granted", false);
                editor.putBoolean("sms_alert_enabled", false);
            }
            editor.apply();
            notifyFragment3();
        });

        checkAndRequestSmsPermission();



        // Initialize the TextViews
        textViewAccel = view.findViewById(R.id.textView3);
        textViewGyro = view.findViewById(R.id.textView5);
        sensorManager = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);

        textViewStatus = view.findViewById(R.id.textViewStatus);
        progressBar = view.findViewById(R.id.progressBar);

        // Set initial status to "Monitoring..."
        updateStatus("Monitoring Fall...", android.R.color.holo_green_light);
        progressBar.setVisibility(View.VISIBLE);

        // Load fall sensitivity from SharedPreferences
        int sensitivity = sharedPreferences.getInt("fall_sensitivity", 50); // Default to 50% sensitivity
        fallThreshold = MIN_FALL_THRESHOLD + (MAX_FALL_THRESHOLD - MIN_FALL_THRESHOLD) * (100 - sensitivity) / 100.0f;

        // Initialize the DatabaseHelper to manage the SQLite database
        databaseHelper = new DatabaseHelper(getContext());

        //Initialize shared preferences
        sharedPreferences = requireActivity().getSharedPreferences("Settings", Context.MODE_PRIVATE);

        // Initialize EditText and Button fields
        editTextName = view.findViewById(R.id.cName);
        editTextPhone = view.findViewById(R.id.cNumber);
        buttonSaveContact = view.findViewById(R.id.buttonSaveContact);

        // Set OnClickListener for the button
        buttonSaveContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveContact();
            }
        });

        if (sensorManager != null) {
            // Get the accelerometer and gyroscope sensors
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        }
        return view;
    }

    private void checkAndRequestSmsPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            requestSmsPermission();
        } else {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("sms_permission_granted", true);
            editor.apply();
            notifyFragment3();
        }
    }


    private void disableContactInput() {
        editTextName.setEnabled(false);
        editTextPhone.setEnabled(false);
        editTextName.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray));
        editTextPhone.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray));
        buttonSaveContact.setEnabled(false);
        buttonSaveContact.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray));
    }



    private void requestSmsPermission() {
        requestPermissionLauncher.launch(Manifest.permission.SEND_SMS);
    }

    private void notifyFragment3() {
        Fragment fragment3 = getParentFragmentManager().findFragmentByTag("SETTINGS_FRAGMENT");
        if (fragment3 != null && fragment3 instanceof fragment3) {
            ((fragment3) fragment3).updateSwitchState();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Register the listener for the accelerometer and gyroscope sensors
        if (sensorManager != null) {
            if (accelerometer != null) {
                sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            }
            if (gyroscope != null) {
                sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // Unregister the sensor listener to save resources
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        // Check which sensor triggered the event and update the corresponding variables and TextViews
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            System.arraycopy(sensorEvent.values, 0, accelerationValues, 0, sensorEvent.values.length);


            // Calculate the SMV
            float smv = (float) Math.sqrt(
                    accelerationValues[0] * accelerationValues[0] +
                            accelerationValues[1] * accelerationValues[1] +
                            accelerationValues[2] * accelerationValues[2]
            );

            textViewAccel.setText(String.format(Locale.getDefault(), "X: %f m/s²\nY: %f m/s²\nZ: %f m/s²", accelerationValues[0], accelerationValues[1], accelerationValues[2]));

            Log.d("FallDetection", "SMV: " + smv);

            // Detect potential fall based on the SMV threshold
            if (smv > fallThreshold) {
                potentialFallDetected = true;
                lastFallTime = System.currentTimeMillis();
                Log.d("FallDetection", "Potential fall detected. SMV: " + smv);
                updateStatus("Potential Fall Detected", android.R.color.holo_orange_light);
            }

            // Check for a sudden stop within the time window to confirm the fall
            if (potentialFallDetected) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastFallTime < STOP_TIME_WINDOW_MS && smv < STOP_THRESHOLD) {
                    Log.d("FallDetection", "Sudden stop detected after potential fall.");
                    handleFall();
                    potentialFallDetected = false;
                } else if (currentTime - lastFallTime >= STOP_TIME_WINDOW_MS) {
                    potentialFallDetected = false;
                    updateStatus("Monitoring Fall...", android.R.color.holo_green_light);
                }
            }

        } else if (sensorEvent.sensor.getType() == Sensor.TYPE_GYROSCOPE) {

            System.arraycopy(sensorEvent.values, 0, gyroscopeValues, 0, sensorEvent.values.length);

            // Calculate the angular velocity magnitude
            float angularVelocity = (float) Math.sqrt(
                    gyroscopeValues[0] * gyroscopeValues[0] +
                            gyroscopeValues[1] * gyroscopeValues[1] +
                            gyroscopeValues[2] * gyroscopeValues[2]
            );

            textViewGyro.setText(String.format(Locale.getDefault(), "X: %f rad/s\nY: %f rad/s\nZ: %f rad/s", gyroscopeValues[0], gyroscopeValues[1], gyroscopeValues[2]));


            if (angularVelocity > GYRO_THRESHOLD) {
                Log.d("FallDetection", "High angular velocity detected. Angular velocity: " + angularVelocity);
                if (potentialFallDetected) {
                    handleFall();
                    potentialFallDetected = false;
                }
                // Detect significant angular velocity as an independent indicator of a fall
                else {
                    Log.d("FallDetection", "Fall detected due to significant angular velocity.");
                    handleFall();
                }
            }
        }

    }

    private void handleFall() {
        Toast.makeText(getContext(), "Fall Detected!", Toast.LENGTH_SHORT).show();
        updateStatus("Fall Detected!", android.R.color.holo_red_light);

        // Check if SMS alerts are enabled
        boolean smsAlertEnabled = sharedPreferences.getBoolean("sms_alert_enabled", true);


        if (smsAlertEnabled && ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            Cursor cursor = databaseHelper.getContact();
            if (cursor.moveToFirst()) {
                int phoneIndex = cursor.getColumnIndex("phone");
                if (phoneIndex != -1) {
                    String phoneNumber = cursor.getString(phoneIndex);
                    sendSMS(phoneNumber, "Fall detected! Please check.");
                } else {
                    Toast.makeText(getContext(), "Phone column not found in database.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "No contact found in database.", Toast.LENGTH_SHORT).show();
            }
            cursor.close();
        }

        handler.postDelayed(() -> {
            updateStatus("Monitoring Fall...", android.R.color.holo_green_light);
            progressBar.setVisibility(View.VISIBLE);
        }, 5000);
    }


    private void sendSMS(String phoneNumber, String message) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            Toast.makeText(getContext(), "SMS sent.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getContext(), "SMS failed to send.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void updateStatus(String status, int colorResId) {
        textViewStatus.setText("Status: " + status);
        textViewStatus.setBackgroundColor(ContextCompat.getColor(requireContext(), colorResId));
        progressBar.setVisibility(View.GONE); // hide progress bar when a fall is detected or potential for fall is detected.
    }

    // Method to save contact details into the database
    private void saveContact() {
        String name = editTextName != null ? editTextName.getText().toString().trim() : "";
        String phone = editTextPhone != null ? editTextPhone.getText().toString().trim() : "";

        Log.d("SaveContact", "Name: " + name);
        Log.d("SaveContact", "Phone: " + phone);

        if (name.isEmpty() || phone.isEmpty()) {
            Toast.makeText(getContext(), "Please enter both name and phone number.", Toast.LENGTH_SHORT).show();
        } else {
            String resultMessage = databaseHelper.addOrUpdateContact(name, phone);
            Toast.makeText(getContext(), resultMessage, Toast.LENGTH_SHORT).show();

            // Clear the input fields
            editTextName.setText("");
            editTextPhone.setText("");


        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
