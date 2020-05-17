package com.tamir.followear.rest;

import com.tamir.followear.dto.*;
import com.tamir.followear.services.FeedService;
import com.tamir.followear.services.LikeService;
import com.tamir.followear.services.ScrapingService;
import com.tamir.followear.services.UserService;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("general")
public class GeneralController {

    private final Logger logger = LoggerFactory.getLogger(GeneralController.class);

    @Autowired
    UserService userService;

    @Autowired
    FeedService feedService;

    @Autowired
    LikeService likeService;

    @Autowired
    ScrapingService scrapingService;

    @GetMapping("search")
    @ResponseBody
    public List<SearchDTO> search(@RequestParam String query) {
        logger.info("starting search input query: {}", query);
        return userService.searchAutocomplete(query);
    }

    @GetMapping("like-list")
    @ResponseBody
    public List<FeedFollowDTO> likeList(@RequestParam long postId) {
        logger.info("starting likeList input postId: {}", postId);
        return likeService.likeList(postId, 15);
    }

    @GetMapping("discover-people")
    @ResponseBody
    public List<DiscoverPeopleDTO> discoverPeople() {
        logger.info("starting getGeneralDiscoverPeople");
        return feedService.getDiscoverPeople();
    }

    @PostMapping("explore-feed")
    @ResponseBody
    public FeedResultDTO getExploreFeed(@RequestBody Optional<FilteringDTO> filters) {
        logger.info("starting getGeneralExploreFeed input filters: {}", filters);
        return feedService.getExploreFeed(filters);
    }

    @GetMapping("scraping-helper")
    @ResponseBody
    public String scrapingHelper(@RequestParam String link) {
        WebDriver driver = scrapingService.getDriver();
        driver.get(link);
        String pageSource = driver.getPageSource();
        driver.close();
        return pageSource;
    }

}
