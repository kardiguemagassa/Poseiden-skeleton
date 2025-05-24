package com.nnk.springboot.repositories;

import com.nnk.springboot.domain.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Repository for user management.
 * <p>
 *     Provides data access operations for the {@link Users} entity,
 *     used in particular in the context of authentication with Spring Security.
 * </p>
 */
@Repository
public interface UserRepository extends JpaRepository<Users, Integer>, JpaSpecificationExecutor<Users> {

    /**
     * Search for a user by their username.
     * <p>
     *     This method is used by the {@link org.springframework.security.core.userdetails.UserDetailsService}
     *     to load a user's information at authentication time.
     * </p>
     *
     * @param username the username to search for
     * @return a {@link Optional} containing the user if found, otherwise empty
     */
    Optional<Users> findByUsername(String username);
}
