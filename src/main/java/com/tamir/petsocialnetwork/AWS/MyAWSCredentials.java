package com.tamir.petsocialnetwork.AWS;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import org.springframework.stereotype.Component;

@Component
public class MyAWSCredentials {

    private AWSCredentials credentials;

    public MyAWSCredentials(){
        this.credentials = new BasicAWSCredentials("AKIAJM4VXI4NLF7EOKJQ",
                "lDQS7sfGGTQRAeWiiN1gUxCDQyPZlxB46I1DS8Dc");
    }

    public AWSCredentials getCredentials(){
        return credentials;
    }

}
