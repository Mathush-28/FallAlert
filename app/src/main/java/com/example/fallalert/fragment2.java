package com.example.fallalert;

import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.Manifest;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
//import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
//import androidx.activity.EdgeToEdge;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.graphics.Insets;
//import androidx.core.view.ViewCompat;
//import androidx.core.view.WindowInsetsCompat;
//import androidx.viewpager2.widget.ViewPager2;
//import com.google.android.material.tabs.TabLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Locale;

public class fragment2 extends Fragment implements SensorEventListener {


    private TextView textViewAccel;
    private TextView textViewGyro;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor gyroscope;


    // Variables to store sensor values
    private float accelX, accelY, accelZ;
    private float gyroX, gyroY, gyroZ;

    //for handling permission requests
    private ActivityResultLauncher<String> requestPermissionLauncher;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_fragment2, container, false);

//        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
//            if (isGranted) {
//                // Permission granted
//                // You can now perform the SMS sending operation
//            } else {
//                // Permission denied
//                // Show a toast message and close the app
//                Toast.makeText(requireContext(), "SMS permission denied. Closing app.", Toast.LENGTH_SHORT).show();
//                requireActivity().finish();
//            }
//        });


//        // Check if the SMS permission is already available
//        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
//            // If not, request the permission
//            requestPermissionLauncher.launch(Manifest.permission.SEND_SMS);
//        }


        // Initialize the TextViews
        textViewAccel = view.findViewById(R.id.textView3);
        textViewGyro = view.findViewById(R.id.textView5);
        sensorManager = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);

        if (sensorManager != null) {
            // Get the accelerometer and gyroscope sensors
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        }

        return view;
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
            accelX = sensorEvent.values[0];
            accelY = sensorEvent.values[1];
            accelZ = sensorEvent.values[2];
            textViewAccel.setText(String.format(Locale.getDefault(), "X: %f m/s²\nY: %f m/s²\nZ: %f m/s²", accelX, accelY, accelZ));



        } else if (sensorEvent.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            gyroX = sensorEvent.values[0];
            gyroY = sensorEvent.values[1];
            gyroZ = sensorEvent.values[2];
            textViewGyro.setText(String.format(Locale.getDefault(), "X: %f rad/s\nY: %f rad/s\nZ: %f rad/s", gyroX, gyroY, gyroZ));
        }
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}