package com.achyutha.bankingapp.domain.service;

import com.achyutha.bankingapp.auth.model.Role;
import com.achyutha.bankingapp.auth.model.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Role Repository.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * To find a Role based on the role type.
     * @param name The role type.
     * @return Role, if present.
     */
    Optional<Role> findByName(RoleType name);
}
