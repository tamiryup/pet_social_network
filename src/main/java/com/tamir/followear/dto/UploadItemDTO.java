package com.tamir.followear.dto;

import com.tamir.followear.enums.Currency;
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
    private long storeId;
    private String designer;
    private String imgExtension;
    private String productId;
    private List<String> thumbnails;
    private String category;
    private String productType;

}
