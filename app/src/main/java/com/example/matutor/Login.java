package com.example.matutor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.matutor.databinding.ActivityLoginBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class Login extends AppCompatActivity {

    ActivityLoginBinding binding;
    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            Intent intent = new Intent(getApplicationContext(), Dashboard.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();

        binding.loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = binding.emailInput.getText().toString().trim();
                String password = binding.passwordInput.getText().toString().trim();

                if (!email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    if (!password.isEmpty()) {
                        // Check in the "user_learner" collection
                        firestore.collection("user_learner")
                                .whereEqualTo("learnerEmail", email)
                                .get()
                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot learnerSnapshots) {
                                        if (!learnerSnapshots.isEmpty()) {
                                            // User found in "user_learner" collection
                                            loginUser(email, password);
                                        } else {
                                            // User not found in "user_learner" collection, check in "user_tutor" collection
                                            firestore.collection("user_tutor")
                                                    .whereEqualTo("tutorEmail", email)
                                                    .get()
                                                    .addOnCompleteListener(tutorTask -> {
                                                        if (tutorTask.isSuccessful()) {
                                                            QuerySnapshot tutorSnapshots = tutorTask.getResult();

                                                            if (tutorSnapshots != null) {
                                                                if (!tutorSnapshots.isEmpty()) {
                                                                    // User found in "user_tutor" collection
                                                                    loginUser(email, password);
                                                                } else {
                                                                    // User not found in "user_tutor" collection
                                                                    Toast.makeText(getApplicationContext(), "User not found in user_tutor collection", Toast.LENGTH_SHORT).show();
                                                                }
                                                            } else {
                                                                // Log an error message if tutorSnapshots is null
                                                                Toast.makeText(getApplicationContext(), "tutorSnapshots is empty", Toast.LENGTH_SHORT).show();
                                                            }
                                                        } else {
                                                            // Handle the failure case
                                                            Toast.makeText(getApplicationContext(), "Error: " + tutorTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                        }
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        binding.passwordInput.setError("Please enter your password");
                    }
                } else if (email.isEmpty()) {
                    binding.emailInput.setError("Please enter your email address.");
                } else {
                    binding.emailInput.setError("Please enter a valid email address.");
                }
            }
        });


        //register here button
        binding.regHereButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), RegisterEmailPass.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
            }
        });

    }

    //Exit Message Prompt Validation
    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Exit");
        builder.setMessage("Exit application?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
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

    private void loginUser(String email, String password) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(Login.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> authTask) {
                        if (authTask.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "Login Successful!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getApplicationContext(), Dashboard.class);
                            startActivity(intent);
                            finish();
                        } else {
                            // Check if the failure is due to user not being found
                            if (authTask.getException() != null &&
                                    authTask.getException().getMessage() != null &&
                                    authTask.getException().getMessage().contains("no user record corresponding")) {
                                // Do nothing or handle as needed
                            } else {
                                // Display the toast for other login failures
                                Toast.makeText(getApplicationContext(), "Login Failed!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }
}
