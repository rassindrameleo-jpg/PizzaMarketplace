package fr.pizzeriamarketplace.domain.payment;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
class PaymentServiceTest {


    @Test
    void shouldSwitchPaymentWhenStripeFails() {

        StripeAdapter stripe = new StripeAdapter(new StripePayment());
        stripe.setAvailable(false);

        PaymentService service = new PaymentService(
                stripe,
                new PaypalAdapter(new PaypalPayment())
        );

        assertTrue(service.pay(2000));
    }
}