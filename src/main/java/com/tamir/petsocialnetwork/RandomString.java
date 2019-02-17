package com.tamir.petsocialnetwork;

import java.security.SecureRandom;
import java.util.Random;

public class RandomString {

    private static final String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private static final String lower = upper.toLowerCase();

    private static final String digits = "1234567890";

    /* Assign a string that contains the set of characters you allow. */
    private static final String symbols = upper + lower + digits;

    private final Random random = new SecureRandom();

    private final char[] buf;

    public RandomString(int length) {
        if (length < 1)
            throw new IllegalArgumentException("length < 1: " + length);
        buf = new char[length];
    }

    public String nextString() {
        for (int idx = 0; idx < buf.length; idx++)
            buf[idx] = symbols.charAt(random.nextInt(symbols.length()));
        return new String(buf);
    }

}