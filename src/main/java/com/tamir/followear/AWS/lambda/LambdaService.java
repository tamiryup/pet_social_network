package com.tamir.followear.AWS.lambda;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tamir.followear.AWS.MyAWSCredentials;
import com.tamir.followear.AWS.cognito.CognitoService;
import com.tamir.followear.dto.ScrapingEventDTO;
import com.tamir.followear.dto.UploadItemDTO;
import io.netty.buffer.ByteBufInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@Service
public class LambdaService {

    Logger logger = LoggerFactory.getLogger(LambdaService.class);

    @Autowired
    private MyAWSCredentials myAWSCreds;

    private AWSLambda awsLambda;

    private ObjectMapper mapper;

    @Value("${fw.scraping-lambda.name}")
    private String scrapingLambdaName;

    @PostConstruct
    public void init() {
        awsLambda = AWSLambdaClientBuilder.standard()
            .withCredentials(new AWSStaticCredentialsProvider(myAWSCreds.getCredentials()))
            .withRegion(Regions.EU_WEST_1)
            .build();

        mapper = new ObjectMapper();
    }

    public UploadItemDTO invokeScrapingLambda(ScrapingEventDTO scrapingEvent) throws IOException {

        ByteBuffer payload = ByteBuffer.wrap(mapper.writeValueAsBytes(scrapingEvent));

        InvokeRequest invokeRequest = new InvokeRequest()
                .withFunctionName(scrapingLambdaName)
                .withPayload(payload);

        InvokeResult result = awsLambda.invoke(invokeRequest);

        UploadItemDTO itemDTO = mapper.readValue(result.getPayload().array(), UploadItemDTO.class);

        return itemDTO;
    }

}
