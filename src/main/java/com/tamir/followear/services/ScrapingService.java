package com.tamir.followear.services;

import com.tamir.followear.dto.UploadItemDTO;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.*;

import com.tamir.followear.enums.Currency;

@Service
public class ScrapingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScrapingService.class);

    @Value("${fw.chrome.driver}")
    private String chromedriverPath;

    @Value("${fw.chrome.binary}")
    private String chromeBinary;

    @PostConstruct
    public void init() throws IOException {
        System.setProperty("webdriver.chrome.driver", chromedriverPath);
    }

    private WebDriver getDriver(String productPageLink) {
        ChromeOptions options = new ChromeOptions();

        options.setBinary(chromeBinary);
        options.addArguments("--headless", "--no-sandbox", "--disable-gpu", "--window-size=1280x1696",
                "--user-data-dir=/tmp/user-data", "--hide-scrollbars", "--enable-logging",
                "--log-level=0", "--v=99", "--single-process", "--data-path=/tmp/data-path",
                "--ignore-certificate-errors", "--homedir=/tmp", "--disk-cache-dir=/tmp/cache-dir",
                "user-agent=Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36" +
                        " (KHTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36");

        WebDriver driver = new ChromeDriver(options);
        driver.get(productPageLink);
        return driver;
    }

    public UploadItemDTO extractItem(String website, String productPageLink) {
        UploadItemDTO itemDTO = new UploadItemDTO();

        switch (website) {
            case "asos":
                itemDTO = asosDTO(website, productPageLink);
                break;
            case "net-a-porter":
                itemDTO = netaporterDTO(website, productPageLink);
                break;
            case "adikastyle":
                itemDTO = adikaDTO(website, productPageLink);
                break;
            case "terminalx":
                itemDTO = terminalxDTO(website, productPageLink);
                break;
            case "farfetch":
                itemDTO = farfetchDTO(website, productPageLink);
                break;
            case "shein":
                itemDTO = sheinDTO(website, productPageLink);
                break;
            case "zara":
                itemDTO = zaraDTO(website, productPageLink);
                break;
            case "hm":
                itemDTO = hmDTO(website, productPageLink);
                break;
            case "shopbop":
                itemDTO = shopBopDTO(website, productPageLink);
                break;
        }
        return itemDTO;
    }

    public List<String> getThumbnailImages(long storeId, String productPageLink) {
        List<String> links = new ArrayList<>();

        try {

            switch ((int) storeId) {
                case 1:
                    links = asosThumbnails(productPageLink);
                    break;
                case 2:
                    links = netAPorterThumbnails(productPageLink);
                    break;
                case 3:
                    links = terminalXThumbnails(productPageLink);
                    break;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return links;
    }

    //TODO: replace addresses with the addresses of better image qualities
    private List<String> asosThumbnails(String productPageLink) throws IOException {
        List<String> links;
        Document document = Jsoup.connect(productPageLink).get();
        Element elem = document.selectFirst(".thumbnails");
        Elements imageElements = elem.getElementsByTag("img");
        links = imageElements.eachAttr("src");
        links.remove(0);
        return links;
    }

    private List<String> netAPorterThumbnails(String productPageLink) throws IOException {
        List<String> links;
        Document document = Jsoup.connect(productPageLink).get();
        Element elem = document.selectFirst(".thumbnail-wrapper");
        Elements imageElements = elem.getElementsByTag("img");
        links = imageElements.eachAttr("src");
        links.remove(0);

        for (int i = 0; i < links.size(); i++) {
            links.set(i, "https:" + links.get(i));
        }

        return links;
    }

    //TODO: replace addresses with the addresses of better image qualities
    private List<String> terminalXThumbnails(String productPageLink) {
        List<String> links;
        WebDriver driver = getDriver(productPageLink);
        Document document = Jsoup.parse(driver.getPageSource());

        Elements elements = document.select(".fotorama__thumb img");
        links = elements.eachAttr("src");
        links.remove(0);

        driver.close();
        return links;
    }


    public Map<String, List<String>> InitilizeItemsHebrewDict() {
        Map<String, List<String>> hebrewDictionary = new HashMap<>();
        List<String> topsValues = Arrays.asList("שירט", "אוברול", "חולצ", "גופיי", "סווט", "סווד", "טופ", "בגד גוף");
        List<String> dressValues = Arrays.asList("שמלת", "חצאית");
        List<String> pantsValues = Arrays.asList("ג'ינס", "שורטס", "מכנס");
        List<String> shoesValues = Arrays.asList("נעל", "spadrilles",
                "קבקבי", "סנדל", "מגפ", "מגף");
        List<String> coatsAndJacketsValues = Arrays.asList("ג'קט", "מעיל", "וסט", "ז'קט");
        List<String> swimwearValues = Arrays.asList("בגד ים", "ביקיני");
        List<String> accesoriesValues = Arrays.asList("תכשיט",
                "משקפי שמש",
                "משקפיי",
                "חגור",
                "כובע",
                "גרבי",
                "מטפחת",
                "צעיף",
                "עגיל",
                "קשת");

        hebrewDictionary.put("top", topsValues);
        hebrewDictionary.put("dress", dressValues);
        hebrewDictionary.put("pants", pantsValues);
        hebrewDictionary.put("shoes", shoesValues);
        hebrewDictionary.put("coatsAndJackets", coatsAndJacketsValues);
        hebrewDictionary.put("swimwear", swimwearValues);
        hebrewDictionary.put("accessories", accesoriesValues);

        return hebrewDictionary;
    }


    public Map<String, List<String>> InitilizeItemsEnglishDict() {

        Map<String, List<String>> englishDictionary = new HashMap<>();
        List<String> topsValues = Arrays.asList("top", "tee", "weater", "jumper", "hirt", "tank",
                "cami", "bodysuit", "blouse", "bandeau", "vest", "singlet", "body",
                "hoodie", "sweatshirt", "pullover", "turtleneck", "polo", "tunic");
        List<String> dressValues = Arrays.asList("dress", "skirt");
        List<String> pantsValues = Arrays.asList("pants", "trousers",
                "legging", "short", "jeans");
        List<String> shoesValues = Arrays.asList("shoes", "spadrilles",
                "heel", "boots", "trainers", "slippers", "sandals", "runner", "slider", "sneakers");
        List<String> coatsAndJacketsValues = Arrays.asList("vest", "blazer",
                "coat", "jacket", "waistcoat", "pullover", "parka", "poncho", "bomber", "suit",
                "duster", "kimono", "wrap");
        List<String> bagValues = Arrays.asList("bag", "tote",
                "clutch", "crossbody", "cross-body", "wallet", "backpack", "satchel", "handbag",
                "basket", "clutch-bag", "handbag");

        englishDictionary.put("top", topsValues);
        englishDictionary.put("dress", dressValues);
        englishDictionary.put("pants", pantsValues);
        englishDictionary.put("shoes", shoesValues);
        englishDictionary.put("coatsAndJackets", coatsAndJacketsValues);
        englishDictionary.put("bag", bagValues);

        return englishDictionary;
    }


    public List<String> itemClassification(String productDescription, Map<String, List<String>> dict) {
        List<String> itemTags = new ArrayList<>();
        Boolean pantsKey = false;
        Boolean otherJeansKey = false;
        for (Map.Entry<String, List<String>> entry : dict.entrySet()) {
            String key = entry.getKey();
            List<String> value = entry.getValue();
            for (String aString : value) {
                if (productDescription.toLowerCase().contains(aString)) {
                    itemTags.add(key);
                    if (key == "pants") {
                        pantsKey = true;
                    }
                    if ((key == "coatsAndJackets") || (key == "top") || (key == "dress")) {
                        otherJeansKey = true;
                    }
                    if (pantsKey && otherJeansKey) {
                        itemTags.remove("pants");
                    }
                }
            }
        }

        return itemTags;
    }


    public List<Object> priceTag(String fullPrice) {
        String price = "";
        String itemCurrency = "";
        for (int i = 0; i < fullPrice.length(); i++) {
            char c = fullPrice.toLowerCase().charAt(i);
            if (c >= '0' && c <= '9' || c == '.') {
                price += c;
            }
            if (c >= 'a' && c <= 'z') {
                itemCurrency += c;
            }
            //TODO: check how ASOS displays price when browsing from AWS
            if (c == '$') {
                itemCurrency = "USD";

            }
        }
        return Arrays.asList(price, itemCurrency);
    }


    private UploadItemDTO asosDTO(String website, String productPageLink) {
        String productID;
        int beginIndex = productPageLink.indexOf("/prd/");
        if (beginIndex == -1) {
            System.out.println("this is not a product page");
            return null;
        }
        beginIndex = beginIndex + 5;
        int endIndex = beginIndex + 8;
        productID = productPageLink.substring(beginIndex, endIndex);
        String designer = null;

        Currency currency = Currency.ILS;
        WebDriver driver = getDriver(productPageLink);
        Document document = Jsoup.parse(driver.getPageSource());
        Element descriptionDiv = document.select("div.product-hero").first();
        Element descriptionText = descriptionDiv.select("h1").first();
        String description = descriptionText.text();
        Element priceSpan = document.select("span.current-price").first();
        String fullPrice = priceSpan.text();
        List<Object> priceRes = priceTag(fullPrice);
        String price = String.valueOf(priceRes.get(0));
        String itemCurrency = String.valueOf(priceRes.get(1));
        Elements imagesDiv = document.select("div.fullImageContainer");
        Elements images = imagesDiv.select("img");
        String imgExtension = "jpg";

        List<String> links = images.eachAttr("src");
        String imageAddr = links.get(0);
        links.remove(0);
        driver.close();

        Map<String, List<String>> dict = InitilizeItemsEnglishDict();
        List<String> itemTags = itemClassification(description, dict);

//        return new UploadItemDTO(imageAddr, productPageLink, description,
//                price, currency, website, designer, imgExtension, productID, links, itemTags);
        return new UploadItemDTO();
    }

    private UploadItemDTO netaporterDTO(String website, String productPageLink) {
        String productID;
        int beginIndex = productPageLink.indexOf("/product/");
        if (beginIndex == -1) {
            System.out.println("this is not a product page");
            return null;
        }
        beginIndex = beginIndex + 9;
        int endIndex = beginIndex + 7;
        productID = productPageLink.substring(beginIndex, endIndex);
        Currency currency = Currency.GBP;
        WebDriver driver = getDriver(productPageLink);
        Document document = Jsoup.parse(driver.getPageSource());
        Element descriptionDiv = document.select(" h2.product-name").first();
        String description = descriptionDiv.text();
        Element priceSpan = document.select("span.full-price.style-scope.nap-price").first();
        String price = priceSpan.text();
        Element designerDiv = document.select("a.designer-name span").first();
        String designer = designerDiv.text();
        Element imageDiv = document.select("img.product-image.first-image").first();
        String imageAddr = imageDiv.absUrl("src");
        String imgExtension = "jpg";

        Element elem = document.selectFirst(".thumbnail-wrapper");
        Elements imageElements = elem.getElementsByTag("img");
        List<String> links = imageElements.eachAttr("src");
        links.remove(0);

        for (int i = 0; i < links.size(); i++) {
            links.set(i, "https:" + links.get(i));
        }
        driver.close();

        Map<String, List<String>> dict = InitilizeItemsEnglishDict();
        List<String> itemTags = itemClassification(description, dict);

//        return new UploadItemDTO(imageAddr, productPageLink, description,
//                price, currency, website, designer, imgExtension, productID, links, itemTags);
        return new UploadItemDTO();
    }


    private UploadItemDTO terminalxDTO(String website, String productPageLink) {
        String productID = null;
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
            System.out.println("this is not a product page");
            return null;
        } else {
            productID = productID.substring(beginIndex, endIndex);
        }

        Currency currency = Currency.ILS;
        WebDriver driver = getDriver(productPageLink);
        Document document = Jsoup.parse(driver.getPageSource());
        Element descriptionDiv = document.select("span.base.attribute_name").first();
        String description = descriptionDiv.text();
        Element priceSpan = document.select("span.price").first();
        String price = priceSpan.text();

        Element designerDiv = document.select("div.product-item-brand a").first();
        String designer = designerDiv.text();

        Elements imagesDiv = document.select(
                "div.fotorama__thumb.fotorama_vertical_ratio.fotorama__loaded.fotorama__loaded--img");
        Elements imageElements = imagesDiv.select("img");
        List<String> links = imageElements.eachAttr("src");
        String imgExtension = "jpg";
        String imageAddr = links.get(0);
        links.remove(0);

        driver.close();

        Map<String, List<String>> dict = InitilizeItemsHebrewDict();
        List<String> itemTags = itemClassification(description, dict);

//        return new UploadItemDTO(imageAddr, productPageLink, description,
//                price, currency, website, designer, imgExtension, productID, links, itemTags);
        return new UploadItemDTO();
    }


    private UploadItemDTO adikaDTO(String website, String productPageLink) {
        String productID;
        List<String> links;
        String designer = null;
        Currency currency = Currency.ILS;
        WebDriver driver = getDriver(productPageLink);
        Document document = Jsoup.parse(driver.getPageSource());
        Element imageDiv = document.select("div#image-zoom-0").first();
        Element image = imageDiv.select("img").first();
        String imageAddr = image.absUrl("src");
        String imgExtension = "jpg";
        int startIndex = imageAddr.length() - 14;
        int endIndex = imageAddr.length() - 4;
        productID = imageAddr.substring(startIndex, endIndex);
        if (productID == null) {
            System.out.println("this is not a product page");
            return null;
        }
        Element descriptionDiv = document.select("div.product-name.only-tablet-desktop").first();
        Element descriptionText = descriptionDiv.select("h1").first();
        String description = descriptionText.text();
        Element priceSpan = document.select("span.price").first();
        String price = priceSpan.text();


        Element elem = document.selectFirst(".more-views-thumbs");
        Elements imageElements = elem.getElementsByTag("a");
        links = imageElements.eachAttr("href");
        links.remove(0);

        driver.close();

        Map<String, List<String>> dict = InitilizeItemsHebrewDict();
        List<String> itemTags = itemClassification(description, dict);

//        return new UploadItemDTO(imageAddr, productPageLink, description,
//                price, currency, website, designer, imgExtension, productID, links, itemTags);
        return new UploadItemDTO();
    }


    private UploadItemDTO farfetchDTO(String website, String productPageLink) {
        String productID = null;
        List<String> links = new ArrayList<>();
        Elements thumbnails;
        Currency currency = Currency.USD;
        WebDriver driver = getDriver(productPageLink);
        Document document = Jsoup.parse(driver.getPageSource());
        Element descriptionDiv = document.select("span._b4693b").first();
        String description = descriptionDiv.text();
        Element designerDiv = document.select("a._7fe79a._3f59ca._dc2535._f01e99 span").first();
        String designer = designerDiv.text();
        Element priceSpan = document.select("div._5ebd85 span").first();
        String price = priceSpan.text();
        Element image = document.select("img._4b16f7").first();

        if (image == null) {
            image = document.select("div._190541._8a3ab8 img").first();
            thumbnails = document.select("div._190541._8a3ab8 img");
        } else {
            thumbnails = document.select("img._4b16f7");
        }
        for (Element imgThumbnail : thumbnails) {
            String imgSrc = imgThumbnail.absUrl("src");
            links.add(imgSrc);
        }

        String imageAddr = image.absUrl("src");
        String imgExtension = "jpg";
        Pattern MY_PATTERN = Pattern.compile("\\d+");
        Matcher m = MY_PATTERN.matcher(productPageLink);

        while (m.find()) {
            String s = m.group(0);
            productID = s;
            break;
        }
        driver.close();

        Map<String, List<String>> dict = InitilizeItemsEnglishDict();
        List<String> itemTags = itemClassification(description, dict);

//        return new UploadItemDTO(imageAddr, productPageLink, description,
//                price, currency, website, designer, imgExtension, productID, links, itemTags);
        return new UploadItemDTO();
    }

    private UploadItemDTO sheinDTO(String website, String productPageLink) {
        String productID = null;
        String price = null;
        List<String> links = new ArrayList<>();
        Currency currency = Currency.ILS;
        WebDriver driver = getDriver(productPageLink);
        Document document = Jsoup.parse(driver.getPageSource());
        Element descriptionDiv = document.select("h1.name").first();
        String description = descriptionDiv.text();
        String designer = document.select("h1.name span").first().text();
        Element priceSpan = document.select("div.price-origin j-price-origin").first();
        if (priceSpan != null) {
            price = priceSpan.text();
        }
        if (priceSpan == null) {
            priceSpan = document.select("div.price-discount.j-price-discount.she-color-black").first();
            price = priceSpan.text();
        }

        Element imageDiv = document.select("img.j-lazy-dpr-img.j-change-main_image").first();
        String imageAddr = imageDiv.attr("src");
        String imgExtension = "jpg";
        Pattern MY_PATTERN = Pattern.compile("\\d+");
        Matcher m = MY_PATTERN.matcher(productPageLink);

        while (m.find()) {
            String s = m.group(0);
            productID = s;
            break;
        }
        driver.close();
        Elements thumbnails = document.select("img.j-verlok-lazy.j-change-dt_image");
        for (Element imgThumbnail : thumbnails) {
            String imgSrc = imgThumbnail.attr("src");
            links.add(imgSrc);
        }

        Map<String, List<String>> dict = InitilizeItemsEnglishDict();
        List<String> itemTags = itemClassification(description, dict);

//        return new UploadItemDTO(imageAddr, productPageLink, description,
//                price, currency, website, designer, imgExtension, productID, links, itemTags);
        return new UploadItemDTO();
    }


    private UploadItemDTO zaraDTO(String website, String productPageLink) {
        Currency currency = Currency.ILS;
        WebDriver driver = getDriver(productPageLink);
        Document document = Jsoup.parse(driver.getPageSource());
        Element descriptionDiv = document.select(" h1.product-name").first();
        String description = descriptionDiv.text();
        Element priceSpan = document.select("div.price._product-price span").first();
        String price = priceSpan.text();
        Element fullDescriptionDiv = document.select("p.description").first();
        String designer = fullDescriptionDiv.text();
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
        driver.close();

        Map<String, List<String>> dict = InitilizeItemsHebrewDict();
        List<String> itemTags = itemClassification(description, dict);

//        return new UploadItemDTO(imageAddr, productPageLink, description,
//                price, currency, website, designer, imgExtension, productID, links, itemTags);
        return new UploadItemDTO();
    }

    private UploadItemDTO hmDTO(String website, String productPageLink) {
        String designer = null;
        Currency currency = Currency.USD;
        WebDriver driver = getDriver(productPageLink);
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
        driver.close();

        Map<String, List<String>> dict = InitilizeItemsEnglishDict();
        List<String> itemTags = itemClassification(description, dict);

//        return new UploadItemDTO(imageAddr, productPageLink, description,
//                price, currency, website, designer, imgExtension, productID, links, itemTags);
        return new UploadItemDTO();
    }


    private UploadItemDTO shopBopDTO(String website, String productPageLink) {
        Currency currency = Currency.USD;
        WebDriver driver = getDriver(productPageLink);
        Document document = Jsoup.parse(driver.getPageSource());
        Element descriptionDiv = document.select(" div#product-title").first();
        String description = descriptionDiv.text();
        String designer = document.select("span.brand-name").first().text();
        Element priceSpan = document.select("span.pdp-price").first();
        String price = priceSpan.text();
        Element imageDiv = document.select("div.product-detail-main-image-container img").first();

        Elements elem = document.select("img.display-image");
        List<String> links = elem.eachAttr("src");
        String imageAddr = links.get(0);
        String imgExtension = "jpg";
        links.remove(0);
        int endIndex = productPageLink.indexOf("htm");
        int startIndex = endIndex - 11;
        String productID = productPageLink.substring(startIndex, endIndex - 1);
        driver.close();

        Map<String, List<String>> dict = InitilizeItemsEnglishDict();
        List<String> itemTags = itemClassification(description, dict);

//        return new UploadItemDTO(imageAddr, productPageLink, description,
//                price, currency, website, designer, imgExtension, productID, links, itemTags);
        return new UploadItemDTO();
    }

}
