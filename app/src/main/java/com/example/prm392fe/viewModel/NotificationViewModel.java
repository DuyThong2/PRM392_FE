package com.example.prm392fe.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import com.example.prm392fe.repositories.NotificationRepository;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotificationViewModel extends ViewModel {

    final NotificationRepository repository;
    final LiveData<Integer> chatBadgeCount;
    final LiveData<Integer> cartBadgeCount;
    final LiveData<String> generalNotification;
    final MediatorLiveData<Integer> chatBadgeLiveData = new MediatorLiveData<>();

    public NotificationViewModel(NotificationRepository repository) {
        this.repository = repository;
        // 1. Tải số đếm ban đầu khi ViewModel được tạo
        repository.fetchInitialChatBadgeCount();
        repository.fetchInitialCartBadgeCount();

        // QUAN TRỌNG: để có thể lấy dữ liệu real-time LiveData từ repo
        repository.startListeningForPersistenQueues();

        // 3. Lấy LiveData cho các loại thông báo
        this.chatBadgeCount = repository.getChatBadgeCount();
        this.cartBadgeCount = repository.getCartBadgeCount();
        this.generalNotification = repository.getGeneralNotification();
    }

    // Getter cho LiveData Badge Count
    public LiveData<Integer> getChatBadgeCount() {
        return chatBadgeCount;
    }

    public LiveData<Integer> getCartBadgeCount() {
        return cartBadgeCount;
    }

    // Getter cho LiveData Thông báo Chung
    public LiveData<String> getGeneralNotification() {
        return generalNotification;
    }

    // Khi Activity/Fragment resume, bạn có thể gọi hàm này
    public void refreshBadgeManually() {
        chatBadgeLiveData.setValue(repository.getChatBadgeCount().getValue());
    }

    // Dọn dẹp khi ViewModel bị hủy
    @Override
    protected void onCleared() {
        super.onCleared();
    }
}
