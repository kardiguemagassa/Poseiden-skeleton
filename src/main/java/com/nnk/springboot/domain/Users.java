package com.nnk.springboot.domain;

import com.nnk.validator.ValidPassword;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;


/**
 * Entity representing a user of the application.
 * <p>
 *     This class is mapped to the {@code users} table in the database
 *     and is used for authentication and authorization operations via Spring Security.
 * </p>
 */
@Entity
@Table(name = "users")
public class Users {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Integer id;
    @Column(unique = true, nullable = false)
    @NotBlank(message = "Username is mandatory")
    private String username;
    @ValidPassword
    @NotBlank(message = "Password is mandatory")
    private String password;
    @NotBlank(message = "FullName is mandatory")
    private String fullname;
    @NotBlank(message = "Role is mandatory")
    private String role;

    /**
     * Unique user identifier.
     * @return the user's ID, or {@code null} if the user has not been persisted yet.
     */
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Username (login) used for authentication.
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Secure user password.
     * This field is validated and encoded before being stored.
     * @return password hash, or {@code null} if the user has not been persisted yet.
     */
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * User's full name (for display purposes).
     * @return full name, or {@code null} if the user has not been persisted yet.
     */
    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    /**
     * User role in the application (eg: ADMIN, USER).
     * Used to determine access rights via Spring Security.
     * @return the role of the user
     */
    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
