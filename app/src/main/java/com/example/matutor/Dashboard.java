package com.example.matutor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.core.view.GravityCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.matutor.databinding.ActivityDashboardBinding;
import com.example.matutor.databinding.SidebarBinding;
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

import kotlinx.coroutines.channels.ChannelResult;

public class Dashboard extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    ActivityDashboardBinding binding;
    SidebarBinding sidebarBinding;
    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN); // removes status bar
        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SharedPreferences pref = getSharedPreferences("user_type", MODE_PRIVATE);
        String userType = pref.getString("user_type", "");

        binding.bottomNavigator.setSelectedItemId(R.id.dashboard);

        //fetch user's info to display in the sidemenu header
        //fetchUserInfoHeader();

        //FOR DRAWER SIDE MENU
        setSupportActionBar(binding.toolbar);
        //NAV MENU
        binding.navView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, binding.drawerLayout, binding.toolbar, R.string.drawer_open, R.string.drawer_close);
        binding.drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        binding.navView.setNavigationItemSelectedListener(this);

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

    //sidemenu
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.side_dashboard) {
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

    /*
    private void fetchUserInfoHeader() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            String userEmail = currentUser.getEmail();

            if (!TextUtils.isEmpty(userEmail)) {
                if (!TextUtils.isEmpty(userEmail)) {
                    DocumentReference userRef = firestore.collection("all_users")
                            .document(getUserType(userEmail)) // Use getUserType method to ensure correct type
                            .collection("users")
                            .document(userEmail);

                    userRef.get()
                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    if (documentSnapshot.exists()) {
                                        // Fetch user info
                                        firestore.collection("all_users")
                                                .document(getUserType(userEmail)) // Maintain consistent user type retrieval
                                                .collection("users")
                                                .document(userEmail)
                                                .get()
                                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                        if (documentSnapshot.exists()) {
                                                            String email = documentSnapshot.getString("userEmail");
                                                            String firstname = documentSnapshot.getString("userFirstname");
                                                            String lastname = documentSnapshot.getString("userLastname");

                                                            // Display user info in side menu header
                                                            if (!lastname.isEmpty() && !firstname.isEmpty()) {
                                                                String fullname = firstname + " " + lastname;
                                                                sidebarBinding.userFullnameTextView.setText(fullname);
                                                            } else if (lastname.isEmpty()) {
                                                                Toast.makeText(getApplicationContext(), "Last name is empty.", Toast.LENGTH_SHORT).show();
                                                            } else if (firstname.isEmpty()) {
                                                                Toast.makeText(getApplicationContext(), "First name is empty.", Toast.LENGTH_SHORT).show();
                                                            }

                                                            if (!email.isEmpty()) {
                                                                sidebarBinding.userEmailTextView.setText(email);
                                                            } else {
                                                                Toast.makeText(getApplicationContext(), "Email does not exist.", Toast.LENGTH_SHORT).show();
                                                            }
                                                        } else {
                                                            // Document does not exist for email within userType collection
                                                            Toast.makeText(getApplicationContext(), "Document does not exist for email (userType): " + userEmail, Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                })
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(getApplicationContext(), "Error: (document userType)" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                });
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
    }

    private String getUserType(String userEmail) {
        SharedPreferences preferences = getSharedPreferences("YOUR_PREFERENCES_NAME", MODE_PRIVATE);
        String userType = preferences.getString("user_type", "learner"); // Use "learner" as default if not set
        return userType;
    }
    */

    private void logoutConfirmation () {
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

    //Exit Message Prompt Validation
    @Override
    public void onBackPressed () {
        //to avoid closing the application on back press
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            showExitConfirmation();
        }
    }

    private void showExitConfirmation () {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Return");
        builder.setMessage("Return to login?");
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
