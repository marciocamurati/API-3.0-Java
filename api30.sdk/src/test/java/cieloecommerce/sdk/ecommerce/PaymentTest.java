package cieloecommerce.sdk.ecommerce;

import cieloecommerce.sdk.Merchant;
import cieloecommerce.sdk.ecommerce.request.CieloError;
import cieloecommerce.sdk.ecommerce.request.CieloRequestException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * Created by marciocamurati on 31/01/17.
 */
public class PaymentTest {

    private Merchant merchant;

    @Before
    public void setUp() throws Exception {
        merchant = new Merchant("XXXXXXXXXXXXXXXXXXXXXXXXXXX", "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
    }

    /**
     * <p>
     *     Debit card payment method test
     * </p>
     */
    @Test
    public void testWithDebitCard() {
        Sale sale = new Sale("1");

        Customer customer = sale.customer("Comprador Teste");

        Assert.assertNotNull(customer);

        Payment payment = sale.payment(15700);
        payment.setReturnUrl("http://requestb.in/1ixzstp1");

        Assert.assertNotNull(payment);

        payment.debitCard("123", "Visa")
                .setExpirationDate("12/2018")
                .setCardNumber("0000000000000001")
                .setHolder("Fulano de Tal");

        try {
            sale = new CieloEcommerce(merchant, Environment.SANDBOX).createSale(sale);

            Assert.assertNotNull(sale);

            Assert.assertSame(0, sale.getPayment().getStatus());

            Assert.assertEquals("1", sale.getPayment().getReturnCode());

            String paymentId = sale.getPayment().getPaymentId();

            Assert.assertNotNull(paymentId);
        } catch (CieloRequestException e) {
            CieloError error = e.getError();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * <p>
     *     Credit card payment method test
     * </p>
     */
    @Test
    public void testWithCreditCard() {
        Sale sale = new Sale("1");

        Customer customer = sale.customer("Comprador Teste");

        Assert.assertNotNull(customer);

        Payment payment = sale.payment(15700);

        Assert.assertNotNull(payment);

        payment.creditCard("123", "Visa")
                .setExpirationDate("12/2018")
                .setCardNumber("0000000000000001")
                .setHolder("Fulano de Tal");

        try {
            sale = new CieloEcommerce(merchant, Environment.SANDBOX).createSale(sale);

            Assert.assertNotNull(sale);

            String paymentId = sale.getPayment().getPaymentId();

            Assert.assertNotNull(paymentId);

            sale = new CieloEcommerce(merchant, Environment.SANDBOX).captureSale(paymentId, 15700, 0);

            Assert.assertSame(0, sale.getReasonCode());

            sale = new CieloEcommerce(merchant, Environment.SANDBOX).cancelSale(paymentId, 15700);

            Assert.assertSame(0, sale.getReasonCode());
        } catch (CieloRequestException e) {
            CieloError error = e.getError();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
