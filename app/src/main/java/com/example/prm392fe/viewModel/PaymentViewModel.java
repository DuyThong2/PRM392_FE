package com.example.prm392fe.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.prm392fe.models.requests.PaymentRequest;
import com.example.prm392fe.repositories.PaymentRepository;


public class PaymentViewModel extends ViewModel {
    private final PaymentRepository repository = PaymentRepository.getInstance();;

    public LiveData<String> createVNPAYPayment(PaymentRequest request) {
        return repository.createVNPAYPayment(request);
    }

    public LiveData<String> createMomoPayment(PaymentRequest request) {
        return repository.createMomoPayment(request);
    }
}

