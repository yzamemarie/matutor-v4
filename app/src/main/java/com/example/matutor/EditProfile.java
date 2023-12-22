package com.example.matutor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.matutor.databinding.ActivityEditProfileBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import java.util.Locale;

public class EditProfile extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final int SELECT_PROFILE_IMAGE = 1;
    private static final int MAX_TAGS = 5;
    private List<String> tagsList = new ArrayList<>();
    private String userType;
    private ActivityEditProfileBinding binding;
    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    FirebaseStorage storage = FirebaseStorage.getInstance();
    String profileImgFileName;
    Intent imageData = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN); // removes status bar
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //FOR DRAWER SIDE MENU
        setSupportActionBar(binding.toolbar);
        //NAV MENU
        binding.navView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, binding.drawerLayout, binding.toolbar, R.string.drawer_open, R.string.drawer_close);
        binding.drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        binding.navView.setNavigationItemSelectedListener(this);

        //fetch user's info to display in the sidemenu header
        fetchUserInfoHeader();

        // Get email intent from Profile activity
        Intent intent = getIntent();
        String getEmail = intent.getStringExtra("Email");
        binding.editEmailInput.setText(getEmail);

        // Populate the Spinner using a loop
        List<String> items = new ArrayList<>();
        for (int i = 0; i <= 100; i++) {
            items.add(String.valueOf(i));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.editAgeSpinner.setAdapter(adapter);

        binding.editDateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog();
            }
        });

        binding.addTagButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String tag = binding.editTagText.getText().toString().trim();

                if (!tag.isEmpty() && tagsList.size() < MAX_TAGS) {
                    tagsList.add(tag);
                    updateTagButtons();
                    binding.editTagText.getText().clear();
                } else if (tagsList.size() > MAX_TAGS){
                    Toast.makeText(getApplicationContext(), "Maximum number of tags added.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        binding.editProfileImgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openPickerDialog(SELECT_PROFILE_IMAGE);
            }
        });

        // Save changes button
        binding.editSaveChangesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseUser currentUser = auth.getCurrentUser();
                if (currentUser != null) {
                    String userEmail = currentUser.getEmail();

                    binding.editEmailInput.setText(userEmail);

                    // Extract new values from the input fields
                    String newFirstName = binding.editFirstnameInput.getText().toString().trim();
                    String newLastName = binding.editLastnameInput.getText().toString().trim();
                    String newAbout = binding.editAboutMeInput.getText().toString().trim();
                    String newBirthdate = binding.editDateText.getText().toString().trim();
                    String newAge = binding.editAgeSpinner.getSelectedItem().toString().trim();
                    String newAddress = binding.editAddressInput.getText().toString().trim();
                    String newContact = binding.editContactInput.getText().toString().trim();
                    String newGuardianName = binding.editGuardianNameInput.getText().toString().trim();
                    String newGuardianEmail = binding.editGuardianEmailInput.getText().toString().trim();
                    String newPassword = binding.editPasswordInput.getText().toString().trim();
                    String newConfirmPassword = binding.editConfirmPasswordInput.getText().toString().trim();

                    // Get a reference to the user document in Firestore
                    DocumentReference userRef = firestore.collection("all_users")
                            .document(userType)
                            .collection("users")
                            .document(userEmail);

                    // Update each field individually
                    userRef.update("tags", tagsList);

                    if (!newFirstName.isEmpty()) {
                        userRef.update("userFirstname", newFirstName);
                    }
                    if (!newLastName.isEmpty()) {
                        userRef.update("userLastname", newLastName);
                    }
                    if (!newAbout.isEmpty()) {
                        userRef.update("userAbout", newAbout);
                    }
                    if (!newBirthdate.isEmpty()) {
                        userRef.update("userBdate", newBirthdate);
                    }
                    if (!newAge.isEmpty()) {
                        userRef.update("userAge", newAge);
                    }
                    if (!newAddress.isEmpty()) {
                        userRef.update("userAddress", newAddress);
                    }
                    if (!newContact.isEmpty()) {
                        userRef.update("userContact", newContact);
                    }
                    if (!newGuardianName.isEmpty()) {
                        userRef.update("userGuardianName", newGuardianName);
                    }
                    if (!newGuardianEmail.isEmpty()) {
                        userRef.update("userGuardianEmail", newGuardianEmail);
                    }
                    if (!newPassword.isEmpty() && !newConfirmPassword.isEmpty() && newConfirmPassword.equals(newPassword)) {
                        userRef.update("userPassword", newPassword);
                    }


                    Toast.makeText(EditProfile.this, "Changes saved successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), Profile.class);
                    startActivity(intent);
                    finishAffinity();

                } else {
                    Toast.makeText(EditProfile.this, "User not found.", Toast.LENGTH_SHORT).show();
                }
            }
        });


        //delete account button
        binding.editDeleteAccountButton.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(EditProfile.this);
            builder.setTitle("Confirm");
            builder.setMessage("Are you sure you want to delete your account?");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    FirebaseUser currentUser = auth.getCurrentUser();
                    if (currentUser != null) {
                        String userEmail = currentUser.getEmail();
                        firestore.collection("all_users")
                                .document(userType)
                                .collection("users")
                                .document(userEmail)
                                .delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        currentUser.delete()
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            Toast.makeText(getApplicationContext(), "Account successfully deleted!", Toast.LENGTH_SHORT).show();

                                                            Intent intent = new Intent(getApplicationContext(), Login.class);
                                                            startActivity(intent);
                                                            finishAffinity();
                                                        } else {
                                                            Toast.makeText(getApplicationContext(), "Failed to delete account.", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                        builder.show();
                    }
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.show();
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

        binding.editTagText.setText("");
    }

    private void showDatePickerDialog() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, yearSelected, monthOfYear, dayOfMonth) -> {
                    String selectedDate = (monthOfYear + 1) + "/" + dayOfMonth + "/" + yearSelected;
                    binding.editDateText.setText(selectedDate);
                }, year, month, day);

        datePickerDialog.show();
    }

    private void openPickerDialog(int perm) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Source");
        builder.setItems(new CharSequence[]{"Camera", "Gallery"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int choice) {
                switch (choice) {
                    case 0:
                        openCamera(perm);
                        break;
                    case 1:
                        openGallery(perm);
                        break;
                }
            }
        });
        builder.show();
    }

    private void openCamera(int perm) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, perm);
        }
    }

    private void openGallery(int perm) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, perm);
    }

    private void displayImageFileName(int perm, String fileName) {
        switch (perm) {
            case SELECT_PROFILE_IMAGE:
                profileImgFileName = fileName;
                binding.profilePathTextView.setText(fileName);
                break;
        }
    }

    private boolean isImageSelected(int perm, String fileName) {
        boolean isSelected = !fileName.isEmpty();

        if (!isSelected) {
            // Display a toast or handle the case where no image is selected
            Toast.makeText(getApplicationContext(), "Please select an image. (isImageSelected)", Toast.LENGTH_SHORT).show();
        }

        return isSelected;
    }

    private String getImageFileName(int perm, Intent data) {
        String fileName = null;
        if (data != null && data.getData() != null) {
            // Get file name from the content URI
            fileName = getFileNameFromUri(data.getData());
        } else if (data != null && data.getExtras() != null && data.getExtras().get("data") != null) {
            // If the image is captured with the camera, create a file and use its path as the file name
            fileName = createFileNameFromBitmap((Bitmap) data.getExtras().get("data"), "image_" + System.currentTimeMillis());
        }
        return fileName;
    }

    private String getFileNameFromUri(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor != null) {
            int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            cursor.moveToFirst();
            String fileName = cursor.getString(nameIndex);
            cursor.close();
            return fileName;
        }
        return "";
    }

    private String createFileNameFromBitmap(Bitmap bitmap, String fileName) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        fileName = "IMG_" + timeStamp + ".jpg";

        // Save the bitmap to a file
        File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
            return file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return ""; // Return an empty string if file creation fails
        }
    }

    private void uploadImageToFirestore(String userEmail, int perm, Intent data, String fileName) {
        StorageReference storageRef = storage.getReference().child(userEmail);
        UploadTask uploadTask = storageRef.putFile(getImageUri(perm, data, fileName));

        uploadTask.addOnSuccessListener(taskSnapshot -> {
            storageRef.child(fileName).getDownloadUrl().addOnSuccessListener(uri -> {
                String imageUri = uri.toString();

                String updateFieldImageUri;
                switch (perm) {
                    case SELECT_PROFILE_IMAGE:
                        updateFieldImageUri = "updatedProfilePicture";
                        break;
                    default:
                        updateFieldImageUri = "";
                }

                if (!updateFieldImageUri.isEmpty()) {
                    DocumentReference userRef = firestore.collection("user_user").document(userEmail);
                    userRef.update(updateFieldImageUri, imageUri)
                            .addOnSuccessListener(aVoid -> {
                                // Image URL updated successfully in Firestore
                            })
                            .addOnFailureListener(e -> {
                                // Handle the case where the image URL update fails
                            });
                }
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(getApplicationContext(), "Error uploading image: (uploadImageToFirestore)" + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private Uri getImageUri(int perm, Intent data, String fileName) {
        if (perm == SELECT_PROFILE_IMAGE) {
            if (data != null && data.getData() != null) {
                // Return the URI of the selected image
                return data.getData();
            } else if (data != null && data.getExtras() != null && data.getExtras().get("data") != null) {
                // If the image is captured with the camera, return the URI of the captured image
                Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                String path = MediaStore.Images.Media.insertImage(getApplicationContext().getContentResolver(), imageBitmap, fileName, null);
                return Uri.parse(path);
            }
        }
        return null; // Return null if URI retrieval fails or if perm is not recognized
    }

    private void displayImagePreview(int perm, Intent data) {
        if (data != null && data.getData() != null) {
            // Display the selected image in the corresponding ImageView
            switch (perm) {
                case SELECT_PROFILE_IMAGE:
                    binding.userProfilePic.setImageURI(data.getData());
                    break;

            }
        } else if (data != null && data.getExtras() != null && data.getExtras().get("data") != null) {
            //If the image is captured with the camera, display the captured image
            Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
            switch (perm) {
                case SELECT_PROFILE_IMAGE:
                    binding.userProfilePic.setImageBitmap(imageBitmap);
                    break;

            }
        }
    }

    protected void onActivityResult(int perm, int result, Intent data) {
        super.onActivityResult(perm, result, data);

        if (result == RESULT_OK) {
            if (perm == SELECT_PROFILE_IMAGE) {
                // Save the data for later use
                imageData = data;

                // Get the file name and update the corresponding EditText
                String fileName = getImageFileName(perm, imageData);
                if (!fileName.isEmpty()) {
                    displayImageFileName(perm, fileName);

                    // Check if an image is selected
                    if (isImageSelected(perm, fileName)) {
                        // Display the selected image in the preview ImageView
                        displayImagePreview(perm, imageData);
                    } else {
                        // Handle the case where the image is not selected
                        Toast.makeText(getApplicationContext(), "Please select an image. (onActivityResult)", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Error: Unable to get the file name. (onActivityResult)", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onBackPressed () {
        Intent intent = new Intent(getApplicationContext(), Profile.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_out_right, R.anim.slide_in_left);
        finish();
    }

}
