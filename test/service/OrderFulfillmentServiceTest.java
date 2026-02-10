import domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import repository.InMemoryRepository;
import service.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OrderFulfillmentServiceTest {
    private InventoryService inventoryService;
    private ShipmentService shipmentService;
    private WarehouseService warehouseService;
    private ProductService productService;
    private CustomerService customerService;
    private OrderService orderService;
    private OrderFulfillmentService orderFulfillmentService;

    private InMemoryRepository<Warehouse> warehouseStorage;
    private InMemoryRepository<Shipment> shipmentStorage;
    private InMemoryRepository<Product> productStorage;
    private InMemoryRepository<Customer> customerStorage;
    private InMemoryRepository<Order> orderStorage;
    private InMemoryRepository<InventoryItem> inventoryItemStorage;

    @BeforeEach
    void setUp() {
        warehouseStorage = new InMemoryRepository<>();
        shipmentStorage = new InMemoryRepository<>();
        productStorage = new InMemoryRepository<>();
        customerStorage = new InMemoryRepository<>();
        orderStorage = new InMemoryRepository<>();
        inventoryItemStorage = new InMemoryRepository<>();

        inventoryService = new InventoryService(inventoryItemStorage, productStorage, warehouseStorage);
        shipmentService = new ShipmentService(shipmentStorage, inventoryService);
        warehouseService = new WarehouseService(warehouseStorage, inventoryService);
        productService = new ProductService(productStorage);
        customerService = new CustomerService(customerStorage);
        orderService = new OrderService(orderStorage);
        orderFulfillmentService = new OrderFulfillmentService(orderService, warehouseService, shipmentService, inventoryService, customerService);

        // Create products
        productService.createNewProduct("Product1", ProductType.ELECTRONIC, "N/A");
        productService.createNewProduct("Product2", ProductType.ELECTRONIC, "N/A");
        productService.createNewProduct("Product3", ProductType.ELECTRONIC, "N/A");
        productService.createNewProduct("Product4", ProductType.ELECTRONIC, "N/A");

        // Create warehouses with unique names
        warehouseService.createWarehouse("Warehouse1", new Location("Location1", 20, 20), 200);
        warehouseService.createWarehouse("Warehouse2", new Location("Location2", 100, 100), 200);
        warehouseService.createWarehouse("Warehouse3", new Location("Location3", 700, 700), 200);

        List<Warehouse> warehouses = warehouseService.getAllWarehouses();
        List<Product> products = productService.getAllProducts();

        // Add inventory items with quantities
        inventoryService.addInventoryItem(warehouseService.getWarehouseByName("Warehouse1").getId(), productService.getProductByName("Product1").getId(), 100);
        inventoryService.addInventoryItem(warehouseService.getWarehouseByName("Warehouse1").getId(), productService.getProductByName("Product2").getId(), 50);

        inventoryService.addInventoryItem(warehouseService.getWarehouseByName("Warehouse2").getId(), productService.getProductByName("Product2").getId(), 75);
        inventoryService.addInventoryItem(warehouseService.getWarehouseByName("Warehouse2").getId(), productService.getProductByName("Product3").getId(), 50);

        inventoryService.addInventoryItem(warehouseService.getWarehouseByName("Warehouse3").getId(), productService.getProductByName("Product1").getId(), 200);
        inventoryService.addInventoryItem(warehouseService.getWarehouseByName("Warehouse3").getId(), productService.getProductByName("Product2").getId(), 200);
        inventoryService.addInventoryItem(warehouseService.getWarehouseByName("Warehouse3").getId(), productService.getProductByName("Product3").getId(), 200);
        inventoryService.addInventoryItem(warehouseService.getWarehouseByName("Warehouse3").getId(), productService.getProductByName("Product4").getId(), 200);

        customerService.createCustomer("Google", new Location("US", 10, 10), "google@gmail.com");
        customerService.createCustomer("NVIDIA", new Location("US", 90, 90), "nvidia@gmail.com");
        customerService.createCustomer("Tesla", new Location("US", 300, 300), "tesla@gmail.com");
    }

    @Test
    public void testCreateAndFulfillNonTransactionalOrder() {
        List<OrderItem> orderItems = new ArrayList<>();

        OrderItem orderItem1 = new OrderItem(productService.getProductByName("Product1").getId(), 40);
        OrderItem orderItem2 = new OrderItem(productService.getProductByName("Product2").getId(), 500);
        orderItems.add(orderItem1);
        orderItems.add(orderItem2);

        Order order = orderService.createOrder(customerService.getCustomerByName("Google").getId(), orderItems, false);
        boolean result = orderFulfillmentService.fulfillOrder(order.getId());
        
        // Assertions
        assert(result);
        Order updatedOrder = orderService.getOrderById(order.getId());
        assert(updatedOrder.getStatus() == OrderStatus.ALLOCATED);
        
        // Verify shipments were created
        List<Shipment> shipments = shipmentService.getShipmentsByOrder(order.getId());
        assert(!shipments.isEmpty());
    }
    
    @Test
    public void testCreateAndFulfillTransactionalOrder() {
        // Create order items that can be fulfilled from a single warehouse (Warehouse3 has all products)
        List<OrderItem> orderItems = new ArrayList<>();
        orderItems.add(new OrderItem(productService.getProductByName("Product1").getId(), 50));
        orderItems.add(new OrderItem(productService.getProductByName("Product2").getId(), 40));
        orderItems.add(new OrderItem(productService.getProductByName("Product3").getId(), 30));
        
        Order order = orderService.createOrder(
            customerService.getCustomerByName("Tesla").getId(), 
            orderItems, 
            true  // Transactional
        );
        
        boolean result = orderFulfillmentService.fulfillOrder(order.getId());
        
        // Assertions
        assert(result);
        Order updatedOrder = orderService.getOrderById(order.getId());
        assert(updatedOrder.getStatus() == OrderStatus.ALLOCATED);
        
        // Verify only ONE shipment was created (transactional)
        List<Shipment> shipments = shipmentService.getShipmentsByOrder(order.getId());
        assert(shipments.size() == 1);
    }
    
    @Test
    public void testShipOrder() {
        // Create and fulfill order
        List<OrderItem> orderItems = new ArrayList<>();
        UUID product1Id = productService.getProductByName("Product1").getId();
        orderItems.add(new OrderItem(product1Id, 20));
        
        System.out.println("Product1 ID: " + product1Id);
        System.out.println("Warehouse1 has Product1: " + warehouseService.getWarehouseByName("Warehouse1").getId());
        System.out.println("Warehouse3 has Product1: " + warehouseService.getWarehouseByName("Warehouse3").getId());
        
        Order order = orderService.createOrder(
            customerService.getCustomerByName("Google").getId(), 
            orderItems, 
            false
        );
        
        boolean fulfilled = orderFulfillmentService.fulfillOrder(order.getId());
        assert(fulfilled);
        
        // Get the shipment to find which warehouse was used
        List<Shipment> shipmentsBeforeShipping = shipmentService.getShipmentsByOrder(order.getId());
        assert(!shipmentsBeforeShipping.isEmpty());
        
        System.out.println("Shipment warehouse ID: " + shipmentsBeforeShipping.get(0).getWarehouseId());
        
        // Ship the order
        orderFulfillmentService.shipOrder(order.getId());
        
        // Verify order status updated
        Order shippedOrder = orderService.getOrderById(order.getId());
        assert(shippedOrder.getStatus() == OrderStatus.SHIPPED);
        
        // Verify shipment status updated
        List<Shipment> shipments = shipmentService.getShipmentsByOrder(order.getId());
        shipments.forEach(shipment -> {
            assert(shipment.getStatus() == ShipmentStatus.SHIPPED);
        });
        
        System.out.println("✅ Ship order test passed!");
    }
    
    @Test
    public void testFulfillOrderWithInsufficientInventory() {
        // Try to order more than available
        List<OrderItem> orderItems = new ArrayList<>();
        orderItems.add(new OrderItem(productService.getProductByName("Product1").getId(), 10000));
        
        Order order = orderService.createOrder(
            customerService.getCustomerByName("Google").getId(), 
            orderItems, 
            false
        );
        
        boolean result = orderFulfillmentService.fulfillOrder(order.getId());
        
        // Should return false when can't fulfill
        assert(!result);
        
        // Order status should still be CREATED
        Order failedOrder = orderService.getOrderById(order.getId());
        assert(failedOrder.getStatus() == OrderStatus.CREATED);
    }
    
    @Test
    public void testMultipleShipmentsForNonTransactionalOrder() {
        // Create order with products from different warehouses
        List<OrderItem> orderItems = new ArrayList<>();
        orderItems.add(new OrderItem(productService.getProductByName("Product1").getId(), 50));
        orderItems.add(new OrderItem(productService.getProductByName("Product3").getId(), 30));
        
        Order order = orderService.createOrder(
            customerService.getCustomerByName("NVIDIA").getId(), 
            orderItems, 
            false  // Non-transactional - can split across warehouses
        );
        
        boolean result = orderFulfillmentService.fulfillOrder(order.getId());
        
        assert(result);
        
        // Verify multiple shipments may be created
        List<Shipment> shipments = shipmentService.getShipmentsByOrder(order.getId());
        assert(!shipments.isEmpty());
        
        // Verify all shipments are for the correct order
        shipments.forEach(shipment -> {
            assert(shipment.getOrderId().equals(order.getId()));
        });
    }
    
    @Test
    public void testOptimalWarehouseSelection() {
        // Setup: Product1 is available in both Warehouse1 (20,20) and Warehouse3 (700,700)
        // Google customer is at (10,10), so Warehouse1 should be selected (closer)
        
        List<OrderItem> orderItems = new ArrayList<>();
        orderItems.add(new OrderItem(productService.getProductByName("Product1").getId(), 30));
        
        Order order = orderService.createOrder(
            customerService.getCustomerByName("Google").getId(),  // Location: (10, 10)
            orderItems,
            true  // Transactional - must use single warehouse
        );
        
        boolean result = orderFulfillmentService.fulfillOrder(order.getId());
        assert(result);
        
        // Get the shipment and verify it's from the closest warehouse
        List<Shipment> shipments = shipmentService.getShipmentsByOrder(order.getId());
        assert(shipments.size() == 1);  // Transactional order = 1 shipment
        
        Shipment shipment = shipments.get(0);
        UUID selectedWarehouseId = shipment.getWarehouseId();
        
        // Verify it's Warehouse1 (the closest one)
        UUID warehouse1Id = warehouseService.getWarehouseByName("Warehouse1").getId();
        
        System.out.println("Expected warehouse (Warehouse1): " + warehouse1Id);
        System.out.println("Selected warehouse: " + selectedWarehouseId);
        System.out.println("Are they equal? " + selectedWarehouseId.equals(warehouse1Id));
        
        assert(selectedWarehouseId.equals(warehouse1Id));
        
        System.out.println("✅ Optimal warehouse selection test passed! Selected closest warehouse.");
    }
    
    @Test
    public void testOptimalWarehouseSelectionForDistantCustomer() {
        // Tesla customer is at (300,300), closest to Warehouse2 (100,100)
        // Product3 is available in both Warehouse2 and Warehouse3
        
        List<OrderItem> orderItems = new ArrayList<>();
        orderItems.add(new OrderItem(productService.getProductByName("Product3").getId(), 20));
        
        Order order = orderService.createOrder(
            customerService.getCustomerByName("Tesla").getId(),  // Location: (300, 300)
            orderItems,
            true  // Transactional
        );
        
        boolean result = orderFulfillmentService.fulfillOrder(order.getId());
        assert(result);
        
        List<Shipment> shipments = shipmentService.getShipmentsByOrder(order.getId());
        assert(shipments.size() == 1);
        
        Shipment shipment = shipments.get(0);
        UUID selectedWarehouseId = shipment.getWarehouseId();
        
        // Calculate distances to verify
        Location teslaLocation = customerService.getCustomerLocation(customerService.getCustomerByName("Tesla").getId());
        Warehouse warehouse2 = warehouseService.getWarehouseByName("Warehouse2");
        Warehouse warehouse3 = warehouseService.getWarehouseByName("Warehouse3");
        
        double distanceToW2 = warehouse2.distanceTo(teslaLocation);
        double distanceToW3 = warehouse3.distanceTo(teslaLocation);
        
        System.out.println("Distance to Warehouse2: " + distanceToW2);
        System.out.println("Distance to Warehouse3: " + distanceToW3);
        
        // Warehouse2 should be closer
        assert(distanceToW2 < distanceToW3);
        
        // Verify Warehouse2 was selected
        assertEquals(selectedWarehouseId, warehouse2.getId(), "Selected warehouse does not match the expected warehouse");

        System.out.println("✅ Optimal warehouse selection for distant customer test passed!");
    }
}