public OrderFulfillmentServiceTest{
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
    private InMemoryReposiory<Customer> customerStorage;
    private InMemoryRepository<Order> orderStorage;
    private InMemoryRepository<InventoryItem> inventoryItemStorage;

    @BeforeEach
    void setUp() {
        warehouseStorage = new InMemoryRepository<Warehouse>();
        shipmentStorage = new InMemoryRepository<Shipment>();
        productStorage = new InMemoryRepository<Product>();
        customerStorage = new InMemoryRepository<Customer>();
        orderStorage = new InMemoryRepository<Order>();
        inventoryItemStorage = new InMemoryRepository<InventoryItem>();

        inventoryService = new InventoryService(inventoryItemStorage, productStorage, warehouseStorage);
        shipmentService = new ShipmentService(shipmentStorage, inventoryService);
        warehouseService = new WarehosueService(warehouseStorage, inventoryService);
        productService = new ProductService(productStorage);
        customerService = new CustomerService(customerStorage);
        orderService = new OrderService(orderStorage);
        orderFulfillmentService = new OrderFulfillmentService(orderService, warehouseService, shipmentService, inventoryService, customerService);
    }
}