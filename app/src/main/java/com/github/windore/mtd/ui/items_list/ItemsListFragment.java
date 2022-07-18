package com.github.windore.mtd.ui.items_list;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
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
        mtd = ((MainActivity) requireActivity()).getMtd();

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

        mtd.addObserver((__, ___) -> updateShownItems());

        binding.btnAddItem.setOnClickListener(view1 -> addNewItem(requireActivity()));
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

    private void addNewItem(Activity activity) {
        new AlertDialog.Builder(activity)
                .setTitle(R.string.select_item_type_prompt)
                .setItems(R.array.item_types, (dialogInterface, which) -> {
                    if (which == 0) {
                        addNewTodo(activity);
                    } else {
                        addNewTask(activity);
                    }
                    dialogInterface.dismiss();
                })
                .create()
                .show();
    }

    private void addNewTodo(Activity activity) {
        LayoutInflater inflater = activity.getLayoutInflater();

        View layout = inflater.inflate(R.layout.dialog_todo, null, false);
        Spinner weekdaySpinner = layout.findViewById(R.id.spinner_weekday);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                activity,
                R.array.weekdays,
                android.R.layout.simple_spinner_dropdown_item
        );
        weekdaySpinner.setAdapter(adapter);
        weekdaySpinner.setSelection(DayOfWeek.from(LocalDate.now()).getValue() - 1);

        new AlertDialog.Builder(activity)
                .setTitle(R.string.add_new_todo)
                .setView(layout)
                .setPositiveButton(R.string.ok, (dialogInterface, i) -> {
                    AlertDialog dialog = (AlertDialog) dialogInterface;
                    EditText bodyET = dialog.findViewById(R.id.edit_text_body);
                    DayOfWeek weekday = DayOfWeek.of(weekdaySpinner.getSelectedItemPosition() + 1);
                    mtd.addTodo(bodyET.getText().toString(), weekday);
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.cancel, (dialogInterface, i) -> dialogInterface.dismiss())
                .create()
                .show();
    }

    private void addNewTask(Activity activity) {
        LayoutInflater inflater = activity.getLayoutInflater();

        View layout = inflater.inflate(R.layout.dialog_task, null, false);

        // Ton of repetition here, would probably be good to check into alternatives that reduce
        // repetition. For now this is fine.
        CheckBox monCB = layout.findViewById(R.id.check_box_mon);
        CheckBox tueCB = layout.findViewById(R.id.check_box_tue);
        CheckBox wedCB = layout.findViewById(R.id.check_box_wed);
        CheckBox thuCB = layout.findViewById(R.id.check_box_thu);
        CheckBox friCB = layout.findViewById(R.id.check_box_fri);
        CheckBox satCB = layout.findViewById(R.id.check_box_sat);
        CheckBox sunCB = layout.findViewById(R.id.check_box_sun);

        new AlertDialog.Builder(activity)
                .setTitle(R.string.add_new_task)
                .setView(layout)
                .setPositiveButton(R.string.ok, (dialogInterface, i) -> {
                    AlertDialog dialog = (AlertDialog) dialogInterface;
                    EditText bodyET = dialog.findViewById(R.id.edit_text_body);

                    ArrayList<DayOfWeek> weekdays = new ArrayList<>();
                    if (monCB.isChecked()) weekdays.add(DayOfWeek.MONDAY);
                    if (tueCB.isChecked()) weekdays.add(DayOfWeek.TUESDAY);
                    if (wedCB.isChecked()) weekdays.add(DayOfWeek.WEDNESDAY);
                    if (thuCB.isChecked()) weekdays.add(DayOfWeek.THURSDAY);
                    if (friCB.isChecked()) weekdays.add(DayOfWeek.FRIDAY);
                    if (satCB.isChecked()) weekdays.add(DayOfWeek.SATURDAY);
                    if (sunCB.isChecked()) weekdays.add(DayOfWeek.SUNDAY);

                    // At least one weekday must be selected. Select the current weekday by default.
                    if (weekdays.size() == 0) {
                        weekdays.add(DayOfWeek.from(LocalDate.now()));
                    }

                    mtd.addTask(bodyET.getText().toString(), weekdays.toArray(new DayOfWeek[]{}));
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.cancel, (dialogInterface, i) -> dialogInterface.dismiss())
                .create()
                .show();
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
    public void onNothingSelected(AdapterView<?> adapterView) {
    }
}