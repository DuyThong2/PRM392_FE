package com.example.prm392fe.models.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponse {
    int id;
    int orderId;
    int productId;
    String productImage;
    String productName;
    int quantity;
}