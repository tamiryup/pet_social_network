package com.tamir.followear.enums;

import lombok.Getter;

public enum Currency {

    USD("$"),
    ILS("₪"),
    EUR("€"),
    GBP("£");


    @Getter
    private String sign;

    Currency(String sign) {
        this.sign = sign;
    }
}
