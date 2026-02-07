package domain.model;

/**
 * Represents the status of a shipment in the supply chain system.
 */
public enum ShipmentStatus {
    CREATED,    // Shipment has been created but not yet shipped
    SHIPPED,    // Shipment has been shipped
    DELIVERED,  // Shipment has been delivered to customer
    CANCELLED, // Shipment has been cancelled
    FAILED      // Shipment failed
}
