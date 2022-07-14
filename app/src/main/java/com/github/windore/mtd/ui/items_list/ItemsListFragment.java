package com.github.windore.mtd.ui.items_list;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.github.windore.mtd.databinding.FragmentItemsListBinding;

public class ItemsListFragment extends Fragment {

    private FragmentItemsListBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ItemsListViewModel itemsListViewModel =
                new ViewModelProvider(this).get(ItemsListViewModel.class);

        binding = FragmentItemsListBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        //final TextView textView = binding.textItemsList;
        //itemsListViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}