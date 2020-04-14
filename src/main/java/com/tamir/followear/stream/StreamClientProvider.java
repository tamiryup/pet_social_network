package com.tamir.followear.stream;

import io.getstream.client.Client;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.MalformedURLException;

@Component
public class StreamClientProvider {

    private Client streamClient;

    @Value("${fw.stream.key}")
    private String appKey;

    @Value("${fw.stream.secret}")
    private String appSecret;

    public StreamClientProvider() {
    }

    @PostConstruct
    public void init() throws MalformedURLException {
        this.streamClient = Client.builder(appKey, appSecret).build();
    }

    public Client getClient() {
        return streamClient;
    }
}
