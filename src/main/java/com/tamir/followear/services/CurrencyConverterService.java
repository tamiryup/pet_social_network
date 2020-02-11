package com.tamir.followear.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.tamir.followear.OkHttpClientProvider;
import com.tamir.followear.enums.Currency;
import com.tamir.followear.exceptions.ExchangeRateException;
import okhttp3.*;
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

    @Autowired
    private OkHttpClientProvider okHttpClientProvider;

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
                .refreshAfterWrite(2, TimeUnit.HOURS)
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
            throw new ExchangeRateException("Received error from currconv api servers");
        }

        ResponseBody responseBody = response.body();
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Double> map = mapper.readValue(responseBody.string(), Map.class);

        // json response might be int (ILS = 1 ILS), this handles that case (will otherwise throw an error)
        Number rate = map.get(query);
        Double rateAsDouble = rate.doubleValue();
        return rateAsDouble;
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

        } catch(Exception e) {
            throw new ExchangeRateException(e.getMessage());
        }
    }
}
