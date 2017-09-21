package cieloecommerce.sdk.helper;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * <p>
 *     Test {@link MaskHelper} mask sensitive card informations
 * </p>
 */
public class MaskHelperTest {

    @Test
    public void maskSensitiveCardNumberTest() {
        List<CardTest> cards = Arrays.asList(
                new CardTest("5555555555554444", "555555******4444"),
                new CardTest("5454545454545454", "545454******5454"),
                new CardTest("4444333322221111", "444433******1111"),
                new CardTest("4911830000000", "491183***0000"),
                new CardTest("4462030000000000", "446203******0000"),
                new CardTest("36700102000000", "367001****0000"),
                new CardTest("3528000700000000", "352800******0000"),
                new CardTest("34343434343434", "3434343***3434"),
                new CardTest("51286454210121", "512864****0121")
        );

        for (CardTest card:cards) {
            String maskedCard = MaskHelper.maskSensitiveCardInformations(card.getNumber());

            Assert.assertNotNull(maskedCard);

            Assert.assertNotEquals("", maskedCard);

            Assert.assertEquals(card.getMaskNumber(), maskedCard);
        }
    }

    /**
     * Simple Card object
     */
    class CardTest {
        private String number;

        private String maskNumber;

        public CardTest(String number, String maskNumber)   {
            this.number = number;
            this.maskNumber = maskNumber;
        }

        public String getNumber() {
            return number;
        }

        public String getMaskNumber() {
            return maskNumber;
        }
    }
}
