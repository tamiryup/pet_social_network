package com.tamir.followear.rest;

import com.tamir.followear.dto.SearchDTO;
import com.tamir.followear.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("general")
public class GeneralController {

    @Autowired
    UserService userService;

    @GetMapping("search")
    public List<SearchDTO> search(@RequestParam String query) {
        return userService.searchAutocomplete(query);
    }

}
