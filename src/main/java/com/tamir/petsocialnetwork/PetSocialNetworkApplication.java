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
//		UploadItemDTO output = scrapingService.extractItem("shopbop",
//                "https://www.shopbop.com/drama-jean-mother/vp/v=1/1518623454.htm?folderID=13377&fm=other-shopbysize-viewall&os=false&colorId=15F90");
//		System.out.println(output);
	}
}
