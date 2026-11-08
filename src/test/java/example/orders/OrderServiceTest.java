package example.orders;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderServiceTest {
    private final OrderService service = new OrderService();

    @Test
    void calculatesSubtotalInCents() {
        assertEquals(3_750, service.subtotal(1_250, 3));
        assertEquals(0, service.subtotal(0, 1));
    }

    @Test
    void rejectsNegativePrices() {
        assertThrows(IllegalArgumentException.class, () -> service.subtotal(-1, 1));
    }

    @Test
    void rejectsZeroAndNegativeQuantities() {
        assertThrows(IllegalArgumentException.class, () -> service.subtotal(100, 0));
        assertThrows(IllegalArgumentException.class, () -> service.subtotal(100, -1));
    }

    @Test
    void detectsArithmeticOverflow() {
        assertThrows(ArithmeticException.class, () -> service.subtotal(Long.MAX_VALUE, 2));
    }

    @Test
    void chargesShippingBelowFreeShippingThreshold() {
        assertEquals(499, service.shipping(0));
        assertEquals(499, service.shipping(4_999));
    }

    @Test
    void providesFreeShippingAtAndAboveThreshold() {
        assertEquals(0, service.shipping(5_000));
        assertEquals(0, service.shipping(5_001));
    }

    @Test
    void rejectsNegativeSubtotals() {
        assertThrows(IllegalArgumentException.class, () -> service.shipping(-1));
    }
}
