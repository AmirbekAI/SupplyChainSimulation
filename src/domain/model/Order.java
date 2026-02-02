package domain.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a customer order in the supply chain system.
 */
public class Order {
    private final String id;
    private final String customerId;
    private final List<OrderItem> orderItems;
    private OrderStatus status;
    private final boolean isTransactional;
    private final LocalDateTime createdAt;

    public Order(String customerId, List<OrderItem> orderItems, boolean isTransactional) {
        this.id = UUID.randomUUID().toString();
        this.customerId = customerId;
        this.orderItems = new ArrayList<>(orderItems); // Defensive copy
        this.status = OrderStatus.CREATED;
        this.isTransactional = isTransactional;
        this.createdAt = LocalDateTime.now();
    }

    // Constructor for loading existing orders (e.g., from repository)
    public Order(String id, String customerId, List<OrderItem> orderItems, OrderStatus status, 
                 boolean isTransactional, LocalDateTime createdAt) {
        this.id = id;
        this.customerId = customerId;
        this.orderItems = new ArrayList<>(orderItems);
        this.status = status;
        this.isTransactional = isTransactional;
        this.createdAt = createdAt;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public List<OrderItem> getOrderItems() {
        return new ArrayList<>(orderItems); // Return defensive copy
    }

    public OrderStatus getStatus() {
        return status;
    }

    public boolean isTransactional() {
        return isTransactional;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // Setters
    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Order{" +
                "id='" + id + '\'' +
                ", customerId='" + customerId + '\'' +
                ", orderItems=" + orderItems +
                ", status=" + status +
                ", isTransactional=" + isTransactional +
                ", createdAt=" + createdAt +
                '}';
    }
}
