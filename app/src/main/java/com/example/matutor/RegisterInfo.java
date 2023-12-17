package com.example.matutor;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.matutor.databinding.ActivityRegisterInfoBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RegisterInfo extends AppCompatActivity {

    private static final int SELECT_ID_FRONT = 3;
    private static final int SELECT_ID_BACK = 4;
    private static final int SELECT_SELFIE = 5;
    private static final int MAX_TAGS = 5; //max number of tags allowed
    private List<String> tagsList = new ArrayList<>();//string to hold tags
    private String userType;

    ActivityRegisterInfoBinding binding;
    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    FirebaseStorage storage = FirebaseStorage.getInstance();
    String idFrontFileName, idBackFileName, selfieFileName;
    Intent imageData = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        binding = ActivityRegisterInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        String getEmail = intent.getStringExtra("Email");
        String getPassword = intent.getStringExtra("Password");
        String getConfirm = intent.getStringExtra("Confirm Password");

        binding.regEmailInput.setText(getEmail);
        binding.regPasswordInput.setText(getPassword);
        binding.regConfirmPasswordInput.setText(getConfirm);

        List<String> items = new ArrayList<>();
        for (int i = 0; i <= 100; i++) {
            items.add(String.valueOf(i));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.regAgeSpinner.setAdapter(adapter);

        binding.regEditDate.setOnClickListener(new View.OnClickListener() {
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

        binding.regIdFrontButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openPickerDialog(SELECT_ID_FRONT);
            }
        });

        binding.regIdBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openPickerDialog(SELECT_ID_BACK);
            }
        });

        binding.regSelfieButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openPickerDialog(SELECT_SELFIE);
            }
        });

        //click register button to proceed to login
        binding.registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userType = "learner"; //only learners can register through the mobile app
                
                //get text from text fields
                String userFirstname = binding.regFirstnameInput.getText().toString().trim();
                String userLastname = binding.regLastnameInput.getText().toString().trim();
                String userEmail = binding.regEmailInput.getText().toString().trim();
                String userPassword = binding.regPasswordInput.getText().toString().trim();
                String confirmPass = binding.regConfirmPasswordInput.getText().toString().trim();
                String userBdate = binding.regEditDate.getText().toString().trim();
                String userAge = binding.regAgeSpinner.getSelectedItem().toString().trim();
                String userAddress = binding.regAddressInput.getText().toString().trim();
                String userContact = binding.regContactInput.getText().toString().trim();
                String userGuardianName = binding.regGuardianNameInput.getText().toString().trim();
                String userGuardianEmail = binding.regGuardianEmailInput.getText().toString().trim();

                //checks if front and back ID images and selfie are selected
                if (binding.idFrontPathTextView.getText().toString().isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Select image of your ID (front).", Toast.LENGTH_SHORT).show();
                } else if (binding.idBackPathTextView.getText().toString().isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Select image of your ID (back).", Toast.LENGTH_SHORT).show();
                } else if (binding.selfiePathTextView.getText().toString().isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Select selfie.", Toast.LENGTH_SHORT).show();
                }

                //checks if text fields are empty and displays toast prompt if true
                if (userFirstname.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please enter your first name.", Toast.LENGTH_SHORT).show();
                } else if (userLastname.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please enter your last name.", Toast.LENGTH_SHORT).show();
                } else if (userEmail.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please enter your email.", Toast.LENGTH_SHORT).show();
                } else if (userPassword.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please enter your password.", Toast.LENGTH_SHORT).show();
                } else if (confirmPass.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please confirm your password.", Toast.LENGTH_SHORT).show();
                } else if (userBdate.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please select your birthdate.", Toast.LENGTH_SHORT).show();
                } else if (binding.regAgeSpinner.getSelectedItem().equals("0")) {
                    Toast.makeText(getApplicationContext(), "Please select your age.", Toast.LENGTH_SHORT).show();
                } else if (userAddress.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please enter your address.", Toast.LENGTH_SHORT).show();
                } else if (userContact.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please enter your contact number.", Toast.LENGTH_SHORT).show();
                } else if (userGuardianName.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please enter your parent's or guardian's name.", Toast.LENGTH_SHORT).show();
                } else if (userGuardianEmail.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please enter your parent's or guardian's email.", Toast.LENGTH_SHORT).show();
                } else if (!confirmPass.equals(userPassword)) {
                    Toast.makeText(getApplicationContext(), "Passwords do not match. Please enter again.", Toast.LENGTH_SHORT).show();
                } else {
                    auth.createUserWithEmailAndPassword(userEmail, userPassword)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    String uid = auth.getCurrentUser().getUid();
                                    if (uid != null) {
                                        //create user document for firestore
                                        Map<String, Object> learnerData = new HashMap<>();
                                        learnerData.put("userUid", uid);
                                        learnerData.put("userType", userType);
                                        learnerData.put("userFirstname", userFirstname);
                                        learnerData.put("userLastname", userLastname);
                                        learnerData.put("userEmail", userEmail);
                                        learnerData.put("userPassword", userPassword);
                                        learnerData.put("userBdate", userBdate);
                                        learnerData.put("userAge", userAge);
                                        learnerData.put("userContact", userContact);
                                        learnerData.put("userAddress", userAddress);
                                        learnerData.put("userTag", tagsList);
                                        learnerData.put("userGuardianName", userGuardianName);
                                        learnerData.put("userGuardianEmail", userGuardianEmail);

                                        if (userType.equals("learner")) {
                                            firestore.collection("all_users")
                                                    .document(userType)
                                                    .collection("users")
                                                    .document(userEmail)
                                                    .set(learnerData)
                                                    .addOnSuccessListener(aVoid -> {
                                                        binding.regFirstnameInput.getText().clear();
                                                        binding.regLastnameInput.getText().clear();
                                                        binding.regEmailInput.getText().clear();
                                                        binding.regPasswordInput.getText().clear();
                                                        binding.regEditDate.getText().clear();
                                                        binding.regAgeSpinner.setSelection(0);
                                                        binding.regAddressInput.getText().clear();
                                                        binding.regContactInput.getText().clear();
                                                        binding.regGuardianNameInput.getText().clear();
                                                        binding.regGuardianEmailInput.getText().clear();

                                                        // Upload images to Firestore Storage
                                                        uploadDefaultProfile(userEmail, firestore);
                                                        uploadImageToFirestore(userEmail, SELECT_ID_FRONT, imageData, idFrontFileName);
                                                        uploadImageToFirestore(userEmail, SELECT_ID_BACK, imageData, idBackFileName);
                                                        uploadImageToFirestore(userEmail, SELECT_SELFIE, imageData, selfieFileName);

                                                        Toast.makeText(getApplicationContext(), "Learner has successfully registered!", Toast.LENGTH_SHORT).show();
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Toast.makeText(getApplicationContext(), "Error: (all_user)" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    });
                                        }

                                    }
                                    startActivity(new Intent(getApplicationContext(), Login.class));
                                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                                    finish();

                                } else {
                                    Toast.makeText(getApplicationContext(), "Registration failed!", Toast.LENGTH_SHORT).show();
                                }

                            });

                    //add code that stores user info for admin approval
                }
            }
        });

        //return to login button
        binding.loginHereButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelConfirmation();
            }
        });
    }

    @Override
    public void onBackPressed() {
        cancelConfirmation();
    }

    public void cancelConfirmation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
                    binding.regEditDate.setText(selectedDate);
                },
                year, month, day);

        datePickerDialog.show();
    }

    private void uploadDefaultProfile(String userEmail, FirebaseFirestore firestore) {
        int defaultProfilePictureResourceId = R.drawable.user_pp;
        Uri defaultProfilePictureUri = Uri.parse("android.resource://" + getPackageName() + "/" + defaultProfilePictureResourceId);
        StorageReference storageRef = storage.getReference().child("profile_pictures/" + userEmail + "/user_pp.png");
        UploadTask uploadTask = storageRef.putFile(defaultProfilePictureUri);

        uploadTask.addOnSuccessListener(taskSnapshot -> updateProfilePictureInFirestore(userEmail, firestore))
                .addOnFailureListener(e -> {
                    Toast.makeText(getApplicationContext(), "Error: (uploadDefaultProfile)" + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateProfilePictureInFirestore(String userEmail, FirebaseFirestore firestore) {
        StorageReference storageRef = storage.getReference().child("profile_pictures/" + userEmail + "/user_pp.png");
        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
            String downloadUrl = uri.toString();
            DocumentReference userRef = firestore.collection("all_users")
                    .document(userType) // Use userType variable here
                    .collection("users") // Add "users" subcollection
                    .document(userEmail); // Use userEmail variable here
            userRef.update("userProfilePicture", downloadUrl)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getApplicationContext(), "Successfully updated to Firestore ", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getApplicationContext(), "Error: (updateProfilePictureInFirestore)" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
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
            case SELECT_ID_FRONT:
                idFrontFileName = fileName;
                binding.idFrontPathTextView.setText(fileName);
                break;
            case SELECT_ID_BACK:
                idBackFileName = fileName;
                binding.idBackPathTextView.setText(fileName);
                break;
            case SELECT_SELFIE:
                selfieFileName = fileName;
                binding.selfiePathTextView.setText(fileName);
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
                    case SELECT_ID_FRONT:
                        updateFieldImageUri = "userIdFrontImage";
                        break;
                    case SELECT_ID_BACK:
                        updateFieldImageUri = "userIdBackImage";
                        break;
                    case SELECT_SELFIE:
                        updateFieldImageUri = "userSelfieImage";
                        break;
                    default:
                        updateFieldImageUri = "";
                }

                if (!updateFieldImageUri.isEmpty()) {
                    DocumentReference userRef = firestore.collection("all_users")
                            .document(userType) // Use userType variable instead of "user_learner"
                            .collection("users") // Add "users" subcollection
                            .document(userEmail); // Use learnerEmail variable
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
        if (perm == SELECT_ID_FRONT || perm == SELECT_ID_BACK || perm == SELECT_SELFIE) {
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
                case SELECT_ID_FRONT:
                    binding.idFrontPreview.setImageURI(data.getData());
                    break;
                case SELECT_ID_BACK:
                    binding.idBackPreview.setImageURI(data.getData());
                    break;
                case SELECT_SELFIE:
                    binding.selfiePreview.setImageURI(data.getData());
                    break;
            }
        } else if (data != null && data.getExtras() != null && data.getExtras().get("data") != null) {
            //If the image is captured with the camera, display the captured image
            Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
            switch (perm) {
                case SELECT_ID_FRONT:
                    binding.idFrontPreview.setImageBitmap(imageBitmap);
                    break;
                case SELECT_ID_BACK:
                    binding.idBackPreview.setImageBitmap(imageBitmap);
                    break;
                case SELECT_SELFIE:
                    binding.selfiePreview.setImageBitmap(imageBitmap);
                    break;
            }
        }
    }

    protected void onActivityResult(int perm, int result, Intent data) {
        super.onActivityResult(perm, result, data);

        if (result == RESULT_OK) {
            if (perm == SELECT_ID_FRONT || perm == SELECT_ID_BACK || perm == SELECT_SELFIE) {
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
}