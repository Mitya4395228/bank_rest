package com.example.bankcards.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class CardNumberUtilTest {
    @Test
    void testGenerate() {
        assertTrue(CardNumberUtil.generate().matches(CardNumberUtil.VALID_CARD_NUMBER));
    }

    @Test
    void testMask() {
        assertEquals("**** **** **** 1234", CardNumberUtil.mask("1234123412341234"));
    }
}
