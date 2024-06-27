package cass.btg_challenge.listener.dto;

import java.util.List;

public record OrderCreatedEvent(Long orderCode,
                                Long customerCode,
                                List<OrderItemEvent> items) {
}
