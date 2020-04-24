package com.tamir.followear.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.tamir.followear.OkHttpClientProvider;
import com.tamir.followear.enums.Currency;
import com.tamir.followear.exceptions.ExchangeRateException;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Helps convert prices to different currencies.
 *
 * This services uses an InMemory Cache to minimize conversion time
 * and number of requests to the conversion rate service (100 requests per hour limit)
 */
@Service
public class CurrencyConverterService {

    private final Logger logger = LoggerFactory.getLogger(CurrencyConverterService.class);

    @Autowired
    private OkHttpClientProvider okHttpClientProvider;

    @Autowired
    private ExchangeRateService exchangeRateService;

    /**
     * key format:
     *      USD_ILS - maps to the exchange rate of 1 USD to ILS
     */
    private LoadingCache<String, Double> ratesCache;

    @Value("${fw.currconv.key}")
    private String currconvKey;

    private String conversionBaseUrl = "https://free.currconv.com/api/v7/convert";

    @PostConstruct
    private void init() {
        ratesCache = CacheBuilder.newBuilder()
                .refreshAfterWrite(12, TimeUnit.HOURS)
                .build(
                        new CacheLoader<String, Double>() {
                            @Override
                            public Double load(String query) throws Exception {
                                return loadRate(query);
                            }
                        }
                );
    }

    private double loadRate(String query) throws IOException {
        OkHttpClient client = okHttpClientProvider.getClient();

        HttpUrl.Builder urlBuilder = HttpUrl.parse(conversionBaseUrl).newBuilder();
        urlBuilder.addQueryParameter("q", query);
        urlBuilder.addQueryParameter("compact", "ultra");
        urlBuilder.addQueryParameter("apiKey", currconvKey);
        String url = urlBuilder.build().toString();

        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        if(response.code() != 200) {
            logger.warn("Received error from currconv api servers");
            return getRateByQueryString(query);
        }

        ResponseBody responseBody = response.body();
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Double> map = mapper.readValue(responseBody.string(), Map.class);

        // json response might be int (ILS = 1 ILS), this handles that case (will otherwise throw an error)
        Number rate = map.get(query);
        Double rateAsDouble = rate.doubleValue();
        updateRateByQueryString(query, rateAsDouble);

        return rateAsDouble;
    }

    /**
     * gets rate by query string from database
     *
     * @param query at the same format as cache key
     * @return the rate in the database
     */
    private double getRateByQueryString(String query) {
        String[] currencies = query.split("_");
        Currency fromCurrency = Currency.valueOf(currencies[0]);
        Currency toCurrency = Currency.valueOf(currencies[1]);
        return exchangeRateService.getRate(fromCurrency, toCurrency);
    }

    /**
     * updates the rate in the database by query string
     *
     * @param query at the same format as cache key
     * @param rate
     */
    private void updateRateByQueryString(String query, double rate){
        String[] currencies = query.split("_");
        Currency fromCurrency = Currency.valueOf(currencies[0]);
        Currency toCurrency = Currency.valueOf(currencies[1]);
        exchangeRateService.updateRate(fromCurrency, toCurrency, rate);
    }

    public double convert(Currency from, Currency to, double amount) {
        try {

            DecimalFormat doubleFormat = new DecimalFormat("#.##");
            double exchangeRate = ratesCache.get(from.name() + "_" + to.name());
            double result = amount * exchangeRate;

            //keep 2 decimal places
            String resultAsString = doubleFormat.format(result);
            result = Double.valueOf(resultAsString);

            return result;

        } catch(RuntimeException e) {
            throw e;
        } catch(Exception e) {
            throw new ExchangeRateException(e.getMessage());
        }
    }
}
