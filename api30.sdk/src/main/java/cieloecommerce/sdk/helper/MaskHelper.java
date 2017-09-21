package cieloecommerce.sdk.helper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>
 *     Helper to mask sensitive information
 * </p>
 */
public final class MaskHelper {
    /*
     * RegExp patterns to PCI log informations
     */
    private final static Pattern patternCreditCardNumber = Pattern.compile("(?:4[0-9]{12}(?:[0-9]{3})?|5[1-5][0-9]{14}|5[1-5][0-9]{12}|" +
            "6(?:011|5[0-9][0-9])[0-9]{12}|3[47][0-9]{13}|3(?:0[0-5]|" +
            "[68][0-9])[0-9]{11}|(?:2131|1800|35\\d{3})\\d{11})");

    private final static Pattern patternSecurityCode = Pattern.compile("\\\"(\\d{3}|\\d{4})\\\"");

    private final static Pattern patternValidateDate = Pattern.compile("\\\"\\d{1,2}/\\d{4}\\\"");

    private final static Pattern patternMaskedCreditCardNumber = Pattern.compile("\\d{6}\\*+\\d{4}");

    /**
     * <p>
     *     Try to mask the sensitive credit card informations, like CVV, validate date and card number
     *     If couldn't return a fixed text message
     * </p>
     * @param value
     * @return
     */
    public final static String maskSensitiveCardInformations(String value) {
        Matcher matcher = patternCreditCardNumber.matcher(value);

        if (matcher.find()) {
            String cardNumber = matcher.group(0);

            StringBuilder maskCardNumber = new StringBuilder();

            maskCardNumber.append(cardNumber.substring(0, 6));

            for (int mask = 0; mask < (cardNumber.length() - 10); mask++) {
                maskCardNumber.append("*");
            }

            maskCardNumber.append(cardNumber.substring(cardNumber.length() - 4));

            value = value.replace(cardNumber, maskCardNumber.toString());

            matcher = patternValidateDate.matcher(value);

            if (matcher.find()) {
                value = matcher.replaceAll("\"*\"");
            }

            matcher = patternSecurityCode.matcher(value);

            if (matcher.find()) {
                value =  matcher.replaceAll("\"*\"");
            }

            return value;
        }	else	{
            matcher = patternMaskedCreditCardNumber.matcher(value);

            if (matcher.find()) {
                matcher = patternValidateDate.matcher(value);

                if (matcher.find()) {
                    value = matcher.replaceAll("\"*\"");
                }

                matcher = patternSecurityCode.matcher(value);

                if (matcher.find()) {
                    value =  matcher.replaceAll("\"*\"");
                }

                return value;
            }
        }

        return "Find a credit card number but can't mask it!";
    }

}
