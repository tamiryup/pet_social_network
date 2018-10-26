package com.tamir.petsocialnetwork.helpers;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

public class FileHelper {

    public static File multipartToFile(MultipartFile multipartFile) throws IOException {
        File convFile = new File(multipartFile.getOriginalFilename());
        multipartFile.transferTo(convFile);
        return convFile;
    }

    public static String getMultipartFileExtension(MultipartFile multipartFile){
        return MimeTypes.getDefaultExt(multipartFile.getContentType());
    }
}
