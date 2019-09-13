package com.tamir.followear.dto;

import com.tamir.followear.enums.Category;
import com.tamir.followear.enums.ProductType;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@ToString
public class FilteringDTO {

    private Category category;
    private List<ProductType> productTypes;
    private List<String> designers;
    private List<Long> stores; //stores ids
    private int minPrice; //in ILS, 0 is ignore
    private int maxPrice; //in ILS, 0 is ignore

}
