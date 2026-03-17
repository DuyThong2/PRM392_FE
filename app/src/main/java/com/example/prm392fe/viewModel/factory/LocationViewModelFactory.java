package com.example.prm392fe.viewModel.factory;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.prm392fe.repositories.LocationRepository;
import com.example.prm392fe.viewModel.LocationViewModel;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class LocationViewModelFactory implements ViewModelProvider.Factory {
    final LocationRepository repository;

    public LocationViewModelFactory (LocationRepository repository) {
        this.repository = repository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(LocationViewModel.class)) {
            // Đây là nơi tạo instance ViewModel bằng cách truyền Repository vào
            return (T) new LocationViewModel(repository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }

}
