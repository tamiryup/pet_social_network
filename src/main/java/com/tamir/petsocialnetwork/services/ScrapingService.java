package com.tamir.petsocialnetwork.services;

import com.tamir.petsocialnetwork.dto.UploadItemDTO;
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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Service
public class ScrapingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScrapingService.class);

    @Value("${ps.chrome.driver}")
    private String chromedriverPath;

    @Value("${ps.chrome.binary}")
    private String chromeBinary;

    @PostConstruct
    public void init() throws IOException{
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
