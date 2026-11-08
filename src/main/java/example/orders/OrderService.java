package example.orders;

/** Small, framework-free backend domain service. All prices are integer cents. */
public final class OrderService {
    public long subtotal(long unitPriceCents, int quantity) {
        if (unitPriceCents < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        if (quantity < 1) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        return Math.multiplyExact(unitPriceCents, quantity);
    }

    public long shipping(long subtotalCents) {
        if (subtotalCents < 0) {
            throw new IllegalArgumentException("Subtotal cannot be negative");
        }
        return subtotalCents >= 5_000 ? 0 : 499;
    }
}
