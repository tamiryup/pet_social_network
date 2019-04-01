package com.tamir.petsocialnetwork.helpers;

import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class FileHelper {

    public static File multipartToFile(MultipartFile multipartFile) throws IOException {
        File convFile = new File(multipartFile.getOriginalFilename());
        multipartFile.transferTo(convFile);
        return convFile;
    }

    public static String getMultipartFileExtension(MultipartFile multipartFile){
        return MimeTypes.getDefaultExt(multipartFile.getContentType());
    }

    public static InputStream urlToInputStream(String url) throws IOException {
        BufferedInputStream in = new BufferedInputStream(
                new URL(url).openStream());

        return in;
    }
}
