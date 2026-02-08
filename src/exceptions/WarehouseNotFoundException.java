package exceptions;

public class WarehouseNotFoundException extends SupplyChainException {
    public WarehouseNotFoundException(String message) {
        super(message);
    }
}
