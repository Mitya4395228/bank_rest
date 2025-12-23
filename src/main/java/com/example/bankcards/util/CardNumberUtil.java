package com.example.bankcards.util;

import java.util.Random;

/**
 * Util class for card number generation and validation
 */

public final class CardNumberUtil {

    /**
     * Regex for validating card number
     */
    public final static String VALID_CARD_NUMBER = "^[0-9]{16}$";

    private CardNumberUtil() {
    }

    /**
     * Generates random card number
     *
     * @return generated card number
     */
    public static String generate() {
        var cardNumber = new StringBuilder();
        var random = new Random();
        for (int i = 0; i < 16; i++) {
            cardNumber.append(random.nextInt(10));
        }
        return cardNumber.toString();
    }

    /**
     * Masks card number
     *
     * @param number {@code String} card number
     * @return {@code String} masked card number
     */
    public static String mask(String number) {
        return "**** ".repeat(3) + number.substring(12);
    }

}
