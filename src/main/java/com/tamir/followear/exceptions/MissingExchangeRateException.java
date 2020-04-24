package com.tamir.followear.exceptions;

import com.tamir.followear.enums.Currency;

public class MissingExchangeRateException extends RuntimeException {

    MissingExchangeRateException(String message) {
        super(message);
    }

    public MissingExchangeRateException(Currency fromCurrency, Currency toCurrency) {
        this("Could not convert " + fromCurrency + " to " + toCurrency);
    }
}
