package exceptions;

public class OrderNotFoundException extends SupplyChainException{
    public OrderNotFoundException(String message) {
        super(message);
    }
}
