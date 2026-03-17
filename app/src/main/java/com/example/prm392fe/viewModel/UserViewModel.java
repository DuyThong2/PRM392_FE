package com.example.prm392fe.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.prm392fe.models.responses.UserResponse;
import com.example.prm392fe.repositories.UserRepository;

public class UserViewModel extends ViewModel {
    private final UserRepository userRepository  = UserRepository.getInstance();

    public LiveData<UserResponse> getInfo () {
        return userRepository.getInfo();
    }

}
