package service;

import domain.model.InventoryItem;
import domain.model.OrderItem;
import domain.model.Product;
import domain.model.Warehouse;
import exceptions.InsufficientStockException;
import exceptions.InventoryItemNotFoundException;
import exceptions.ProductNotFoundException;
import exceptions.WarehouseNotFoundException;
import repository.InMemoryRepository;
import repository.InMemoryRepositoryInterface;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class InventoryService {
    private final InMemoryRepository<InventoryItem> storage;
    private final InMemoryRepository<Product> productRepository;
    private final InMemoryRepository<Warehouse> warehouseRepository;

    public InventoryService(InMemoryRepository<InventoryItem> storage, 
                           InMemoryRepository<Product> productRepository,
                           InMemoryRepository<Warehouse> warehouseRepository) {
        this.storage = storage;
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
    }
    private Optional<InventoryItem> findInventoryItem(UUID warehouseId, UUID productId) {
        List<InventoryItem> items = storage.findAll();
        return items.stream()
                .filter(it -> it.getProductId().equals(productId) && it.getWarehouseId().equals(warehouseId))
                .findFirst();
    }

    /**
     * Creates a new inventory item for a product in a warehouse.
     * Validates that both product and warehouse exist before creating.
     * 
     * @param warehouseId The warehouse ID
     * @param productId The product ID
     * @param initialQuantity The initial available quantity
     * @return The created inventory item
     * @throws IllegalArgumentException if inventory item already exists
     */
    public InventoryItem addInventoryItem(UUID warehouseId, UUID productId, int initialQuantity) {
        // Validate product exists
        if (productRepository.findById(productId).isEmpty()) {
            throw new ProductNotFoundException("Product " + productId + " not found");
        }
        
        // Validate warehouse exists
        if (warehouseRepository.findById(warehouseId).isEmpty()) {
            throw new WarehouseNotFoundException("Warehouse " + warehouseId + " not found");
        }
        
        // Check if inventory item already exists
        Optional<InventoryItem> existingItem = findInventoryItem(warehouseId, productId);
        if (existingItem.isPresent()) {
            throw new IllegalArgumentException(
                "Inventory item already exists for product " + productId + 
                " in warehouse " + warehouseId + ". Use restockWarehouse() to add more quantity."
            );
        }
        
        // Validate quantity
        if (initialQuantity < 0) {
            throw new IllegalArgumentException("Initial quantity cannot be negative: " + initialQuantity);
        }
        
        // Create and save new inventory item
        InventoryItem newItem = new InventoryItem(productId, warehouseId, initialQuantity);
        storage.save(newItem.getId(), newItem);
        
        return newItem;
    }


    public void reserveStock(UUID warehouseId, List<OrderItem> orderItems) {

        for (OrderItem item: orderItems) {
            UUID productId = item.getProductId();
            int quantity = item.getQuantity();
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
