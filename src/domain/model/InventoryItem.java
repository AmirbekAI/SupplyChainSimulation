package domain.model;

import java.util.UUID;

/**
 * Represents inventory for a specific product in a specific warehouse.
 * Tracks both available and reserved quantities.
 */
public class InventoryItem {
    private final UUID id;
    private final UUID productId;
    private final UUID warehouseId;
    private int reservedQuantity;
    private int availableQuantity;

    public InventoryItem(UUID productId, UUID warehouseId, int availableQuantity) {
        this.id = UUID.randomUUID();
        this.productId = productId;
        this.warehouseId = warehouseId;
        this.availableQuantity = availableQuantity;
        this.reservedQuantity = 0;
    }

    // Constructor for loading existing inventory items (e.g., from repository)
    public InventoryItem(UUID id, UUID productId, UUID warehouseId, int reservedQuantity, int availableQuantity) {
        this.id = id;
        this.productId = productId;
        this.warehouseId = warehouseId;
        this.reservedQuantity = reservedQuantity;
        this.availableQuantity = availableQuantity;
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public UUID getProductId() {
        return productId;
    }

    public UUID getWarehouseId() {
        return warehouseId;
    }

    public int getReservedQuantity() {
        return reservedQuantity;
    }

    public int getAvailableQuantity() {
        return availableQuantity;
    }

    /**
     * Computed property: total quantity = reserved + available
     */
    public int getTotalQuantity() {
        return reservedQuantity + availableQuantity;
    }

    // Setters
    public void setReservedQuantity(int reservedQuantity) {
        this.reservedQuantity = reservedQuantity;
    }

    public void setAvailableQuantity(int availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    /**
     * Reserve stock by moving from available to reserved.
     */
    public void reserve(int quantity) {
        if (quantity > availableQuantity) {
            throw new IllegalArgumentException("Cannot reserve " + quantity + " units. Only " + availableQuantity + " available.");
        }
        this.availableQuantity -= quantity;
        this.reservedQuantity += quantity;
    }

    /**
     * Release stock by moving from reserved back to available.
     */
    public void release(int quantity) {
        if (quantity > reservedQuantity) {
            throw new IllegalArgumentException("Cannot release " + quantity + " units. Only " + reservedQuantity + " reserved.");
        }
        this.reservedQuantity -= quantity;
        this.availableQuantity += quantity;
    }

    /**
     * Deduct stock from reserved (used when shipping).
     */
    public void deduct(int quantity) {
        if (quantity > reservedQuantity) {
            throw new IllegalArgumentException("Cannot deduct " + quantity + " units. Only " + reservedQuantity + " reserved.");
        }
        this.reservedQuantity -= quantity;
    }

    /**
     * Add stock to available (used when restocking).
     */
    public void restock(int quantity) {
        this.availableQuantity += quantity;
    }

    @Override
    public String toString() {
        return "InventoryItem{" +
                "id='" + id + '\'' +
                ", productId='" + productId + '\'' +
                ", warehouseId='" + warehouseId + '\'' +
                ", reservedQuantity=" + reservedQuantity +
                ", availableQuantity=" + availableQuantity +
                ", totalQuantity=" + getTotalQuantity() +
                '}';
    }
}
