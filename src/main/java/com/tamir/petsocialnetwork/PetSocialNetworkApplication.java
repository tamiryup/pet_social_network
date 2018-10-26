package com.tamir.petsocialnetwork;

import com.tamir.petsocialnetwork.AWS.s3.S3Service;
import com.tamir.petsocialnetwork.entities.User;
import com.tamir.petsocialnetwork.repositories.FollowRepository;
import com.tamir.petsocialnetwork.repositories.UserRepository;
import com.tamir.petsocialnetwork.stream.StreamService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SpringBootApplication
public class PetSocialNetworkApplication implements CommandLineRunner {

	private final Logger logger = LoggerFactory.getLogger(PetSocialNetworkApplication.class);

	@Autowired
	private FollowRepository followRepo;

	@Autowired
    S3Service s3Service;

	@Autowired
    StreamService streamService;

	@Autowired
    UserRepository userRepo;


	public static void main(String[] args) {
		SpringApplication.run(PetSocialNetworkApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		logger.info("start execution");


		/*Follow follow = new Follow(1,2);
		followRepo.save(follow);

		Optional<Follow> follow1 = followRepo.findById(new FollowKey(1,2));

		if(follow1.isPresent()){
		    logger.info(""+follow1.get().getMasterId());
        }*/
	}
}
