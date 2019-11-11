package com.tamir.followear.repositories;

import com.tamir.followear.entities.User;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.List;


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
    @Query("Update User user Set user.fullName = :fullName WHERE user.id = :id")
    void updateFullNameById(@Param("id") long id, @Param("fullName") String fullName);

    @Transactional
    @Modifying
    @Query("UPDATE User user SET user.profileImageAddr = :addr WHERE user.id = :id")
    void updateProfileImageAddrById(@Param("id") long id, @Param("addr") String addr);

    @Transactional
    @Query(value =
            "SELECT * FROM users u WHERE u.username ILIKE CONCAT(:q, '%')" +
            " or u.full_name ILIKE CONCAT(:q, '%') or u.full_name ILIKE CONCAT('% ', :q, '%') LIMIT 10",
    nativeQuery = true)
    List<User> searchByQuery(@Param("q") String query);

}
