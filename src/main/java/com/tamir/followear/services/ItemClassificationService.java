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
    Map<String, ProductType> hebrewDict;

    @Getter
    Map<String, ProductType> englishDict;


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


    private Map<String, ProductType> insertIntoDict (List<String> topsValues,List<String> dressValues,List<String> pantsValues,List<String> shoesValues,List<String> coatsAndJacketsValues,List<String> bagValues,List<String> lingerieValues,List<String> accessoriesValues,List<String> swimwearValues ){
        Map<String, ProductType> englishDictionary = new HashMap<>();
        for (String top: topsValues){
            englishDictionary.put(top, ProductType.Tops);
        }
        for (String dress: dressValues){
            englishDictionary.put(dress, ProductType.DressesOrSkirts);
        }
        for (String pants: pantsValues){
            englishDictionary.put(pants, ProductType.Pants);
        }
        for (String shoe: shoesValues){
            englishDictionary.put(shoe, ProductType.Shoes);
        }
        for (String jacket: coatsAndJacketsValues){
            englishDictionary.put(jacket, ProductType.JacketsOrCoats);
        }
        for (String bag: bagValues){
            englishDictionary.put(bag, ProductType.Bags);
        }
        for (String lingerie: lingerieValues){
            englishDictionary.put(lingerie, ProductType.Lingerie);
        }
        for (String accessorie: accessoriesValues){
            englishDictionary.put(accessorie, ProductType.Accessories);
        }
        for (String swimwear: swimwearValues){
            englishDictionary.put(swimwear, ProductType.Swimwear);
        }
        return englishDictionary;
    }

    private Map<String, ProductType> createEnglishDict () {
        List<String> topsValues = Arrays.asList("top", "tee", "sweater", "jumper", "shirt", "tank",
                "cami", "bodysuit", "blouse", "bandeau", "vest", "singlet", "body",
                "hoodie", "sweatshirt","sweater","t-shirt", "pullover", "turtleneck", "polo", "tunic", "jumpsuit", "shirt", "hoodie");
        List<String> dressValues = Arrays.asList("dress", "skirt","culottes","skorts");
        List<String> pantsValues = Arrays.asList("pants", "trousers",
                "legging","leggings", "short", "jeans","shorts");
        List<String> shoesValues = Arrays.asList("shoes", "espadrilles","mules","pumps","slides","boot","loafers",
                "heel", "trainers", "slippers", "sandals","stilletos","toe", "runner", "slider", "sneakers","flats");
        List<String> coatsAndJacketsValues = Arrays.asList("vest", "blazer", "cardigan",
                "coat", "jacket", "waistcoat", "pullover", "parka", "poncho", "bomber", "suit",
                "duster", "kimono", "wrap");
        List<String> bagValues = Arrays.asList("bag", "tote",
                "clutch", "crossbody", "cross-body", "wallet", "backpack", "satchel", "handbag",
                "basket", "clutch-bag","pouch");
        List<String> lingerieValues = Arrays.asList("bra","thong","camisole","briefs","robe","chemise");
        List<String> accessoriesValues = Arrays.asList("gloves","turban","hair","beanie","sunglasses","sunglases","scarf","belt","hat","headband","case","cardholder","necklace","earrings","choker","ring","bracelet", "wallet","cap","visor","cuff","watch","earmuffs","beret","fedora","fascinator");
        List<String> swimwearValues = Arrays.asList("bikini","swimsuit","body");

        return insertIntoDict(topsValues,dressValues,pantsValues,shoesValues,coatsAndJacketsValues,bagValues,lingerieValues,accessoriesValues,swimwearValues);

    }


    private Map<String, ProductType> createHebrewDict() {
        List<String> topsValues = Arrays.asList("טי-שירט","שירט", "אוברול", "חולצת","חולצה","גופיית", "גופייה", "סווטשירט", "סוודר", "טופ", "גוף","top");
        List<String> dressValues = Arrays.asList("שמלת", "שמלה","חצאית");
        List<String> pantsValues = Arrays.asList("ג'ינס", "שורטס", "טייץ", "מכנסיים","מכנס","מכנס-חצאית","מכנסי","leggings");
        List<String> shoesValues = Arrays.asList("נעל", "espadrilles", "סנדלי", "נעלי",
                "קבקבי", "סנדל", "מגפ", "מגף");
        List<String> bagValues = Arrays.asList("תיק","bag","פאוץ'");
        List<String> coatsAndJacketsValues = Arrays.asList("ג'קט", "קרדיגן", "מעיל", "וסט", "ז'קט");
        List<String> swimwearValues = Arrays.asList("ים", "bikini","ביקיני");
        List<String> accessoriesValues = Arrays.asList("תכשיט","מגבת","שיער","צמיד", "משקפי שמש", "משקפיים","משקפים","מזוודת", "חגורה","חגורת", "כובע", "ארנק","גרבי", "מטפחת", "צעיף","עגילים", "עגיל", "נרתיק","עגילי","מחרוזת","שרשרת","שרשראות","קשת");
        List<String> lingerieValues = Arrays.asList("חזיה","תחתונים","תחתון","תחתוני","חזיית");
        return insertIntoDict(topsValues,dressValues,pantsValues,shoesValues,coatsAndJacketsValues,bagValues,lingerieValues,accessoriesValues,swimwearValues);
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
        for (String str: desc){

            ProductType key = dict.get(str);
            if(key == null) {
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
            if (key == productType.Lingerie){
                lingerieKey = true;
            }
            if (key == ProductType.Swimwear){
                swimwearKey = true;
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
        if (accessoriesKey && (dressesOrSkirts || topsKey || jacketsOrCoatsKey || shoesKey || pantsKey)){
            category = Category.Clothing;
            if (dressesOrSkirts) {
                productType = ProductType.DressesOrSkirts;
            }
            if(topsKey){
                productType = productType.Tops;
            }
            if (jacketsOrCoatsKey) {
                productType = ProductType.JacketsOrCoats;
            }
            if (shoesKey){
                productType = ProductType.Shoes;
            }
            if (pantsKey){
                productType = ProductType.Pants;
            }
        }
        if (lingerieKey){
            productType = ProductType.Lingerie;
        }

        if (swimwearKey && (topsKey || lingerieKey)){
            productType = ProductType.Swimwear;
        }
        if (swimwearKey && bagsKey){
            category = Category.Bags;
            productType = ProductType.Bags;
        }

        itemTags.category = category;
        itemTags.productType = productType;

        return itemTags;

    }
}
