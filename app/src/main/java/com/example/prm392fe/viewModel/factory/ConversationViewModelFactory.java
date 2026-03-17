package com.example.prm392fe.viewModel.factory;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.prm392fe.repositories.ConversationRepository;
import com.example.prm392fe.viewModel.ConversationViewModel;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ConversationViewModelFactory implements ViewModelProvider.Factory {

    ConversationRepository repository;

    // Dùng FACTORY Pattern trong Android Architecture Components
    // Bạn dùng Factory vì bạn cần ViewModelProvider để quản lý vòng đời, và ViewModelProvider bắt buộc phải có Factory để biết cách tạo ra các ViewModel có tham số.
    // Factory là cách duy nhất để kết hợp việc tiêm phụ thuộc (DI) với cơ chế quản lý vòng đời của Android Framework.
    // -----------------------------------------------------
    // Trong Android, không có một Container DI cấp cao sẵn có như Spring (trước khi có Hilt), vì vậy, việc quản lý vòng đời (được tích hợp sâu vào
    // Android Framework) và tiêm phụ thuộc (DI) (thường do lập trình viên hoặc thư viện thứ ba quản lý) là hai cơ chế riêng biệt, và chúng cần Factory
    // để giao tiếp với nhau.
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(ConversationViewModel.class)) {
            return (T) new ConversationViewModel(repository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }

}
