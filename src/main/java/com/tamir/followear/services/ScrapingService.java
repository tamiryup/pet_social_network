package com.tamir.followear.services;

import com.tamir.followear.dto.UploadItemDTO;
import com.tamir.followear.entities.Store;
import com.tamir.followear.enums.Category;
import com.tamir.followear.enums.ProductType;
import com.tamir.followear.exceptions.BadLinkException;
import com.tamir.followear.exceptions.ScrapingError;
import lombok.ToString;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
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
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.*;

import com.tamir.followear.enums.Currency;

@Service
public class ScrapingService {

    @Autowired
    StoreService storeService;

    @Autowired
    CurrencyConverterService currConverterService;

    @Autowired
    ItemClassificationService classificationService;

    private static final Logger LOGGER = LoggerFactory.getLogger(ScrapingService.class);

    @Value("${fw.chrome.driver}")
    private String chromedriverPath;

    @Value("${fw.chrome.binary}")
    private String chromeBinary;

    @PostConstruct
    public void init() throws IOException {
        System.setProperty("webdriver.chrome.driver", chromedriverPath);
    }

    @ToString
    public class ItemPriceCurr {
        private Currency currency;
        private String price;

        public ItemPriceCurr(Currency currency, String price) {
            this.currency = currency;
            this.price = price;
        }
    }

    public WebDriver getDriver() {
        ChromeOptions options = new ChromeOptions();
        options.setBinary(chromeBinary);

        options.addArguments("--headless", "--no-sandbox", "--disable-gpu", "--window-size=1280x1696",
                "--user-data-dir=/tmp/user-data", "--hide-scrollbars", "--enable-logging",
                "--log-level=0", "--v=99", "--single-process", "--data-path=/tmp/data-path",
                "--ignore-certificate-errors", "--homedir=/tmp", "--disk-cache-dir=/tmp/cache-dir",
                "user-agent=Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36" +
                        " (KHTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36");

        WebDriver driver = new ChromeDriver(options);
        return driver;
    }

    private long getStoreID(String website) {
        Store store = storeService.findByWebsite(website);
        if (store == null) {
            throw new BadLinkException("website is not supported");
        }
        long id = store.getId();
        return id;
    }

    private String correctLink(String productPageLink) throws URISyntaxException {
        URI uri = new URI(productPageLink);
        String domain = uri.getHost();
        if(domain.startsWith("m.")) {
           return productPageLink.replaceFirst("m.", "www.");
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
        } else {
            return domain;
        }
    }

    public UploadItemDTO extractItem(String productPageLink) {
        WebDriver driver = null;
        UploadItemDTO itemDTO;
        long storeId;
        String website;
        try {
            productPageLink = correctLink(productPageLink);
            website = getDomainName(productPageLink);
        } catch (URISyntaxException e) {
            throw new BadLinkException("invalid link");
        }
        try {
            driver = getDriver();
            storeId = getStoreID(website);
            switch (website) {
                case "asos.com":
                    itemDTO = asosDTO(productPageLink, storeId, driver);
                    break;
                case "net-a-porter.com":
                    itemDTO = netaporterDTO(productPageLink, storeId, driver);
                    break;
                case "terminalx.com":
                    itemDTO = terminalxDTO(productPageLink, storeId, driver);
                    break;
                case "farfetch.com":
                    itemDTO = farfetchDTO(productPageLink, storeId, driver);
                    break;
                case "shein.com":
                    itemDTO = sheinDTO(productPageLink, storeId, driver);
                    break;
                case "zara.com":
                    itemDTO = zaraDTO(productPageLink, storeId, driver);
                    break;
                case "hm.com":
                    itemDTO = hmDTO(productPageLink, storeId, driver);
                    break;
                case "shopbop.com":
                    itemDTO = shopBopDTO(productPageLink, storeId, driver);
                    break;
                default:
                    throw new BadLinkException("website is not supported");
            }
        } catch (BadLinkException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new ScrapingError(e.toString());
        } finally {
            if (driver != null) {
                driver.close();
            }
        }
        return itemDTO;
    }


