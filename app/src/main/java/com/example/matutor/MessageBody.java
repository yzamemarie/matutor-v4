package com.example.matutor;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.WindowManager;

import com.example.matutor.databinding.ActivityDashboardBinding;
import com.example.matutor.databinding.ActivityMessageBodyBinding;

public class MessageBody extends AppCompatActivity {

    ActivityMessageBodyBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN); // removes status bar
        binding = ActivityMessageBodyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }
}