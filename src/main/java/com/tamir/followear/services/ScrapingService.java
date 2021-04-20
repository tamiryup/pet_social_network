package com.tamir.followear.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.sun.org.apache.xpath.internal.operations.Bool;
import com.tamir.followear.dto.UploadItemDTO;
import com.tamir.followear.entities.Store;
import com.tamir.followear.enums.Category;
import com.tamir.followear.enums.ProductType;
import com.tamir.followear.exceptions.BadLinkException;
import com.tamir.followear.exceptions.NonFashionItemException;
import com.tamir.followear.exceptions.ScrapingError;
import com.tamir.followear.helpers.StringHelper;
import lombok.ToString;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
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

//        String proxyUrl = "http://il.smartproxy.com:30001";
//
//        options.addArguments("--headless", "--no-sandbox", "--disable-gpu", "--window-size=1280x1696",
//                "--user-data-dir=/tmp/user-data", /*"--remote-debugging-port=9222",*/ "--hide-scrollbars",
//                "--enable-logging", "--log-level=0", "--v=99", "--single-process",
//                "--data-path=/tmp/data-path", "--ignore-certificate-errors", "--homedir=/tmp",
//                "--disk-cache-dir=/tmp/cache-dir", "--proxy-server=" + proxyUrl,
//                "user-agent=Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36" +
//                        " (KHTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36");

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
        if (domain.startsWith("m.")) {
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
        }
        if(domain.equals("api-shein.shein.com")){
            return "shein.com";
        }
        else {
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
                case "mytheresa.com":
                    itemDTO = mytheresaDTO(productPageLink, storeId, driver);
                    break;
                case "theoutnet.com":
                    itemDTO = outnetDTO(productPageLink, storeId, driver);
                    break;
                case "massimodutti.com":
                    itemDTO = massimoDuttiDTO(productPageLink, storeId, driver);
                    break;
                case "boohoo.com":
                    itemDTO = boohooDTO(productPageLink, storeId, driver);
                    break;
//                case "adikastyle.com":
//                    itemDTO = adikaDTO(productPageLink, storeId, driver);
//                    break;
                case "renuar.co.il":
                    itemDTO = renuarDTO(productPageLink, storeId, driver);
                    break;
                case "twentyfourseven.co.il":
                    itemDTO = twentyFourSevenDTO(productPageLink, storeId, driver);
                    break;
                default:
                    throw new BadLinkException("This website is not supported");
            }
        } catch (BadLinkException e) {
            throw e;
        } catch (NonFashionItemException e) {
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
        String productID = "";
        Category category;
        ProductType productType;
        String designer = null;
        String imageAddr;

        int endIndex = 0;
        int beginIndex = productPageLink.indexOf("/prd/");
        if (beginIndex == -1) {
            beginIndex = productPageLink.indexOf("/grp/");
            if (beginIndex == -1) {
                throw new BadLinkException("This is not a product page");
            }
        }

        beginIndex = beginIndex + 5;
        for (int i = beginIndex; i < productPageLink.length(); i++) {
            if (Character.isDigit(productPageLink.charAt(i))) {
                endIndex = i;
            } else {
                break;
            }
        }
        productID = productPageLink.substring(beginIndex, endIndex + 1);
        driver.get(productPageLink);
        Document document = Jsoup.parse(driver.getPageSource());
        List<String> breadCrumbsElem = document.select("nav._1MMuO3r li a").eachText();
        for (String i : breadCrumbsElem) {
            if (i.equals("Face + Body") || (i.equals("New In: Face + Body"))) {
                throw new NonFashionItemException();
            }
        }
        Elements descriptionDiv = document.select("div.product-hero");
        String description = descriptionDiv.select("h1").text();
        String price = "";
        String salePrice = "";
        Currency currency = Currency.GBP;
        Elements images = document.select("img.gallery-image");

        String imgExtension = "jpg";
        List<String> links = new ArrayList<>();
        for (String imgSrc:images.eachAttr("src")){
            if (imgSrc.contains("wid=513")){
                links.add(imgSrc);
            }
        }
        if (links.size() > 1) {
            imageAddr = links.get(1);
            links.remove(1);
        } else {
            imageAddr = links.get(0);
            links.remove(0);
        }

        Map<String, ProductType> dict = classificationService.getEnglishDict();
        ItemClassificationService.ItemTags itemTags = classificationService.classify(description, dict);

        category = itemTags.getCategory();
        productType = itemTags.getProductType();

        try {
            price = document.selectFirst("span.product-prev-price[data-id='previous-price']").text();
            ItemPriceCurr itemPriceCurr = priceTag(price);
            price = itemPriceCurr.price;
            salePrice = document.select("span.current-price.product-price-discounted").first().text();
            ItemPriceCurr itemPriceCurrSale = priceTag(salePrice);
            currency = itemPriceCurrSale.currency;
            salePrice = itemPriceCurrSale.price;
        } catch (NullPointerException e) {
            price = driver.findElement(By.xpath(
                    "//span[contains(@class,'current-price')]"))
                    .getAttribute("innerHTML");
            price = price.replace(",", "");
            ItemPriceCurr itemPriceCurr = priceTag(price);
            currency = itemPriceCurr.currency;
            price = itemPriceCurr.price;
        }


        return new UploadItemDTO(imageAddr, productPageLink, description,
                price, salePrice, currency, storeID, designer, imgExtension, productID, links, category, productType);
    }

    private UploadItemDTO netaporterDTO(String productPageLink, long storeId, WebDriver driver) {
        String productID;
        Category category;
        ProductType productType;
        String price = null;
        String salePrice = "";
        String description = "";
        String designer ="";
        String imageAddr = "";
        String priceSymbol;
        driver.get(productPageLink);
        Document document = Jsoup.parse(driver.getPageSource());
        productID = driver.findElement(By.xpath("//meta[@itemprop='productID']"))
                .getAttribute("content");
        if (productID == null) {
            throw new BadLinkException("This is not a product page");
        }

        price = driver.findElement(By.xpath("//span[@itemprop='price']"))
                .getAttribute("content");
        priceSymbol = driver.findElement(By.xpath("//meta[@itemprop='priceCurrency']"))
                .getAttribute("content");

        ItemPriceCurr itemPriceCurr = priceTag(priceSymbol);
        Currency currency = itemPriceCurr.currency;


        // extract designer and description
        List<WebElement> itemsProps = driver.findElements(By.xpath("//meta[@itemprop='name']"));
        if (itemsProps.size()  >= 2){
            designer = itemsProps.get(0).getAttribute("content");
            description = itemsProps.get(1).getAttribute("content");
        }
        // can't extract designer and description
         else{
            throw new BadLinkException("This is not a product page");
        }

         //get(0).findElements(By.xpath("//*[contains(@class,'__links')]"))

        List<WebElement> webElements = driver.findElements(By.xpath("//*[contains(@class,'__shopMore--bottomDetails')]")).get(0).findElements(By.xpath(".//a"));
        for (WebElement webElement:webElements) {
            if (webElement.getAttribute("href").contains("shop/beauty") || webElement.getAttribute("href").contains("accessories/lifestyle") || webElement.getAttribute("href").contains("accessories/books")){
                throw new NonFashionItemException();
            }
        }



        String imgExtension = "jpg";
        Elements imageElements = document.select("picture img");
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

    private UploadItemDTO outnetDTO(String productPageLink, long storeId, WebDriver driver) {
        String productID;
        Category category;
        ProductType productType;
        String price = null;
        String salePrice = "";
        String priceSymbol;
        driver.get(productPageLink);
        Document document = Jsoup.parse(driver.getPageSource());
        productID = driver.findElement(By.xpath("//meta[@itemprop='productID']"))
                .getAttribute("content");
        if (productID == null) {
            throw new BadLinkException("This is not a product page");
        }
        salePrice = driver.findElement(By.xpath("//span[@itemprop='price']"))
                .getAttribute("content");
        try {
            price = document.select(" s.PriceWithSchema9__wasPrice").first().text();
            price = price.replaceAll("[^0-9]", "");
        } catch (NullPointerException e) {
        }

        String description = driver.findElement(By.xpath("//meta[@name='twitter:image:alt']"))
                .getAttribute("content");
        priceSymbol = driver.findElement(By.xpath("//meta[@itemprop='priceCurrency']"))
                .getAttribute("content");
        ItemPriceCurr itemPriceCurr = priceTag(priceSymbol);
        Currency currency = itemPriceCurr.currency;


        String designer = driver.findElement(By.xpath("//meta[@itemprop='name']"))
                .getAttribute("content");
        String imageAddr = "";
        String imgExtension = "jpg";
        Elements imageDiv = document.select(".Image18__imageContainer.ImageCarousel83__thumbnailImage");
        Elements imageElements = imageDiv.select("img");
        List<String> links = imageElements.eachAttr("src");

        for (int i = 0; i < 2; i++) {
            links.set(i, "https:" + links.get(i));
        }
        imageAddr = links.get(0);
        links.remove(0);
        links = links.subList(0, 1);


        Map<String, ProductType> dict = classificationService.getEnglishDict();
        ItemClassificationService.ItemTags itemTags = classificationService.classify(description, dict);
        category = itemTags.getCategory();
        productType = itemTags.getProductType();

        return new UploadItemDTO(imageAddr, productPageLink, description,
                price, salePrice, currency, storeId, designer, imgExtension, productID, links, category, productType);
    }


    String getNetaporterPriceSymbol(String price) {
        String priceSymbol = "$";
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

    boolean isStringOnlyAlphabet(String str) {
        return ((!str.equals(""))
                && (str != null)
                && (str.matches("^[a-zA-Z]*$")));
    }


    private UploadItemDTO terminalxDTO(String productPageLink, long storeId, WebDriver driver) {
            Category category;
            ProductType productType;
            driver.get(productPageLink);
            String price = "";
            String salePrice = "";
            String imgExtension = "jpg";
            List<String> links = new ArrayList<>();
            List<WebElement> visibleThumbnails = new ArrayList<>();
            Currency currency = Currency.ILS;
            Document document = Jsoup.parse(driver.getPageSource());
            String description = driver.findElement(By.xpath("//span[@itemprop='name']")).getText();
            String productPageType = document.select(".product-item-brand").first().attr(
                    "data-div-top");
            if (("גברים".equals(productPageType)) || ("נשים".equals(productPageType))) {

            } else {
                throw new NonFashionItemException();
            }
            String productID = document.select(".price-box.price-final_price").first().attr(
                    "data-product-id");

            try {
                price = document.select("span#old-price-" + productID).first().attr("data-price-amount");
                ItemPriceCurr itemPriceCurr = priceTag(price);
                price = itemPriceCurr.price;
                salePrice = document.select("span#product-price-" + productID).first().attr("data-price-amount");
                ItemPriceCurr itemPriceCurrSale = priceTag(salePrice);
                salePrice = itemPriceCurrSale.price;
            } catch (NullPointerException e) {
                price = document.select("span#product-price-" + productID).first().attr("data-price-amount");
                ItemPriceCurr itemPriceCurr = priceTag(price);
                price = itemPriceCurr.price;
            }

            String designer = document.select("div.product-item-brand a").first().text();
            //String imageAddr = document.select("img.magnifier-large").eachAttr("src").get(0);
            String imageAddr = document.select("div.fotorama__stage__shaft img").attr("src");
 //           driver.findElements(By.xpath("//div[@class='fotorama__nav__frame fotorama__nav__frame--thumb']")).get(1).click();

//            try {
//                visibleThumbnails = new WebDriverWait(driver, 10)
//                        .until(driverx -> driverx.findElements(By.xpath("//img[@id='magnifier-item-0-large']")));
//                System.out.println(visibleThumbnails);
//            }catch (TimeoutException e){
//                System.out.println(e);
//            }

//            try {
//                links.add(document.select("div.fotorama__stage__frame.fotorama_vertical_ratio.fotorama__loaded.fotorama__loaded--img img").eachAttr("src").get(2));
//            }catch (IndexOutOfBoundsException e){
//
//            }
            Map<String, ProductType> dict = classificationService.getHebrewDict();
            ItemClassificationService.ItemTags itemTags = classificationService.classify(description, dict);
            category = itemTags.getCategory();
            productType = itemTags.getProductType();

            return new UploadItemDTO(imageAddr, productPageLink, description,
                    price, salePrice, currency, storeId, designer, imgExtension, productID, links, category, productType);
    }


    private UploadItemDTO adikaDTO(String productPageLink, long storeId, WebDriver driver) {
        String productID = "";
        Category category;
        ProductType productType;
        List<String> links = new ArrayList<>();
        String salePrice = "";
        String price="";
        String designer = null;
        Currency currency = Currency.ILS;
        driver.get(productPageLink);
        Document document = Jsoup.parse(driver.getPageSource());
        if (productPageLink.contains("/myhome/")){
            throw new NonFashionItemException();
        }

        String scriptTag = driver.findElement(By.xpath("//script")).getAttribute("innerHTML");
        //String jsonString = scriptTag.substring(1,scriptTag.length()-1);


        System.out.println(scriptTag);

        List<String> breadCrumbsElem = document.select(".breadcrumbs ul li a").eachAttr("href");
        for (String i : breadCrumbsElem) {
            if (i.contains("back-in-stock-living") || i.contains("adika-living")) {
                throw new NonFashionItemException();
            }
        }
        String description = document.select("div.product-name h1").first().text();
        Element imageDiv = document.select("div#image-zoom-0").first();
        Element image = imageDiv.select("img").first();
        String imageAddr = image.absUrl("src");
        String imgExtension = "jpg";

        productID = document.select(".product-view.initialised-validation").attr("id");
        if (productID == "") {
            throw new BadLinkException("This isn't a product page");
        }

        try {
            price = document.select("p.old-price").first().attr("data-price-amount");
            ItemPriceCurr itemPriceCurr = priceTag(price);
            price = itemPriceCurr.price;
            salePrice = document.select("p.special-price").first().attr("data-price-amount");
            ItemPriceCurr itemPriceCurrSale = priceTag(salePrice);
            salePrice = itemPriceCurrSale.price;
        } catch (NullPointerException e) {
            price = document.select("span.price").first().text();
            ItemPriceCurr itemPriceCurr = priceTag(price);
            price = itemPriceCurr.price;
        }


        Elements imageElements = document.selectFirst(".more-views-thumbs").getElementsByTag("a");
        if (imageElements.size()>1){
            links.add(imageElements.get(1).attr("href"));
        }
        Map<String, ProductType> dict = classificationService.getHebrewDict();
        ItemClassificationService.ItemTags itemTags = classificationService.classify(description, dict);
        category = itemTags.getCategory();
        productType = itemTags.getProductType();

        return new UploadItemDTO(imageAddr, productPageLink, description,
                price, salePrice, currency, storeId, designer, imgExtension, productID, links, category, productType);

    }


    private UploadItemDTO farfetchDTO(String productPageLink, long storeId, WebDriver driver) {
        driver.get(productPageLink);
        driver.manage().window().maximize();
        Document document = Jsoup.parse(driver.getPageSource());
        Category category;
        ProductType productType;
        String salePrice = "";
        String price = "";
        String productID="";
        String imageAddr="";
        String firstThumbnailImage="";
        List<String> links = new ArrayList<>(1);
        try {
            productID = driver.findElement(By.xpath("//meta[@itemprop='productID']")).getAttribute("content");
        }catch (NoSuchElementException e) {
           int productIdEndIndex = productPageLink.indexOf(".aspx");
            if (productIdEndIndex != -1) {
                int productIdBeginIndex = productIdEndIndex - 8;
                productID = productPageLink.substring(productIdBeginIndex,productIdEndIndex);
            }
       }
        if (productID.length()<1) {
            throw new BadLinkException("This isn't a product page");
        }
        List<WebElement> breadCrumbsElem = driver.findElements(By.xpath("//li[@itemprop='itemListElement']"));
        //System.out.println(driver.findElements(By.xpath("//ol[@data-tstid='breadcrumb']")));

        for (WebElement breadCrumb : breadCrumbsElem) {
            if (breadCrumb.getText().equals("Homeware")) {
                throw new NonFashionItemException();
            }
        }
        String description = driver.findElement(By.xpath("//meta[@itemprop='name']")).getAttribute("content");
        String designer = driver.findElement(By.xpath("//a[@data-tstid='cardInfo-title']")).getAttribute("aria-label");
        Currency currency = Currency.USD;
        try {
            salePrice = driver.findElement(By.xpath("//strong[@data-tstid='priceInfo-onsale']")).getText();
            salePrice = salePrice.replaceAll("[^0-9\\,\\.]", "");
            price = driver.findElement(By.xpath("//span[@data-tstid='priceInfo-original']")).getText();
            ItemPriceCurr itemPriceCurr = priceTag(price);
            price = itemPriceCurr.price;
            //salePrice = driver.findElement(By.xpath("//meta[@itemprop='price']")).getAttribute("content");
            String stringCurrency = driver.findElement(By.xpath("//meta[@itemprop='priceCurrency']")).getAttribute("content");
            ItemPriceCurr itemPriceCurrSale = priceTag(stringCurrency);
            currency = itemPriceCurrSale.currency;
        } catch (NoSuchElementException e) {
            price = driver.findElement(By.xpath("//meta[@itemprop='price']")).getAttribute("content");
            String stringCurrency = driver.findElement(By.xpath("//meta[@itemprop='priceCurrency']")).getAttribute("content");
            ItemPriceCurr itemPriceCurr = priceTag(stringCurrency);
            currency = itemPriceCurr.currency;
        }
        try {
            imageAddr = driver.findElement(By.xpath("//img[@data-index='0'][@data-tstid='slick-active']")).getAttribute("src");
            firstThumbnailImage = driver.findElement(By.xpath("//img[@data-index='1']")).getAttribute("src");
        }catch (NoSuchElementException e){
            imageAddr = driver.findElement(By.xpath("//img[@data-test='imagery-img0']")).getAttribute("src");
            firstThumbnailImage = driver.findElement(By.xpath("//img[@data-test='imagery-img1']")).getAttribute("src");
        }
        links.add(firstThumbnailImage);
        String imgExtension = "jpg";
        Map<String, ProductType> dict = classificationService.getEnglishDict();
        ItemClassificationService.ItemTags itemTags = classificationService.classify(description, dict);
        category = itemTags.getCategory();
        productType = itemTags.getProductType();

        return new UploadItemDTO(imageAddr, productPageLink, description,
                price, salePrice, currency, storeId, designer, imgExtension, productID, links, category, productType);
    }

    private UploadItemDTO sheinDTO(String productPageLink, long storeId, WebDriver driver) {
        String productID = null;
        Category category;
        ProductType productType;
        String price = null;
        List<String> links = new ArrayList<>();
        driver.get(productPageLink);
        Document document = Jsoup.parse(driver.getPageSource());
        List<String> breadCrumbsElem = document.select("div.bread-crumb__inner div a").eachText();
        for (String i : breadCrumbsElem) {
            if (i.contains("Event & Party Supplies") || i.contains("ביוטי") || i.contains("Beauty") || i.contains("טיפוח אישי") || (i.contains("בית & חיות מחמד"))) {
                throw new NonFashionItemException();
            }
        }
        Element descriptionDiv = document.select("div.product-intro__head-name").first();
        String description = descriptionDiv.text();
        String designer = null;
        String salePrice = "";
        Currency currency = Currency.USD;
        try {
            price = document.select("div.product-intro__head-price del.del-price").first().text();
            ItemPriceCurr itemPriceCurr = priceTag(price);
            price = itemPriceCurr.price;
            salePrice = document.select("div.product-intro__head-price .discount span").first().text();
            ItemPriceCurr itemPriceCurrSale = priceTag(salePrice);
            currency = itemPriceCurrSale.currency;
            salePrice = itemPriceCurrSale.price;
        } catch (NullPointerException e) {
            price = document.select("div.product-intro__head-price .original span").first().text();
            ItemPriceCurr itemPriceCurr = priceTag(price);
            currency = itemPriceCurr.currency;
            price = itemPriceCurr.price;
        }

        Element imageDiv = document.select("div.swiper-slide.product-intro__main-item.cursor-zoom-in.swiper-slide-active").first().attr("data-swiper-slide-index", "0");
        String imageAddr = imageDiv.select("img.j-verlok-lazy.loaded").attr("src");
        imageAddr = "https:" + imageAddr;
        String correctImageAddr = imageAddr.replace(".webp", ".jpg");
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
        String correctImgSrc = imgSrc.replace(".webp", ".jpg");
        links.add(correctImgSrc);
        Map<String, ProductType> dict = classificationService.getEnglishDict();
        ItemClassificationService.ItemTags itemTags = classificationService.classify(description, dict);
        category = itemTags.getCategory();
        productType = itemTags.getProductType();
        return new UploadItemDTO(correctImageAddr, productPageLink, description,
                price, salePrice, currency, storeId, designer, imgExtension, productID, links, category, productType);
    }


    private String correctZaraLink(String productPageLink) {
        String result = productPageLink.replaceFirst("/share/", "/");

        //remove all irrelevant text between
        int startIrrelvantIndex = result.indexOf("html?") + 5;
        int endIrrelevantIndex = result.indexOf("v1=");
        int linkLength = result.length();
        result = result.substring(0, startIrrelvantIndex) + result.substring(endIrrelevantIndex, linkLength);

//        if (StringHelper.doesContainHebrew(productPageLink)) {
//            int startIndex = result.lastIndexOf('/') + 1;
//            int htmlStringIndex = result.indexOf(".html");
//            int endIndex = result.lastIndexOf("-p", htmlStringIndex);
//            linkLength = result.length();
//            result = result.substring(0, startIndex) + result.substring(endIndex, linkLength);
//        }
        return result;
    }

    private UploadItemDTO zaraDTO(String productPageLink, long storeId, WebDriver driver) throws IOException {
        Category category;
        ProductType productType;
        List<String> links = new ArrayList<>();
        String imageAddr = "";
        productPageLink = correctZaraLink(productPageLink);
        LOGGER.info("zara productPageLink is: {}", productPageLink);
        driver.get(productPageLink);
        Map<String, ProductType> dict;
        Document document = Jsoup.parse(driver.getPageSource());
        List<String> breadCrumbsElem = document.select(".breadcrumbs._breadcrumbs li a span").eachText();
        for (String i : breadCrumbsElem) {
            if (i == "KIDS") {
                throw new NonFashionItemException();
            }
        }
        //System.out.println(breadCrumbsElem);
        String description = document.select(" h1.product-detail-info__name").first().text();
        description = description.replace("פרטי", "");
        String salePrice = "";
        String price = "";
        Currency currency = Currency.USD;
        String designer = "";
        String imgExtension = "jpg";

        String scriptTag = driver.findElement(By.xpath("//script[@type='application/ld+json']")).getAttribute("innerHTML");
        String jsonString = scriptTag.substring(1,scriptTag.length()-1);


        JsonNode jsonNode = new ObjectMapper().readTree(jsonString);
        JsonNode offersJsonNode = new ObjectMapper().readTree(jsonNode.get("offers").toString());


        ArrayNode arrayNode = (ArrayNode) jsonNode.get("image");
        imageAddr = arrayNode.get(0).textValue();
        if (arrayNode.size()>1) {
            links.add(arrayNode.get(1).textValue());
        }else{
            links.add("");
        }
        ItemPriceCurr itemPriceCurr = priceTag(offersJsonNode.get("priceCurrency").toString());
        currency = itemPriceCurr.currency;

        try {
            price = document.select(".product-detail-info__price-amount.price span.price__amount.price__amount--old").first().text();
            price = price.replaceAll("[^\\d.]", "");
            salePrice = offersJsonNode.get("price").textValue();


        } catch (NullPointerException e) {
            price = offersJsonNode.get("price").textValue();
            salePrice="";
        }

        int endIndex = productPageLink.indexOf("html");
        int startIndex = endIndex - 10;
        if (productPageLink.charAt(startIndex)!= 'p'){
            throw new BadLinkException("This isn't a product page");
        }
        String productID = productPageLink.substring(startIndex+1, endIndex - 1);
        if (StringHelper.doesContainHebrew(description)) {
            dict = classificationService.getHebrewDict();
        } else {
            dict = classificationService.getEnglishDict();
        }
        ItemClassificationService.ItemTags itemTags = classificationService.classify(description, dict);
        category = itemTags.getCategory();
        productType = itemTags.getProductType();

        return new UploadItemDTO(imageAddr, productPageLink, description,
                price, salePrice, currency, storeId, designer, imgExtension, productID, links, category, productType);
    }

    private UploadItemDTO massimoDuttiDTO(String productPageLink, long storeId, WebDriver driver) throws IOException {
        Category category;
        ProductType productType;
        List<String> links = new ArrayList<>();
        driver.get(productPageLink);
        Map<String, ProductType> dict;
        Document document = Jsoup.parse(driver.getPageSource());
        String productID="";
//        try{
//            document.select("html#ItxProductPage").first().text();
//        }catch (NullPointerException e){
//
//            throw new BadLinkException("This isn't a product page");
//        }
        int endIndex = productPageLink.indexOf(".html");
        int beginIndex=0;
        for (int i=endIndex;i>0;i--){
            if (productPageLink.charAt(i)=='-'){
                beginIndex = i;
                break;
            }
        }

        if (beginIndex > 0){
            productID = productPageLink.substring(beginIndex+1,endIndex);
        }
        String salePrice = "";
        String price = "";
        Currency currency = Currency.USD;
        String designer = "";
        String imgExtension = "jpg";

        String scriptTag = driver.findElement(By.xpath("//script[@type='application/ld+json']")).getAttribute("innerHTML");
        String jsonString = scriptTag.substring(1,scriptTag.length()-1);


        JsonNode jsonNode = new ObjectMapper().readTree(jsonString);
        JsonNode offersJsonNode = new ObjectMapper().readTree(jsonNode.get("offers").toString());

        String description = jsonNode.get("name").textValue();
        price = offersJsonNode.get("price").textValue();
        String imageAddr = jsonNode.get("image").textValue();
        ItemPriceCurr itemPriceCurr = priceTag(offersJsonNode.get("priceCurrency").toString());
        currency = itemPriceCurr.currency;
        if (StringHelper.doesContainHebrew(description)) {
            dict = classificationService.getHebrewDict();
        } else {
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

        List<WebElement> breadCrumbsElem = driver.findElements(By.xpath("//li[@itemprop='itemListElement']"));
        //System.out.println(driver.findElements(By.xpath("//ol[@data-tstid='breadcrumb']")));

        for (WebElement breadCrumb : breadCrumbsElem) {
            if (breadCrumb.getText().equals("Trend: Self-Care Essentials") || breadCrumb.getText().equals("Home & Gifts")) {
                throw new NonFashionItemException();
            }
        }

        Element descriptionDiv = document.select(" div#product-title").first();
        String description = descriptionDiv.text();
        String designer = document.select("span.brand-name").first().text();
        String salePrice = "";
        String price = "";
        Currency currency = Currency.USD;
        try {
            price = document.select("span.retail-price").first().text();
            ItemPriceCurr itemPriceCurr = priceTag(price);
            price = itemPriceCurr.price;
            salePrice = document.select("span.pdp-price").first().text();
            ItemPriceCurr itemPriceCurrSale = priceTag(salePrice);
            currency = itemPriceCurrSale.currency;
            salePrice = itemPriceCurrSale.price;
        } catch (NullPointerException e) {
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
        String productPageType = driver.findElement(By.xpath("//meta[@name='twitter:label2']")).getAttribute("content");
        if (productPageType.equals("Beauty")) {
            throw new NonFashionItemException();
        }
        Element descriptionDiv = document.select("h1.product-name--lg.u-text-transform--none.u-margin-t--none.u-margin-b--sm").first();
        String description = descriptionDiv.text();
        String designer = null;
        String salePrice = "";
        String price = "";
        price = document.select("span#retailPrice").first().text();
        ItemPriceCurr itemPriceCurr = priceTag(price);
        Currency currency = itemPriceCurr.currency;
        price = itemPriceCurr.price;

        salePrice = document.select("span#markdownPrice").first().text();
        ItemPriceCurr itemPriceCurrSale = priceTag(salePrice);
        salePrice = itemPriceCurrSale.price;
        if (salePrice.equalsIgnoreCase(price)) {
            salePrice = "";
        }
        Elements elem = document.select("img.slideshow__pager-img");
        List<String> links = elem.eachAttr("src");
        for (int i = 0; i < links.size(); i++) {
            if (i > 1) {
                links.remove(i);
            } else {
                links.set(i, links.get(i).replace("/dt/", "/z/"));
            }
        }
        String imageAddr = links.get(0);
        String imgExtension = "jpg";
        links.remove(0);

        int startIndex = productPageLink.indexOf("/dp/");
        int endIndex = productPageLink.length();
        String productID = productPageLink.substring(startIndex + 4, endIndex - 1);
        Map<String, ProductType> dict = classificationService.getEnglishDict();
        ItemClassificationService.ItemTags itemTags = classificationService.classify(description, dict);
        category = itemTags.getCategory();
        productType = itemTags.getProductType();

        // revolve flips dots and commas when currency is euro
        if (currency == Currency.EUR) {
            price = StringHelper.replaceBothWays(price, '.', ',');
            salePrice = StringHelper.replaceBothWays(salePrice, '.', ',');
        }

        return new UploadItemDTO(imageAddr, productPageLink, description,
                price, salePrice, currency, storeId, designer, imgExtension, productID, links, category, productType);
    }

    private UploadItemDTO factoryDTO(String productPageLink, long storeId, WebDriver driver) {
        Category category;
        ProductType productType;
        driver.get(productPageLink);
        if (productPageLink.contains("https://www.factory54.co.il/kids")){
            throw new NonFashionItemException();
        }
        Document document = Jsoup.parse(driver.getPageSource());
        List<String> breadCrumbsElem = document.select("div.links.clearfix ul li a").eachText();
        for (String element : breadCrumbsElem) {
            if (element.contains("נרות") || element.contains("בישום") || element.contains("איפור") || element.contains("ספרים")) {
                throw new NonFashionItemException();
            }
        }
        String productID = driver.findElement(By.xpath("//input[@id='product-id']"))
                .getAttribute("value");
        String designer = document.select("h1#manufacturer_header a").attr("title");
        String description = document.select("p.product_note").first().text();
        String salePrice = "";
        String price = "";
        Currency currency = Currency.USD;

        try {
            price = document.select("p.old-price span.price").first().text();
            ItemPriceCurr itemPriceCurr = priceTag(price);
            price = itemPriceCurr.price;
            salePrice = document.select("span.final-price").first().text();
            ItemPriceCurr itemPriceCurrSale = priceTag(salePrice);
            currency = itemPriceCurrSale.currency;
            salePrice = itemPriceCurrSale.price;
        } catch (NullPointerException e) {
            price = document.select("span.price").first().text();
            ItemPriceCurr itemPriceCurr = priceTag(price);
            currency = itemPriceCurr.currency;
            price = itemPriceCurr.price;
        }


        List<String> links = new ArrayList<>();
        String imageAddr = document.select("img#image-main.main_img").attr("src");
        String imageThumbnail = driver.findElement(By.xpath("//a[@data-image-index='2']"))
                .getAttribute("data-zoom-image");
        String imgExtension = "jpg";
        links.add(imageThumbnail);

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
        String price = "";
        String salePrice = "";
        Currency currency = Currency.USD;
        try {
            price = document.select("p.old-price span.price").first().text();
            ItemPriceCurr itemPriceCurr = priceTag(price);
            price = itemPriceCurr.price;
            salePrice = document.select("p.special-price.1 span.price").first().text();
            ItemPriceCurr itemPriceCurrSale = priceTag(salePrice);
            currency = itemPriceCurrSale.currency;
            salePrice = itemPriceCurrSale.price;
        } catch (NullPointerException e) {
            price = document.select("span.regular-price span.price").first().text();
            ItemPriceCurr itemPriceCurr = priceTag(price);
            currency = itemPriceCurr.currency;
            price = itemPriceCurr.price;
        }

        String productID = document.select(".product-view.initialised-validation").attr("id");
        productID = productID.replaceAll("[^0-9]", "");
        List<String> links = new ArrayList<>();
        String thumbnail = document.select("img#image-1").attr("src");
        links.add(thumbnail);
        String imageAddr = document.select("img#image-0").attr("src");
        String imgExtension = "jpg";
        Map<String, ProductType> dict = classificationService.getHebrewDict();
        ItemClassificationService.ItemTags itemTags = classificationService.classify(description, dict);
        category = itemTags.getCategory();
        productType = itemTags.getProductType();

        return new UploadItemDTO(imageAddr, productPageLink, description,
                price, salePrice, currency, storeId, designer, imgExtension, productID, links, category, productType);
    }

    private UploadItemDTO mytheresaDTO(String productPageLink, long storeId, WebDriver driver) {
        Category category;
        ProductType productType;
        driver.get(productPageLink);
        Document document = Jsoup.parse(driver.getPageSource());
        List<String> breadCrumbsElem = document.select(".breadcrumbs li a span").eachText();
        for (String i : breadCrumbsElem) {
            if (i.equals("Kids")) {
                throw new NonFashionItemException();
            }
        }
        Element descriptionDiv = document.select("div.product-name").first();
        String description = descriptionDiv.text();
        String designer = document.select("div.product-designer").first().text();
        String priceSpan = null;
        ;
        String productID = driver.findElement(By.xpath("//input[@name='product']"))
                .getAttribute("value");
        String price = "";
        String salePrice = "";
        Currency currency = Currency.USD;
        try {
            price = document.select("span#old-price-" + productID).first().text();
            ItemPriceCurr itemPriceCurr = priceTag(price);
            price = itemPriceCurr.price;
            salePrice = document.select("p.special-price span#product-price-" + productID).first().text();
            ItemPriceCurr itemPriceCurrSale = priceTag(salePrice);
            currency = itemPriceCurrSale.currency;
            salePrice = itemPriceCurrSale.price;
        } catch (NullPointerException e) {
            price = document.select("span#product-price-" + productID).first().text();
            ItemPriceCurr itemPriceCurr = priceTag(price);
            currency = itemPriceCurr.currency;
            price = itemPriceCurr.price;
        }

        List<String> links = new ArrayList<>();
        links.add(document.select("img#image-1").attr("src"));
        links.set(0, "https:" + links.get(0));
        String imageAddr = "https:" + document.select("img#image-0").attr("src");
        String imgExtension = "jpg";

        Map<String, ProductType> dict = classificationService.getEnglishDict();
        ItemClassificationService.ItemTags itemTags = classificationService.classify(description, dict);
        category = itemTags.getCategory();
        productType = itemTags.getProductType();

        return new UploadItemDTO(imageAddr, productPageLink, description,
                price, salePrice, currency, storeId, designer, imgExtension, productID, links, category, productType);
    }

    private UploadItemDTO boohooDTO(String productPageLink, long storeId, WebDriver driver) {
        Category category;
        ProductType productType;
        driver.get(productPageLink);
        Document document = Jsoup.parse(driver.getPageSource());
        String productID = driver.findElement(By.xpath("//span[@itemprop='sku']"))
                .getAttribute("data-masterid");
        String designer = "";
        String description = driver.findElement(By.xpath("//h1[@itemprop='name']")).getAttribute("textContent");
        String salePrice = "";
        String price = "";
        String imageThumbnail="";
        Currency currency = Currency.USD;
        String stringCurrency = driver.findElement(By.xpath("//meta[@itemprop='priceCurrency']"))
                .getAttribute("content");
        currency = priceTag(stringCurrency).currency;

        try {
            document.select("price-sales").text(); //check sale price exists
            salePrice = driver.findElement(By.xpath("//span[@itemprop='price']")).getAttribute("content");
            price = document.select("span.price-standard").first().text();
            ItemPriceCurr itemPriceCurr = priceTag(price);
            price = itemPriceCurr.price;
        } catch (NullPointerException e) {
            price = driver.findElement(By.xpath("//span[@itemprop='price']"))
                    .getText();
        }
        List<String> links = new ArrayList<>();
        List<String> imagesThumbnails = document.select("li.thumb a").eachAttr("data-image");

        String imageAddr = imagesThumbnails.get(0);
        if (imagesThumbnails.size() > 1) {
            imageThumbnail = imagesThumbnails.get(1);
        }
        links.add(imageThumbnail);
        String imgExtension = "webp";
        Map<String, ProductType> dict;
        if (StringHelper.doesContainHebrew(description)) {
            dict = classificationService.getHebrewDict();
        } else {
            dict = classificationService.getEnglishDict();
        }
        ItemClassificationService.ItemTags itemTags = classificationService.classify(description, dict);
        category = itemTags.getCategory();
        productType = itemTags.getProductType();

        return new UploadItemDTO(imageAddr, productPageLink, description,
                price, salePrice, currency, storeId, designer, imgExtension, productID, links, category, productType);
    }

    private UploadItemDTO renuarDTO(String productPageLink, long storeId, WebDriver driver) {
        String productID = "";
        Category category;
        ProductType productType;
        List<String> links = new ArrayList<>();
        String salePrice = "";
        String price="";
        String designer = null;
        Currency currency = Currency.ILS;
        driver.get(productPageLink);
        String productCategory = driver.findElement(By.xpath("//meta[@property='product:category']")).getAttribute("content");
        Document document = Jsoup.parse(driver.getPageSource());
        if (productCategory.equals("אקססוריז2")){
            Boolean isFashionItem = false;
            String pageTile = document.select("title").text();
                if (pageTile.contains("נשים")){
                    isFashionItem = true;
                }
                if (!isFashionItem) {
                    throw new NonFashionItemException();
                }
        }

        String description = document.select("div.product-name h1").first().text();
        String imgExtension = "jpg";

        int endIndex = productPageLink.indexOf(".html");
        int beginIndex = 0;
        for (int i = endIndex; i > 0; i--){
            if (productPageLink.charAt(i) == '/'){
                beginIndex = i;
                break;
            }
        }
        if (beginIndex > 0){
            productID = productPageLink.substring(beginIndex+1,endIndex);
        }
        if (productID == "") {
            throw new BadLinkException("This isn't a product page");
        }

        try {
            price = document.select("p.old-price span.price").first().text();
            ItemPriceCurr itemPriceCurr = priceTag(price);
            price = itemPriceCurr.price;
            salePrice = document.select("p.special-price span.price").first().text();
            ItemPriceCurr itemPriceCurrSale = priceTag(salePrice);
            salePrice = itemPriceCurrSale.price;
        } catch (NullPointerException e) {
            price = document.select("span.price").first().text();
            ItemPriceCurr itemPriceCurr = priceTag(price);
            price = itemPriceCurr.price;
        }


        Elements imageElements = document.select("li.item a img");
        String imageAddr = imageElements.get(0).attr("src");
        if (imageElements.size()>1){
            links.add(imageElements.get(1).attr("src"));
        }
        Map<String, ProductType> dict = classificationService.getHebrewDict();
        ItemClassificationService.ItemTags itemTags = classificationService.classify(description, dict);
        category = itemTags.getCategory();
        productType = itemTags.getProductType();

        return new UploadItemDTO(imageAddr, productPageLink, description,
                price, salePrice, currency, storeId, designer, imgExtension, productID, links, category, productType);

    }

    private UploadItemDTO twentyFourSevenDTO(String productPageLink, long storeId, WebDriver driver) {
        String productID = "";
        Category category;
        ProductType productType;
        List<String> links = new ArrayList<>();
        String salePrice = "";
        String price="";
        String designer = null;
        Currency currency = Currency.ILS;
        driver.get(productPageLink);


        Document document = Jsoup.parse(driver.getPageSource());
        String productCategory = document.select("poloriz-stories#poloriz-widget-rectangles").attr("category");
        if (productCategory.equals("אקססוריזX") || productCategory.equals("עציצים") || productCategory.equals("חדר אמבטיה") || productCategory.equals("HOME")){
            throw new NonFashionItemException();
        }
        String description = document.select("div.product-name h1").first().text();
        String imgExtension = "jpg";

        int endIndex = productPageLink.indexOf(".html");
        int beginIndex = 0;
        for (int i = endIndex; i > 0; i--){
            if (productPageLink.charAt(i) == '/'){
                beginIndex = i;
                break;
            }
        }
        if (beginIndex > 0){
            productID = productPageLink.substring(beginIndex+1,endIndex);
        }
        if (productID == "") {
            throw new BadLinkException("This isn't a product page");
        }

        try {
            price = document.select("p.old-price span.price").first().text();
            ItemPriceCurr itemPriceCurr = priceTag(price);
            price = itemPriceCurr.price;
            salePrice = document.select("p.special-price span.price").first().text();
            ItemPriceCurr itemPriceCurrSale = priceTag(salePrice);
            salePrice = itemPriceCurrSale.price;
        } catch (NullPointerException e) {
            price = document.select("span.price").first().text();
            ItemPriceCurr itemPriceCurr = priceTag(price);
            price = itemPriceCurr.price;
        }

        String imageAddr = driver.findElement(By.xpath("//a[@data-index='0']")).findElement(By.xpath(".//img")).getAttribute("src");
        try {
            String thumbImage = driver.findElement(By.xpath("//a[@data-index='2']")).findElement(By.xpath(".//img")).getAttribute("src");
            links.add(thumbImage);
        }catch (NoSuchElementException e){

        }

        Map<String, ProductType> dict = classificationService.getHebrewDict();
        ItemClassificationService.ItemTags itemTags = classificationService.classify(description, dict);
        category = itemTags.getCategory();
        productType = itemTags.getProductType();

        return new UploadItemDTO(imageAddr, productPageLink, description,
                price, salePrice, currency, storeId, designer, imgExtension, productID, links, category, productType);

    }




}
