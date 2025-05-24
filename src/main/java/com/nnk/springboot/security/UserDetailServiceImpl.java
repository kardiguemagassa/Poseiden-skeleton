package com.nnk.springboot.security;

import com.nnk.springboot.domain.Users;
import com.nnk.springboot.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * Custom implementation of {@link UserDetailsService} used by Spring Security.
 *
 * <p>
 *     This class allows you to load a user from the database based on their username.
 *     and provide the information necessary for authentication and authorization.
 * </p>
 */
@Service
public class UserDetailServiceImpl implements UserDetailsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserDetailServiceImpl.class);

    private final UserRepository userRepository;

    /**
     * Constructor injecting the user repository.
     *
     * @param userRepository the repository to access the entities {@link Users}
     */
    public UserDetailServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Loads a user by their username.
     * <p>This method is automatically called by Spring Security during the login process.</p>
     *
     * @param username the username
     * @return the user's details, including password and roles
     * @throws UsernameNotFoundException if no matching user is found
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        LOGGER.info("Trying to load user: {}", username);
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    LOGGER.error("User not found: {}", username);
                    return new UsernameNotFoundException("User not found");
                });

        LOGGER.info("User found: {}, Role: {}", user.getUsername(), user.getRole());
        LOGGER.info("Stored password hash: " + user.getPassword());
        LOGGER.info("User role: " + user.getRole());

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole()))
        );
    }
}
