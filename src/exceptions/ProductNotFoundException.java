package exceptions;

public class ProductNotFoundException extends SupplyChainException {
    public ProductNotFoundException(String message) {
        super(message);
    }
}
