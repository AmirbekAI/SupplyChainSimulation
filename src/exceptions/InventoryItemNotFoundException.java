package exceptions;

import domain.model.InventoryItem;

import java.util.UUID;

public class InventoryItemNotFoundException extends SupplyChainException{

    private final UUID productId;
    private final UUID warehouseId;

    public InventoryItemNotFoundException(UUID productId, UUID warehouseId) {
        super(
                "Inventory item not found. Product=" + productId +
                        ", Warehouse=" + warehouseId
        );
        this.productId = productId;
        this.warehouseId = warehouseId;
    }

    public UUID getProductId() {
        return productId;
    }

    public UUID getWarehouseId() {
        return warehouseId;
    }
}
