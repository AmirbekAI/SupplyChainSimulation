# Supply Chain System Design

## Requirements

### Functional Requirements:

**Core:**
- Create and manage products
- Create and manage warehouses
- Add and manage customers
- Create orders for customers
- Allocate orders to warehouse
- Handle both transactional and non-transactional orders
- Ship items and update the inventory

**Operations:**
- Restock warehouses
- View warehouses
- View order status
- View shipment status

### Non-functional Requirements:
- System should be modular and easy to extend
- Clear separation between business logic and data storage
- Simple error handling with meaningful exceptions

---

## High-level Architecture

```
Presentation Layer (CLI/API)
         |
Application Layer (Services)
         |
   Domain Layer (Entities)
         |
Infrastructure Layer (Repositories)
```

---

## Domain Modelling

### Entities:

**Product**
- id: UUID
- name: String
- type: String
- description: String (optional)

**Customer**
- id: UUID
- name: String
- location: String
- email: String (optional)

**Warehouse**
- id: UUID
- name: String
- location: String
- maxCapacity: int

**InventoryItem**
- id: UUID
- productId: UUID
- warehouseId: UUID
- reservedQuantity: int
- availableQuantity: int
- totalQuantity: int (computed: reserved + available)

**Order**
- id: UUID
- customerId: UUID
- orderItems: List<OrderItem>
- status: OrderStatus
- isTransactional: boolean
- createdAt: timestamp

**OrderItem**
- productId: UUID
- quantity: int

**Shipment**
- id: UUID
- orderId: UUID
- warehouseId: UUID
- customerId: UUID
- shipmentItems: List<OrderItem>
- status: ShipmentStatus
- createdAt: timestamp
- shippedAt: timestamp (optional)

### Enums:

**OrderStatus**
- CREATED
- ALLOCATED
- SHIPPED
- CANCELLED

**ShipmentStatus**
- CREATED
- SHIPPED
- DELIVERED
- FAILED

---

## Core Use Cases

### 1. Create Order
**Steps:**
1. Receive order request (customerId, List<OrderItem>, isTransactional)
2. Validate customer exists
3. Validate all products exist
4. Create order with status CREATED
5. Save the order
6. Return order

### 2. Allocate Order
**Steps:**
1. Retrieve order by ID
2. Check if order status is CREATED
3. If transactional:
   - Find single warehouse that can fulfill all items
   - Reserve all inventory in that warehouse
   - Create single shipment
4. If non-transactional:
   - For each order item, find closest warehouse with stock
   - Reserve inventory in each warehouse
   - Create multiple shipments (one per warehouse)
5. Update order status to ALLOCATED
6. Return list of shipments

### 3. Ship Order
**Steps:**
1. Retrieve shipment by ID
2. Verify shipment status is CREATED or ALLOCATED
3. Mark shipment as SHIPPED
4. Deduct reserved inventory from warehouse
5. Update shipment's shippedAt timestamp
6. If all shipments for order are shipped, update order status to SHIPPED
7. Return updated shipment

### 4. Restock Warehouse
**Steps:**
1. Receive restock request (warehouseId, productId, quantity)
2. Validate warehouse exists
3. Validate product exists
4. Find or create inventory item for warehouse + product
5. Add quantity to availableQuantity
6. Save inventory item

### 5. Cancel Order
**Steps:**
1. Retrieve order by ID
2. Check if order can be cancelled (status is CREATED or ALLOCATED)
3. If ALLOCATED, release all reserved inventory
4. Cancel all associated shipments
5. Update order status to CANCELLED

---

## Services

### ProductService
**Responsibilities:** Manage product lifecycle

**Methods:**
- `createProduct(name, type, description): Product`
- `getProductById(id): Product`
- `getAllProducts(): List<Product>`
- `deleteProduct(id): void`

---

### CustomerService
**Responsibilities:** Manage customer data

**Methods:**
- `createCustomer(name, location, email): Customer`
- `getCustomerById(id): Customer`
- `getAllCustomers(): List<Customer>`
- `deleteCustomer(id): void`

---

### WarehouseService
**Responsibilities:** Manage warehouses and allocation logic

**Methods:**
- `createWarehouse(name, location, maxCapacity): Warehouse`
- `getWarehouseById(id): Warehouse`
- `getAllWarehouses(): List<Warehouse>`
- `deleteWarehouse(id): void`
- `findWarehouseForOrder(orderItems, isTransactional): List<Warehouse>`
  - If transactional: find single warehouse with all items
  - If non-transactional: find multiple warehouses (one per item)

**Helper Methods:**
- `findSingleWarehouseForOrderItems(orderItems): Warehouse`
  - Returns warehouse that can fulfill all items or throws exception
- `findWarehousesForOrderItems(orderItems): Map<Warehouse, List<OrderItem>>`
  - Returns map of warehouses to their order items, grouping items that can be fulfilled from the same warehouse

