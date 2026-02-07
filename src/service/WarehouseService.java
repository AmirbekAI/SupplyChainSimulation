package service;

import domain.model.InventoryItem;
import domain.model.Location;
import domain.model.OrderItem;
import domain.model.Warehouse;
import exceptions.NoAvailableWarehouseException;
import repository.InMemoryRepository;

import java.util.*;

public class WarehouseService {
    private final InMemoryRepository<Warehouse> storage;
    private final InventoryService inventoryService;

    public WarehouseService(InMemoryRepository<Warehouse> inMemoryRepository, InventoryService inventoryService) {
        this.storage = inMemoryRepository;
        this.inventoryService = inventoryService;
    }

    public void createWarehouse(String name, Location location, int maxCapacity) {
        Warehouse warehouse = new Warehouse(name, location, maxCapacity);
        storage.save(warehouse.getId(), warehouse);
    }

    public Optional<Warehouse> getWarehouseById(UUID id) {
        return storage.findById(id);
    }

    public List<Warehouse> getAllWarehouses() {
        return storage.findAll();
    }

    public void deleteWarehouse(UUID id) {
        storage.deleteById(id);
    }

    private List<Warehouse> findAvailableWarehousesForOrderItems(List<OrderItem> orderItems, Location location) {
        return getAllWarehouses().stream()
                .filter(wh -> inventoryService.canFulfillItems(wh.getId(), orderItems))
                .sorted(Comparator.comparingDouble(wh -> wh.distanceTo(location)))
                .toList();
    }

    public Warehouse findSingleOptimalWarehouseForOrderItems(List<OrderItem> orderItems, Location location) {
        List<Warehouse> warehouses = findAvailableWarehousesForOrderItems(orderItems, location);

        if (warehouses.isEmpty()) {
            throw new NoAvailableWarehouseException("No single available warehouse found for orderItems (Transactional)");
        }

        return warehouses.getFirst();
    }

    public Map<Warehouse, List<OrderItem>> findWarehousesForOrderItems(List<OrderItem> orderItems, Location location) {
        Objects.requireNonNull(orderItems, "orderItems cannot be null");
        Objects.requireNonNull(location, "location cannot be null");

        if (orderItems.isEmpty()) {
            throw new IllegalArgumentException("orderItems cannot be empty");
        }

        Map<Warehouse, List<OrderItem>> warehouseToOrderItems = new HashMap<>();
        for (OrderItem orderItem: orderItems) {
            List<Warehouse> warehouses = findAvailableWarehousesForOrderItems(Collections.singletonList(orderItem), location);

            if (!warehouses.isEmpty()) {
                warehouseToOrderItems.computeIfAbsent(warehouses.getFirst(), k -> new ArrayList<>()).add(orderItem);
            }
        }

        if (warehouseToOrderItems.isEmpty()) {
            throw new NoAvailableWarehouseException("No warehouses were found for orderItems (Non Transactional)");
        }
        return warehouseToOrderItems;
    }
}
