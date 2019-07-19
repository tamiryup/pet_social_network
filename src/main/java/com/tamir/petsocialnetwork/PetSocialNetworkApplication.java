package com.tamir.petsocialnetwork;

import com.tamir.petsocialnetwork.AWS.s3.S3Service;
import com.tamir.petsocialnetwork.dto.UploadItemDTO;
import com.tamir.petsocialnetwork.services.ScrapingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;

@SpringBootApplication
public class PetSocialNetworkApplication implements CommandLineRunner {

	private final Logger logger = LoggerFactory.getLogger(PetSocialNetworkApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(PetSocialNetworkApplication.class, args);
	}

	@Value("${ps.cognito.issuer}")
	private String issuer;

	@Autowired
    private S3Service s3Service;

	@Autowired
	ScrapingService scrapingService;

	@Override
	public void run(String... args) throws Exception {
		logger.info("start execution");
//		List<String> links = scrapingService.getThumbnailImages("www.asos.com",
//				"https://www.asos.com/aratta/aratta-maxi-kimono-in-linen-with-premium-floral-embroidery/prd/12354837?clr=faded-peach&colourWayId=16392061&SearchQuery=&cid=15198");
//		System.out.println(links);
		UploadItemDTO item  = scrapingService.extractItem("asos", "productpage");
	}
}