    private ItemPriceCurr priceTag(String fullPrice) {
        Currency curr = Currency.USD;
        String price;
        for (Currency currency : Currency.values()) {
            if (fullPrice.contains(currency.name()) || fullPrice.contains(currency.getSign())) {
                curr = currency;
            }
        }

        price = fullPrice.replaceAll("[^0-9\\,\\.]", "");
        ItemPriceCurr itemPriceCurr = new ItemPriceCurr(curr, price);
        return itemPriceCurr;
    }


    private UploadItemDTO asosDTO(String productPageLink, long storeID, WebDriver driver) {
        String productID;
        Category category;
        ProductType productType;
        String designer = null;
        int endIndex;
        int beginIndex = productPageLink.indexOf("/prd/");
        if (beginIndex == -1) {
            throw new BadLinkException("this is not a product page");
        } else {
            beginIndex = beginIndex + 5;
            endIndex = beginIndex + 8;
        }
        productID = productPageLink.substring(beginIndex, endIndex);

        driver.get(productPageLink);
        Document document = Jsoup.parse(driver.getPageSource());
        Elements descriptionDiv = document.select("div.product-hero");
        String description = descriptionDiv.select("h1").text();
        Element priceSpan = document.select("span.current-price").first();
        String fullPrice = priceSpan.text();
        ItemPriceCurr itemPriceCurr = priceTag(fullPrice);
        Currency currency = itemPriceCurr.currency;

        String price = itemPriceCurr.price;
        double priceInILS = currConverterService.convert(currency, Currency.ILS, Double.valueOf(price));
        price = Double.toString(priceInILS);

        Elements imagesDiv = document.select("div.fullImageContainer");
        Elements images = imagesDiv.select("img");
        String imgExtension = "jpg";
        List<String> links = images.eachAttr("src");
        String imageAddr = links.get(1);
        links.remove(1);
        links.remove(3);

        Map<ProductType, List<String>> dict = classificationService.getEnglishDict();
        ItemClassificationService.ItemTags itemTags = classificationService.classify(description, dict);
        category = itemTags.getCategory();
        productType = itemTags.getProductType();

        return new UploadItemDTO(imageAddr, productPageLink, description,
                price, currency, storeID, designer, imgExtension, productID, links, category, productType);

    }

    private UploadItemDTO netaporterDTO(String productPageLink, long storeId, WebDriver driver) {
        String productID;
        Category category;
        ProductType productType;
        int beginIndex = productPageLink.indexOf("/product/");
        if (beginIndex == -1) {
            throw new BadLinkException("this is not a product page");
        }
        beginIndex = beginIndex + 9;
        int endIndex = beginIndex + 7;
        productID = productPageLink.substring(beginIndex, endIndex);

        driver.get(productPageLink);
        Document document = Jsoup.parse(driver.getPageSource());
        Element descriptionDiv = document.select(" h2.product-name").first();
        String description = descriptionDiv.text();
        Element priceSpan = document.select("span.full-price.style-scope.nap-price").first();
        Element priceSymbol = document.select("span.currency.style-scope.nap-price").first();
        String price = priceSpan.text();
        String priceSymbolText = priceSymbol.text();
        ItemPriceCurr itemPriceCurr = priceTag(priceSymbolText);
        Currency currency = itemPriceCurr.currency;
        Element designerDiv = document.select("a.designer-name span").first();
        String designer = designerDiv.text();
        Element imageDiv = document.select("img.product-image.first-image").first();
        String imageAddr = imageDiv.absUrl("src");
        String imgExtension = "jpg";
        Element elem = document.selectFirst(".thumbnail-wrapper");
        Elements imageElements = elem.getElementsByTag("img");
        List<String> links = imageElements.eachAttr("src");
        links.remove(0);
        int endOfThumbnails = 3;
        int size = links.size();
        int maxThumbnails = Math.min(endOfThumbnails, size);
        links = links.subList(0, maxThumbnails);
        for (int i = 0; i < links.size(); i++) {
            links.set(i, "https:" + links.get(i));
        }

        Map<ProductType, List<String>> dict = classificationService.getEnglishDict();
        ItemClassificationService.ItemTags itemTags = classificationService.classify(description, dict);
        category = itemTags.getCategory();
        productType = itemTags.getProductType();

        return new UploadItemDTO(imageAddr, productPageLink, description,
                price, currency, storeId, designer, imgExtension, productID, links, category, productType);
    }


