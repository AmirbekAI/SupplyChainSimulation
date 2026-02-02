package test.service;

import domain.model.InventoryItem;
import domain.model.OrderItem;
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
    private InMemoryRepository<InventoryItem> repository;

    private UUID warehouseId;
    private UUID productId;

    @BeforeEach
    void setUp() {
        repository = new InMemoryRepository<InventoryItem>();
        inventoryService = new InventoryService(repository);

        warehouseId = UUID.randomUUID();
        productId = UUID.randomUUID();

        InventoryItem inventoryItem = new InventoryItem(
                productId,
                warehouseId,
                100
        );

        repository.save(UUID.randomUUID(), inventoryItem);
    }

    @Test
    void reserveStock_reducesAvailableQuantity() {
        inventoryService.reserveStock(warehouseId, productId, 20);

        int available = inventoryService.getAvailableQuantity(warehouseId, productId);

        assertEquals(80, available);
    }

    @Test
    void reserveStock_throwsInsufficientStockException_whenNoEnoughAvailableStock() {
        assertThrows(
                InsufficientStockException.class,
                () -> inventoryService.reserveStock(warehouseId, productId, 10000)
        );
    }

    @Test
    void reserveStock_throwsInventoryItemNotFoundException_whenInventoryItemNotFound() {
        UUID randomProductId = UUID.randomUUID();
        assertThrows(
                InventoryItemNotFoundException.class,
                () -> inventoryService.reserveStock(randomProductId, warehouseId, 10)
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

        repository.save(invItem1.getId(), invItem1);
        repository.save(invItem2.getId(), invItem2);

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

        repository.save(invItem1.getId(), invItem1);
        repository.save(invItem2.getId(), invItem2);

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
