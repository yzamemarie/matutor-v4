package com.example.matutor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.GravityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.example.matutor.adapters.createdPost_adapter;
import com.example.matutor.databinding.ActivityDashboardBinding;
import com.example.matutor.databinding.ActivityViewCreatedPostsBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import models.createPost_model;

public class ViewCreatedPosts extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private createdPost_adapter postAdapter;
    ActivityViewCreatedPostsBinding binding;
    FirebaseAuth auth = FirebaseAuth.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN); // removes status bar
        binding = ActivityViewCreatedPostsBinding.inflate(getLayoutInflater());
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

        // Initialize the RecyclerView and set the adapter
        binding.createdPostingRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        postAdapter = new createdPost_adapter(new ArrayList<>());
        binding.createdPostingRecyclerView.setAdapter(postAdapter);

        // Fetch and display the created post data
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String learnerEmail = currentUser.getEmail();

            // Creating a reference to the learner's collection in "createdPost_learner"
            CollectionReference createdPostCollection = FirebaseFirestore.getInstance()
                    .collection("createdPost_learner")
                    .document(learnerEmail)
                    .collection("created_posts");

            // Fetching created posts from Firestore
            createdPostCollection.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    List<createPost_model> createdPostList = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        // Convert each document to a createPost_model object
                        createPost_model post = document.toObject(createPost_model.class);
                        createdPostList.add(post);
                    }

                    // Set the created post data to the RecyclerView adapter
                    postAdapter.setCreatedPostList(createdPostList);
                } else {
                    // Handle the error
                    Toast.makeText(this, "Error fetching created posts", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Handle the case where the user is not authenticated
            Toast.makeText(this, "User not authenticated. (ViewCreatedPost)", Toast.LENGTH_SHORT).show();
        }

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
        //to avoid closing the application on back press
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            Intent intent = new Intent(getApplicationContext(), Dashboard.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
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
            startActivity(new Intent(getApplicationContext(), ViewProgressReportsLearner.class));
            return true;
        }
        else if (itemId == R.id.side_yourPostings) {
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
}