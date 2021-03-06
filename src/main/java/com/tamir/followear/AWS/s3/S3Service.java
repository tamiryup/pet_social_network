package com.tamir.followear.AWS.s3;

import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.fasterxml.uuid.Generators;
import com.tamir.followear.AWS.MyAWSCredentials;
import com.tamir.followear.enums.ImageType;
import com.tamir.followear.exceptions.S3Exception;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

@Service
public class S3Service {

    Logger logger = LoggerFactory.getLogger(S3Service.class);

    @Autowired
    private MyAWSCredentials myAWSCreds;

    private AmazonS3 s3Client;

    @Value("${fw.s3.bucket-name}")
    private String bucketName;

    public S3Service() {

    }

    @PostConstruct
    public void init() {
        s3Client = AmazonS3ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(myAWSCreds.getCredentials()))
                .withRegion(Regions.EU_WEST_1)
                .build();
    }

    private String generateKey(ImageType imageType, String fileExtension) {
        String path = imageType.getPath();
        String uuid = Generators.timeBasedGenerator().generate().toString();
        String key = path + "/" + uuid + "." + fileExtension;
        return key;
    }

    public String uploadImage(ImageType imageType, File file, String fileExtension) {
        String key = generateKey(imageType, fileExtension);
        try {
            s3Client.putObject(bucketName, key, file);
        } catch (SdkClientException e) {
            throw new S3Exception(e.getMessage());
        }
        return key;
    }

    public String uploadImage(ImageType imageType, MultipartFile multipartFile, String fileExtension)
            throws IOException {
        return uploadImage(imageType, multipartFile.getInputStream(), fileExtension);
    }

    public String uploadImage(ImageType imageType, InputStream inputStream, String fileExtension) throws IOException {
        String key = generateKey(imageType, fileExtension);
        ObjectMetadata metadata = new ObjectMetadata();
        byte[] bytes = IOUtils.toByteArray(inputStream);
        metadata.setContentLength(bytes.length);

        try {
            s3Client.putObject(bucketName, key, new ByteArrayInputStream(bytes), metadata);
        } catch (SdkClientException e) {
            throw new S3Exception(e.getMessage());
        }

        return key;
    }

    public byte[] getFileBytes(String key) throws IOException {
        S3Object s3Object;
        byte[] bytes;

        try {
            s3Object = s3Client.getObject(bucketName, key);
        } catch (SdkClientException e) {
            throw new S3Exception(e.getMessage());
        }

        try (InputStream objStream = s3Object.getObjectContent()) {
            bytes = IOUtils.toByteArray(objStream);
        }
        return bytes;
    }

    public void deleteByKey(String key) {
        if (key == null || key.equals(""))
            return;

        try {
            s3Client.deleteObject(bucketName, key);
        } catch (SdkClientException e) {
            throw new S3Exception(e.getMessage());
        }
    }

    public InputStream getFileAsStream(String key) {
        S3Object s3Object = s3Client.getObject(bucketName, key);
        return s3Object.getObjectContent();
    }


}
