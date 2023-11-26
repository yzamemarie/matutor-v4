package com.example.matutor;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.media.MediaTimestamp;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.matutor.databinding.ActivityCreatePostingBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreatePosting extends AppCompatActivity {
    private List<String> tagsList = new ArrayList<>();//string to hold tags
    private static final int MAX_TAGS = 5;
    FirebaseAuth auth = FirebaseAuth.getInstance();
    ActivityCreatePostingBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN); // removes status bar
        binding = ActivityCreatePostingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.addTagButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String tag = binding.postTagEditText.getText().toString().trim();

                if (!tag.isEmpty() && tagsList.size() < MAX_TAGS) {
                    tagsList.add(tag);
                    updateTagButtons();
                    binding.postTagEditText.getText().clear();
                } else if (tagsList.size() > MAX_TAGS) {
                    Toast.makeText(getApplicationContext(), "Maximum number of tags added.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        binding.newTagButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tagsList.remove(binding.newTagButton.getText().toString());
                updateTagButtons();
            }
        });

        binding.postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Fetching the learner's email from the authenticated user
                FirebaseUser currentUser = auth.getCurrentUser();
                if (currentUser != null) {
                    String learnerEmail = currentUser.getEmail();

                    if (!TextUtils.isEmpty(learnerEmail)) {
                        // Creating a reference to the learner's document in "user_learner" collection
                        DocumentReference learnerDocument = FirebaseFirestore.getInstance()
                                .collection("user_learner")
                                .document(learnerEmail);

                        // Fetching learner's data from the document snapshot
                        learnerDocument.get().addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                // Extracting learner's data
                                String learnerUid = documentSnapshot.getString("learnerUid");
                                String learnerFirstname = documentSnapshot.getString("learnerFirstname");
                                String learnerLastname = documentSnapshot.getString("learnerLastname");

                                // Creating a reference to the learner's collection in "createdPost_learner"
                                CollectionReference createdPostCollection = FirebaseFirestore.getInstance()
                                        .collection("createdPost_learner")
                                        .document(learnerEmail)
                                        .collection("created_posts");

                                // Getting post details from UI input fields
                                String postTitle = binding.postTitleInput.getText().toString();
                                String postDescription = binding.postDescInput.getText().toString();
                                String postId = createdPostCollection.document().getId();

                                // Creating a map to store post data
                                Map<String, Object> createdPostData = new HashMap<>();
                                createdPostData.put("postId", postId);
                                createdPostData.put("postTitle", postTitle);
                                createdPostData.put("postDescription", postDescription);
                                createdPostData.put("postTags", tagsList);
                                createdPostData.put("learnerUid", learnerUid);
                                createdPostData.put("learnerFirstname", learnerFirstname);
                                createdPostData.put("learnerLastname", learnerLastname);
                                createdPostData.put("learnerEmail", learnerEmail);

                                // Adding the post to the learner's "created_posts" collection with auto-generated ID
                                createdPostCollection.add(createdPostData)
                                        .addOnCompleteListener(postTask -> {
                                            if (postTask.isSuccessful()) {
                                                // Post creation successful
                                                Toast.makeText(getApplicationContext(), "Post created!", Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(getApplicationContext(), ViewCreatedPosts.class);
                                                startActivity(intent);
                                                overridePendingTransition( R.anim.slide_out_left, R.anim.slide_in_right);
                                                finish();
                                            } else {
                                                // Post creation failed
                                                Toast.makeText(getApplicationContext(), "Post creation failed!", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            } else {
                                // Document does not exist for learnerEmail in "user_learner"
                                Toast.makeText(getApplicationContext(), "Document does not exist for learnerEmail: " + learnerEmail, Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(e -> {
                            // Error fetching learner's data
                            Toast.makeText(getApplicationContext(), "Error fetching learner's data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    } else {
                        // Handling case where learner email is empty
                        Toast.makeText(getApplicationContext(), "Learner email is empty", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Handling case where user is not authenticated
                    Toast.makeText(getApplicationContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void updateTagButtons() {
        LinearLayout tagButtonsFrame = findViewById(R.id.tagButtonsFrame);
        LinearLayout tagButtonsFrame2 = findViewById(R.id.tagButtonsFrame2);
        tagButtonsFrame.removeAllViews();
        tagButtonsFrame2.removeAllViews();

        int maxButtonsPerRow = 3;

        for (int i = 0; i < Math.min(tagsList.size(), 5); i++) {
            LinearLayout targetLayout = (i < maxButtonsPerRow) ? tagButtonsFrame : tagButtonsFrame2;

            Button tagButton = new Button(this);
            tagButton.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            tagButton.setText(tagsList.get(i));
            tagButton.setTextColor(getResources().getColor(android.R.color.darker_gray));
            tagButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
            tagButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));

            tagButton.setId(View.generateViewId());

            tagButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    tagsList.remove(tagButton.getText().toString());
                    updateTagButtons();
                }
            });

            targetLayout.addView(tagButton);
        }

        binding.postTagEditText.setText("");
    }

    @Override
    public void onBackPressed() {
        cancelPostConfirmation();
    }

    private void cancelPostConfirmation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm");
        builder.setMessage("Discard posting?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(getApplicationContext(), Posting.class);
                startActivity(intent);
                overridePendingTransition( R.anim.slide_out_left, R.anim.slide_in_right);
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

}