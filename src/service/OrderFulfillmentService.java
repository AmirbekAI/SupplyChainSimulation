package service;

import domain.model.*;
import exceptions.NoAvailableWarehouseException;
import exceptions.OrderNotFoundException;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class OrderFulfillmentService {
    private final OrderService orderService;
    private final WarehouseService warehouseService;
    private final ShipmentService shipmentService;
    private final InventoryService inventoryService;
    private final CustomerService customerService;

    public OrderFulfillmentService(OrderService orderService,
                                   WarehouseService warehouseService,
                                   ShipmentService shipmentService,
                                   InventoryService inventoryService,
                                   CustomerService customerService) {
        Objects.requireNonNull(orderService, "orderService cannot be null");
        Objects.requireNonNull(warehouseService, "warehouseService cannot be null");
        Objects.requireNonNull(shipmentService, "shipmentService cannot be null");
        Objects.requireNonNull(inventoryService, "inventoryService cannot be null");

        this.orderService = orderService;
        this.warehouseService = warehouseService;
        this.shipmentService = shipmentService;
        this.inventoryService = inventoryService;
        this.customerService = customerService;
    }

    public boolean fulfillOrder(UUID orderId) {
        Objects.requireNonNull(orderId, "orderId cannot be null");

        try {
            Order order = orderService.getOrderById(orderId);
            Location customerLocation =
                    customerService.getCustomerLocation(order.getCustomerId());
            if (order.isTransactional()) {
                Warehouse warehouse =
                        warehouseService.findSingleOptimalWarehouseForOrderItems(
                                order.getOrderItems(), customerLocation);

                shipmentService.createShipment(
                        orderId,
                        order.getCustomerId(),
                        warehouse.getId(),
                        customerLocation,
                        order.getOrderItems()
                );

                inventoryService.reserveStock(
                        warehouse.getId(),
                        order.getOrderItems()
                );
            } else {
                Map<Warehouse, List<OrderItem>> warehouseToOrderItems =
                        warehouseService.findWarehousesForOrderItems(
                                order.getOrderItems(), customerLocation);

                for (Map.Entry<Warehouse, List<OrderItem>> entry: warehouseToOrderItems.entrySet()) {
                    Warehouse warehouse = entry.getKey();
                    List<OrderItem> items = entry.getValue();
                    shipmentService.createShipment(
                            orderId,
                            order.getCustomerId(),
                            warehouse.getId(),
                            customerLocation,
                            items);
                }
            }
            orderService.updateStatus(order.getId(), OrderStatus.ALLOCATED);
            return true;
        } catch (NoAvailableWarehouseException | OrderNotFoundException e) {
            return false;
        }
    }

    public void shipOrder(UUID orderId) {
        Objects.requireNonNull(orderId, "orderId cannot be null");

        List<Shipment> shipments = shipmentService.getShipmentsByOrder(orderId);
        shipments.forEach(sp -> shipmentService.shipOrderItems(sp.getId()));

        orderService.updateStatus(orderId, OrderStatus.SHIPPED);
    }

}
