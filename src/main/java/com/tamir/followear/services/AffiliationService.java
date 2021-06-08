package com.tamir.followear.services;

import com.tamir.followear.entities.Post;
import com.tamir.followear.helpers.StringHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Service
public class AffiliationService {

    @Value("${fw.skimlinks.id}")
    private String skimlinksId;

    private static final Logger LOGGER = LoggerFactory.getLogger(AffiliationService.class);

    // Asos, Farfetch, Shein, Shopbop, Revolve, Outnet, AloYoga
    private static List<Long> skimlinksStores = Arrays.asList(1l, 3l, 5l, 6l, 9l, 13l, 21l);

    public String getAffiliatedLink(Post post, long userId, long storeId) {
        String link = post.getLink();

        if(storeId == 7) { //terminalX
            return encodeTerminal(link, userId);
        }

        if(storeId == 17) { //renuar
            return encodeRenuar(link, userId);
        }
        if(storeId == 18) { //TFS
            return encodeTFS(link, userId);
        }

        if(skimlinksStores.contains(storeId)) {
            return encodeSkimlinks(link, userId, post.getId());
        }

        return link;
    }

    private String encodeTerminal(String link, long userId) {
        String terminalSuffix = "?utm_source=IG&utm_medium=Followear%20platform&utm_campaign=" + userId;
        return link + terminalSuffix;
    }

    private String encodeRenuar(String link, long userId) {
        String renuarSuffix = "?utm_source=FW&utm_medium=Followear_Platform&utm_campaign=" + userId;
        return link + renuarSuffix;
    }

    private String encodeTFS(String link, long userId) {
        String tfsSuffix = "?utm_source=FW&utm_medium=Followear_Platform&utm_campaign=" + userId;
        return link + tfsSuffix;
    }

    public String encodeSkimlinks(String link, long userId, long postId) {
        String encodedUrl, refferingPageEncoded;
        String refferingPage = "followear.com/product-page/" + postId + "?uid=" + userId +
                "&source=app";

        try {
            encodedUrl = StringHelper.encodeUrl(link);
            refferingPageEncoded = StringHelper.encodeUrl(refferingPage);
        } catch (IOException e) {
            LOGGER.error("Failed to encode link. link: {}", link);
            return link;
        }

        String prefix = "https://go.skimresources.com/?";
        StringBuilder linkBuildr = new StringBuilder();

        linkBuildr.append(prefix)
                .append("id=")
                .append(skimlinksId)
                .append("&url=")
                .append(encodedUrl)
                .append("&xcust=")
                .append(userId)
                .append("&sref=")
                .append(refferingPageEncoded);

        return linkBuildr.toString();
    }
}
