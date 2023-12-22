package com.example.matutor.models;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.matutor.data.createdPost_data;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class createdPost_model extends ViewModel {
    private String userType;
    private MutableLiveData<List<createdPost_data>> createdPost = new MutableLiveData<>();
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    public LiveData<List<createdPost_data>> getCreatedPosts() {
        return createdPost;
    }

    public void loadCreatedPosts() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userEmail = currentUser.getEmail();

            firestore.collection("createdPosts")
                    .document("createdPost_" + userType)
                    .collection(userEmail)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot querySnapshots) {
                            List<createdPost_data> createdPostList = new ArrayList<>();
                            for (QueryDocumentSnapshot document : querySnapshots) {
                                createdPost_data createdPostData = document.toObject(createdPost_data.class);
                                createdPostList.add(createdPostData);
                            }
                            createdPost.setValue(createdPostList);
                        }
                    });
        }
    }

}