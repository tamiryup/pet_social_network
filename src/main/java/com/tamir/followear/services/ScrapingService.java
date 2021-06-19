package com.tamir.followear.services;

import com.tamir.followear.AWS.lambda.LambdaService;
import com.tamir.followear.dto.ScrapingEventDTO;
import com.tamir.followear.dto.UploadItemDTO;
import com.tamir.followear.entities.Store;
import com.tamir.followear.enums.ProductType;
import com.tamir.followear.exceptions.BadLinkException;
import com.tamir.followear.exceptions.ScrapingError;
import com.tamir.followear.helpers.StringHelper;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

@Service
public class ScrapingService {

    @Autowired
    StoreService storeService;

    @Autowired
    LambdaService lambdaService;

    @Autowired
    ItemClassificationService classificationService;

    private static final Logger logger = LoggerFactory.getLogger(ScrapingService.class);

    @Value("${fw.chrome.driver}")
    private String chromedriverPath;

    @Value("${fw.chrome.binary}")
    private String chromeBinary;

    @PostConstruct
    public void init() throws IOException {
        System.setProperty("webdriver.chrome.driver", chromedriverPath);
    }

    public WebDriver getDriver() {
        ChromeOptions options = new ChromeOptions();
        options.setBinary(chromeBinary);

        String proxyUrl = "http://il.smartproxy.com:30001";

        options.addArguments("--headless", "--no-sandbox", "--disable-gpu", "--window-size=1280x1696",
                "--user-data-dir=/tmp/user-data", /*"--remote-debugging-port=9222",*/ "--hide-scrollbars",
                "--enable-logging", "--log-level=0", "--v=99", "--single-process",
                "--data-path=/tmp/data-path", "--ignore-certificate-errors", "--homedir=/tmp",
                "--disk-cache-dir=/tmp/cache-dir", "--proxy-server=" + proxyUrl,
                "user-agent=Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36" +
                        " (KHTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36");

        WebDriver driver = new ChromeDriver(options);
        return driver;
    }

    public UploadItemDTO extractItem(String productPageLink) {
        UploadItemDTO itemDTO = null;
        ScrapingEventDTO scrapingEvent = createScrapingEvent(productPageLink);

        try {
            itemDTO = lambdaService.invokeScrapingLambda(scrapingEvent);
        } catch (IOException e) {
            logger.error("ScrapingError: ", e);
            throw new ScrapingError(e.toString());
        }

        classifyItem(itemDTO);

        return itemDTO;
    }

    private long getStoreID(String website) {
        Store store = storeService.findByWebsite(website);
        if (store == null) {
            throw new BadLinkException("This website is not supported");
        }
        long id = store.getId();
        return id;
    }

    private String correctLink(String productPageLink) throws URISyntaxException { ;
        URI uri = new URI(productPageLink);
        String domain = uri.getHost();

        if (domain.startsWith("m.")) {
            return productPageLink.replaceFirst("m.", "www.");
        }
        if(domain.startsWith("www2.")) {
            return productPageLink.replaceFirst("www2.", "www.");
        }
        return productPageLink;
    }

    private String getDomainName(String productPageLink) throws URISyntaxException {
        URI uri = new URI(productPageLink);
        String domain = uri.getHost();

        if (domain.startsWith("www.")) {
            return domain.substring(4);
        }
        if (domain.startsWith("il.")) {
            return domain.substring(3);
        }
        if(domain.equals("api-shein.shein.com")) {
            return "shein.com";
        }
        else {
            return domain;
        }
    }

    public ScrapingEventDTO createScrapingEvent(String productPageLink) {
        long storeId;
        String website;

        try {
            productPageLink = correctLink(productPageLink);
            website = getDomainName(productPageLink);
        } catch (URISyntaxException e) {
            throw new BadLinkException("Invalid link");
        }

        storeId = getStoreID(website);

        return new ScrapingEventDTO(productPageLink, storeId);
    }

    public UploadItemDTO classifyItem(UploadItemDTO itemDTO) {
        Map<String, ProductType> dict;

        if (StringHelper.doesContainHebrew(itemDTO.getDescription())) {
            dict = classificationService.getHebrewDict();
        } else {
            dict = classificationService.getEnglishDict();
        }
        ItemClassificationService.ItemTags itemTags = classificationService.classify(itemDTO.getDescription(), dict);

        itemDTO.setCategory(itemTags.getCategory());
        itemDTO.setProductType(itemTags.getProductType());

        return itemDTO;
    }

}
