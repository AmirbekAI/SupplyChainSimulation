package test.service;

import domain.model.InventoryItem;
import domain.model.OrderItem;
import domain.model.Product;
import domain.model.ProductType;
import domain.model.Warehouse;
import domain.model.Location;
import exceptions.InsufficientStockException;
import exceptions.InventoryItemNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import repository.InMemoryRepository;
import service.InventoryService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class InventoryServiceTest {

    private InventoryService inventoryService;
    private InMemoryRepository<InventoryItem> inventoryRepository;
    private InMemoryRepository<Product> productRepository;
    private InMemoryRepository<Warehouse> warehouseRepository;

    private UUID warehouseId;
    private UUID productId;

    @BeforeEach
    void setUp() {
        inventoryRepository = new InMemoryRepository<>();
        productRepository = new InMemoryRepository<>();
        warehouseRepository = new InMemoryRepository<>();
        
        inventoryService = new InventoryService(inventoryRepository, productRepository, warehouseRepository);

        warehouseId = UUID.randomUUID();
        productId = UUID.randomUUID();
        
        // Create mock product and warehouse
        Product product = new Product(productId, "Test Product", ProductType.ELECTRONIC, "Test");
        Warehouse warehouse = new Warehouse(warehouseId, "Test Warehouse", new Location("Test", 0, 0), 1000);
        
        productRepository.save(productId, product);
        warehouseRepository.save(warehouseId, warehouse);

        InventoryItem inventoryItem = new InventoryItem(
                productId,
                warehouseId,
                100
        );

        inventoryRepository.save(inventoryItem.getId(), inventoryItem);
    }

    @Test
    void reserveStock_reducesAvailableQuantity() {
        List<OrderItem> orderItems = List.of(new OrderItem(productId, 20));
        inventoryService.reserveStock(warehouseId, orderItems);

        int available = inventoryService.getAvailableQuantity(warehouseId, productId);

        assertEquals(80, available);
    }

    @Test
    void reserveStock_throwsInsufficientStockException_whenNoEnoughAvailableStock() {
        List<OrderItem> orderItems = List.of(new OrderItem(productId, 10000));
        assertThrows(
                InsufficientStockException.class,
                () -> inventoryService.reserveStock(warehouseId, orderItems)
        );
    }

    @Test
    void reserveStock_throwsInventoryItemNotFoundException_whenInventoryItemNotFound() {
        UUID randomProductId = UUID.randomUUID();
        UUID randomWarehouseId = UUID.randomUUID();
        List<OrderItem> orderItems = List.of(new OrderItem(randomProductId, 10));
        assertThrows(
                InventoryItemNotFoundException.class,
                () -> inventoryService.reserveStock(randomWarehouseId, orderItems)
        );
    }

    @Test
    void getAvaialbleQuantity_returnsTheTotalNumberOfAProduct() {
        int available = inventoryService.getAvailableQuantity(warehouseId, productId);
        assertEquals(100, available);
    }

    @Test
    void canFulfillItems_returnsTrueIfCanFulfillItems() {
        UUID randomProductId1 = UUID.randomUUID();
        UUID randomProductId2 = UUID.randomUUID();
        UUID randomWarehouseId = UUID.randomUUID();

        InventoryItem invItem1 = new InventoryItem(randomProductId1, randomWarehouseId, 10);
        InventoryItem invItem2 = new InventoryItem(randomProductId2, randomWarehouseId, 30);

        inventoryRepository.save(invItem1.getId(), invItem1);
        inventoryRepository.save(invItem2.getId(), invItem2);

        OrderItem invItemForRequest1 = new OrderItem(randomProductId1, 5);
        OrderItem invItemForRequest2 = new OrderItem(randomProductId2, 20);

        List<OrderItem> orderItems = new ArrayList<>();
        orderItems.add(invItemForRequest1);
        orderItems.add(invItemForRequest2);

        boolean answer = inventoryService.canFulfillItems(randomWarehouseId, orderItems);

        assertEquals(true, answer);
    }

    @Test
    void canFulfillItems_returnsFalseIfCantFulfillItems() {
        UUID randomProductId1 = UUID.randomUUID();
        UUID randomProductId2 = UUID.randomUUID();
        UUID randomWarehouseId = UUID.randomUUID();

        InventoryItem invItem1 = new InventoryItem(randomProductId1, randomWarehouseId, 10);
        InventoryItem invItem2 = new InventoryItem(randomProductId2, randomWarehouseId, 30);

        inventoryRepository.save(invItem1.getId(), invItem1);
        inventoryRepository.save(invItem2.getId(), invItem2);

        OrderItem invItemForRequest1 = new OrderItem(randomProductId1, 20);
        OrderItem invItemForRequest2 = new OrderItem(randomProductId2, 20);

        List<OrderItem> orderItems = new ArrayList<>();
        orderItems.add(invItemForRequest1);
        orderItems.add(invItemForRequest2);

        boolean answer = inventoryService.canFulfillItems(randomWarehouseId, orderItems);

        assertEquals(false, answer);
    }

    @Test
    void canFulfillItems_throwsIllegalArgumentExceptionIfListIsEmpty() {
       assertThrows(
               IllegalArgumentException.class,
               () -> inventoryService.canFulfillItems(warehouseId, Collections.emptyList())
                    );
    }
}
