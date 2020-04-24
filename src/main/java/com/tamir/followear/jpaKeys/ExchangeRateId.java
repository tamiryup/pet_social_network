package com.tamir.followear.jpaKeys;

import com.tamir.followear.enums.Currency;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class ExchangeRateId implements Serializable {

    @Enumerated(EnumType.STRING)
    @Column(length = 8)
    private Currency fromCurrency;

    @Enumerated(EnumType.STRING)
    @Column(length = 8)
    private Currency toCurrency;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExchangeRateId)) return false;
        ExchangeRateId that = (ExchangeRateId) o;

        return (that.getFromCurrency() == this.getFromCurrency()
                && that.getToCurrency() == this.getToCurrency());
    }

    @Override
    public int hashCode() {
        return Objects.hash(fromCurrency, toCurrency);
    }
}
