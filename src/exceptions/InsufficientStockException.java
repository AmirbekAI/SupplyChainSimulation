package exceptions;

import java.util.Objects;
import java.util.UUID;

public class InsufficientStockException extends SupplyChainException{
    private final UUID productId;
    private final UUID warehouseId;

    public InsufficientStockException(UUID productId, UUID warehouseId, int requestedQuantity, int availableQuantity) {
        super(
                "Insufficient stock. Product=" + productId +
                        ", Warehouse=" + warehouseId +
                        ", requested quantity=" + String.valueOf(requestedQuantity) +
                        ", available quantity=" + String.valueOf(availableQuantity)
        );
        this.productId = productId;
        this.warehouseId = warehouseId;
    }

}
