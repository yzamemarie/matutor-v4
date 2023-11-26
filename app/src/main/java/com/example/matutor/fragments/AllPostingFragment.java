package com.example.matutor.fragments;

import android.os.Bundle;

import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.example.matutor.R;
import com.example.matutor.adapters.posting_adapter;
import com.example.matutor.databinding.FragmentAllPostingBinding;

public class AllPostingFragment extends Fragment {

    FragmentAllPostingBinding binding;
    posting_adapter postingAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment using ViewBinding
        binding = FragmentAllPostingBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(requireContext());
        binding.postingRecyclerView.setLayoutManager(layoutManager);
        binding.postingRecyclerView.setAdapter(postingAdapter);

        //searchview
        binding.postingSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Handle search submission if needed
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Handle search text change and filter your data accordingly
                // Update the RecyclerView adapter with filtered data
                return false;
            }
        });

        /*/spinner
        // Populate the spinner with filter options
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(), R.array.search_filter, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.postingFilterSpinner.setAdapter(adapter);

        binding.postingFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                // Handle filter selection and update the RecyclerView adapter
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                //do nothing
            }
        }); */

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Release the ViewBinding object when the view is destroyed
    }

}