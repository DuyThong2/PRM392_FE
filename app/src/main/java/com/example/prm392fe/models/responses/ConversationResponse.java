package com.example.prm392fe.models.responses;

import java.util.Date;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ConversationResponse {
    int conversationId;
    String customerName;
    String latestMessageContent;
    Date latestMessageSentAt;
    int customerId;
    int lastestSenderId;
}
