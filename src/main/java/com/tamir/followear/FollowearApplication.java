package com.tamir.followear;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.*;
import com.tamir.followear.AWS.MyAWSCredentials;
import com.tamir.followear.AWS.cognito.CognitoService;
import com.tamir.followear.AWS.lambda.LambdaService;
import com.tamir.followear.dto.ScrapingEventDTO;
import com.tamir.followear.dto.UploadItemDTO;
import com.tamir.followear.enums.Category;
import com.tamir.followear.enums.Currency;
import com.tamir.followear.enums.ProductType;
import com.tamir.followear.exceptions.BadLinkException;
import com.tamir.followear.services.ItemClassificationService;
import com.tamir.followear.services.PostService;
import com.tamir.followear.services.ScrapingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


@SpringBootApplication
public class FollowearApplication implements CommandLineRunner {

	private final Logger logger = LoggerFactory.getLogger(FollowearApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(FollowearApplication.class, args);
	}

	@Autowired
    PostService postService;

	@Autowired
    ScrapingService scrapingService;

	@Autowired
    ItemClassificationService classificationService;

	@Override
	public void run(String... args) throws Exception {
	    logger.info("start execution");
	}

	private void fillUpDatabase() throws Exception {
        UploadItemDTO coat = new UploadItemDTO(
                "https://images.asos-media.com/products/vila-longline-faux-fur-jacket/12801107-1-tan?$XXL$&wid=513&fit=constrain",
                "https://www.asos.com/vila/vila-longline-faux-fur-jacket/prd/12801107?clr=tan&colourWayId=16465324&SearchQuery=&cid=2641",
                "Vila longline faux fur jacket", "393.61", "", Currency.ILS, 1l, null, "jpg", "12801107",
                Arrays.asList(
                        "https://images.asos-media.com/products/vila-longline-faux-fur-jacket/12801107-2?$S$&wid=40&fit=constrain",
                        "https://images.asos-media.com/products/vila-longline-faux-fur-jacket/12801107-3?$S$&wid=40&fit=constrain"),
                Category.Clothing, ProductType.JacketsOrCoats);

        UploadItemDTO top = new UploadItemDTO(
                "https://images.asos-media.com/products/morgan-sleeveless-wrap-front-slim-rib-knitted-top-with-button-shoulder-detail-in-white/12126285-1-white?$XXL$&wid=513&fit=constrain",
                "https://www.asos.com/morgan/morgan-sleeveless-wrap-front-slim-rib-knitted-top-with-button-shoulder-detail-in-white/prd/12126285?clr=white&colourWayId=16421965&SearchQuery=&cid=4169",
                "Morgan sleeveless wrap front slim rib knitted top with button shoulder detail in white",
                "178.44", "", Currency.ILS, 1l, null, "jpg", "12126285",
                Arrays.asList(
                        "https://images.asos-media.com/products/morgan-sleeveless-wrap-front-slim-rib-knitted-top-with-button-shoulder-detail-in-white/12126285-2?$S$&wid=40&fit=constrain",
                        "https://images.asos-media.com/products/morgan-sleeveless-wrap-front-slim-rib-knitted-top-with-button-shoulder-detail-in-white/12126285-3?$S$&wid=40&fit=constrain"
                ),
                Category.Clothing, ProductType.Tops);

        UploadItemDTO bag = new UploadItemDTO(
                "https://images.asos-media.com/products/kate-spade-molly-black-leater-tote-bag/12062117-1-blackleather?$XXL$&wid=513&fit=constrain",
                "https://www.asos.com/kate-spade/kate-spade-molly-black-leater-tote-bag/prd/12062117?clr=black-leather&colourWayId=16467990&SearchQuery=&cid=8730",
                "Kate Spade Molly black leater tote bag", "1,023.38", "", Currency.ILS, 1l, null, "jpg", "12062117",
                Arrays.asList(
                        "https://images.asos-media.com/products/kate-spade-molly-black-leater-tote-bag/12062117-2?$S$&wid=40&fit=constrain",
                        "https://images.asos-media.com/products/kate-spade-molly-black-leater-tote-bag/12062117-3?$S$&wid=40&fit=constrain"
                ),
                Category.Bags, ProductType.Default);

        UploadItemDTO shoes = new UploadItemDTO(
                "https://images.asos-media.com/products/vans-classic-slip-on-black-trainers/11446318-1-black?$XXL$&wid=513&fit=constrain",
                "https://www.asos.com/vans/vans-classic-slip-on-black-trainers/prd/11446318?clr=black&colourWayId=16327920&SearchQuery=&cid=4172",
                "Vans Classic Slip-On black trainers", "165.32", "", Currency.ILS, 1l, null, "jpg", "11446318",
                Arrays.asList(
                        "https://images.asos-media.com/products/vans-classic-slip-on-black-trainers/11446318-2?$S$&wid=40&fit=constrain",
                        "https://images.asos-media.com/products/vans-classic-slip-on-black-trainers/11446318-3?$S$&wid=40&fit=constrain"
                ),
                Category.Shoes, ProductType.Default);

        long userId = 2;

        for(int i=0; i<25; i++) {
            postService.uploadItemPost(userId, coat);
            postService.uploadItemPost(userId, top);
            postService.uploadItemPost(userId, bag);
            postService.uploadItemPost(userId, shoes);
        }
    }
}
