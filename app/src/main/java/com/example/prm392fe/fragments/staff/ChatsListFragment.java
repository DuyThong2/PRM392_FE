package com.example.prm392fe.fragments.staff;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.prm392fe.SessionManager;
import com.example.prm392fe.activities.ChatActivity;
import com.example.prm392fe.adapter.ConversationAdapter;
import com.example.prm392fe.api.ApiClient;
import com.example.prm392fe.api.ApiService;
import com.example.prm392fe.databinding.FragmentChatsListBinding;
import com.example.prm392fe.repositories.ConversationRepository;
import com.example.prm392fe.utils.AppStompClient;
import com.example.prm392fe.viewModel.ConversationViewModel;
import com.example.prm392fe.viewModel.factory.ConversationViewModelFactory;

import java.util.ArrayList;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChatsListFragment extends Fragment {
    FragmentChatsListBinding binding;
    AppStompClient stompClient;
    final String TAG = "CHATS_LIST_FRAGMENT_TAG";
    ApiService apiService = ApiClient.getApiService();
    ConversationAdapter conversationAdapter;
    ConversationViewModel conversationViewModel;

    // Khi 1 Fragment được tạo ra thì vòng đời mặc định sẽ bao gồm các callbacks của hệ thống Android
    // onCreateView (1) --> Tạo giao diện (binding) trả về view gốc
    // onViewCreated (2) --> ngay sau khi onCreateView hoàn thành View gốc được trả về

    // Ngoài ra thì còn onChanged() --> Kích hoạt (tức thì nếu LiveData có dữ liệu, hoặc sau khi API hoàn tất).

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // 1. Inflate layout bằng View Binding
        binding = FragmentChatsListBinding.inflate(inflater, container, false);
        // 2. TRẢ VỀ VIEW GỐC CỦA BINDING
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViewModel();
        setupRecyclerView();
        observeViewModel();
    }

    private void setupRecyclerView() {
        // Lấy User ID
        int currentUserId = Integer.parseInt(SessionManager.getInstance(getActivity()).getUserId());
        conversationAdapter = new ConversationAdapter(currentUserId, new ArrayList<>());

        // Sử dụng binding.tên_id_của_recyclerview
        binding.chatsListRV.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.chatsListRV.setAdapter(conversationAdapter);

        conversationAdapter.setOnItemClickListener(conversation -> {
            Toast.makeText(getActivity(), "customerID: "+conversation.getCustomerId()+", name: "+conversation.getCustomerName(), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getActivity(), ChatActivity.class);
            intent.putExtra("CUSTOMER_ID", String.valueOf(conversation.getCustomerId()));
            intent.putExtra("CUSTOMER_NAME", conversation.getCustomerName());
            intent.putExtra("CONVERSATION_ID", String.valueOf(conversation.getConversationId()));
            startActivity(intent);
        });
    }

    private void setupViewModel() {
        ConversationRepository repository = new ConversationRepository(ApiClient.getApiService(), SessionManager.getInstance(getActivity()));
        ConversationViewModelFactory factory = new ConversationViewModelFactory(repository);

        // ViewModelProvider(this, ...) sử dụng Fragment làm LifecycleOwner
        conversationViewModel = new ViewModelProvider(this, factory).get(ConversationViewModel.class);
    }

    // onChanged() là callback hệ thống khác với observeViewModel()
    // observeViewModel() (Phương thức của bạn) là hành động thiết lập sự lắng nghe (The Setup).
    // onChanged() (Callback của Observer) là sự kiện phản ứng với sự thay đổi (The Reaction).
    private void observeViewModel() {
        // gọi phương thức observeViewModel()
        conversationViewModel.getConversationList().observe(getViewLifecycleOwner(), conversations -> {
            if (conversations != null) {
                // thiết lập hàm phản ứng onChanged
                conversationAdapter.setConversations(conversations);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Cần dọn dẹp binding để tránh memory leak
        binding = null;
    }

}