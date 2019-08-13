package com.tamir.petsocialnetwork.dto;

import com.tamir.petsocialnetwork.enums.Currency;
import lombok.*;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@ToString
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
    private List<String> itemClassification;

}
