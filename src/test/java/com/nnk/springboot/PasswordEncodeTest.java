package com.nnk.springboot;

import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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

    /*@Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    public void printEncodedPassword() {
        System.out.println("Encoded 'admin123': " + passwordEncoder.encode("admin123"));
        System.out.println("Encoded 'user123': " + passwordEncoder.encode("user123"));
    }

    @Test
    public void verifyAdminPassword() {
        String rawPassword = "admin123"; // Le mot de passe que vous essayez
        String storedHash = "$2a$10$4MH6YUArhKqL6H1YpjEI7uHo2JhWW00ZET0I.qMAiPqdJwheIH3bG";

        boolean matches = passwordEncoder.matches(rawPassword, storedHash);
        assertTrue(matches);
        System.out.println("Password matches: " + matches);
    }*/


    @Test
    public void testPassword() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String pw = encoder.encode("123456");
        System.out.println("[ "+ pw + " ]");
    }
}
