package com.nnk.springboot.service.serviceImpl;

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

@Service
public class UserDetailServiceImpl implements UserDetailsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserDetailServiceImpl.class);

    private final UserRepository userRepository;

    public UserDetailServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

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
