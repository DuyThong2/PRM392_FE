package com.example.prm392fe.models.responses;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CountResponse implements Serializable {

    private Integer count;
    private String type; // Ví dụ: UNREAD_MESSAGE, ORDER_BADGE, CART_BADGE

}
