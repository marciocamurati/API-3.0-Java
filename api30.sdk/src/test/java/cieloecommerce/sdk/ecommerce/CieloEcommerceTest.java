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
public class CieloEcommerceTest {

    private Merchant merchant;

    private Environment environment;

    private final Integer amount = 1 * 100; // R$ 1.00

    @Before
    public void setUp() throws Exception {
//        PROD
//        merchant = new Merchant("XXXXXXXX", "XXXXXXXX");
//        environment = Environment.PRODUCTION;

        merchant = new Merchant("XXXXXXXX", "XXXXXXXX");
        environment = Environment.SANDBOX;
    }

    /**
     * <p>
     *     Eletronic transfer payment method test
     * </p>
     */
    @Test
    public void testWithEletronicTransfer() {
        Sale sale = new Sale("1");

        Customer customer = sale.customer("XXXXXX");

        Assert.assertNotNull(customer);

        Payment payment = sale.payment(amount);
        payment.setType(Payment.Type.ElectronicTransfer);
        payment.setReturnUrl("XXXXXXXXX");

        Assert.assertNotNull(payment);

        payment.setProvider(Payment.Provider.Bradesco);

        payment.setSoftDescriptor("XXXXXX");

        try {
            sale = new CieloEcommerce(merchant, environment).createSale(sale);

            Assert.assertNotNull(sale);

            Assert.assertSame(0, sale.getPayment().getStatus());

            Assert.assertEquals("0", sale.getPayment().getReturnCode());

            String paymentId = sale.getPayment().getPaymentId();

            Assert.assertNotNull(paymentId);
        } catch (CieloRequestException e) {
            CieloError error = e.getError();
            Assert.fail();
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    /**
     * <p>
     *     Debit card payment method test
     * </p>
     */
    @Test
    public void testWithDebitCard() {
        Sale sale = new Sale("1");

        Customer customer = sale.customer("XXXXXXXX");

        Assert.assertNotNull(customer);

        Payment payment = sale.payment(amount);
        payment.setReturnUrl("XXXXXXX");

        Assert.assertNotNull(payment);

        payment.debitCard("XX", "XXXXX")
                .setExpirationDate("XX/XXXX")
                .setCardNumber("XXXXXXXXX")
                .setHolder("XXXXXXXXXX");

        payment.setSoftDescriptor("XXXXXX");

        try {
            sale = new CieloEcommerce(merchant, environment).createSale(sale);

            Assert.assertNotNull(sale);

            Assert.assertSame(0, sale.getPayment().getStatus());

            Assert.assertEquals("0", sale.getPayment().getReturnCode());

            String paymentId = sale.getPayment().getPaymentId();

            Assert.assertNotNull(paymentId);
        } catch (CieloRequestException e) {
            CieloError error = e.getError();
            Assert.fail();
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail();
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

        Customer customer = sale.customer("XXXXXXXX");

        Assert.assertNotNull(customer);

        Payment payment = sale.payment(amount);

        Assert.assertNotNull(payment);

        payment.creditCard("XXXXXXXX", "XXXXXXXX")
                .setExpirationDate("XX/XXXX")
                .setCardNumber("XXXXXXXX")
                .setHolder("XXXXXXXX");

        payment.setSoftDescriptor("XXXXXXXX");

        try {
            sale = new CieloEcommerce(merchant, environment).createSale(sale);

            Assert.assertNotNull(sale);

            String paymentId = sale.getPayment().getPaymentId();

            Assert.assertNotNull(paymentId);

            SaleResponse saleResponse = new CieloEcommerce(merchant, environment).captureSale(paymentId, amount, 0);

            Assert.assertSame(0, saleResponse.getReasonCode());
        } catch (CieloRequestException e) {
            CieloError error = e.getError();
            Assert.fail();
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    /**
     * <p>
     *     Tokenizer a card
     * </p>
     */
    @Test
    public void testTokenizer() {
        String cardToken = "XXXXXXXX";

        try {
            Card card = new CieloEcommerce(merchant, environment).queryCardToken(cardToken);

            Assert.assertEquals("XXXXXXXX", card.getHolder());
        } catch (CieloRequestException e) {
            CieloError error = e.getError();
            Assert.fail();
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    /**
     * <p>
     *     Refund with payment id
     * </p>
     */
    @Test
    public void testRefund() {
        try {
            SaleResponse sale = new CieloEcommerce(merchant, environment).cancelSale("XXXXXXXX", amount);

            Assert.assertSame(0, sale.getReasonCode());
        } catch (CieloRequestException e) {
            CieloError error = e.getError();
            Assert.fail();
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }
}
