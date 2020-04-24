package com.tamir.followear.services;

import com.tamir.followear.entities.ExchangeRate;
import com.tamir.followear.enums.Currency;
import com.tamir.followear.exceptions.ExchangeRateException;
import com.tamir.followear.jpaKeys.ExchangeRateId;
import com.tamir.followear.repositories.ExchangeRateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Optional;

@Transactional
@Service
public class ExchangeRateService {

    @Autowired
    ExchangeRateRepository exchangeRateRepo;

    public double getRate(Currency fromCurrency, Currency toCurrency) {
        ExchangeRateId exchangeRateId = new ExchangeRateId(fromCurrency, toCurrency);
        Optional<ExchangeRate> optExchangeRate = exchangeRateRepo.findById(exchangeRateId);

        if(!optExchangeRate.isPresent()) {
            throw new ExchangeRateException("Could not convert " + fromCurrency + " to " +
                    toCurrency + ", value isn't present in database");
        }

        ExchangeRate exchangeRate = optExchangeRate.get();
        return exchangeRate.getRate();
    }

    public void updateRate(Currency fromCurrency, Currency toCurrency, double rate) {
        ExchangeRateId exchangeRateId = new ExchangeRateId(fromCurrency, toCurrency);
        ExchangeRate exchangeRate = new ExchangeRate(exchangeRateId, rate);
        exchangeRateRepo.save(exchangeRate);
    }

}
