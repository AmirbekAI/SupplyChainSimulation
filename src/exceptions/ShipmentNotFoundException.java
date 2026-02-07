package exceptions;

public class ShipmentNotFoundException extends SupplyChainException{

    public ShipmentNotFoundException(String message) {
        super(message);
    }
}
