package cass.btg_challenge.service;

import cass.btg_challenge.controller.dto.OrderResponse;
import cass.btg_challenge.entity.OrderEntity;
import cass.btg_challenge.entity.OrderItem;
import cass.btg_challenge.listener.dto.OrderCreatedEvent;
import cass.btg_challenge.repository.OrderRepository;
import com.mongodb.client.MongoClient;
import org.bson.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    //for complex queries
    private final MongoTemplate mongoTemplate;
    private final MongoClient mongo;

    public OrderService(OrderRepository orderRepository, MongoTemplate mongoTemplate, MongoClient mongo) {
        this.orderRepository = orderRepository;
        this.mongoTemplate = mongoTemplate;
        this.mongo = mongo;
    }

    public void save(OrderCreatedEvent orderEvent) {
        var entity = new OrderEntity();

        entity.setOrderId(orderEvent.orderCode());
        entity.setCustomerId(orderEvent.customerCode());
        entity.setItemList(getOrderItems(orderEvent));
        entity.setTotal(getTotal(orderEvent));

        orderRepository.save(entity);
    }

    public Page<OrderResponse> findAllByCustomerId(Long customerId, PageRequest pageRequest) {
        var ordersByCustomerId = orderRepository.findAllByCustomerId(customerId, pageRequest);
        return ordersByCustomerId.map(OrderResponse::fromEntity);
        //return ordersByCustomerId.map(OrderResponse::new);
    }

    public BigDecimal findTotalOnOrdersByCustomerId(Long customerId) {
        var aggregations = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("customerId").is(customerId)),
                Aggregation.group().sum("total").as("total")
        );

        var response = mongoTemplate.aggregate(aggregations, "tb_orders", Document.class);

        return new BigDecimal(response.getUniqueMappedResult().get("total").toString());
    }

    private BigDecimal getTotal(OrderCreatedEvent orderEvent) {
        return orderEvent.items()
                .stream()
                .map(it -> it.price().multiply(BigDecimal.valueOf(it.quantity())))
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
    }

    private List<OrderItem> getOrderItems(OrderCreatedEvent orderEvent) {
        return orderEvent.items()
                .stream()
                .map(it -> new OrderItem(
                        it.product(),
                        it.quantity(),
                        it.price()))
                .toList();
    }

}
