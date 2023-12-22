package com.example.matutor;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.matutor.databinding.ActivityRegisterEmailPassBinding;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegisterEmailPass extends AppCompatActivity {

    private ActivityRegisterEmailPassBinding binding;
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterEmailPassBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String getEmail = binding.regEmailInput.getText().toString().trim();
                String getPassword = binding.regPasswordInput.getText().toString().trim();
                String getConfirm = binding.regConfirmPasswordInput.getText().toString().trim();

                if (getEmail.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please enter your email.", Toast.LENGTH_SHORT).show();
                } else if (getPassword.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please enter your password.", Toast.LENGTH_SHORT).show();
                } else if (getConfirm.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please confirm your password.", Toast.LENGTH_SHORT).show();
                } else if (!getConfirm.equals(getPassword)) {
                    Toast.makeText(getApplicationContext(), "Passwords do not match. Please enter again.", Toast.LENGTH_SHORT).show();
                } else {
                    firestore.collection("all_users")
                            .document("learner")  // Using default userType "learner"
                            .collection("users")
                            .document(getEmail)
                            .get()
                            .addOnSuccessListener(documentSnapshot -> {
                                if (documentSnapshot.exists()) {
                                    Toast.makeText(getApplicationContext(), "Email already exists. Please try a different one.", Toast.LENGTH_SHORT).show();
                                } else {
                                    // Email doesn't exist, proceed to next activity
                                    Intent intent = new Intent(getApplicationContext(), RegisterInfo.class);
                                    intent.putExtra("Email", getEmail);
                                    intent.putExtra("Password", getPassword);
                                    intent.putExtra("Confirm Password", getConfirm);
                                    startActivity(intent);
                                }
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getApplicationContext(), "Error checking email existence: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                }

            }
        });

        //return to login button
        binding.loginHereButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cancel");
        builder.setMessage("Cancel registration?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(getApplicationContext(), Login.class));
                overridePendingTransition(0, 0);
                finishAffinity();
                System.exit(0);
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

}