package com.example.prm392fe.viewModel.factory;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.prm392fe.repositories.NotificationRepository;
import com.example.prm392fe.viewModel.NotificationViewModel;

public class NotificationViewModelFactory implements ViewModelProvider.Factory {

    private final NotificationRepository repository;

    public NotificationViewModelFactory(NotificationRepository repository) {
        this.repository = repository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(NotificationViewModel.class)) {
            return (T) new NotificationViewModel(repository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
