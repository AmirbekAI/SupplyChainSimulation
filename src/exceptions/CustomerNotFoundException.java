package exceptions;

public class CustomerNotFoundException extends SupplyChainException{
    public CustomerNotFoundException(String message) {
        super(message);
    }
}