---

### InventoryService
**Responsibilities:** Handle stock operations

**Methods:**
- `restockWarehouse(warehouseId, productId, quantity): void`
- `reserveStock(warehouseId, productId, quantity): void`
  - Moves quantity from available to reserved
- `releaseStock(warehouseId, productId, quantity): void`
  - Moves quantity from reserved back to available
- `deductStock(warehouseId, productId, quantity): void`
  - Removes quantity from reserved (used when shipping)
- `getAvailableQuantity(warehouseId, productId): int`
- `canFulfillItems(warehouseId, orderItems): boolean`
- `findInventoryItem(warehouseId, productId): InventoryItem`

---

### OrderService
**Responsibilities:** Manage order lifecycle

**Methods:**
- `createOrder(customerId, orderItems, isTransactional): Order`
- `allocateOrder(orderId): List<Shipment>`
  - Finds warehouses, reserves inventory, creates shipments
- `getOrderById(id): Order`
- `getAllOrders(): List<Order>`
- `cancelOrder(orderId): void`
- `getOrdersByCustomer(customerId): List<Order>`

---

### ShipmentService
**Responsibilities:** Manage shipments

**Methods:**
- `createShipment(orderId, warehouseId, customerId, shipmentItems): Shipment`
- `shipShipment(shipmentId): Shipment`
  - Marks as shipped, deducts inventory
- `getShipmentById(id): Shipment`
- `getShipmentsByOrder(orderId): List<Shipment>`
- `cancelShipment(shipmentId): void`
- `updateShipmentStatus(shipmentId, status): void`

---

## Data Layer (Repositories)

### Repository Interface Pattern
Each entity has a repository for data persistence:

**ProductRepository:**
- `save(product): void`
- `findById(id): Optional<Product>`
- `findAll(): List<Product>`
- `delete(id): void`

**CustomerRepository:**
- `save(customer): void`
- `findById(id): Optional<Customer>`
- `findAll(): List<Customer>`
- `delete(id): void`

**WarehouseRepository:**
- `save(warehouse): void`
- `findById(id): Optional<Warehouse>`
- `findAll(): List<Warehouse>`
- `delete(id): void`

**InventoryRepository:**
- `save(inventoryItem): void`
- `findById(id): Optional<InventoryItem>`
- `findByWarehouseAndProduct(warehouseId, productId): Optional<InventoryItem>`
- `findByWarehouse(warehouseId): List<InventoryItem>`
- `findAll(): List<InventoryItem>`

**OrderRepository:**
- `save(order): void`
- `findById(id): Optional<Order>`
- `findAll(): List<Order>`
- `findByCustomer(customerId): List<Order>`

**ShipmentRepository:**
- `save(shipment): void`
- `findById(id): Optional<Shipment>`
- `findByOrder(orderId): List<Shipment>`
- `findAll(): List<Shipment>`

### Implementation Strategy
- Start with **in-memory** implementation (HashMap-based)
- Later can extend to file-based or database storage
- All repositories share common interface pattern

---

## Error Handling

### Custom Exceptions:

- `ProductNotFoundException`: When product ID doesn't exist
- `CustomerNotFoundException`: When customer ID doesn't exist
- `WarehouseNotFoundException`: When warehouse ID doesn't exist
- `OrderNotFoundException`: When order ID doesn't exist
- `ShipmentNotFoundException`: When shipment ID doesn't exist
- `InsufficientStockException`: When warehouse doesn't have enough stock
- `NoWarehouseAvailableException`: When no warehouse can fulfill order
- `InvalidOrderStateException`: When operation not allowed in current order state
- `InvalidShipmentStateException`: When operation not allowed in current shipment state

### Error Handling Strategy:
- Services throw exceptions for business rule violations
- Presentation layer catches and displays user-friendly messages
- All exceptions extend a base `SupplyChainException`

---

## Key Design Decisions

1. **Transactional vs Non-Transactional Orders:**
   - Transactional: All items must come from single warehouse (atomic)
   - Non-Transactional: Items can come from multiple warehouses

2. **Inventory Management:**
   - Track both reserved and available quantities
   - Reserve on allocation, deduct on shipment
   - Release on cancellation

3. **Shipment Model:**
   - One shipment per warehouse per order
   - Allows tracking partial fulfillment
   - Each shipment has independent status

4. **Simple ID Generation:**
   - Use UUID for all entity IDs
   - Simple and collision-free

5. **No Complex Optimization:**
   - Warehouse selection uses simple "first available" strategy
   - Can be enhanced later with distance/cost optimization

---

## Future Enhancements (Out of Scope for V1)

- Payment processing
- Return/refund handling
- Warehouse capacity constraints
- Product categories and SKUs
- Batch processing
- Event sourcing
- Notifications
- Analytics and reporting





