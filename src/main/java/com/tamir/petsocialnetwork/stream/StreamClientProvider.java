package com.tamir.petsocialnetwork.stream;

import io.getstream.client.StreamClient;
import io.getstream.client.apache.StreamClientImpl;
import io.getstream.client.config.ClientConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.validation.Valid;

@Component
public class StreamClientProvider {

    private ClientConfiguration clientConfig;

    private StreamClient streamClient;

    @Value("${ps.stream.key}")
    private String appKey;

    @Value("${ps.stream.secret}")
    private String appSecret;

    public StreamClientProvider() {
    }

    @PostConstruct
    public void init() {
        this.clientConfig = new ClientConfiguration();
        this.streamClient = new StreamClientImpl(clientConfig, appKey, appSecret);
    }

    public StreamClient getClient() {
        return streamClient;
    }
}
