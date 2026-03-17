package com.example.prm392fe.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.prm392fe.SessionManager;
import com.example.prm392fe.adapter.MessageAdapter;
import com.example.prm392fe.databinding.ActivityChatBinding;
import com.example.prm392fe.models.responses.MessageResponse;
import com.example.prm392fe.repositories.MessageRepository;
import com.example.prm392fe.utils.AppStompClient;
import com.example.prm392fe.viewModel.MessageViewModel;
import com.example.prm392fe.viewModel.factory.MessageViewModelFactory;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    private ActivityChatBinding binding;
    private String TAG = "CHAT_TAG";
    private MessageAdapter messageAdapter;
    private MessageViewModel chatViewModel;

    // Khai báo hằng số để đồng bộ key (Extra)
    public static final String EXTRA_CUSTOMER_ID = "CUSTOMER_ID";
    public static final String EXTRA_CUSTOMER_NAME = "CUSTOMER_NAME";
    public static final String EXTRA_CONVERSATION_ID ="CONVERSATION_ID";
    private String customerId;
    private String userId;
    private String conversationId;

    private AppStompClient stompClient = AppStompClient.getInstance(SessionManager.getInstance(this).getAuthToken());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // --- Tránh vùng camera (notch) ---
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(0, bars.top, 0, 0);
            return insets;
        });

        Intent intent = getIntent();
        if (intent != null) {
            Log.i(TAG, "customerId: "+intent.getStringExtra(EXTRA_CUSTOMER_ID));
            Log.i(TAG, "conversationId: "+intent.getStringExtra(EXTRA_CONVERSATION_ID));
            customerId = intent.getStringExtra(EXTRA_CUSTOMER_ID);
            userId = SessionManager.getInstance(this).getUserId();
            conversationId = intent.getStringExtra(EXTRA_CONVERSATION_ID);
            if (SessionManager.getInstance(this).getRole().equals("STAFF"))
                binding.toolbarTitleTv.setText(intent.getStringExtra(EXTRA_CUSTOMER_NAME));
            Glide.with(this)
                    .load("https://i.pinimg.com/736x/30/a8/49/30a8490ff409df33d1e23702cf2c4aa8.jpg")
                    .override(300, 300) // fix size 200x200 pixel
                    .centerCrop()       // cắt giữa hình để không méo
                    .into(binding.toolbarProfileIv);
        }

        // 1. Thiết lập Adapter
        RecyclerView chatRV = binding.chatRV;
        chatRV.setLayoutManager(new LinearLayoutManager(this));
        messageAdapter = new MessageAdapter(SessionManager.getInstance(this), new ArrayList<>());
        chatRV.setAdapter(messageAdapter);

        // 2. Quan sát LiveData
        MessageRepository repository = new MessageRepository(stompClient, customerId, userId, conversationId);
        MessageViewModelFactory factory = new MessageViewModelFactory(repository);
        chatViewModel = new ViewModelProvider(this, factory).get(MessageViewModel.class);

        chatViewModel.getMessageList().observe(this, new Observer<List<MessageResponse>>() {
            @Override
            public void onChanged(List<MessageResponse> messageList) {
                if (messageList != null) {
                    messageAdapter.setMessages(messageList);
                    // Tự động cuộn xuống tin nhắn mới nhất
                    chatRV.scrollToPosition(messageList.size() - 1);
                }
            }
        });

        // --- OBSERVE TIN NHẮN MỚI ĐẾN (REAL-TIME) ---
        chatViewModel.getNewIncomingMessage().observe(this, newMessage -> {
            if (newMessage != null) {
                // Cập nhật adapter bằng tin nhắn mới
                messageAdapter.addMessage(newMessage); // Yêu cầu MessageAdapter có addMessage
                chatRV.scrollToPosition(messageAdapter.getItemCount() - 1);
            }
        });

        binding.toolbarBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        EditText content = binding.messageEt;
        binding.sendFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chatViewModel.sendMessage(Integer.parseInt(intent.getStringExtra(EXTRA_CONVERSATION_ID)), content.getText().toString());
                binding.messageEt.setText("");
            }
        });
    }

}