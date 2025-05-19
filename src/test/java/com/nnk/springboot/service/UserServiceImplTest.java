package com.nnk.springboot.service;

import com.nnk.springboot.domain.Users;
import com.nnk.springboot.exceptions.*;
import com.nnk.springboot.repositories.UserRepository;
import com.nnk.springboot.service.serviceImpl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private Users user;

    @BeforeEach
    void setUp() {
        user = new Users();
        user.setId(1);
        user.setUsername("testuser");
        user.setPassword("password");
        user.setRole("USER");
    }

    @Test
    void testFindAll_success() {
        when(userRepository.findAll()).thenReturn(List.of(user));

        List<Users> users = userService.findAll();

        assertThat(users).containsExactly(user);
    }

    @Test
    void testFindAll_exceptionThrown() {
        when(userRepository.findAll()).thenThrow(new RuntimeException("DB error"));

        assertThatThrownBy(() -> userService.findAll())
                .isInstanceOf(CustomDataAccessException.class)
                .hasMessageContaining("Error retrieving users");
    }

    @Test
    void testFindById_found() {
        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        Users result = userService.findById(1);

        assertThat(result).isEqualTo(user);
    }

    @Test
    void testFindById_notFound() {
        when(userRepository.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(1))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User");
    }

    @Test
    void testSave_newUser_success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password")).thenReturn("hashedpassword");
        when(userRepository.save(any(Users.class))).thenReturn(user);

        Users savedUser = userService.save(user);

        assertThat(savedUser).isEqualTo(user);
        verify(passwordEncoder).encode("password");
        verify(userRepository).save(any(Users.class));
    }

    @Test
    void testSave_existingUser_conflict() {
        Users existingUser = new Users();
        existingUser.setId(2); // différent ID
        existingUser.setUsername("testuser");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(existingUser));

        assertThatThrownBy(() -> userService.save(user))
                .isInstanceOf(AlreadyExistsException.class)
                .hasMessageContaining("User");
    }

    @Test
    void testSave_dataPersistException() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password")).thenReturn("hashedpassword");
        when(userRepository.save(any(Users.class))).thenThrow(new RuntimeException("DB error"));

        assertThatThrownBy(() -> userService.save(user))
                .isInstanceOf(DataPersistException.class)
                .hasMessageContaining("Error saving user");
    }

    @Test
    void testDeleteById_success() {
        when(userRepository.existsById(1)).thenReturn(true);

        userService.deleteById(1);

        verify(userRepository).deleteById(1);
    }

    @Test
    void testDeleteById_notFound() {
        when(userRepository.existsById(1)).thenReturn(false);

        assertThatThrownBy(() -> userService.deleteById(1))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User");
    }

    @Test
    void testDeleteById_dataDeleteException() {
        when(userRepository.existsById(1)).thenReturn(true);
        doThrow(new RuntimeException("DB fail")).when(userRepository).deleteById(1);

        assertThatThrownBy(() -> userService.deleteById(1))
                .isInstanceOf(DataDeleteException.class)
                .hasMessageContaining("Error deleting user");
    }
}
