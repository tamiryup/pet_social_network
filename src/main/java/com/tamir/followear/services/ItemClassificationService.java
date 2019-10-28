package com.tamir.followear.services;

import com.tamir.followear.enums.Category;
import com.tamir.followear.enums.ProductType;
import lombok.Getter;
import lombok.ToString;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ItemClassificationService {

    @Getter
    Map<ProductType, List<String>> hebrewDict;

    @Getter
    Map<ProductType, List<String>> englishDict;

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

    private Map<ProductType, List<String>> createHebrewDict() {
        Map<ProductType, List<String>> hebrewDictionary = new HashMap<>();
        List<String> topsValues = Arrays.asList("שירט", "אוברול", "חולצ", "גופיי", "סווט", "סווד", "טופ", "בגד גוף");
        List<String> dressValues = Arrays.asList("שמלת", "חצאית");
        List<String> pantsValues = Arrays.asList("ג'ינס", "שורטס", "טייץ", "מכנס");
        List<String> shoesValues = Arrays.asList("נעל", "spadrilles",
                "קבקבי", "סנדל", "מגפ", "מגף");
        List<String> coatsAndJacketsValues = Arrays.asList("ג'קט", "קרדיגן", "מעיל", "וסט", "ז'קט");
        List<String> swimwearValues = Arrays.asList("בגד ים", "ביקיני");
        List<String> accesoriesValues = Arrays.asList("תכשיט", "משקפי שמש", "משקפיי", "חגור", "כובע",
                "גרבי", "מטפחת", "צעיף", "עגיל", "קשת");

        hebrewDictionary.put(ProductType.Tops, topsValues);
        hebrewDictionary.put(ProductType.DressesOrSkirts, dressValues);
        hebrewDictionary.put(ProductType.Pants, pantsValues);
        hebrewDictionary.put(ProductType.Shoes, shoesValues);
        hebrewDictionary.put(ProductType.JacketsOrCoats, coatsAndJacketsValues);
        hebrewDictionary.put(ProductType.Swimwear, swimwearValues);
        hebrewDictionary.put(ProductType.Accessories, accesoriesValues);

        return hebrewDictionary;
    }

    private Map<ProductType, List<String>> createEnglishDict() {
        Map<ProductType, List<String>> englishDictionary = new HashMap<>();
        List<String> topsValues = Arrays.asList("top", "tee", "weater", "jumper", "hirt", "tank",
                "cami", "bodysuit", "blouse", "bandeau", "vest", "singlet", "body",
                "hoodie", "sweatshirt", "pullover", "turtleneck", "polo", "tunic", "jumpsuit");
        List<String> dressValues = Arrays.asList("dress", "skirt");
        List<String> pantsValues = Arrays.asList("pants", "trousers",
                "legging", "short", "jeans");
        List<String> shoesValues = Arrays.asList("shoes", "spadrilles",
                "heel", "boots", "trainers", "slippers", "sandals", "runner", "slider", "sneakers");
        List<String> coatsAndJacketsValues = Arrays.asList("vest", "blazer", "cardigan",
                "coat", "jacket", "waistcoat", "pullover", "parka", "poncho", "bomber", "suit",
                "duster", "kimono", "wrap");
        List<String> bagValues = Arrays.asList("bag", "tote",
                "clutch", "crossbody", "cross-body", "wallet", "backpack", "satchel", "handbag",
                "basket", "clutch-bag", "handbag");

        englishDictionary.put(ProductType.Tops, topsValues);
        englishDictionary.put(ProductType.DressesOrSkirts, dressValues);
        englishDictionary.put(ProductType.Pants, pantsValues);
        englishDictionary.put(ProductType.Shoes, shoesValues);
        englishDictionary.put(ProductType.JacketsOrCoats, coatsAndJacketsValues);
        englishDictionary.put(ProductType.Bags, bagValues);

        return englishDictionary;
    }

    public ItemTags classify(String productDescription, Map<ProductType, List<String>> dict) {
        ProductType productType = ProductType.Default;
        Category category = Category.Clothing;
        ItemTags itemTags = new ItemTags(category, productType);
        Boolean pantsKey = false;
        Boolean topsKey = false;
        Boolean jacketsOrCoatsKey = false;
        Boolean dressesOrSkirts = false;
        for (Map.Entry<ProductType, List<String>> entry : dict.entrySet()) {
            ProductType key = entry.getKey();
            List<String> value = entry.getValue();
            for (String aString : value) {
                if (productDescription.toLowerCase().contains(aString)) {

                    productType = key;
                    if (key == ProductType.Bags) {
                        productType = ProductType.Default;
                        category = Category.Bags;
                    }
                    if (key == ProductType.Shoes) {
                        productType = ProductType.Default;
                        category = Category.Shoes;
                    }
                    if (key == ProductType.Accessories) {
                        productType = ProductType.Default;
                        category = Category.Accessories;
                    }
                    if (key == ProductType.Pants) {
                        pantsKey = true;
                    }
                    if (key == ProductType.JacketsOrCoats) {
                        jacketsOrCoatsKey = true;
                    }
                    if (key == ProductType.Tops) {
                        topsKey = true;
                    }
                    if (key == ProductType.DressesOrSkirts) {
                        dressesOrSkirts = true;
                    }
                }

                if (pantsKey && (jacketsOrCoatsKey || topsKey || dressesOrSkirts)) {
                    if (jacketsOrCoatsKey) {
                        key = ProductType.JacketsOrCoats;
                    }
                    if (topsKey) {
                        key = ProductType.Tops;
                    }
                    if (dressesOrSkirts) {
                        key = ProductType.DressesOrSkirts;
                    }
                }
            }
        }
        itemTags.category = category;
        itemTags.productType = productType;

        return itemTags;
    }
}
