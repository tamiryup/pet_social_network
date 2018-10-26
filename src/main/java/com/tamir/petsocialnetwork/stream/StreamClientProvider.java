package com.tamir.petsocialnetwork.stream;

import io.getstream.client.StreamClient;
import io.getstream.client.apache.StreamClientImpl;
import io.getstream.client.config.ClientConfiguration;
import org.springframework.stereotype.Component;

@Component
public class StreamClientProvider {

    private ClientConfiguration clientConfig;

    private StreamClient streamClient;

    public StreamClientProvider(){
        this.clientConfig = new ClientConfiguration();
        this.streamClient = new StreamClientImpl(clientConfig, "xe2eeutkupcf",
                "26ru4qj4sjp3vafa7jpsxqe4jzmpmszedas7tq3eja7k8j42huczr945pay4tf8v");
    }

    public StreamClient getClient(){
        return streamClient;
    }
}
