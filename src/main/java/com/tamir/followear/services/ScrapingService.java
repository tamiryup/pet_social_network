package com.tamir.followear.services;

import com.tamir.followear.dto.UploadItemDTO;
import com.tamir.followear.entities.Store;
import com.tamir.followear.enums.Category;
import com.tamir.followear.enums.ProductType;
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
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

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

    @Autowired
    StoreService storeService;

    public class ItemTags {
        private Category category;
        private ProductType productType;

        public ItemTags(Category category,ProductType product){
            this.category = category;
            this.productType = product;
        }
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

    public long getStoreID(String website){
        Store store = storeService.findByWebsite(website);
        long id = store.getId();
        return id;
    }

    public static String getDomainName(String productPageLink) throws URISyntaxException {
        URI uri = new URI(productPageLink);
        String domain = uri.getHost();
        if (domain.startsWith("www.")) {
            return domain.substring(4);
        }
        if (domain.startsWith("il.")){
            return  domain.substring(3);
        }
        else{
            return domain;
        }
    }

    public UploadItemDTO extractItem(String productPageLink) {
        UploadItemDTO itemDTO = new UploadItemDTO();
        long storeId;
        String website;
        try{
            website = getDomainName(productPageLink);
        }
        catch (Exception e){
            System.out.println("error - couldn't extract website");
            System.out.println(e);
            return null; // TODO return error!
        }
        storeId = getStoreID(website);
        switch (website) {
            case "asos.com":
                itemDTO = asosDTO(productPageLink, storeId);
                break;
            case "net-a-porter.com":
                itemDTO = netaporterDTO(productPageLink, storeId);
                break;
            case "terminalx.com":
                itemDTO = terminalxDTO(productPageLink, storeId);
                break;
            case "farfetch.com":
                itemDTO = farfetchDTO(productPageLink, storeId);
                break;
            case "shein.com":
                itemDTO = sheinDTO(productPageLink, storeId);
                break;
            case "zara.com":
                itemDTO = zaraDTO(productPageLink, storeId);
                break;
            case "hm.com":
                itemDTO = hmDTO(productPageLink, storeId);
                break;
            case "shopbop.com":
                itemDTO = shopBopDTO(productPageLink, storeId);
                break;
            default:
                System.out.println("Looking forward to the Weekend");
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


    public Map<ProductType, List<String>> InitilizeItemsHebrewDict() {
        Map<ProductType, List<String>> hebrewDictionary = new HashMap<>();
        List<String> topsValues = Arrays.asList("שירט", "אוברול", "חולצ", "גופיי", "סווט", "סווד", "טופ", "בגד גוף");
        List<String> dressValues = Arrays.asList("שמלת", "חצאית");
        List<String> pantsValues = Arrays.asList("ג'ינס", "שורטס", "טייץ","מכנס");
        List<String> shoesValues = Arrays.asList("נעל", "spadrilles",
                "קבקבי", "סנדל", "מגפ", "מגף");
        List<String> coatsAndJacketsValues = Arrays.asList("ג'קט","קרדיגן", "מעיל", "וסט", "ז'קט");
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

        hebrewDictionary.put(ProductType.Tops, topsValues);
        hebrewDictionary.put(ProductType.DressesOrSkirts, dressValues);
        hebrewDictionary.put(ProductType.Pants, pantsValues);
        hebrewDictionary.put(ProductType.Shoes, shoesValues);
        hebrewDictionary.put(ProductType.JacketsOrCoats, coatsAndJacketsValues);
        //hebrewDictionary.put(ProductType.Bags, bagValues);
        hebrewDictionary.put(ProductType.Swimwear, swimwearValues);
        hebrewDictionary.put(ProductType.Accessories, accesoriesValues);

        return hebrewDictionary;
    }


    public Map<ProductType, List<String>> InitilizeItemsEnglishDict() {
        ProductType productType;

        Map<ProductType, List<String>> englishDictionary = new HashMap<>();
        List<String> topsValues = Arrays.asList("top", "tee", "weater", "jumper", "hirt", "tank",
                "cami", "bodysuit", "blouse", "bandeau", "vest", "singlet", "body",
                "hoodie", "sweatshirt", "pullover", "turtleneck", "polo", "tunic","jumpsuit");
        List<String> dressValues = Arrays.asList("dress", "skirt");
        List<String> pantsValues = Arrays.asList("pants", "trousers",
                "legging", "short", "jeans");
        List<String> shoesValues = Arrays.asList("shoes", "spadrilles",
                "heel", "boots", "trainers", "slippers", "sandals", "runner", "slider", "sneakers");
        List<String> coatsAndJacketsValues = Arrays.asList("vest", "blazer","cardigan",
                "coat", "jacket", "waistcoat", "pullover", "parka", "poncho", "bomber", "suit",
                "duster", "kimono", "wrap");
        List<String> bagValues = Arrays.asList("bag", "tote",
                "clutch", "crossbody", "cross-body", "wallet", "backpack", "satchel", "handbag",
                "basket", "clutch-bag", "handbag");

        englishDictionary.put(ProductType.Tops, topsValues);
        englishDictionary.put(ProductType.DressesOrSkirts, dressValues);
        englishDictionary.put(ProductType.Pants, pantsValues);
        englishDictionary.put(ProductType.Shoes, shoesValues);
        englishDictionary.put(ProductType.JacketsOrCoats, coatsAndJacketsValues);
        englishDictionary.put(ProductType.Bags, bagValues);

        return englishDictionary;
    }


    public ItemTags itemClassification(String productDescription, Map<ProductType, List<String>> dict) {
        ProductType productType= ProductType.Default;
        Category category = Category.Clothing;
        Map<String,ItemTags> result = null;
        ItemTags itemTags = new ItemTags(category,productType);
        Boolean pantsKey = false;
        Boolean topsKey = false;
        Boolean jacketsOrCoatsKey = false;
        Boolean dressesOrSkirts = false;
        for (Map.Entry<ProductType, List<String>> entry : dict.entrySet()) {
            ProductType key = entry.getKey();
            List<String> value = entry.getValue();
            for (String aString : value) {
                if (productDescription.toLowerCase().contains(aString)) {

                    productType = key;
                    if (key == ProductType.Bags ){
                        productType = ProductType.Default;
                        category = Category.Bags;
                    }
                    if (key == ProductType.Shoes){
                        productType = ProductType.Default;
                        category = Category.Shoes;
                    }
                    if (key == ProductType.Accessories){
                        productType = ProductType.Default;
                        category = Category.Accessories;
                    }
                    if (key == ProductType.Pants) {
                        pantsKey = true;
                    }
                    if (key == ProductType.JacketsOrCoats){
                        jacketsOrCoatsKey = true;
                    }
                    if (key == ProductType.Tops) {
                        topsKey = true;
                    }
                    if (key == ProductType.DressesOrSkirts) {
                        dressesOrSkirts = true;
                    }
                }
                if (pantsKey && (jacketsOrCoatsKey || topsKey || dressesOrSkirts)) {
                    if (jacketsOrCoatsKey){
                        key = ProductType.JacketsOrCoats;
                    }
                    if (topsKey){
                        key = ProductType.Tops;
                    }
                    if (dressesOrSkirts){
                        key = ProductType.DressesOrSkirts;
                    }
                }
            }
        }
        itemTags.category = category;
        itemTags.productType = productType;


        return itemTags;
    }


    public Currency priceTag(String fullPrice) {
        String CURRENCY_SYMBOLS= "\\p{Sc}\u0024\u060B";
        Pattern p = Pattern.compile("[" +CURRENCY_SYMBOLS + "][\\d,]+");
        Matcher m = p.matcher(fullPrice);
        String symbol = fullPrice;
        Currency result = Currency.USD;

        while (m.find()) {
            symbol = m.group(0);
        }

        symbol = symbol.substring(0,1);

        switch (symbol){
            case "£":
                result = Currency.GBP;
                break;
            case "₪":
                result = Currency.ILS;
                break;
        }
        return result;
    }


    private UploadItemDTO asosDTO(String productPageLink, long storeID) {
        String productID;
        Category category;
        ProductType productType;
        String designer = null;
        int endIndex;
        int beginIndex = productPageLink.indexOf("/prd/");
        if (beginIndex == -1) {
            System.out.println("this is not a product page");
            return null;
        }else{
            beginIndex = beginIndex + 5;
            endIndex = beginIndex + 8;
        }
        productID = productPageLink.substring(beginIndex, endIndex);

        WebDriver driver = getDriver(productPageLink);
        Document document = Jsoup.parse(driver.getPageSource());
        Element descriptionDiv = document.select("div.product-hero").first();
        Element descriptionText = descriptionDiv.select("h1").first();
        String description = descriptionText.text();
        Element priceSpan = document.select("span.current-price").first();
        String fullPrice = priceSpan.text();
        Currency currency = priceTag(fullPrice);
        String price = (fullPrice.substring(1));
        Elements imagesDiv = document.select("div.fullImageContainer");
        Elements images = imagesDiv.select("img");
        String imgExtension = "jpg";
        List<String> links = images.eachAttr("src");
        String imageAddr = links.get(1);
        links.remove(1);
        links.remove(3);
        driver.close();

        Map<ProductType, List<String>> dict = InitilizeItemsEnglishDict();
        ItemTags itemTags = itemClassification(description, dict);
        category = itemTags.category;
        productType = itemTags.productType;

        return new UploadItemDTO(imageAddr, productPageLink, description,
                price, currency, storeID, designer, imgExtension, productID, links, category,productType);

    }

    private UploadItemDTO netaporterDTO(String productPageLink, long storeId) {
        String productID;
        Category category;
        ProductType productType;
        int beginIndex = productPageLink.indexOf("/product/");
        if (beginIndex == -1) {
            System.out.println("this is not a product page");
            return null;
        }
        beginIndex = beginIndex + 9;
        int endIndex = beginIndex + 7;
        productID = productPageLink.substring(beginIndex, endIndex);

        WebDriver driver = getDriver(productPageLink);
        Document document = Jsoup.parse(driver.getPageSource());
        Element descriptionDiv = document.select(" h2.product-name").first();
        String description = descriptionDiv.text();
        Element priceSpan = document.select("span.full-price.style-scope.nap-price").first();
        Element priceSymbol = document.select("span.currency.style-scope.nap-price").first();
        String price = priceSpan.text();
        String priceSymbolText = priceSymbol.text();
        Currency currency = priceTag(priceSymbolText);
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
        int maxThumbnails = Math.min(endOfThumbnails,size);
        links = links.subList(0,maxThumbnails);
        for (int i = 0; i < links.size(); i++) {
            links.set(i, "https:" + links.get(i));
        }
        driver.close();

        Map<ProductType, List<String>> dict = InitilizeItemsEnglishDict();
        ItemTags itemTags = itemClassification(description, dict);
        category = itemTags.category;
        productType = itemTags.productType;

        return new UploadItemDTO(imageAddr, productPageLink, description,
                price, currency,storeId, designer, imgExtension, productID, links, category,productType);
    }


    private UploadItemDTO terminalxDTO(String productPageLink, long storeId) {
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
            System.out.println("this is not a product page");
            return null;
        } else {
            productID = productID.substring(beginIndex, endIndex);
        }
        Currency currency = Currency.ILS; //terminalx only uses ILS

        WebDriver driver = getDriver(productPageLink);
        Document document = Jsoup.parse(driver.getPageSource());
        Element descriptionDiv = document.select("span.base.attribute_name").first();
        String description = descriptionDiv.text();
        Element priceSpan = document.select("span.price").first();
        String price = priceSpan.text();
        Element designerDiv = document.select("div.product-item-brand a").first();
        String designer = designerDiv.text();
        Elements imagesDiv = document.select(
                "div.magnifier-preview");
        Elements imageElements = imagesDiv.select("img");
        List<String> links = imageElements.eachAttr("src");
        String imgExtension = "jpg";
        String imageAddr = links.get(0);
        links.remove(0);
        driver.close();

        Map<ProductType, List<String>> dict = InitilizeItemsHebrewDict();
        ItemTags itemTags = itemClassification(description, dict);
        category = itemTags.category;
        productType = itemTags.productType;


        return new UploadItemDTO(imageAddr, productPageLink, description,
                price, currency,storeId, designer, imgExtension, productID, links, category,productType);
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
//        driver.close();
//
//        Map<String, List<String>> dict = InitilizeItemsHebrewDict();
//        List<String> itemTags = itemClassification(description, dict);

//        return new UploadItemDTO(imageAddr, productPageLink, description,
//                price, currency, website, designer, imgExtension, productID, links, itemTags);
//        return new UploadItemDTO();
//    }


    private UploadItemDTO farfetchDTO(String productPageLink, long storeId) {
        String productID = null;
        Category category;
        ProductType productType;

        WebDriver driver = getDriver(productPageLink);
        Document document = Jsoup.parse(driver.getPageSource());
        Element descriptionDiv = document.select("span._b4693b").first();
        String description = descriptionDiv.text();
        Element designerDiv = document.select("a._7fe79a._3f59ca._dc2535._f01e99 span").first();
        String designer = designerDiv.text();
        Element priceSpan = document.select("span._def925._b4693b").first();
        String price = priceSpan.text();
        Currency currency = priceTag(price);
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
        driver.close();

        Map<ProductType, List<String>> dict = InitilizeItemsEnglishDict();
        ItemTags itemTags = itemClassification(description, dict);
        category = itemTags.category;
        productType = itemTags.productType;


        return new UploadItemDTO(imageAddr, productPageLink, description,
                price, currency,storeId, designer, imgExtension, productID, links, category,productType);
    }

    private UploadItemDTO sheinDTO(String productPageLink, long storeId) {
        String productID = null;
        Category category;
        ProductType productType;
        String price = null;
        List<String> links = new ArrayList<>();
        Currency currency;
        WebDriver driver = getDriver(productPageLink);
        Document document = Jsoup.parse(driver.getPageSource());
        Element descriptionDiv = document.select("h1.name").first();
        String description = descriptionDiv.text();
//        String designer = document.select("h1.name.span").first().text();
        String designer = "";
        Element priceSpan = document.select("div.price-origin.j-price-origin").first();
        Element discountedPriceSpan = document.select("div.price-discount.j-price-discount").first();


        if (discountedPriceSpan!=null) {
            price = discountedPriceSpan.text();
            currency = priceTag(price);
            price = price.substring(1);
        }

        else  {
            price = priceSpan.text();
            currency = priceTag(price);
            price = price.substring(1);
        }

        Element imageDiv = document.select("img.j-lazy-dpr-img.j-change-main_image").first();
        String imageAddr = imageDiv.attr("src");
        imageAddr = "https:" + imageAddr;
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
        for (int i = 0; i < links.size(); i++) {
            links.set(i, "https:" + links.get(i));
        }
        Map<ProductType, List<String>> dict = InitilizeItemsHebrewDict();
        ItemTags itemTags = itemClassification(description, dict);
        category = itemTags.category;
        productType = itemTags.productType;
        if (productType==ProductType.Default){
            dict = InitilizeItemsEnglishDict();
            itemTags = itemClassification(description, dict);
            category = itemTags.category;
            productType = itemTags.productType;
        }


        return new UploadItemDTO(imageAddr, productPageLink, description,
                price, currency,storeId, designer, imgExtension, productID, links, category,productType);
    }


    private UploadItemDTO zaraDTO(String productPageLink, long storeId) {
        Category category;
        ProductType productType;
        WebDriver driver = getDriver(productPageLink);
        Document document = Jsoup.parse(driver.getPageSource());
        Element descriptionDiv = document.select(" h1.product-name").first();
        String description = descriptionDiv.text();
        description = description.replace("פרטי", "");
        Element priceSpan = document.select("div.price._product-price span").first();
        String price = priceSpan.text();
        Currency currency = priceTag(price);
        price = price.substring(1);
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
        driver.close();

        Map<ProductType, List<String>> dict = InitilizeItemsHebrewDict();
        ItemTags itemTags = itemClassification(description, dict);
        category = itemTags.category;
        productType = itemTags.productType;


        return new UploadItemDTO(imageAddr, productPageLink, description,
                price, currency,storeId, designer, imgExtension, productID, links, category,productType);
    }

    private UploadItemDTO hmDTO(String productPageLink, long storeId) {
        String designer = null;
        Category category;
        ProductType productType;
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

        Map<ProductType, List<String>> dict = InitilizeItemsEnglishDict();
        ItemTags itemTags = itemClassification(description, dict);
        category = itemTags.category;
        productType = itemTags.productType;

        return new UploadItemDTO(imageAddr, productPageLink, description,
                price, currency,storeId, designer, imgExtension, productID, links, category,productType);

    }


    private UploadItemDTO shopBopDTO(String productPageLink, long storeId) {
        Category category;
        ProductType productType;
        WebDriver driver = getDriver(productPageLink);
        Document document = Jsoup.parse(driver.getPageSource());
        Element descriptionDiv = document.select(" div#product-title").first();
        String description = descriptionDiv.text();
        String designer = document.select("span.brand-name").first().text();
        Element priceSpan = document.select("span.pdp-price").first();
        String price = priceSpan.text();
        Currency currency = priceTag(price);
        price = price.substring(1);
        Elements elem = document.select("img.display-image");
        List<String> links = elem.eachAttr("src");
        String imageAddr = links.get(0);
        String imgExtension = "jpg";
        links.remove(0);
        int endIndex = productPageLink.indexOf("htm");
        int startIndex = endIndex - 11;
        String productID = productPageLink.substring(startIndex, endIndex - 1);
        driver.close();

        Map<ProductType, List<String>> dict = InitilizeItemsEnglishDict();
        ItemTags itemTags = itemClassification(description, dict);
        category = itemTags.category;
        productType = itemTags.productType;

        return new UploadItemDTO(imageAddr, productPageLink, description,
                price, currency,storeId, designer, imgExtension, productID, links, category,productType);
    }

}
