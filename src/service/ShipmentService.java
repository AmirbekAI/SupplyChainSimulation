package service;

import domain.model.Location;
import domain.model.OrderItem;
import domain.model.Shipment;
import domain.model.ShipmentStatus;
import exceptions.ShipmentNotFoundException;
import repository.InMemoryRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ShipmentService {
    private InMemoryRepository<Shipment> storage;
    private final InventoryService inventoryService;

    public ShipmentService(InMemoryRepository<Shipment> storage,
                           InventoryService inventoryService) {
        this.storage = storage;
        this.inventoryService = inventoryService;
    }

    public Shipment createShipment(UUID orderId,
                                   UUID customerId,
                                   UUID warehouseId,
                                   Location destination,
                                   List<OrderItem> shipmentItems) {

        Shipment shipment =
                new Shipment(
                        orderId,
                        warehouseId,
                        customerId,
                        destination,
                        shipmentItems
                );

        storage.save(shipment.getId(), shipment);
        return shipment;
    }

    public Shipment getShipmentById(UUID id) {
        return storage.findById(id)
                .orElseThrow(() -> new ShipmentNotFoundException("Shipment: " + id.toString() + " not found"));
    }

    public List<Shipment> getShipmentsByOrder(UUID orderId) {
        return storage.findAll().stream()
                .filter(sh -> sh.getOrderId().equals(orderId))
                .toList();
    }

    public void updateShipmentStatus(UUID shipmentId, ShipmentStatus status) {
        Optional<Shipment> shipmentOpt = storage.findById(shipmentId);

        if (shipmentOpt.isEmpty()) {
            throw new ShipmentNotFoundException("Shipment: " + shipmentId.toString() + " not found");
        }

        Shipment shipment = shipmentOpt.get();
        shipment.setStatus(status);

        storage.deleteById(shipmentId);
        storage.save(shipment.getId(), shipment);

    }

    public void cancelShipment(UUID shipmentId) { updateShipmentStatus(shipmentId, ShipmentStatus.CANCELLED); }

    public void shipOrderItems(UUID shipmentId) {
        Shipment shipment = getShipmentById(shipmentId);
        List<OrderItem> orderItems = shipment.getShipmentItems();

        orderItems.forEach(orderItem -> inventoryService.deductStock(
                shipment.getWarehouseId(),
                orderItem.getProductId(),
                orderItem.getQuantity())
        );

        updateShipmentStatus(shipmentId, ShipmentStatus.SHIPPED);
    }


}
