package com.example.matutor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.matutor.databinding.ActivityProfileBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.List;

public class Profile extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private String userType;
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

        //fetch user's info to display in the sidemenu header
        fetchUserInfoHeader();

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
            String userEmail = currentUser.getEmail();

            if (!TextUtils.isEmpty(userEmail)) {
                SharedPreferences pref = getSharedPreferences("user_type", MODE_PRIVATE);
                userType = pref.getString("user_type", "");

                DocumentReference userRef = firestore.collection("all_users")
                        .document(userType)
                        .collection("users")
                        .document(userEmail);

                userRef.get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (documentSnapshot.exists()) {
                                    Picasso picasso = new Picasso.Builder(getApplicationContext()).build();

                                    if (documentSnapshot.contains("userProfilePicture")) {
                                        String profilePicUrl = documentSnapshot.getString("userProfilePicture");
                                        if (!TextUtils.isEmpty(profilePicUrl)) {
                                            picasso.load(profilePicUrl).into(binding.userProfilePic);
                                        }
                                    }

                                    binding.lastnameTextView.setText(documentSnapshot.getString("userLastname"));
                                    binding.firstnameTextView.setText(documentSnapshot.getString("userFirstname"));
                                    binding.emailDetails.setText(documentSnapshot.getString("userEmail"));
                                    binding.birthdateDetails.setText(documentSnapshot.getString("userBdate"));
                                    binding.ageDetails.setText(documentSnapshot.getString("userAge"));
                                    binding.addressDetails.setText(documentSnapshot.getString("userAddress"));
                                    binding.contactDetails.setText(documentSnapshot.getString("userContact"));

                                    Object postTagsObj = documentSnapshot.get("userTag");
                                    if (postTagsObj instanceof List) {
                                        List<String> postTags = (List<String>) postTagsObj;
                                        displayPostTags(postTags);
                                    } else {
                                        Toast.makeText(getApplicationContext(), "userTag field is not a List, skipping display", Toast.LENGTH_SHORT).show();
                                    }

                                    if (userType.equals("tutor")) {
                                        // Remove guardian details for tutors
                                        binding.guardianHeader.setVisibility(View.GONE);
                                        binding.guardianNameDetails.setVisibility(View.GONE);
                                        binding.guardianEmailDetails.setVisibility(View.GONE);

                                        createLayoutTutorDetails(documentSnapshot);

                                    } else {
                                        binding.guardianNameDetails.setText(documentSnapshot.getString("userGuardianName"));
                                        binding.guardianEmailDetails.setText(documentSnapshot.getString("userGuardianEmail"));

                                    }

                                } else {
                                    Toast.makeText(getApplicationContext(), "Document does not exist for : (userRef)" + userEmail, Toast.LENGTH_SHORT).show();
                                }

                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getApplicationContext(), "Error fetching data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                Toast.makeText(getApplicationContext(), "User email is empty.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), "User not authenticated.", Toast.LENGTH_SHORT).show();
        }

        //click to edit user profile
        binding.editProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String getEmail = binding.emailDetails.getText().toString().trim();

                if (!getEmail.isEmpty()) {
                    Intent intent = new Intent(getApplicationContext(), EditProfile.class);
                    intent.putExtra("Email", getEmail);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_out_left, R.anim.slide_in_right);
                    finish();
                } else {

                }
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

    private void fetchUserInfoHeader() {
        View headerView = binding.navView.getHeaderView(0);
        TextView headerFullname = headerView.findViewById(R.id.userFullnameSidebar);
        TextView headerEmail = headerView.findViewById(R.id.userEmailSidebar);
        String currentUserEmail = auth.getCurrentUser() != null ? auth.getCurrentUser().getEmail() : null;

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            String userEmail = currentUser.getEmail();

            if (currentUserEmail != null) {
                SharedPreferences pref = getSharedPreferences("user_type", MODE_PRIVATE);
                String userType = pref.getString("user_type", "");

                DocumentReference userRef = firestore.collection("all_users")
                        .document(userType)
                        .collection("users")
                        .document(userEmail);

                userRef.get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (documentSnapshot.exists()) {
                                    String email = documentSnapshot.getString("userEmail");
                                    String firstname = documentSnapshot.getString("userFirstname");
                                    String lastname = documentSnapshot.getString("userLastname");
                                    String fullname = firstname + " " + lastname;

                                    headerFullname.setText(fullname);
                                    headerEmail.setText(email);

                                } else {
                                    Toast.makeText(getApplicationContext(), "Document does not exist for email (documentShapshot.exists): " + userEmail, Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

            }
        }
    }

    private void createLayoutTutorDetails(DocumentSnapshot documentSnapshot) {
        // Create LinearLayout for "Tutor Rating"
        LinearLayout tutorRatingLayout = new LinearLayout(this);
        tutorRatingLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        tutorRatingLayout.setOrientation(LinearLayout.VERTICAL);

        TextView tutorRatingHeader = new TextView(this);
        tutorRatingHeader.setText("Tutor Rating");
        tutorRatingHeader.setTextSize(16);
        tutorRatingHeader.setTypeface(null, Typeface.BOLD);
        tutorRatingHeader.setTextColor(getResources().getColor(R.color.text_grey));
        tutorRatingLayout.addView(tutorRatingHeader);

        TextView tutorRatingValue = new TextView(this);
        tutorRatingValue.setText(documentSnapshot.getString("userRating"));
        tutorRatingValue.setTextSize(14);
        tutorRatingLayout.addView(tutorRatingValue);

        // Create LinearLayout for "Tutoring Center"
        LinearLayout tutoringCenterLayout = createTutorDetailsSubLayout(
                "Tutoring Center",
                documentSnapshot.getString("userTutoringCenter"));

        // Create LinearLayout for "Rate per Session"
        LinearLayout ratePerSessionLayout = createTutorDetailsSubLayout(
                "Rate per Session",
                documentSnapshot.getString("userSessionPrice"));

        // Add all layouts to the main layout (assuming a parent LinearLayout exists)
        LinearLayout parentLayout = findViewById(R.id.profileLinearLayout); // Replace with your actual parent layout ID
        parentLayout.addView(tutorRatingLayout);
        parentLayout.addView(tutoringCenterLayout);
        parentLayout.addView(ratePerSessionLayout);
    }

    private LinearLayout createTutorDetailsSubLayout(String headerText, String valueText) {
        LinearLayout layout = new LinearLayout(this);
        layout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        layout.setOrientation(LinearLayout.VERTICAL);

        TextView header = new TextView(this);
        header.setText(headerText);
        header.setTextSize(16);
        header.setTypeface(null, Typeface.BOLD);
        header.setTextColor(getResources().getColor(R.color.text_grey));
        layout.addView(header);

        TextView value = new TextView(this);
        value.setText(valueText);
        value.setTextSize(14);
        layout.addView(value);

        return layout;
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
