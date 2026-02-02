package domain.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a shipment from a warehouse to a customer.
 * Each shipment is associated with one order and one warehouse.
 */
public class Shipment {
    private final String id;
    private final String orderId;
    private final String warehouseId;
    private final String customerId;
    private final List<OrderItem> shipmentItems;
    private ShipmentStatus status;
    private final LocalDateTime createdAt;
    private LocalDateTime shippedAt;

    public Shipment(String orderId, String warehouseId, String customerId, List<OrderItem> shipmentItems) {
        this.id = UUID.randomUUID().toString();
        this.orderId = orderId;
        this.warehouseId = warehouseId;
        this.customerId = customerId;
        this.shipmentItems = new ArrayList<>(shipmentItems); // Defensive copy
        this.status = ShipmentStatus.CREATED;
        this.createdAt = LocalDateTime.now();
        this.shippedAt = null;
    }

    // Constructor for loading existing shipments (e.g., from repository)
    public Shipment(String id, String orderId, String warehouseId, String customerId, 
                    List<OrderItem> shipmentItems, ShipmentStatus status, 
                    LocalDateTime createdAt, LocalDateTime shippedAt) {
        this.id = id;
        this.orderId = orderId;
        this.warehouseId = warehouseId;
        this.customerId = customerId;
        this.shipmentItems = new ArrayList<>(shipmentItems);
        this.status = status;
        this.createdAt = createdAt;
        this.shippedAt = shippedAt;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getWarehouseId() {
        return warehouseId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public List<OrderItem> getShipmentItems() {
        return new ArrayList<>(shipmentItems); // Return defensive copy
    }

    public ShipmentStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getShippedAt() {
        return shippedAt;
    }

    // Setters
    public void setStatus(ShipmentStatus status) {
        this.status = status;
    }

    public void setShippedAt(LocalDateTime shippedAt) {
        this.shippedAt = shippedAt;
    }

    @Override
    public String toString() {
        return "Shipment{" +
                "id='" + id + '\'' +
                ", orderId='" + orderId + '\'' +
                ", warehouseId='" + warehouseId + '\'' +
                ", customerId='" + customerId + '\'' +
                ", shipmentItems=" + shipmentItems +
                ", status=" + status +
                ", createdAt=" + createdAt +
                ", shippedAt=" + shippedAt +
                '}';
    }
}
