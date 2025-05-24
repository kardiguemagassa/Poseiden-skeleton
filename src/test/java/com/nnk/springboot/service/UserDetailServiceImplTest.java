package com.nnk.springboot.service;


import com.nnk.springboot.domain.Users;
import com.nnk.springboot.repositories.UserRepository;
import com.nnk.springboot.security.UserDetailServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserDetailServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailServiceImpl userDetailService;

    private Users user;

    @BeforeEach
    void setUp() {
        user = new Users();
        user.setUsername("testuser");
        user.setPassword("hashedpassword");
        user.setRole("USER");
    }

    @Test
    void testLoadUserByUsername_userFound() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        // Act
        UserDetails result = userDetailService.loadUserByUsername("testuser");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getPassword()).isEqualTo("hashedpassword");
        assertThat(result.getAuthorities()).extracting("authority").containsExactly("ROLE_USER");

        verify(userRepository, times(1)).findByUsername("testuser");
    }

    @Test
    void testLoadUserByUsername_userNotFound() {
        // Arrange
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> userDetailService.loadUserByUsername("unknown"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found");

        verify(userRepository, times(1)).findByUsername("unknown");
    }
}
