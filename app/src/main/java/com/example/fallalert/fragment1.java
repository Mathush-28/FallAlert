package com.example.fallalert;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.Calendar;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;



public class fragment1 extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fragment1, container, false);

        Calendar currentTime = Calendar.getInstance();
        int hour = currentTime.get(Calendar.HOUR_OF_DAY);
        String greetingMessage;
        if (hour >= 0 && hour < 12) {
            greetingMessage = "Good Morning User!";
        } else if (hour >= 12 && hour < 17) {
            greetingMessage = "Good Afternoon User!";
        } else {
            greetingMessage = "Good Evening User!";
        }

        TextView greetingTextView = view.findViewById(R.id.greetings);
        greetingTextView.setText(greetingMessage);

        return view;
    }
}