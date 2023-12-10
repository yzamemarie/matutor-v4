package com.example.matutor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.matutor.databinding.ActivityProfileBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.List;

public class Profile extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    ActivityProfileBinding binding;
    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN); // removes status bar
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.bottomNavigator.setSelectedItemId(R.id.dashboard);

        //FOR DRAWER SIDE MENU
        setSupportActionBar(binding.toolbar);
        //NAV MENU
        binding.navView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, binding.drawerLayout, binding.toolbar, R.string.drawer_open, R.string.drawer_close);
        binding.drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        binding.navView.setNavigationItemSelectedListener(this);

        // Fetch and display user's info
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            String learnerEmail = currentUser.getEmail();
            if (!TextUtils.isEmpty(learnerEmail)) {
                firestore.collection("user_learner")
                        .document(learnerEmail)
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                Picasso picasso = new Picasso.Builder(getApplicationContext()).build();

                                if (documentSnapshot.contains("learnerProfilePicture")) {
                                    String profilePicUrl = documentSnapshot.getString("learnerProfilePicture");
                                    if (!TextUtils.isEmpty(profilePicUrl)) {
                                        picasso.load(profilePicUrl)
                                                .into(binding.userProfilePic);
                                    }
                                }

                                String lastname = documentSnapshot.getString("learnerLastname");
                                if (!TextUtils.isEmpty(lastname)) {
                                    binding.lastnameTextView.setText(lastname);
                                }

                                String firstname = documentSnapshot.getString("learnerFirstname");
                                if (!TextUtils.isEmpty(firstname)) {
                                    binding.firstnameTextView.setText(firstname);
                                }

                                String email = documentSnapshot.getString("learnerEmail");
                                if (!TextUtils.isEmpty(email)) {
                                    binding.emailDetails.setText(email);
                                }

                                String bdate = documentSnapshot.getString("learnerBdate");
                                if (!TextUtils.isEmpty(bdate)) {
                                    binding.birthdateDetails.setText(bdate);
                                }

                                String age = documentSnapshot.getString("learnerAge");
                                if (!TextUtils.isEmpty(age)) {
                                    binding.ageDetails.setText(age);
                                }

                                String address = documentSnapshot.getString("learnerAddress");
                                if (!TextUtils.isEmpty(address)) {
                                    binding.addressDetails.setText(address);
                                }

                                String contact = documentSnapshot.getString("learnerContact");
                                if (!TextUtils.isEmpty(contact)) {
                                    binding.contactDetails.setText(contact);
                                }

                                String guardianName = documentSnapshot.getString("learnerGuardianName");
                                if (!TextUtils.isEmpty(guardianName)) {
                                    binding.guardianNameDetails.setText(guardianName);
                                }

                                String guardianEmail = documentSnapshot.getString("learnerGuardianEmail");
                                if (!TextUtils.isEmpty(guardianEmail)) {
                                    binding.guardianEmailDetails.setText(guardianEmail);
                                }

                                List<String> postTags = (List<String>) documentSnapshot.get("learnerTag");
                                displayPostTags(postTags);
                            } else {
                                Toast.makeText(getApplicationContext(), "Document does not exist for learnerEmail: " + learnerEmail, Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(getApplicationContext(), "Error fetching data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });

            } else {
                Toast.makeText(getApplicationContext(), "Learner email is empty", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
        }

        //click to edit user profile
        binding.editProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), EditProfile.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_out_left, R.anim.slide_in_right);
                finish();
            }
        });

        //navbar navigation
        binding.bottomNavigator.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.posting) {
                    startActivity(new Intent(getApplicationContext(), Posting.class));
                    overridePendingTransition(0, 0);
                    return true;
                }
                else if (itemId == R.id.dashboard) {
                    return true;
                }
                else if (itemId == R.id.content) {
                    startActivity(new Intent(getApplicationContext(), Content.class));
                    overridePendingTransition(0, 0);
                    return true;
                }
                else if (itemId == R.id.create) {
                    startActivity(new Intent(getApplicationContext(), CreatePosting.class));
                    overridePendingTransition(0, 0);
                    return true;
                }
                else if (itemId == R.id.notif) {
                    startActivity(new Intent(getApplicationContext(), Notification.class));
                    overridePendingTransition(0, 0);
                    return true;
                }
                return false;
            }
        });

    }
    @Override
    public void onBackPressed() {
        //to avoid closing the application on back press
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            Intent intent = new Intent(getApplicationContext(), Dashboard.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_out_right, R.anim.slide_in_left);
            finish();
        }

    }

    //sidemenu
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.side_dashboard) {
            startActivity(new Intent(getApplicationContext(), Dashboard.class));
            return true;
        }
        else if (itemId == R.id.side_profile) {
            startActivity(new Intent(getApplicationContext(), Profile.class));
            return true;
        }
        else if (itemId == R.id.side_progReports) {
            startActivity(new Intent(getApplicationContext(), ViewProgressReports.class));
            return true;
        }
        else if (itemId == R.id.side_yourPostings) {
            startActivity(new Intent(getApplicationContext(), ViewCreatedPosts.class));
            return true;
        }
        else if (itemId == R.id.side_yourBookings) {
            startActivity(new Intent(getApplicationContext(), Bookings.class));
            return true;
        }
        else if (itemId == R.id.side_yourReviews) {
            startActivity(new Intent(getApplicationContext(), ReviewsHistory.class));
            return true;
        }
        else if (itemId == R.id.side_yourHistory) {
            startActivity(new Intent(getApplicationContext(), BookingsHistory.class));
            return true;
        }
        else if (itemId == R.id.side_help) {
            //create help smth
            return true;
        }
        else if (itemId == R.id.side_logout) {
            logoutConfirmation();
            return true;
        }
        return false;
    }

    private void logoutConfirmation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Logout Session");
        builder.setMessage("Are you sure you want to logout?");
        builder.setPositiveButton("Yes", (dialog, which) -> {
            auth.getInstance().signOut();
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
        builder.setNegativeButton("No", null);
        builder.show();
    }

    private void displayPostTags(List<String> postTags) {
        LinearLayout tagInterestFrame = findViewById(R.id.tagInterestFrame);
        LinearLayout tagInterestFrame2 = findViewById(R.id.tagInterestFrame2);

        // Clear existing buttons and layouts
        tagInterestFrame.removeAllViews();
        tagInterestFrame2.removeAllViews();

        int maxButtonsPerRow = 3;

        for (int i = 0; i < Math.min(postTags.size(), maxButtonsPerRow); i++) {
            Button userInterestTag = createTagButton(postTags.get(i));
            tagInterestFrame.addView(userInterestTag);
        }

        for (int i = maxButtonsPerRow; i < postTags.size(); i++) {
            Button userInterestTag = createTagButton(postTags.get(i));
            tagInterestFrame2.addView(userInterestTag);
        }
    }

    private Button createTagButton(String tagText) {
        Button userInterestTag = new Button(this);
        userInterestTag.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        userInterestTag.setText(tagText);
        userInterestTag.setTextColor(getResources().getColor(android.R.color.darker_gray));
        userInterestTag.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
        userInterestTag.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
        userInterestTag.setId(View.generateViewId());

        // Set onClickListener for the dynamically created tag button
        userInterestTag.setOnClickListener(v -> {
            Toast.makeText(this, "You are interested in  " + tagText.toUpperCase(), Toast.LENGTH_SHORT).show();
        });

        return userInterestTag;
    }
}
