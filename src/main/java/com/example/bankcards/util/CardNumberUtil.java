package com.example.bankcards.util;

import java.util.Random;

public final class CardNumberUtil {

    public final static String VALID_CARD_NUMBER = "^[0-9]{16}$";

    private CardNumberUtil() {
    }

    public static String generate() {
        var cardNumber = new StringBuilder();
        var random = new Random();
        for (int i = 0; i < 16; i++) {
            cardNumber.append(random.nextInt(10));
        }
        return cardNumber.toString();
    }

    public static String mask(String number) {
        return "**** ".repeat(3) + number.substring(12);
    }

}
