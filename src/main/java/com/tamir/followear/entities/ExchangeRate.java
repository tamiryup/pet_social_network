package com.tamir.followear.entities;

import com.tamir.followear.jpaKeys.ExchangeRateId;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "exchange_rates")
@Getter
@ToString
@NoArgsConstructor
public class ExchangeRate {

    @EmbeddedId
    private ExchangeRateId exchangeRateId;

    private double rate;

    @UpdateTimestamp
    private Date updateDate;

    public ExchangeRate(ExchangeRateId exchangeRateId, double rate) {
        this.exchangeRateId = exchangeRateId;
        this.rate = rate;
    }
}
