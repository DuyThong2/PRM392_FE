package com.example.prm392fe.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392fe.R;
import com.example.prm392fe.models.responses.ConversationResponse;
import com.example.prm392fe.utils.DateUtils;

import java.util.List;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class ConversationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    final int staffId;
    List<ConversationResponse> conversationList;
    String TAG = "CONVERSATION_ADAPTER_TAG";

    public ConversationAdapter(int staffId, List<ConversationResponse> list) {
        this.staffId = staffId;
        this.conversationList = list;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_chat, parent, false);
        return new ConversationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ConversationResponse conversation = conversationList.get(position);
        ((ConversationViewHolder) holder).bind(conversation);
    }

    @Override
    public int getItemCount() {
        return conversationList.size();
    }

    public void setConversations(List<ConversationResponse> newConversationList) {
        this.conversationList = newConversationList;
        notifyDataSetChanged();
    }

    // 1. Định nghĩa Interface
    public interface OnItemClickListener {
        // Method sẽ được gọi khi item được click
        void onItemClick(ConversationResponse conversation);
    }

    private OnItemClickListener listener;

    // 2. Setter để Fragment truyền tham chiếu vào
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    class ConversationViewHolder extends RecyclerView.ViewHolder {
        private TextView customerName;
        private TextView lastestMessage;
        private TextView timeStamp;

        public ConversationViewHolder(@NonNull View itemView) {
            super(itemView);
            customerName = itemView.findViewById(R.id.customerNameTv);
            lastestMessage = itemView.findViewById(R.id.lastMessageTv);
            timeStamp = itemView.findViewById(R.id.chatTimestampTv);

            itemView.setOnClickListener(v -> {
                int position = getAbsoluteAdapterPosition();
                Log.i(TAG, "ConversationViewHolder: ");
                if (listener != null) {
                    listener.onItemClick(conversationList.get(position));
                }
            });
        }

        public void bind(ConversationResponse conversation) {
            customerName.setText(conversation.getCustomerName());
            if (conversation.getLastestSenderId() == staffId)
                lastestMessage.setText("Bạn: "+conversation.getLatestMessageContent());
            else lastestMessage.setText("Khách: "+conversation.getLatestMessageContent());
            timeStamp.setText(DateUtils.formatChatTimestamp(conversation.getLatestMessageSentAt()));
        }
    }
}
