package com.tamir.petsocialnetwork.repositories;

import com.tamir.petsocialnetwork.entities.User;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;


public interface UserRepository extends CrudRepository<User, Long> {

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    User findByEmail(String email);

    User findByUsername(String username);

    @Transactional
    @Modifying
    @Query("UPDATE User user SET user.description = :description WHERE user.id = :id")
    void updateDescriptionById(@Param("id") long id, @Param("description") String description);

    @Transactional
    @Modifying
    @Query("UPDATE User user SET user.profileImageAddr = :addr WHERE user.id = :id")
    void updateProfileImageAddrById(@Param("id") long id, @Param("addr") String addr);
}
