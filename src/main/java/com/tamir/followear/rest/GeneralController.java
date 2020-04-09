package com.tamir.followear.rest;

import com.tamir.followear.dto.*;
import com.tamir.followear.entities.User;
import com.tamir.followear.exceptions.InvalidUserException;
import com.tamir.followear.services.FeedService;
import com.tamir.followear.services.ScrapingService;
import com.tamir.followear.services.UserService;
import org.openqa.selenium.WebDriver;
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

    @PostMapping("explore-feed")
    @ResponseBody
    public FeedResultDTO getExploreFeed(@RequestBody Optional<FilteringDTO> filters) {
        return feedService.getExploreFeed(filters);
    }

    @GetMapping("profile-info")
    @ResponseBody
    public UserProfileInfoDTO profileInfo(@RequestParam long userId) {
        User user = userService.findById(userId);
        if(user==null)
            throw new InvalidUserException();
        UserProfileInfoDTO ret = new UserProfileInfoDTO(user.getId(), user.getUsername(), user.getFullName(),
                user.getProfileImageAddr(), user.getDescription());
        return ret;
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
