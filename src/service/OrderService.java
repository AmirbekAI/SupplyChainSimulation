package service;

import domain.model.Order;
import domain.model.OrderItem;
import domain.model.OrderStatus;
import exceptions.OrderNotFoundException;
import repository.InMemoryRepository;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class OrderService {

    private final InMemoryRepository<Order> storage;

    public OrderService(InMemoryRepository<Order> storage) {
        this.storage = storage;
    }

    public Order createOrder(UUID customerId, List<OrderItem> orderItems, boolean isTransactional) {
        Objects.requireNonNull(customerId, "customerId cannot be null");
        Objects.requireNonNull(orderItems, "orderItems cannot be null");
        if (orderItems.isEmpty()) { throw new IllegalArgumentException("orderItems cannot be empty"); }
        Order order = new Order(customerId, List.copyOf(orderItems), isTransactional);

        storage.save(order.getId(), order);
        return order;
    }

    public Order getOrderById(UUID id) {
        return storage.findById(id)
            .orElseThrow(() -> new OrderNotFoundException("Order: " + id + " not found"));
    }

    public List<Order> getOrdersByCustomer(UUID customerId) {
        return storage.findAll().stream()
                .filter(od -> od.getCustomerId().equals(customerId))
                .toList();
    }

    public void updateStatus(UUID orderId, OrderStatus status) {
        Order order = getOrderById(orderId);
        order.setStatus(status);
        storage.save(orderId, order);
    }

    public void cancelOrder(UUID orderId) {
        updateStatus(orderId, OrderStatus.CANCELLED);
    }
    
}
