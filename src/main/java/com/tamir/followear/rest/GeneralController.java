package com.tamir.followear.rest;

import com.tamir.followear.dto.*;
import com.tamir.followear.services.ExploreService;
import com.tamir.followear.services.FeedService;
import com.tamir.followear.services.ScrapingService;
import com.tamir.followear.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("general")
public class GeneralController {

    @Autowired
    UserService userService;

    @Autowired
    FeedService feedService;

    @Autowired
    ScrapingService scrapingService;

    @GetMapping("search")
    @ResponseBody
    public List<SearchDTO> search(@RequestParam String query) {
        return userService.searchAutocomplete(query);
    }

    @GetMapping("discover-people")
    @ResponseBody
    public List<DiscoverPeopleDTO> discoverPeople() {
        return feedService.getDiscoverPeople();
    }

    @GetMapping("explore-feed")
    @ResponseBody
    public FeedResultDTO getExploreFeed(@RequestBody Optional<FilteringDTO> filters) {
        return feedService.getExploreFeed(filters);
    }

    @GetMapping("scraping-helper")
    @ResponseBody
    public String scrapingHelper(@RequestParam String link) {
        return scrapingService.getDriver(link).getPageSource();
    }

}
