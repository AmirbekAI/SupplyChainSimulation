package domain.model;

import java.util.UUID;

/**
 * Represents an item in an order (product + quantity).
 * This is a value object, not an entity (no ID needed).
 */
public class OrderItem {
    private final UUID productId;
    private final int quantity;

    public OrderItem(UUID productId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
        this.productId = productId;
        this.quantity = quantity;
    }

    // Getters
    public UUID getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }

    @Override
    public String toString() {
        return "OrderItem{" +
                "productId='" + productId + '\'' +
                ", quantity=" + quantity +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderItem orderItem = (OrderItem) o;
        return quantity == orderItem.quantity && productId.equals(orderItem.productId);
    }

    @Override
    public int hashCode() {
        int result = productId.hashCode();
        result = 31 * result + quantity;
        return result;
    }
}
