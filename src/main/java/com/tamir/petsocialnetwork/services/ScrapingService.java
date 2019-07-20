package com.tamir.petsocialnetwork.services;

import com.tamir.petsocialnetwork.dto.UploadItemDTO;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ScrapingService {

    @PostConstruct
    public void init() {
        WebDriverManager.chromedriver().setup();
    }

    private WebDriver getDriver(String productPageLink) {
        WebDriver driver = new ChromeDriver();
        driver.get(productPageLink);
        return driver;
    }

    public UploadItemDTO extractItem(String website,String productPageLink) {
        return new UploadItemDTO();
    }


    public List<String> getThumbnailImages(String website, String productPageLink) {
        List<String> links = new ArrayList<>();

        try {

            switch (website) {
                case "www.asos.com":
                    links = asosThumbnails(productPageLink);
                    break;
                case "www.net-a-porter.com":
                    links = netAPorterThumbnails(productPageLink);
                    break;
                case "www.adikastyle.com":
                    links = adikaThumbnails(productPageLink);
                    break;
                case "www.terminalx.com":
                    links = terminalXThumbnails(productPageLink);
                    break;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return links;
    }

    //TODO: replace addresses with the addresses of better image qualities
    private List<String> asosThumbnails(String productPageLink) throws IOException{
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

    private List<String> adikaThumbnails(String productPageLink) {
        List<String> links;
        WebDriver driver = getDriver(productPageLink);
        Document document = Jsoup.parse(driver.getPageSource());

        Element elem = document.selectFirst(".more-views-thumbs");
        Elements imageElements = elem.getElementsByTag("a");
        links = imageElements.eachAttr("href");
        links.remove(0);

        driver.close();
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


}
