package com.tamir.petsocialnetwork;

import com.tamir.petsocialnetwork.AWS.s3.S3Service;
import com.tamir.petsocialnetwork.dto.UploadItemDTO;
import com.tamir.petsocialnetwork.entities.Store;
import com.tamir.petsocialnetwork.repositories.StoreRepository;
import com.tamir.petsocialnetwork.services.ScrapingService;
import org.checkerframework.checker.units.qual.A;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Date;
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
	ScrapingService scrapingService;

	@Autowired
    StoreRepository storeRepo;

	@Override
	public void run(String... args) throws Exception {
		logger.info("start execution");
	}
}
