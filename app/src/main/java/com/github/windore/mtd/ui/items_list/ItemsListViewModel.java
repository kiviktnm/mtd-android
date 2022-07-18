package com.github.windore.mtd.ui.items_list;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ItemsListViewModel extends ViewModel {
    private final MutableLiveData<ShownItem[]> shownItems;

    public ItemsListViewModel() {
        shownItems = new MutableLiveData<>();
        shownItems.setValue(new ShownItem[0]);
    }

    public LiveData<ShownItem[]> getShownItems() {
        return shownItems;
    }

    public void setShownItems(ShownItem[] shownItems) {
        this.shownItems.setValue(shownItems);
    }

}