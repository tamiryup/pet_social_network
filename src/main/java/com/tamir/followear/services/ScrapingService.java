package com.tamir.followear.services;

import com.tamir.followear.dto.UploadItemDTO;
import com.tamir.followear.entities.Store;
import com.tamir.followear.enums.Category;
import com.tamir.followear.enums.ProductType;
import com.tamir.followear.exceptions.BadLinkException;
import com.tamir.followear.exceptions.ScrapingError;
import com.tamir.followear.helpers.StringHelper;
import lombok.ToString;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
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
    Boolean israeliWebsite = false;

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
            throw new BadLinkException("This website is not supported");
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
            throw new BadLinkException("Invalid link");
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
                case "revolve.com":
                    itemDTO = revolveDTO(productPageLink, storeId, driver);
                    break;
                case "factory54.co.il":
                    itemDTO = factoryDTO(productPageLink, storeId, driver);
                    break;
                case "topshop.com":
                    itemDTO = topshopDTO(productPageLink, storeId, driver);
                    break;
                case "mytheresa.com":
                    itemDTO = mytheresaDTO(productPageLink, storeId, driver);
                    break;
                default:
                    throw new BadLinkException("This website is not supported");
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
            beginIndex = productPageLink.indexOf("/grp/");
            if (beginIndex == -1) {
                throw new BadLinkException("This is not a product page");
            }else{
                beginIndex = beginIndex + 5;
                endIndex = beginIndex + 5;
            }
        } else {
            beginIndex = beginIndex + 5;
            endIndex = beginIndex + 8;
        }
        productID = productPageLink.substring(beginIndex, endIndex);
        driver.get(productPageLink);
        Document document = Jsoup.parse(driver.getPageSource());
        Elements descriptionDiv = document.select("div.product-hero");
        String description = descriptionDiv.select("h1").text();
        String price="";
        String salePrice="";
        Currency currency = Currency.GBP;
        try {
            price = document.selectFirst("span.product-prev-price[data-id='previous-price']").text();
            ItemPriceCurr itemPriceCurr = priceTag(price);
            price = itemPriceCurr.price;
            salePrice = document.select("span.current-price.product-price-discounted").first().text();
            ItemPriceCurr itemPriceCurrSale = priceTag(salePrice);
            currency = itemPriceCurrSale.currency;
            salePrice = itemPriceCurrSale.price;
        }catch(NullPointerException e){
            price = document.select("span.current-price").first().text();
            ItemPriceCurr itemPriceCurr = priceTag(price);
            currency = itemPriceCurr.currency;
            price = itemPriceCurr.price;
        }
        Elements imagesDiv = document.select("div.fullImageContainer");
        Elements images = imagesDiv.select("img");
        String imgExtension = "jpg";
        List<String> links = images.eachAttr("src");
        String imageAddr = links.get(1);
        links.remove(1);
        links.remove(3);

        Map<String, ProductType> dict = classificationService.getEnglishDict();
        ItemClassificationService.ItemTags itemTags = classificationService.classify(description, dict);

        category = itemTags.getCategory();
        productType = itemTags.getProductType();

        return new UploadItemDTO(imageAddr, productPageLink, description,
                price, salePrice, currency, storeID, designer, imgExtension, productID, links, category, productType);
    }

    private UploadItemDTO netaporterDTO(String productPageLink, long storeId, WebDriver driver) {
        String productID;
        Category category;
        ProductType productType;
        String price=null;
        String salePrice="";
        String priceSymbol;
        driver.get(productPageLink);
        Document document = Jsoup.parse(driver.getPageSource());
        productID = driver.findElement(By.xpath("//meta[@itemprop='productID']"))
                .getAttribute("content");
        if (productID == null){
            throw new BadLinkException("This is not a product page");
        }
        Element descriptionDiv = document.select(" p.ProductInformation77__name").first();
        String description = descriptionDiv.text();
        price = driver.findElement(By.xpath("//span[@itemprop='price']"))
                .getAttribute("content");
        priceSymbol = driver.findElement(By.xpath("//meta[@itemprop='priceCurrency']"))
                  .getAttribute("content");

        ItemPriceCurr itemPriceCurr = priceTag(priceSymbol);
        Currency currency = itemPriceCurr.currency;
        Element designerDiv = document.select("h1.ProductInformation77__designer").first();
        String designer = designerDiv.text();
        String imageAddr = "";
        String imgExtension = "jpg";
        Elements imageDiv = document.select(".Image17__imageContainer.ImageCarousel77__thumbnailImage");
        Elements imageElements = imageDiv.select("img");
        List<String> links = imageElements.eachAttr("src");

        int endOfThumbnails = 3;
        int size = links.size();
        int maxThumbnails = Math.min(endOfThumbnails, size);
        links = links.subList(0, maxThumbnails);
        for (int i = 0; i < links.size(); i++) {
            links.set(i, links.get(i));
        }
        if (size > 0) {
            imageAddr = links.get(0);
        }
        links.remove(0);
        Map<String, ProductType> dict = classificationService.getEnglishDict();
        ItemClassificationService.ItemTags itemTags = classificationService.classify(description, dict);
        category = itemTags.getCategory();
        productType = itemTags.getProductType();

        return new UploadItemDTO(imageAddr, productPageLink, description,
                price, salePrice, currency, storeId, designer, imgExtension, productID, links, category, productType);
    }

    String getNetaporterPriceSymbol(String price) {
        String priceSymbol="$";
        if (price.contains("$")) {
            priceSymbol = "$";
        } else if (price.contains("₪")) {
            priceSymbol = "₪";
        } else if (price.contains("€")) {
            priceSymbol = "€";
        } else if (price.contains("£")) {
            priceSymbol = "£";
        }

        return priceSymbol;
    }

    boolean isStringOnlyAlphabet(String str)
    {
        return ((!str.equals(""))
                && (str != null)
                && (str.matches("^[a-zA-Z]*$")));
    }



    private UploadItemDTO terminalxDTO(String productPageLink, long storeId, WebDriver driver) {

        Category category;
        ProductType productType;
        driver.get(productPageLink);
        String price="";
        String salePrice="";
        Currency currency = Currency.ILS;
        Document document = Jsoup.parse(driver.getPageSource());
        String description = driver.findElement(By.xpath("//span[@itemprop='name']")).getText();
        String productPageType = document.select(".product-item-brand").first().attr(
                "data-div-top");
        if (("גברים".equals(productPageType)) || ("נשים".equals(productPageType))){

        }else{
            throw new BadLinkException("This product can't be shared");
        }
        String productID = document.select(".price-box.price-final_price").first().attr(
                "data-product-id");

        try {
            price = document.select("span#old-price-"+productID).first().attr("data-price-amount");
            ItemPriceCurr itemPriceCurr = priceTag(price);
            price = itemPriceCurr.price;
            salePrice = document.select("span#product-price-"+productID).first().attr("data-price-amount");
            ItemPriceCurr itemPriceCurrSale = priceTag(salePrice);
            salePrice = itemPriceCurrSale.price;
        }catch(NullPointerException e){
            price =  document.select("span#product-price-"+productID).first().attr("data-price-amount");
            ItemPriceCurr itemPriceCurr = priceTag(price);
            price = itemPriceCurr.price;
        }

        String designer = document.select("div.product-item-brand a").first().text();
        Elements imagesDiv = document.select("img.fotorama__img");
        List<String> allThumbnailsLinks = imagesDiv.eachAttr("src");
        String imgExtension = "jpg";
        String imageAddr = allThumbnailsLinks.get(0);
        allThumbnailsLinks.remove(0);
        List<String> links = new ArrayList<>();
        if (allThumbnailsLinks.size()>1){
            links.add(allThumbnailsLinks.get(1));
        }

        Map<String, ProductType> dict = classificationService.getHebrewDict();
        ItemClassificationService.ItemTags itemTags = classificationService.classify(description, dict);
        category = itemTags.getCategory();
        productType = itemTags.getProductType();

        return new UploadItemDTO(imageAddr, productPageLink, description,
                price,salePrice, currency, storeId, designer, imgExtension, productID, links, category, productType);
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
        String productID = driver.findElement(By.xpath("//meta[@itemprop='productID']")).getAttribute("content");
        System.out.println(productID);
//        if (productID == null){
//            throw new BadLinkException("This isn't a product page");
//        }
        Category category;
        ProductType productType;
        String salePrice="";
        String price="";
        driver.get(productPageLink);
        Document document = Jsoup.parse(driver.getPageSource());
        String description = document.select("span._d85b45._1851d6").first().text();
        String designer = document.select("span._e87472._346238._e4b5ec").first().text();
        Currency currency = Currency.USD;
        try {
            price = document.select("span._89a1d3._b764f1").first().text();
            ItemPriceCurr itemPriceCurr = priceTag(price);
            price = itemPriceCurr.price;
            salePrice = document.select("strong._e806a1._366381._d85b45").first().text();
            ItemPriceCurr itemPriceCurrSale = priceTag(salePrice);
            currency = itemPriceCurrSale.currency;
            salePrice = itemPriceCurrSale.price;
        }catch(NullPointerException e){
            price = document.select("span._e806a1._d85b45").first().text();
            ItemPriceCurr itemPriceCurr = priceTag(price);
            currency = itemPriceCurr.currency;
            price = itemPriceCurr.price;
        }
        Elements imagesDiv = document.select("picture._492380._f8a733");
        Elements imageElements = imagesDiv.select("img");
        List<String> links = imageElements.eachAttr("src");
        String imageAddr = links.get(0);
        links.remove(0);
        String imgExtension = "jpg";



        Map<String, ProductType> dict = classificationService.getEnglishDict();
        ItemClassificationService.ItemTags itemTags = classificationService.classify(description, dict);
        category = itemTags.getCategory();
        productType = itemTags.getProductType();

        return new UploadItemDTO(imageAddr, productPageLink, description,
                price,salePrice, currency, storeId, designer, imgExtension, productID, links, category, productType);
    }

    private UploadItemDTO sheinDTO(String productPageLink, long storeId, WebDriver driver) {
        String productID = null;
        Category category;
        ProductType productType;
        String price = null;
        List<String> links = new ArrayList<>();
        driver.get(productPageLink);
        Document document = Jsoup.parse(driver.getPageSource());
        Element descriptionDiv = document.select("div.product-intro__head-name").first();
        String description = descriptionDiv.text();
        String designer = null;
        String salePrice="";
        Currency currency = Currency.USD;
        try {
            price = document.select("div.product-intro__head-price del.del-price").first().text();
            ItemPriceCurr itemPriceCurr = priceTag(price);
            price = itemPriceCurr.price;
            salePrice = document.select("div.product-intro__head-price span.discount").first().text();
            ItemPriceCurr itemPriceCurrSale = priceTag(salePrice);
            currency = itemPriceCurrSale.currency;
            salePrice = itemPriceCurrSale.price;
        }catch(NullPointerException e){
            price = document.select("div.product-intro__head-price span.original").first().text();
            ItemPriceCurr itemPriceCurr = priceTag(price);
            currency = itemPriceCurr.currency;
            price = itemPriceCurr.price;
        }

        Element imageDiv = document.select("div.swiper-slide.product-intro__main-item.cursor-zoom-in.swiper-slide-active").first().attr("data-swiper-slide-index","0");
        String imageAddr = imageDiv.select("img.j-verlok-lazy.loaded").attr("src");
        imageAddr = "https:" + imageAddr;
        String correctImageAddr = imageAddr.replace(".webp",".jpg");
        String imgExtension = "jpg";
        Pattern MY_PATTERN = Pattern.compile("\\d+");
        Matcher m = MY_PATTERN.matcher(productPageLink);

        while (m.find()) {
            String s = m.group(0);
            productID = s;
            break;
        }
        Elements thumbnail = document.select("div.swiper-slide.product-intro__main-item.cursor-zoom-in.swiper-slide-next");
        String imgSrc = thumbnail.select("img.j-verlok-lazy.loaded").attr("src");
        imgSrc = "https:" + imgSrc;
        String correctImgSrc = imgSrc.replace(".webp",".jpg");
        links.add(correctImgSrc);
        Map<String, ProductType> dict = classificationService.getEnglishDict();
        ItemClassificationService.ItemTags itemTags = classificationService.classify(description, dict);
        category = itemTags.getCategory();
        productType = itemTags.getProductType();
        return new UploadItemDTO(correctImageAddr, productPageLink, description,
                price,salePrice, currency, storeId, designer, imgExtension, productID, links, category, productType);
    }


    private String correctZaraLink(String productPageLink) {
        String result = productPageLink.replaceFirst("/share/", "/il/en/");

        if(StringHelper.doesContainHebrew(productPageLink)) {
            this.israeliWebsite = true;
            int startIndex = result.lastIndexOf('/') + 1;
            int htmlStringIndex = result.indexOf(".html");
            int endIndex = result.lastIndexOf("-p", htmlStringIndex);
            int linkLength = result.length();
            result = result.substring(0, startIndex) + productPageLink.substring(endIndex, linkLength);
        }
        return result;
    }

    private UploadItemDTO zaraDTO(String productPageLink, long storeId, WebDriver driver) {
        Category category;
        ProductType productType;
        productPageLink = correctZaraLink(productPageLink);
        driver.get(productPageLink);
        Map<String, ProductType> dict;
        Document document = Jsoup.parse(driver.getPageSource());
        Element descriptionDiv = document.select(" h1.product-name").first();
        String description = descriptionDiv.text();
        description = description.replace("פרטי", "");
        Element priceSpan = document.select("div.price._product-price span").first();
        String fullPrice = priceSpan.text();
        String salePrice="";
        String price="";
        Currency currency = Currency.USD;
        try {
            price = document.select("div.price._product-price span.line-through").first().text();
            ItemPriceCurr itemPriceCurr = priceTag(price);
            price = itemPriceCurr.price;
            salePrice = document.select("div.price._product-price span.sale.discount-percentage").first().text();
            ItemPriceCurr itemPriceCurrSale = priceTag(salePrice);
            currency = itemPriceCurrSale.currency;
            salePrice = itemPriceCurrSale.price;
        }catch(NullPointerException e){
            price = document.select("div.price._product-price span").first().text();
            ItemPriceCurr itemPriceCurr = priceTag(price);
            currency = itemPriceCurr.currency;
            price = itemPriceCurr.price;
        }
        String designer = "";
        String imgExtension = "jpg";
        Elements elem = document.select("div.media-wrap.image-wrap a");
        List<String> links = elem.eachAttr("href");
        String imageAddr = links.get(0);
        links.remove(0);
        int endIndex = productPageLink.indexOf("html");
        int startIndex = endIndex - 9;
        String productID = productPageLink.substring(startIndex, endIndex - 1);
        if (this.israeliWebsite){
            dict = classificationService.getHebrewDict();
        }else {
            dict = classificationService.getEnglishDict();
        }
        ItemClassificationService.ItemTags itemTags = classificationService.classify(description, dict);
        category = itemTags.getCategory();
        productType = itemTags.getProductType();

        return new UploadItemDTO(imageAddr, productPageLink, description,
                price, salePrice, currency, storeId, designer, imgExtension, productID, links, category, productType);
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

        Map<String, ProductType> dict = classificationService.getEnglishDict();
        ItemClassificationService.ItemTags itemTags = classificationService.classify(description, dict);
        category = itemTags.getCategory();
        productType = itemTags.getProductType();

        return new UploadItemDTO(imageAddr, productPageLink, description,
                price, "", currency, storeId, designer, imgExtension, productID, links, category, productType);

    }


    private UploadItemDTO shopBopDTO(String productPageLink, long storeId, WebDriver driver) {
        Category category;
        ProductType productType;
        driver.get(productPageLink);
        Document document = Jsoup.parse(driver.getPageSource());
        Element descriptionDiv = document.select(" div#product-title").first();
        String description = descriptionDiv.text();
        String designer = document.select("span.brand-name").first().text();
        String salePrice="";
        String price="";
        Currency currency = Currency.USD;
        try {
            price = document.select("span.retail-price").first().text();
            ItemPriceCurr itemPriceCurr = priceTag(price);
            price = itemPriceCurr.price;
            salePrice = document.select("span.pdp-price").first().text();
            ItemPriceCurr itemPriceCurrSale = priceTag(salePrice);
            currency = itemPriceCurrSale.currency;
            salePrice = itemPriceCurrSale.price;
        }catch(NullPointerException e){
            price = document.select("span.pdp-price").first().text();
            ItemPriceCurr itemPriceCurr = priceTag(price);
            currency = itemPriceCurr.currency;
            price = itemPriceCurr.price;
        }
        Elements elem = document.select("img.display-image");
        List<String> links = elem.eachAttr("src");
        String imageAddr = links.get(0);
        String imgExtension = "jpg";
        links.remove(0);
        int endIndex = productPageLink.indexOf("htm");
        int startIndex = endIndex - 11;
        String productID = productPageLink.substring(startIndex, endIndex - 1);

        Map<String, ProductType> dict = classificationService.getEnglishDict();
        ItemClassificationService.ItemTags itemTags = classificationService.classify(description, dict);
        category = itemTags.getCategory();
        productType = itemTags.getProductType();

        return new UploadItemDTO(imageAddr, productPageLink, description,
                price, salePrice, currency, storeId, designer, imgExtension, productID, links, category, productType);
    }


    private UploadItemDTO revolveDTO(String productPageLink, long storeId, WebDriver driver) {
        Category category;
        ProductType productType;
        driver.get(productPageLink);
        Document document = Jsoup.parse(driver.getPageSource());
        Element descriptionDiv = document.select("h1.product-name--lg.u-text-transform--none.u-margin-t--none.u-margin-b--sm").first();
        String description = descriptionDiv.text();
        String designer = null;
        String salePrice="";
        String price="";
        price = document.select("span#retailPrice").first().text();
        ItemPriceCurr itemPriceCurr = priceTag(price);
        Currency currency = itemPriceCurr.currency;
        price = itemPriceCurr.price;

        salePrice = document.select("span#markdownPrice").first().text();
        ItemPriceCurr itemPriceCurrSale = priceTag(salePrice);
        salePrice = itemPriceCurrSale.price;
        if (salePrice.equalsIgnoreCase(price)){
            salePrice = "";
        }
        Elements elem = document.select("img.slideshow__pager-img");
        List<String> links = elem.eachAttr("src");
        for (int i = 0; i < links.size(); i++) {
            if (i>1){
                links.remove(i);
            }else {
                links.set(i, links.get(i).replace("/dt/", "/z/"));
            }
        }
        String imageAddr = links.get(0);
        String imgExtension = "jpg";
        links.remove(0);

        int startIndex = productPageLink.indexOf("/dp/");
        int endIndex = productPageLink.length();
        String productID = productPageLink.substring(startIndex+4,endIndex-1);
        Map<String, ProductType> dict = classificationService.getEnglishDict();
        ItemClassificationService.ItemTags itemTags = classificationService.classify(description, dict);
        category = itemTags.getCategory();
        productType = itemTags.getProductType();

        return new UploadItemDTO(imageAddr, productPageLink, description,
                price,salePrice, currency, storeId, designer, imgExtension, productID, links, category, productType);
    }

    private UploadItemDTO factoryDTO(String productPageLink, long storeId, WebDriver driver) {
        Category category;
        ProductType productType;
        driver.get(productPageLink);
        Document document = Jsoup.parse(driver.getPageSource());
        Element descriptionDiv = document.select("p.product_note").first();
        String description = descriptionDiv.text();
        String designer = document.select("h1#manufacturer_header a").attr("title");
        String salePrice="";
        String price="";
        Currency currency = Currency.USD;
        try {
            price = document.select("p.old-price span.price").first().text();
            ItemPriceCurr itemPriceCurr = priceTag(price);
            price = itemPriceCurr.price;
            salePrice = document.select("span.final-price").first().text();
            ItemPriceCurr itemPriceCurrSale = priceTag(salePrice);
            currency = itemPriceCurrSale.currency;
            salePrice = itemPriceCurrSale.price;
        }catch(NullPointerException e){
            price = document.select("span.price").first().text();
            ItemPriceCurr itemPriceCurr = priceTag(price);
            currency = itemPriceCurr.currency;
            price = itemPriceCurr.price;
        }
        Elements elem = document.select(".zoomWindow");
        List<String> links=null;
        String imageAddr = driver.findElement(By.xpath("//div[@class='zoomWindow']"))
                .getCssValue("background-image");
        int beginIndex = 5;
        int endIndex = imageAddr.length();
        imageAddr = imageAddr.substring(beginIndex,endIndex-2);
        String imgExtension = "jpg";
        String productID = driver.findElement(By.xpath("//input[@id='product-id']"))
                .getAttribute("value");
        Map<String, ProductType> dict = classificationService.getEnglishDict();
        ItemClassificationService.ItemTags itemTags = classificationService.classify(description, dict);
        category = itemTags.getCategory();
        productType = itemTags.getProductType();

        return new UploadItemDTO(imageAddr, productPageLink, description,
                price, salePrice, currency, storeId, designer, imgExtension, productID, links, category, productType);
    }

    private UploadItemDTO topshopDTO(String productPageLink, long storeId, WebDriver driver) {
        Category category;
        ProductType productType;
        driver.get(productPageLink);
        Document document = Jsoup.parse(driver.getPageSource());
        Element descriptionDiv = document.select(".product-name").first();
        String description = descriptionDiv.text();
        String designer = null;
        String price="";
        String salePrice="";
        Currency currency = Currency.USD;
        try {
            price = document.select("p.old-price span.price").first().text();
            ItemPriceCurr itemPriceCurr = priceTag(price);
            price = itemPriceCurr.price;
            salePrice = document.select("p.special-price.1 span.price").first().text();
            ItemPriceCurr itemPriceCurrSale = priceTag(salePrice);
            currency = itemPriceCurrSale.currency;
            salePrice = itemPriceCurrSale.price;
        }catch(NullPointerException e){
            price = document.select("span.regular-price span.price").first().text();
            ItemPriceCurr itemPriceCurr = priceTag(price);
            currency = itemPriceCurr.currency;
            price = itemPriceCurr.price;
        }

        String productID = driver.findElement(By.xpath("//input[@name='form_key']"))
                .getAttribute("value");
        List<String>links = new ArrayList<>();
        links.add(document.select("div#image-zoom-2 img").attr("src"));
        String imageAddr = document.select("div#image-zoom-0 img").attr("src");
        String imgExtension = "jpg";

        Map<String, ProductType> dict = classificationService.getHebrewDict();
        ItemClassificationService.ItemTags itemTags = classificationService.classify(description, dict);
        category = itemTags.getCategory();
        productType = itemTags.getProductType();

        return new UploadItemDTO(imageAddr, productPageLink, description,
                price,salePrice ,currency, storeId, designer, imgExtension, productID, links, category, productType);
    }

    private UploadItemDTO mytheresaDTO(String productPageLink, long storeId, WebDriver driver) {
        Category category;
        ProductType productType;
        driver.get(productPageLink);
        Document document = Jsoup.parse(driver.getPageSource());
        Element descriptionDiv = document.select("div.product-name").first();
        String description = descriptionDiv.text();
        String designer = document.select("div.product-designer").first().text();
        String priceSpan = null;;
        String productID = driver.findElement(By.xpath("//input[@name='product']"))
                .getAttribute("value");
        String price="";
        String salePrice="";
        Currency currency = Currency.USD;
        try {
            price = document.select("span#old-price-"+productID).first().text();
            ItemPriceCurr itemPriceCurr = priceTag(price);
            price = itemPriceCurr.price;
            salePrice = document.select("p.special-price span#product-price-"+productID).first().text();
            ItemPriceCurr itemPriceCurrSale = priceTag(salePrice);
            currency = itemPriceCurrSale.currency;
            salePrice = itemPriceCurrSale.price;
        }catch(NullPointerException e){
            price =  document.select("span#product-price-"+productID).first().text();
            ItemPriceCurr itemPriceCurr = priceTag(price);
            currency = itemPriceCurr.currency;
            price = itemPriceCurr.price;
        }

        List<String>links = new ArrayList<>();
        links.add(document.select("img#image-1").attr("src"));
        links.set(0, "https:" + links.get(0));
        String imageAddr = "https:" + document.select("img#image-0").attr("src");
        String imgExtension = "jpg";

        Map<String, ProductType> dict = classificationService.getEnglishDict();
        ItemClassificationService.ItemTags itemTags = classificationService.classify(description, dict);
        category = itemTags.getCategory();
        productType = itemTags.getProductType();

        return new UploadItemDTO(imageAddr, productPageLink, description,
                price, salePrice,currency, storeId, designer, imgExtension, productID, links, category, productType);
    }


}
