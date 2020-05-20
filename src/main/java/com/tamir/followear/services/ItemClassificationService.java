package com.tamir.followear.services;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tamir.followear.AWS.s3.S3Service;
import com.tamir.followear.enums.Category;
import com.tamir.followear.enums.ProductType;
import lombok.Getter;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ItemClassificationService {

    private final Logger logger = LoggerFactory.getLogger(ItemClassificationService.class);

    @Autowired
    S3Service s3Service;

    @Getter
    Map<String, ProductType> hebrewDict;

    @Getter
    Map<String, ProductType> englishDict;

    @Value("${spring.profiles}")
    private String activeProfile;

    @Value("${fw.classification.english-dict}")
    private String englishDictKey;

    @Value("${fw.classification.hebrew-dict}")
    private String hebrewDictKey;


    @PostConstruct
    private void init() {
        hebrewDict = createHebrewDict();
        englishDict = createEnglishDict();
    }

    @ToString
    @Getter
    public class ItemTags {
        private Category category;
        private ProductType productType;

        public ItemTags(Category category, ProductType product) {
            this.category = category;
            this.productType = product;
        }
    }


    private Map<String, ProductType> insertIntoDict(Map<ProductType, List<String>> map) {
        Map<String, ProductType> finalDict = new HashMap<>();

        for(ProductType productType : map.keySet()) {
            List<String> productTypeValues = map.get(productType);
            for(String value : productTypeValues) {
                finalDict.put(value, productType);
            }
        }

        return finalDict;
    }

    private InputStream getDictStream(String dictKey) {
        try {
            if (activeProfile.equals("local")) {
                return new FileInputStream(dictKey);
            }
        } catch (FileNotFoundException e) {
            logger.error("local file not found");
            e.printStackTrace();
        }

        return s3Service.getFileAsStream(dictKey);
    }

    private Map<String, ProductType> createEnglishDict() {
        InputStream jsonSource = getDictStream(englishDictKey);
        TypeReference<HashMap<ProductType, List<String>>> typeRef =
                new TypeReference<HashMap<ProductType, List<String>>>() {};

        Map<ProductType, List<String>> map = new HashMap<>();
        try {
             map = new ObjectMapper().readValue(jsonSource, typeRef);
        } catch (JsonParseException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return insertIntoDict(map);

    }


    private Map<String, ProductType> createHebrewDict() {
        InputStream jsonSource = getDictStream(hebrewDictKey);
        TypeReference<HashMap<ProductType, List<String>>> typeRef =
                new TypeReference<HashMap<ProductType, List<String>>>() {};


        Map<ProductType, List<String>> map = new HashMap<>();
        try {
            map = new ObjectMapper().readValue(jsonSource, typeRef);
        } catch (JsonParseException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return insertIntoDict(map);
    }

    public ItemTags classify(String productDescription, Map<String, ProductType> dict) {
        String[] desc = productDescription.toLowerCase().split(" ");
        Category category = Category.Clothing;
        ProductType productType = ProductType.Default;
        ItemTags itemTags = new ItemTags(category, productType);
        Boolean pantsKey = false;
        Boolean topsKey = false;
        Boolean jacketsOrCoatsKey = false;
        Boolean dressesOrSkirts = false;
        Boolean accessoriesKey = false;
        Boolean shoesKey = false;
        Boolean lingerieKey = false;
        Boolean swimwearKey = false;
        Boolean bagsKey = false;
        for (String str : desc) {

            ProductType key = dict.get(str);
            if (key == null) {
                continue;
            }

            if (key == ProductType.Bags) {
                productType = ProductType.Default;
                category = Category.Bags;
                bagsKey = true;
            }
            if (key == ProductType.Shoes) {
                productType = ProductType.Default;
                category = Category.Shoes;
                shoesKey = true;
            }
            if (key == ProductType.Accessories) {
                productType = ProductType.Default;
                accessoriesKey = true;
                category = Category.Accessories;
            }
            if (key == ProductType.Pants) {
                pantsKey = true;
                productType = ProductType.Pants;
            }
            if (key == ProductType.JacketsOrCoats) {
                jacketsOrCoatsKey = true;
                productType = ProductType.JacketsOrCoats;
            }

            if (key == ProductType.Tops) {
                topsKey = true;
                productType = ProductType.Tops;
            }
            if (key == ProductType.DressesOrSkirts) {
                dressesOrSkirts = true;
                productType = ProductType.DressesOrSkirts;
            }
            if (key == productType.Lingerie) {
                lingerieKey = true;
                productType = ProductType.Lingerie;
            }
            if (key == ProductType.Swimwear) {
                swimwearKey = true;
                productType = ProductType.Swimwear;
            }
        }
        if (pantsKey && (jacketsOrCoatsKey || topsKey || dressesOrSkirts)) {
            if (jacketsOrCoatsKey) {
                productType = ProductType.JacketsOrCoats;
            }
            if (topsKey) {
                productType = ProductType.Tops;
            }
            if (dressesOrSkirts) {
                productType = ProductType.DressesOrSkirts;
            }
        }
        if (accessoriesKey && (dressesOrSkirts || topsKey || jacketsOrCoatsKey || shoesKey || pantsKey)) {
            category = Category.Clothing;
            if (dressesOrSkirts) {
                productType = ProductType.DressesOrSkirts;
            }
            if (topsKey) {
                productType = productType.Tops;
            }
            if (jacketsOrCoatsKey) {
                productType = ProductType.JacketsOrCoats;
            }
            if (shoesKey) {
                productType = ProductType.Shoes;
            }
            if (pantsKey) {
                productType = ProductType.Pants;
            }
        }
        if (lingerieKey) {
            productType = ProductType.Lingerie;
        }

        if (swimwearKey && (topsKey || lingerieKey)) {
            productType = ProductType.Swimwear;
        }
        if (swimwearKey && bagsKey) {
            category = Category.Bags;
            productType = ProductType.Bags;
        }

        itemTags.category = category;
        itemTags.productType = productType;

        return itemTags;

    }
}
