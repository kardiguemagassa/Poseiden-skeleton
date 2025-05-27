package com.nnk.springboot.repository;

import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created by MAGASSA Kardigué.
 * Email:
 * Date: 14/05/2025
 * Time:
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
public class PasswordEncodeTest {

    private final Logger LOGGER = LoggerFactory.getLogger(PasswordEncodeTest.class);

    @Autowired
    private PasswordEncoder passwordEncoder;


    @Test
    public void testPassword() {
        String pw = passwordEncoder.encode("Test@123456");
        LOGGER.info("[ "+ pw + " ]");
    }

    @Test
    public void verifyPassword() {
        String clearPassword = "Test@123456";
        String encryptedPassword = "$2a$10$7g7kgq7awLm4qYdgSj8tfOSW36yH0q9npZOK6M1CqBIyT.ftxhEj.";

        boolean matches = passwordEncoder.matches(clearPassword, encryptedPassword);
        assertTrue(matches);
        LOGGER.info("Password matches: " + true);
    }
}
