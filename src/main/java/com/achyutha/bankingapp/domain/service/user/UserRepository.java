package com.achyutha.bankingapp.domain.service.user;

import com.achyutha.bankingapp.auth.model.Role;
import com.achyutha.bankingapp.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
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
     * @param email The email.
     * @return Boolean yes/no indicating the presence/absence.
     */
    Boolean existsByEmail(String email);

    /**
     * Fetch latest user.
     *  -- Orders entries by Round num and fetches the first entry.
     * @return Optional Round.
     */
    Optional<User> findFirstByOrderByIdDesc();

    /**
     * Fetch all users having specific roles.
     * @param role Set of roles.
     * @return List of users.
     */
    List<User> findByRoles_(Role role);

}