    private UploadItemDTO terminalxDTO(String productPageLink, long storeId, WebDriver driver) {
        String productID = null;
        Category category;
        ProductType productType;
        String regexPattern = "x\\d{9}";
        Pattern MY_PATTERN = Pattern.compile(regexPattern);
        Matcher m = MY_PATTERN.matcher(productPageLink);
        int beginIndex = 1;
        int endIndex = 10;

        while (m.find()) {
            String s = m.group(0);
            productID = s;
            break;
        }
        if (productID == null) {
            throw new BadLinkException("this is not a product page");
        } else {
            productID = productID.substring(beginIndex, endIndex);
        }

        driver.get(productPageLink);
        Document document = Jsoup.parse(driver.getPageSource());
        Element descriptionDiv = document.select("span.base.attribute_name").first();
        String description = descriptionDiv.text();
        Element priceSpan = document.select("span.price").first();
        String fullPrice = priceSpan.text();
        ItemPriceCurr itemPriceCurr = priceTag(fullPrice);
        Currency currency = itemPriceCurr.currency;
        String price = itemPriceCurr.price;
        Element designerDiv = document.select("div.product-item-brand a").first();
        String designer = designerDiv.text();
        Elements imagesDiv = document.select("div#preview.magnifier-preview");
        Elements imageElements = imagesDiv.select("img");
        List<String> links = imageElements.eachAttr("src");
        String imgExtension = "jpg";
        String imageAddr = links.get(0);
        links.remove(0);

        Map<ProductType, List<String>> dict = classificationService.getHebrewDict();
        ItemClassificationService.ItemTags itemTags = classificationService.classify(description, dict);
        category = itemTags.getCategory();
        productType = itemTags.getProductType();

        return new UploadItemDTO(imageAddr, productPageLink, description,
                price, currency, storeId, designer, imgExtension, productID, links, category, productType);
    }


//    private UploadItemDTO adikaDTO(String website, String productPageLink) {
//        String productID;
//        List<String> links;
//        String designer = null;
//        Currency currency = Currency.ILS;
//        WebDriver driver = getDriver(productPageLink);
//        Document document = Jsoup.parse(driver.getPageSource());
//        Element imageDiv = document.select("div#image-zoom-0").first();
//        Element image = imageDiv.select("img").first();
//        String imageAddr = image.absUrl("src");
//        String imgExtension = "jpg";
//        int startIndex = imageAddr.length() - 14;
//        int endIndex = imageAddr.length() - 4;
//        productID = imageAddr.substring(startIndex, endIndex);
//        if (productID == null) {
//            System.out.println("this is not a product page");
//            return null;
//        }
//        Element descriptionDiv = document.select("div.product-name.only-tablet-desktop").first();
//        Element descriptionText = descriptionDiv.select("h1").first();
//        String description = descriptionText.text();
//        Element priceSpan = document.select("span.price").first();
//        String price = priceSpan.text();
//
//
//        Element elem = document.selectFirst(".more-views-thumbs");
//        Elements imageElements = elem.getElementsByTag("a");
//        links = imageElements.eachAttr("href");
//        links.remove(0);
//
//
//        Map<String, List<String>> dict = InitilizeItemsHebrewDict();
//        List<String> itemTags = classify(description, dict);

//        return new UploadItemDTO(imageAddr, productPageLink, description,
//                price, currency, website, designer, imgExtension, productID, links, itemTags);
//        return new UploadItemDTO();
//    }


