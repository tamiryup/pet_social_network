package com.tamir.petsocialnetwork.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class UploadItemDTO {

    private long userId;
    private String imageUrl;
    private String link;
    private String extension;

}
