package com.example.matutor.adapters;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.matutor.R;
import com.example.matutor.databinding.ListCreatedPostsBinding;

import java.util.List;

import models.createPost_model;

public class createdPost_adapter extends RecyclerView.Adapter<createdPost_adapter.PostViewHolder> {
    private List<createPost_model> createdPostList;

    static class PostViewHolder extends RecyclerView.ViewHolder {
        private final ListCreatedPostsBinding createBinding;

        PostViewHolder(ListCreatedPostsBinding createBinding) {
            super(createBinding.getRoot());
            this.createBinding = createBinding;
        }

        public void bind(createPost_model post) {
            createBinding.postTitleTextView.setText(post.getPostTitle());
            createBinding.postDescTextView.setText(post.getPostDescription());
            createBinding.userFirstName.setText(post.getLearnerFirstname());
            createBinding.userLastName.setText(post.getLearnerLastname());

            // Set tags
            for (String tag : post.getPostTags()) {
                Button tagButton = new Button(createBinding.getRoot().getContext());
                tagButton.setText(tag);
                tagButton.setBackgroundResource(R.color.white);
                tagButton.setTextColor(ContextCompat.getColor(createBinding.getRoot().getContext(), android.R.color.darker_gray));
                tagButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
                createBinding.tagButtonsFrame2.addView(tagButton);
            }

            // Set an OnClickListener for the close button (if needed)
            createBinding.closeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Handle button click if needed
                }
            });
        }
    }

    public createdPost_adapter(List<createPost_model> createdPostList) {
        this.createdPostList = createdPostList;
    }

    @NonNull
    @Override
    public createdPost_adapter.PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ListCreatedPostsBinding createBinding = ListCreatedPostsBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new PostViewHolder(createBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull createdPost_adapter.PostViewHolder holder, int position) {
        createPost_model post = createdPostList.get(position);
        holder.bind(post);
    }

    @Override
    public int getItemCount() {
        return createdPostList.size();
    }

    // Method to set the created post list and notify data set changed
    public void setCreatedPostList(List<createPost_model> createdPostList) {
        this.createdPostList = createdPostList;
        notifyDataSetChanged();
    }
}