    private UploadItemDTO farfetchDTO(String productPageLink, long storeId, WebDriver driver) {
        String productID = null;
        Category category;
        ProductType productType;

        driver.get(productPageLink);
        Document document = Jsoup.parse(driver.getPageSource());
        Element descriptionDiv = document.select("span._b4693b").first();
        String description = descriptionDiv.text();
        Element designerDiv = document.select("a._7fe79a._3f59ca._dc2535._f01e99 span").first();
        String designer = designerDiv.text();
        Element priceSpan = document.select("span._def925._b4693b").first();
        String fullPrice = priceSpan.text();
        ItemPriceCurr itemPriceCurr = priceTag(fullPrice);
        Currency currency = itemPriceCurr.currency;
        String price = itemPriceCurr.price;
        Elements imagesDiv = document.select("picture._61beb2._ef9cef");
        Elements imageElements = imagesDiv.select("img");
        List<String> links = imageElements.eachAttr("src");
        String imageAddr = links.get(0);
        links.remove(0);
        String imgExtension = "jpg";
        Pattern MY_PATTERN = Pattern.compile("\\d+");
        Matcher m = MY_PATTERN.matcher(productPageLink);

        while (m.find()) {
            String s = m.group(0);
            productID = s;
            break;
        }

        Map<ProductType, List<String>> dict = classificationService.getEnglishDict();
        ItemClassificationService.ItemTags itemTags = classificationService.classify(description, dict);
        category = itemTags.getCategory();
        productType = itemTags.getProductType();

        return new UploadItemDTO(imageAddr, productPageLink, description,
                price, currency, storeId, designer, imgExtension, productID, links, category, productType);
    }

    private UploadItemDTO sheinDTO(String productPageLink, long storeId, WebDriver driver) {
        String productID = null;
        Category category;
        ProductType productType;
        String price = null;
        ItemPriceCurr itemPriceCurr = null;
        List<String> links = new ArrayList<>();
        Currency currency;
        driver.get(productPageLink);
        Document document = Jsoup.parse(driver.getPageSource());
        Element descriptionDiv = document.select("h1.name").first();
        String description = descriptionDiv.text();
//        String designer = document.select("h1.name.span").first().text();
        String designer = null;
        Element priceSpan = document.select("div.price-origin.j-price-origin").first();
        Element discountedPriceSpan = document.select("div.price-discount.j-price-discount").first();
        if (discountedPriceSpan != null) {
            price = discountedPriceSpan.text();
        } else {
            price = priceSpan.text();
        }
        itemPriceCurr = priceTag(price);
        currency = itemPriceCurr.currency;
        price = itemPriceCurr.price;

        Element imageDiv = document.select("img.j-lazy-dpr-img.j-change-main_image").first();
        String imageAddr = imageDiv.attr("data-src");
        imageAddr = "https:" + imageAddr;
        String imgExtension = "jpg";
        Pattern MY_PATTERN = Pattern.compile("\\d+");
        Matcher m = MY_PATTERN.matcher(productPageLink);

        while (m.find()) {
            String s = m.group(0);
            productID = s;
            break;
        }
        Elements thumbnails = document.select("img.j-verlok-lazy.j-change-dt_image");
        for (Element imgThumbnail : thumbnails) {
            String imgSrc = imgThumbnail.attr("data-src");
            links.add(imgSrc);
        }
        for (int i = 0; i < links.size(); i++) {
            links.set(i, "https:" + links.get(i));
        }
        Map<ProductType, List<String>> dict = classificationService.getHebrewDict();
        ItemClassificationService.ItemTags itemTags = classificationService.classify(description, dict);
        category = itemTags.getCategory();
        productType = itemTags.getProductType();
        if (productType == ProductType.Default) {
            dict = classificationService.getEnglishDict();
            itemTags = classificationService.classify(description, dict);
            category = itemTags.getCategory();
            productType = itemTags.getProductType();
        }


        return new UploadItemDTO(imageAddr, productPageLink, description,
                price, currency, storeId, designer, imgExtension, productID, links, category, productType);
    }


    private String correctZaraLink(String productPageLink) {
        String result = productPageLink.replaceFirst("/share/", "/il/en/");
        return result;
    }

