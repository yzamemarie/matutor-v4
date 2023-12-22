package com.example.matutor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.example.matutor.adapters.createdPost_adapter;
import com.example.matutor.data.createdPost_data;
import com.example.matutor.databinding.ActivityViewCreatedPostsBinding;
import com.example.matutor.models.createdPost_model;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class ViewCreatedPosts extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private String userType;
    private createdPost_adapter adapter;
    private ActivityViewCreatedPostsBinding binding;
    private createdPost_model createdPostModel;
    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN); // removes status bar
        binding = ActivityViewCreatedPostsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SharedPreferences pref = getSharedPreferences("user_type", MODE_PRIVATE);
        userType = pref.getString("user_type", "");

        binding.bottomNavigator.setSelectedItemId(R.id.dashboard);

        fetchUserInfoHeader();

        // FOR DRAWER SIDE MENU
        setSupportActionBar(binding.toolbar);
        // NAV MENU
        binding.navView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, binding.drawerLayout, binding.toolbar, R.string.drawer_open, R.string.drawer_close);
        binding.drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        binding.navView.setNavigationItemSelectedListener(this);

        // Initialize ViewModel
        createdPostModel = new ViewModelProvider(this).get(createdPost_model.class);

        setUpRecyclerView();

        binding.bottomNavigator.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.posting) {
                    startActivity(new Intent(getApplicationContext(), Posting.class));
                    overridePendingTransition(0, 0);
                    return true;
                } else if (itemId == R.id.dashboard) {
                    return true;
                } else if (itemId == R.id.content) {
                    startActivity(new Intent(getApplicationContext(), Content.class));
                    overridePendingTransition(0, 0);
                    return true;
                } else if (itemId == R.id.create) {
                    startActivity(new Intent(getApplicationContext(), CreatePosting.class));
                    overridePendingTransition(0, 0);
                    return true;
                } else if (itemId == R.id.notif) {
                    startActivity(new Intent(getApplicationContext(), Notification.class));
                    overridePendingTransition(0, 0);
                    return true;
                }
                return false;
            }
        });

        // Load created posts
        loadCreatedPosts();
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

    private void setUpRecyclerView() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userEmail = currentUser.getEmail();

            CollectionReference createdPostRef = FirebaseFirestore.getInstance()
                    .collection("createdPosts")
                    .document("createdPost_" + userType)
                    .collection(userEmail);

            Query query = createdPostRef.orderBy("userEmail", Query.Direction.DESCENDING);

            FirestoreRecyclerOptions<createdPost_data> options = new FirestoreRecyclerOptions.Builder<createdPost_data>()
                    .setQuery(query, createdPost_data.class)
                    .setLifecycleOwner(this) // For automatic lifecycle management
                    .build();

            adapter = new createdPost_adapter(options);

            binding.recyclerView.setHasFixedSize(true);
            binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
            binding.recyclerView.setAdapter(adapter);

        } else {
            Toast.makeText(this, "User not authenticated. (ViewCreatedPost, setUpRecyclerView)", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    private void deleteConfirmation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Remove");
        builder.setMessage("Remove post?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(getApplicationContext(), Posting.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_out_left, R.anim.slide_in_right);
                finish();
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

    @Override
    public void onBackPressed() {
        // to avoid closing the application on back press
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            Intent intent = new Intent(getApplicationContext(), Dashboard.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            finish();
        }
    }

    // sidemenu
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.side_dashboard) {
            startActivity(new Intent(getApplicationContext(), Dashboard.class));
            return true;
        } else if (itemId == R.id.side_profile) {
            startActivity(new Intent(getApplicationContext(), Profile.class));
            return true;
        } else if (itemId == R.id.side_progReports) {
            startActivity(new Intent(getApplicationContext(), Profile.class)); // Changed to Profile since wla ang viewprogreport activity
            return true;
        } else if (itemId == R.id.side_yourPostings) {
            return true;
        } else if (itemId == R.id.side_yourBookings) {
            startActivity(new Intent(getApplicationContext(), Bookings.class));
            return true;
        } else if (itemId == R.id.side_yourReviews) {
            startActivity(new Intent(getApplicationContext(), ReviewsHistory.class));
            return true;
        } else if (itemId == R.id.side_yourHistory) {
            startActivity(new Intent(getApplicationContext(), BookingsHistory.class));
            return true;
        } else if (itemId == R.id.side_help) {
            // create help smth
            return true;
        } else if (itemId == R.id.side_logout) {
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
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
        builder.setNegativeButton("No", null);
        builder.show();
    }

    private void loadCreatedPosts() {
        createdPostModel.loadCreatedPosts();
    }
}