package com.example.prm392fe.mapper;

import com.example.prm392fe.enums.OrderStatus;

import java.util.HashMap;
import java.util.Map;

public class StatusMapper {
    private static final Map<OrderStatus, String> statusMap = new HashMap<>();

    static {
        statusMap.put(OrderStatus.PENDING, "Chờ xử lý");
        statusMap.put(OrderStatus.PROCESSING, "Đang giao hàng");
        statusMap.put(OrderStatus.SHIPPED, "Đang vận chuyển");
        statusMap.put(OrderStatus.DELIVERED, "Đã giao hàng");
        statusMap.put(OrderStatus.CANCELLED, "Đã hủy");
        statusMap.put(OrderStatus.RETURNED, "Bị trả lại");
        statusMap.put(OrderStatus.REFUNDED, "Đã hoàn tiền");
    }

    public static String toVietnamese(String statusEnumName) {
        try {
            OrderStatus status = OrderStatus.valueOf(statusEnumName);
            return statusMap.getOrDefault(status, statusEnumName);
        } catch (IllegalArgumentException | NullPointerException e) {
            return statusEnumName; // fallback nếu không match
        }
    }

    public static OrderStatus toEnum(String vn) {
        for (Map.Entry<OrderStatus, String> entry : statusMap.entrySet()) {
            if (entry.getValue().equals(vn)) {
                return entry.getKey();
            }
        }
        return null;
    }
}