    private UploadItemDTO zaraDTO(String productPageLink, long storeId, WebDriver driver) {
        Category category;
        ProductType productType;
        productPageLink = correctZaraLink(productPageLink);
        driver.get(productPageLink);
        Document document = Jsoup.parse(driver.getPageSource());
        Element descriptionDiv = document.select(" h1.product-name").first();
        String description = descriptionDiv.text();
        description = description.replace("פרטי", "");
        Element priceSpan = document.select("div.price._product-price span").first();
        String fullPrice = priceSpan.text();
        ItemPriceCurr itemPriceCurr = priceTag(fullPrice);
        Currency currency = itemPriceCurr.currency;
        String price = itemPriceCurr.price;
        Element fullDescriptionDiv = document.select("p.description").first();
        String designer = "";
        String imgExtension = "jpg";
        Elements elem = document.select("div.media-wrap.image-wrap a");
        List<String> links = elem.eachAttr("href");
        for (int i = 0; i < links.size(); i++) {
            links.set(i, "https:" + links.get(i));
        }
        String imageAddr = links.get(0);
        links.remove(0);
        int endIndex = productPageLink.indexOf("html");
        int startIndex = endIndex - 9;
        String productID = productPageLink.substring(startIndex, endIndex - 1);

        Map<ProductType, List<String>> dict = classificationService.getHebrewDict();
        ItemClassificationService.ItemTags itemTags = classificationService.classify(description, dict);
        if (itemTags.getProductType() == ProductType.Default) {
            dict = classificationService.getEnglishDict();
            itemTags = classificationService.classify(description, dict);
        }
        category = itemTags.getCategory();
        productType = itemTags.getProductType();

        return new UploadItemDTO(imageAddr, productPageLink, description,
                price, currency, storeId, designer, imgExtension, productID, links, category, productType);
    }

    private UploadItemDTO hmDTO(String productPageLink, long storeId, WebDriver driver) {
        String designer = null;
        Category category;
        ProductType productType;
        Currency currency = Currency.USD;
        driver.get(productPageLink);
        Document document = Jsoup.parse(driver.getPageSource());
        Element descriptionDiv = document.select(" h1.primary.product-item-headline").first();
        String description = descriptionDiv.text();
        Element priceSpan = document.select("span.price-value").first();
        String price = priceSpan.text();
        Element imageDiv = document.select("div.product-detail-main-image-container img").first();
        String imageAddr = imageDiv.attr("src");
        String imgExtension = "jpg";
        Elements elem = document.select("figure.pdp-secondary-image.pdp-image img");
        List<String> links = elem.eachAttr("src");
        links.add(imageAddr);
        for (int i = 0; i < links.size(); i++) {
            links.set(i, "https:" + links.get(i));
        }
        imageAddr = links.get(0);
        links.remove(0);
        int endIndex = productPageLink.indexOf("html");
        int startIndex = endIndex - 11;
        String productID = productPageLink.substring(startIndex, endIndex - 1);

        Map<ProductType, List<String>> dict = classificationService.getEnglishDict();
        ItemClassificationService.ItemTags itemTags = classificationService.classify(description, dict);
        category = itemTags.getCategory();
        productType = itemTags.getProductType();

        return new UploadItemDTO(imageAddr, productPageLink, description,
                price, currency, storeId, designer, imgExtension, productID, links, category, productType);

    }


    private UploadItemDTO shopBopDTO(String productPageLink, long storeId, WebDriver driver) {
        Category category;
        ProductType productType;
        driver.get(productPageLink);
        Document document = Jsoup.parse(driver.getPageSource());
        Element descriptionDiv = document.select(" div#product-title").first();
        String description = descriptionDiv.text();
        String designer = document.select("span.brand-name").first().text();
        Element priceSpan = document.select("span.pdp-price").first();
        String fullPrice = priceSpan.text();
        ItemPriceCurr itemPriceCurr = priceTag(fullPrice);
        Currency currency = itemPriceCurr.currency;
        String price = itemPriceCurr.price;
        Elements elem = document.select("img.display-image");
        List<String> links = elem.eachAttr("src");
        String imageAddr = links.get(0);
        String imgExtension = "jpg";
        links.remove(0);
        int endIndex = productPageLink.indexOf("htm");
        int startIndex = endIndex - 11;
        String productID = productPageLink.substring(startIndex, endIndex - 1);

        Map<ProductType, List<String>> dict = classificationService.getEnglishDict();
        ItemClassificationService.ItemTags itemTags = classificationService.classify(description, dict);
        category = itemTags.getCategory();
        productType = itemTags.getProductType();

        return new UploadItemDTO(imageAddr, productPageLink, description,
                price, currency, storeId, designer, imgExtension, productID, links, category, productType);
    }

}
