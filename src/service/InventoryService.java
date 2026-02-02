package service;

import domain.model.InventoryItem;
import domain.model.OrderItem;
import exceptions.InsufficientStockException;
import exceptions.InventoryItemNotFoundException;
import repository.InMemoryRepository;
import repository.InMemoryRepositoryInterface;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class InventoryService {
    private final InMemoryRepository<InventoryItem> storage;

    public InventoryService(InMemoryRepository<InventoryItem> storage) {
        this.storage = storage;
    }
    private Optional<InventoryItem> findInventoryItem(UUID warehouseId, UUID productId) {
        List<InventoryItem> items = storage.findAll();
        return items.stream()
                .filter(it -> it.getProductId().equals(productId) && it.getWarehouseId().equals(warehouseId))
                .findFirst();
    }

    public void reserveStock(UUID warehouseId, UUID productId, int quantity) {
        Optional<InventoryItem> inventoryItemOpt = findInventoryItem(warehouseId, productId);

        if (inventoryItemOpt.isEmpty()) {
            throw new InventoryItemNotFoundException(warehouseId, productId);
        }

        InventoryItem inventoryItem = inventoryItemOpt.get();
        if (inventoryItem.getAvailableQuantity() < quantity) {
            throw new InsufficientStockException(productId, warehouseId, quantity, inventoryItem.getAvailableQuantity());
        }

        inventoryItem.reserve(quantity);
    }

    public void releaseStock(UUID warehouseId, UUID productId, int quantity) {
        Optional<InventoryItem> inventoryItemOpt= findInventoryItem(warehouseId, productId);

        if (inventoryItemOpt.isEmpty()) {
            throw new InventoryItemNotFoundException(warehouseId, productId);
        }
        InventoryItem inventoryItem = inventoryItemOpt.get();

        if (quantity > inventoryItem.getReservedQuantity()) {
            throw new IllegalArgumentException("Release quantity exceeding the reserved quantity. Product=" + productId + ", Warehouse=" + warehouseId +
                    "Release quantity= " + String.valueOf(quantity) + "Reserved quantity=" + String.valueOf(inventoryItem.getReservedQuantity())
                    );
        }

        inventoryItem.release(quantity);
    }

    public void restockWarehouse(UUID warehouseId, UUID productId, int quantity) {
        Optional<InventoryItem> inventoryItemOpt= findInventoryItem(warehouseId, productId);

        if (inventoryItemOpt.isEmpty()) {
            throw new InventoryItemNotFoundException(warehouseId, productId);
        }
        InventoryItem inventoryItem = inventoryItemOpt.get();

        inventoryItem.restock(quantity);
    }

    public void deductStock(UUID warehouseId, UUID productId, int quantity) {
        Optional<InventoryItem> inventoryItemOpt= findInventoryItem(warehouseId, productId);

        if (inventoryItemOpt.isEmpty()) {
            throw new InventoryItemNotFoundException(warehouseId, productId);
        }
        InventoryItem inventoryItem = inventoryItemOpt.get();
        inventoryItem.deduct(quantity);
    }

    public int getAvailableQuantity(UUID warehouseId, UUID productId) {
        Optional<InventoryItem> inventoryItemOpt= findInventoryItem(warehouseId, productId);

        if (inventoryItemOpt.isEmpty()) {
            throw new InventoryItemNotFoundException(warehouseId, productId);
        }

        return inventoryItemOpt.get().getAvailableQuantity();
    }

    public boolean canFulfillItems(UUID warehouseId, List<OrderItem> items) {
        if (items == null) { throw new IllegalArgumentException("Inventory item list cannot be null"); }
        if (items.isEmpty()) { throw new IllegalArgumentException("Inventory items list cannot be empty"); }

        for (OrderItem item: items) {
            Optional<InventoryItem> inventoryItemOpt = findInventoryItem(warehouseId, item.getProductId());
            if (inventoryItemOpt.isEmpty()) { return false; }
            if (inventoryItemOpt.get().getAvailableQuantity() <= item.getQuantity()) { return false; }
        }
        return true;
    }
}
