package cass.btg_challenge.controller.dto;

import cass.btg_challenge.entity.OrderEntity;

import java.math.BigDecimal;

public record OrderResponse(Long orderId,
                            Long customerId,
                            BigDecimal totalPrice) {

    public static OrderResponse fromEntity(OrderEntity orderEntity) {
        return new OrderResponse(orderEntity.getOrderId(), orderEntity.getCustomerId(), orderEntity.getTotal());
    }
    /*public OrderResponse(OrderEntity orderEntity) {
        this(orderEntity.getOrderId(), orderEntity.getCustomerId(), orderEntity.getTotal());
    }*/
}
