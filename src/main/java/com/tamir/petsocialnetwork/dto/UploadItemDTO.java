package com.tamir.petsocialnetwork.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Currency;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class UploadItemDTO {

    private String imageAddr;
    private String link;
    private String description;
    private String price;
    private Currency currency;
    private String website;
    private String designer;
    private String imgExtension;
    private String productId;
    private List<String> thumbnails;

}
