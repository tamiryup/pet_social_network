package com.tamir.followear.rest;

import com.tamir.followear.AWS.s3.S3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLConnection;

@RestController
public class BaseController {

    @Autowired
    S3Service s3Service;

    @GetMapping("/image")
    public ResponseEntity<Resource> getImage(@RequestParam("s3key") String key)
            throws IOException {
        byte[] imageBytes;
        imageBytes = s3Service.getFileBytes(key);
        ByteArrayResource resource = new ByteArrayResource(imageBytes);
        String contentType = URLConnection.guessContentTypeFromName(key);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }

    @GetMapping("/health")
    @ResponseStatus(HttpStatus.OK)
    public void health() {

    }
}
