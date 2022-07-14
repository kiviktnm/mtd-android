package com.github.windore.mtd.ui.items_list;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ItemsListViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public ItemsListViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is items fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}