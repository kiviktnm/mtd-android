package com.github.windore.mtd.ui.items_list;

import androidx.annotation.Nullable;

import com.github.windore.mtd.MtdItemRef;

import java.util.ArrayList;
import java.util.List;

public class ShownItem {
    private final String header;
    private final MtdItemRef item;

    public ShownItem(String header) {
        this.header = header;
        item = null;
    }

    public ShownItem(MtdItemRef item) {
        this.item = item;
        header = null;
    }

    public static List<ShownItem> createFromMtdItems(List<MtdItemRef> items) {
        ArrayList<ShownItem> shownItems = new ArrayList<>();
        for (MtdItemRef item : items) {
            shownItems.add(new ShownItem(item));
        }
        return shownItems;
    }

    public boolean isHeader() {
        return header != null;
    }

    @Nullable
    public String getHeader() {
        return header;
    }

    @Nullable
    public MtdItemRef getItem() {
        return item;
    }
}
