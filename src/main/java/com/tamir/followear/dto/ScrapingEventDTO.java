package com.tamir.followear.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@ToString
public class ScrapingEventDTO {

    private String link;
    private long storeId;

}
