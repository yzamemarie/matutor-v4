package models;

import android.os.Parcel;
import android.os.Parcelable;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.matutor.databinding.ActivityCreatePostingBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class createPost_model implements Parcelable {
    private final String postTitle;
    private final String postDescription;
    private final List<String> postTags;
    private final String learnerUid;
    private final String learnerFirstname;
    private final String learnerLastname;

    ActivityCreatePostingBinding createBinding;

    public createPost_model(String postTitle, String postDescription, List<String> postTags,
                            String learnerUid, String learnerFirstname, String learnerLastname) {
        this.postTitle = postTitle;
        this.postDescription = postDescription;
        this.postTags = postTags;
        this.learnerUid = learnerUid;
        this.learnerFirstname = learnerFirstname;
        this.learnerLastname = learnerLastname;
    }

    protected createPost_model(Parcel in) {
        postTitle = in.readString();
        postDescription = in.readString();
        postTags = in.createStringArrayList();
        learnerUid = in.readString();
        learnerFirstname = in.readString();
        learnerLastname = in.readString();
    }

    public static final Creator<createPost_model> CREATOR = new Creator<createPost_model>() {
        @Override
        public createPost_model createFromParcel(Parcel in) {
            return new createPost_model(in);
        }

        @Override
        public createPost_model[] newArray(int size) {
            return new createPost_model[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    public String getPostTitle() {
        return postTitle;
    }

    public String getPostDescription() {
        return postDescription;
    }

    public List<String> getPostTags() {
        return postTags;
    }

    public String getLearnerUid() {
        return learnerUid;
    }

    public String getLearnerFirstname() {
        return learnerFirstname;
    }

    public String getLearnerLastname() {
        return learnerLastname;
    }

    // Fetch created post data from Firestore
    public static List<createPost_model> getCreatedPostListFromFirestore() {
        CollectionReference createdPostCollection = FirebaseFirestore.getInstance()
                .collection("createdPost_learner");

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String learnerEmail = currentUser.getEmail();

            // Creating a reference to the learner's created_posts collection
            createdPostCollection.document(learnerEmail).collection("created_posts")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            List<createPost_model> createdPostList = new ArrayList<>();

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // Extracting data from the document
                                String postTitle = document.getString("postTitle");
                                String postDescription = document.getString("postDescription");
                                List<String> tagsList = (List<String>) document.get("postTags");
                                String learnerUid = document.getString("learnerUid");
                                String learnerFirstname = document.getString("learnerFirstname");
                                String learnerLastname = document.getString("learnerLastname");

                                // Adding the retrieved data to the createdPostList
                                createdPostList.add(new createPost_model(
                                        postTitle,
                                        postDescription,
                                        tagsList,
                                        learnerUid,
                                        learnerFirstname,
                                        learnerLastname
                                ));
                            }

                        } else {
                            //
                        }
                    });
        }

        // Return an empty list if the data is not yet fetched (asynchronous operation)
        return new ArrayList<>();
    }


    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(postTitle);
        parcel.writeString(postDescription);
        parcel.writeStringList(postTags);
        parcel.writeString(learnerUid);
        parcel.writeString(learnerFirstname);
        parcel.writeString(learnerLastname);
    }
}
