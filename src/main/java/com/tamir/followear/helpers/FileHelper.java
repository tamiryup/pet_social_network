package com.tamir.followear.helpers;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class FileHelper {

    private static OkHttpClient okHttpClient = new OkHttpClient();

    public static File multipartToFile(MultipartFile multipartFile) throws IOException {
        File convFile = new File(multipartFile.getOriginalFilename());
        multipartFile.transferTo(convFile);
        return convFile;
    }

    public static String getMultipartFileExtension(MultipartFile multipartFile){
        return MimeTypes.getDefaultExt(multipartFile.getContentType());
    }

    public static InputStream urlToInputStream(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = okHttpClient.newCall(request).execute();
        ResponseBody responseBody = response.body();
        BufferedInputStream in = new BufferedInputStream(responseBody.byteStream());

        return in;
    }
}
