package com.achyutha.bankingapp.domain.service;

import com.achyutha.bankingapp.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * User Repository.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * To find a user based on the username.
     * @param username The username.
     * @return The user object, if present.
     */
    Optional<User> findByUsername(String username);

    /**
     * To check if a user by a username exists already.
     * @param username The username.
     * @return Boolean yes/no indicating the presence/absence.
     */
    Boolean existsByUsername(String username);

}
