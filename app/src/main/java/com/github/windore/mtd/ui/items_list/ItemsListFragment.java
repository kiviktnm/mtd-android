package com.github.windore.mtd.ui.items_list;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.windore.mtd.MainActivity;
import com.github.windore.mtd.Mtd;
import com.github.windore.mtd.MtdItemRef;
import com.github.windore.mtd.R;
import com.github.windore.mtd.databinding.FragmentItemsListBinding;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;

public class ItemsListFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    private FragmentItemsListBinding binding;
    private ItemsListViewModel itemsListViewModel;
    private Mtd mtd;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        itemsListViewModel = new ViewModelProvider(this).get(ItemsListViewModel.class);
        binding = FragmentItemsListBinding.inflate(inflater, container, false);
        mtd = ((MainActivity)requireActivity()).getMtd();

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Spinner spinner = binding.spinnerWeekdaySelection;
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                view.getContext(),
                R.array.weekdays,
                android.R.layout.simple_spinner_dropdown_item
        );
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
        spinner.setSelection(DayOfWeek.from(LocalDate.now()).getValue() - 1);

        RecyclerView itemsRecyclerView = binding.recyclerviewShownItems;
        itemsRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        ShownItemsAdapter itemsAdapter = new ShownItemsAdapter(itemsListViewModel.getShownItems().getValue(), mtd, DayOfWeek.of(spinner.getSelectedItemPosition() + 1));
        itemsRecyclerView.setAdapter(itemsAdapter);

        itemsListViewModel.getShownItems().observe(getViewLifecycleOwner(), shownItems -> {
            ShownItemsAdapter newItemsAdapter = new ShownItemsAdapter(shownItems, mtd, DayOfWeek.of(spinner.getSelectedItemPosition() + 1));
            itemsRecyclerView.swapAdapter(newItemsAdapter, false);
        });

        mtd.addObserver((__,___) -> updateShownItems());
    }

    private void updateShownItems() {
        if (binding != null) {
            ArrayList<ShownItem> items = new ArrayList<>();

            // Monday is 1 not 0 -> add 1
            int weekdayNum = binding.spinnerWeekdaySelection.getSelectedItemPosition() + 1;
            DayOfWeek weekday = DayOfWeek.of(weekdayNum);

            items.add(new ShownItem("Todos:"));
            items.addAll(ShownItem.createFromMtdItems(mtd.getItemsForWeekday(MtdItemRef.Type.Todo, weekday, false)));
            items.addAll(ShownItem.createFromMtdItems(mtd.getItemsForWeekday(MtdItemRef.Type.Todo, weekday, true)));

            items.add(new ShownItem("Tasks:"));
            items.addAll(ShownItem.createFromMtdItems(mtd.getItemsForWeekday(MtdItemRef.Type.Task, weekday, false)));
            items.addAll(ShownItem.createFromMtdItems(mtd.getItemsForWeekday(MtdItemRef.Type.Task, weekday, true)));

            // Always add two empty shown items to the end of the list to give space for the add new item btn
            items.add(new ShownItem(""));
            items.add(new ShownItem(""));
            itemsListViewModel.setShownItems(items.toArray(new ShownItem[]{}));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        updateShownItems();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {}
}