package com.example.fitnesspal;

import android.net.Uri;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class WorkoutViewModel extends ViewModel {
    private final MutableLiveData<Uri> photoUri = new MutableLiveData<>();

    public void setPhotoUri(Uri uri) {
        photoUri.setValue(uri);
    }

    public LiveData<Uri> getPhotoUri() {
        return photoUri;
    }
}
