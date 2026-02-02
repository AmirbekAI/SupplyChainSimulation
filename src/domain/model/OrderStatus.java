package domain.model;

/**
 * Represents the status of an order in the supply chain system.
 */
public enum OrderStatus {
    CREATED,    // Order has been created but not yet allocated
    ALLOCATED,  // Inventory has been reserved and shipments created
    SHIPPED,    // All shipments have been shipped
    CANCELLED   // Order has been cancelled
}
