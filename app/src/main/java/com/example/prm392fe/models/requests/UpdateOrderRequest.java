package com.example.prm392fe.models.requests;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class UpdateOrderRequest {
    private String orderStatus;
    private BigDecimal totalPrice;
    private String paymentMethod;
    private String billingAddress;

    public UpdateOrderRequest(String orderStatus) {
        this.orderStatus = orderStatus;
    }
}
