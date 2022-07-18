package com.github.windore.mtd.ui.items_list;

import android.app.AlertDialog;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.windore.mtd.Mtd;
import com.github.windore.mtd.MtdItemRef;
import com.github.windore.mtd.R;

import java.time.DayOfWeek;

public class ShownItemsAdapter extends RecyclerView.Adapter<ShownItemsAdapter.ViewHolder> {
    private final ShownItem[] shownItems;
    private final Mtd mtd;
    private final DayOfWeek selectedWeekday;

    public ShownItemsAdapter(ShownItem[] shownItems, Mtd mtd, DayOfWeek selectedWeekday) {
        this.shownItems = shownItems;
        this.mtd = mtd;
        this.selectedWeekday = selectedWeekday;
    }

    @NonNull
    @Override
    public ShownItemsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_shown_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ShownItem item = shownItems[position];
        if (item.isHeader()) {
            holder.setAsHeader(item.getHeader());
        } else {
            holder.setAsItem(item.getItem(), mtd, selectedWeekday);
        }
    }

    @Override
    public int getItemCount() {
        return shownItems.length;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final CheckBox doneCheckBox;
        private final TextView textView;
        private final ImageButton removeBtn;
        private MtdItemRef item;
        private Mtd mtd;
        private DayOfWeek selectedWeekday;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            doneCheckBox = itemView.findViewById(R.id.item_done_check_box);
            textView = itemView.findViewById(R.id.item_tv);
            removeBtn = itemView.findViewById(R.id.remove_item_btn);

            removeBtn.setOnClickListener(view -> {
                if (item == null || mtd == null) return;

                new AlertDialog.Builder(itemView.getContext())
                        .setMessage(R.string.remove_confirm)
                        .setPositiveButton(R.string.ok, (dialogInterface, i) -> {
                            mtd.removeItem(item);
                            dialogInterface.dismiss();
                        })
                        .setNegativeButton(R.string.cancel, (dialogInterface, i) -> dialogInterface.dismiss())
                        .create()
                        .show();
            });

            doneCheckBox.setOnClickListener(view -> {
                if (item == null || mtd == null || selectedWeekday == null) return;

                mtd.modifyItemDoneState(item, doneCheckBox.isChecked(), selectedWeekday);
            });
        }

        public void setAsItem(MtdItemRef item, Mtd mtd, DayOfWeek selectedWeekday) {
            removeBtn.setVisibility(View.VISIBLE);
            doneCheckBox.setVisibility(View.VISIBLE);

            // Do these before setting item/mtd to non null to avoid unnecessary listener callbacks
            doneCheckBox.setChecked(mtd.isItemDone(item));
            textView.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            textView.setText(mtd.getItemBody(item));

            this.item = item;
            this.mtd = mtd;
            this.selectedWeekday = selectedWeekday;
        }

        public void setAsHeader(String header) {
            item = null;
            mtd = null;
            selectedWeekday = null;

            removeBtn.setVisibility(View.GONE);
            doneCheckBox.setVisibility(View.GONE);

            textView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            textView.setText(header);
        }
    }
}
