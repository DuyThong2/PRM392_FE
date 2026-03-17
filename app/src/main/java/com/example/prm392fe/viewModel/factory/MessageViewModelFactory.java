package com.example.prm392fe.viewModel.factory;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.prm392fe.repositories.MessageRepository;
import com.example.prm392fe.viewModel.MessageViewModel;

import lombok.NonNull;

public class MessageViewModelFactory implements ViewModelProvider.Factory {

    private final MessageRepository repository;

    // Factory nhận các tham số cần thiết
    public MessageViewModelFactory(MessageRepository repository) {
        this.repository = repository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(MessageViewModel.class)) {
            // Đây là nơi tạo instance ViewModel bằng cách truyền Repository vào
            return (T) new MessageViewModel(repository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }

}
