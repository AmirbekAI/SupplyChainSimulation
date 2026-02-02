package exceptions;

public class SupplyChainException extends RuntimeException{
    public SupplyChainException(String message) {
        super(message);
    }

    public SupplyChainException(String message, Throwable cause) {
        super(message, cause);
    }
}
