package com.tamir.followear;

import okhttp3.OkHttpClient;
import org.springframework.stereotype.Component;

@Component
public class OkHttpClientProvider {

    private OkHttpClient client;

    public OkHttpClientProvider() {
        client = new OkHttpClient();
    }

    public OkHttpClient getClient() {
        return client;
    }

}
