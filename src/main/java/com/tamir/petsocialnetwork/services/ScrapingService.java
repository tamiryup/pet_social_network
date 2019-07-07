package com.tamir.petsocialnetwork.services;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ScrapingService {

    @PostConstruct
    public void init() {

    }

    public List<String> getThumbnailImages(String website, String productPageLink) {
        List<String> links = new ArrayList<>();

        try {

            Document document = Jsoup.connect(productPageLink).get();

            switch (website) {
                case "www.asos.com":
                    links = asosThumbnails(document);
                    break;
                case "www.net-a-porter.com":
                    links = netAPorterThumbnails(document);
                    break;
                case "www.adikastyle.com":
                    links = adikaThumbnails(document);
                    break;
                case "www.terminalx.com":
                    links = terminalXThumbnails();
                    break;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return links;
    }

    //TODO: replace addresses with the addresses of better image qualities
    private List<String> asosThumbnails(Document document) {
        List<String> links;
        Element elem = document.selectFirst(".thumbnails");
        Elements imageElements = elem.getElementsByTag("img");
        links = imageElements.eachAttr("src");
        links.remove(0);
        return links;
    }

    private List<String> netAPorterThumbnails(Document document) {
        List<String> links;
        Element elem = document.selectFirst(".thumbnail-wrapper");
        Elements imageElements = elem.getElementsByTag("img");
        links = imageElements.eachAttr("src");
        links.remove(0);

        for (int i = 0; i < links.size(); i++) {
            links.set(i, "https:" + links.get(i));
        }

        return links;
    }

    private List<String> adikaThumbnails(Document document) {
        List<String> links;
        Elements elements = document.select("li.image-shade");
        Element elem = document.selectFirst(".more-views-thumbs");
        Elements imageElements = elem.getElementsByTag("a");
        links = imageElements.eachAttr("href");
        return links;
    }

    //TODO: replace addresses with the addresses of better image qualities
    private List<String> terminalXThumbnails() {
        List<String> links = new ArrayList<>();
//        List<WebElement> imageContainerElems = driver.findElements(By.cssSelector(".fotorama__thumb"));
//
//        for (WebElement ele : imageContainerElems) {
//            WebElement imgSrc = ele.findElement(By.tagName("img"));
//            links.add(imgSrc.getAttribute("src"));
//        }

        return links;
    }


}
