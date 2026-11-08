package com.example.tcc_final.ui.conquistas;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ConquistasViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public ConquistasViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is Conquistas fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}