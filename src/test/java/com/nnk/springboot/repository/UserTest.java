package com.nnk.springboot.repository;

import com.nnk.springboot.domain.Users;
import com.nnk.springboot.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Assertions;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith( SpringExtension.class)
@SpringBootTest
public class UserTest {

    private final Logger LOGGER = LoggerFactory.getLogger(UserTest.class);

    @Autowired
    private UserRepository userRepository;

    @Test
    public void userTest() {

        Users user = new Users();
        user.setUsername("userTest");
        user.setPassword("Password@123");
        user.setFullname("User Test");
        user.setRole("USER");

        LOGGER.info("Username: " + user.getUsername());
        LOGGER.info("Password: " + user.getPassword());
        LOGGER.info("Fullname: " + user.getFullname());
        LOGGER.info("Role: " + user.getRole());

        // Save
        user = userRepository.save(user);
        assertNotNull(user.getId());
        assertEquals("userTest", user.getUsername());

        // Update
        user.setFullname("User Test Updated");
        user = userRepository.save(user);
        assertEquals("User Test Updated", user.getFullname());

        // Find
        List<Users> usersList = userRepository.findAll();
        assertFalse(usersList.isEmpty());

        // Delete
        Integer id = user.getId();
        userRepository.delete(user);
        Optional<Users> deletedUser = userRepository.findById(id);
        assertFalse(deletedUser.isPresent());
    }

    @Test
    public void userTestWithInvalidUsername_shouldFail() {
        LOGGER.info("User Test - Invalid username");

        Users invalidUser = new Users();
        invalidUser.setUsername("");
        invalidUser.setPassword("ValidPassword123!");
        invalidUser.setFullname("Test User");
        invalidUser.setRole("USER");

        // exception de validation
        Assertions.assertThrows(ConstraintViolationException.class, () -> {
            userRepository.save(invalidUser);
            userRepository.flush(); // force la validation JPA
        });
    }

}
