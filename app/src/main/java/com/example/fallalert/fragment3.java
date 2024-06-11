package com.example.fallalert;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.widget.Switch;
import android.widget.SeekBar;
import android.widget.TextView;

public class fragment3 extends Fragment {

    private Switch switchSmsAlert;
    private SharedPreferences sharedPreferences;
    private SeekBar seekbarSensitivity;
    private TextView sensitivityValue;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_fragment3, container, false);

        // Initialize SharedPreferences
        sharedPreferences = requireActivity().getSharedPreferences("Settings", Context.MODE_PRIVATE);

        sensitivityValue = view.findViewById(R.id.sensitivity_value);
        seekbarSensitivity = view.findViewById(R.id.seekbar_sensitivity);

        // Initialize the switch
        switchSmsAlert = view.findViewById(R.id.switch_sms_alert);
        switchSmsAlert = view.findViewById(R.id.switch_sms_alert);
        updateSwitchState();

        // Set an onCheckedChangeListener to save the switch state in SharedPreferences
        switchSmsAlert.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("sms_alert_enabled", isChecked);
            editor.apply();

        });

        // Initialize the SeekBar
        int sensitivity = sharedPreferences.getInt("fall_sensitivity", 50);
        seekbarSensitivity.setProgress(sensitivity);
        sensitivityValue.setText("Sensitivity: " + sensitivity);

        seekbarSensitivity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                sensitivityValue.setText("Sensitivity: " + progress);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("fall_sensitivity", progress);
                editor.apply();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Do nothing
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Do nothing
            }
        });

        return view;
    }

    public void updateSwitchState() {
        boolean smsPermissionGranted = sharedPreferences.getBoolean("sms_permission_granted", false);
        switchSmsAlert.setChecked(sharedPreferences.getBoolean("sms_alert_enabled", smsPermissionGranted));

        if (!smsPermissionGranted) {
            switchSmsAlert.setEnabled(false);
        } else {
            switchSmsAlert.setEnabled(true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateSwitchState();
    }

}