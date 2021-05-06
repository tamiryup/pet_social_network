package com.tamir.followear.helpers;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageHelper {

    public static InputStream cropImage(InputStream imageInputStream, int x, int y, int w, int h) throws IOException {
        InputStream retStream;

        BufferedImage bufferedImage = ImageIO.read(imageInputStream);
        BufferedImage croppedImage = bufferedImage.getSubimage(x, y, w, h);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(croppedImage, "jpg", outputStream);
        byte[] bytes = outputStream.toByteArray();
        retStream = new ByteArrayInputStream(bytes);
        return retStream;
    }

}
