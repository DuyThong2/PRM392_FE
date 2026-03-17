package com.example.prm392fe.models.responses;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    int id;
    String billingAddress;
    String orderDate;
    String status;
    String paymentMethod;
    double totalPrice;
    List<OrderItemResponse> orderItems;
}
