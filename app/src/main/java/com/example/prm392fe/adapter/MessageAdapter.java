package com.example.prm392fe.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392fe.R;
import com.example.prm392fe.SessionManager;
import com.example.prm392fe.models.responses.MessageResponse;
import com.example.prm392fe.utils.DateUtils;

import java.util.ArrayList;
import java.util.List;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    static final int VIEW_TYPE_SEND = 1;
    static final int VIEW_TYPE_RECEIVE = 2;

    final int currentUserId;
    List<MessageResponse> messageList;

    public MessageAdapter(SessionManager sessionManager, List<MessageResponse> messageList) {
        this.currentUserId = Integer.parseInt(sessionManager.getUserId());
        this.messageList = messageList; // Dữ liệu khởi tạo
    }

    // ----------------------------------------------------
    // PHƯƠNG THỨC QUAN TRỌNG 1: XÁC ĐỊNH LOẠI VIEW
    // ----------------------------------------------------
    @Override
    public int getItemViewType(int position) {
        MessageResponse message = messageList.get(position);

        // So sánh senderId của tin nhắn với currentUserId
        if (message.getSenderId() == currentUserId) {
            return VIEW_TYPE_SEND;
        } else {
            return VIEW_TYPE_RECEIVE;
        }
    }

    // ----------------------------------------------------
    // PHƯƠNG THỨC QUAN TRỌNG 2: TẠO VIEW HOLDER TƯƠNG ỨNG
    // ----------------------------------------------------
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == VIEW_TYPE_SEND) {
            view = inflater.inflate(R.layout.item_message_right, parent, false);
            return new SendMessageViewHolder(view);
        } else {
            view = inflater.inflate(R.layout.item_message_left, parent, false);
            return new ReceiveMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MessageResponse message = messageList.get(position);

        // Kiểm tra loại ViewHolder để ép kiểu và bind dữ liệu
        if (holder.getItemViewType() == VIEW_TYPE_SEND) {
            ((SendMessageViewHolder) holder).bind(message);
        } else {
            ((ReceiveMessageViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    // Phương thức cập nhật dữ liệu (cần dùng trong MVVM khi LiveData thay đổi)
    public void setMessages(List<MessageResponse> newMessageList) {
        this.messageList = newMessageList;
        notifyDataSetChanged();
        // notifyDataSetChanged() của androidx.recyclerview.widget.RecyclerView.Adapter
        // Tác dụng: Thông báo rằng toàn bộ tập dữ liệu (messageList của bạn) đã thay đổi, không rõ phần nào.
        // Cách xử lý: RecyclerView sẽ buộc phải vẽ lại toàn bộ (rebind) tất cả các ViewHolder hiển thị trên màn hình. Nó kiểm tra lại getItemCount() và gọi lại onBindViewHolder() cho mọi item.
        // Ưu điểm: Đơn giản, an toàn khi bạn thay đổi cấu trúc dữ liệu triệt để (ví dụ: tải lại toàn bộ lịch sử).
        // Nhược điểm: Kém hiệu quả (Inefficient) và Không có hiệu ứng động (No Animation). Vì không biết chính xác thay đổi là gì, nó phải làm lại mọi thứ, dẫn đến giao diện có thể giật hoặc không mượt mà.
        // DÙng khi: Tải lần đầu Lịch sử Chat (khi bạn thay thế toàn bộ messageList).
    }

    public void addMessage(MessageResponse newMessage) {
        if (newMessage != null) {
            // Kiểm tra và khởi tạo nếu list đang null (trường hợp hiếm)
            if (this.messageList == null) {
                this.messageList = new ArrayList<>();
            }

            this.messageList.add(newMessage);
            notifyItemInserted(this.messageList.size() - 1);
            // cũng là của androidx.recyclerview.widget.RecyclerView.Adapter
            // Tác dụng: Thông báo rằng một item mới đã được thêm vào vị trí cụ thể (position).
            // Cách xử lý: RecyclerView chỉ xử lý việc thêm một item tại vị trí đó. Nếu RecyclerView có ItemAnimator (thường là mặc định), nó sẽ tạo ra hiệu ứng động mượt mà (ví dụ: item trượt vào từ dưới lên).
            // Ưu điểm: Cực kỳ hiệu quả (chỉ xử lý 1 item) và Tạo hiệu ứng động (Animations).
            // Nhược điểm: Yêu cầu bạn phải chính xác về vị trí của thay đổi. Nếu bạn khai báo sai vị trí, nó có thể dẫn đến crash (IndexOutOfBoundsException) hoặc lỗi dữ liệu (Data Corruption).
            // DÙng khi: Nhận Tin nhắn Real-Time (khi bạn chỉ thêm một MessageResponse vào cuối danh sách).
        }
    }
    private static class SendMessageViewHolder extends RecyclerView.ViewHolder {
        private TextView messageContent;
        private TextView sentAt;

        public SendMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageContent = itemView.findViewById(R.id.messageTv);
            sentAt = itemView.findViewById(R.id.timeTv);
        }

        public void bind(MessageResponse message) {
            messageContent.setText(message.getContent());
            sentAt.setText(DateUtils.formatChatTimestamp(message.getSentAt()));
        }
    }

    private static class ReceiveMessageViewHolder extends RecyclerView.ViewHolder {
        private TextView messageContent;
        private TextView sentAt;

        public ReceiveMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageContent = itemView.findViewById(R.id.messageTv);
            sentAt = itemView.findViewById(R.id.timeTv);
        }

        public void bind(MessageResponse message) {
            messageContent.setText(message.getContent());
            sentAt.setText(DateUtils.formatChatTimestamp(message.getSentAt()));
        }
    }

}
