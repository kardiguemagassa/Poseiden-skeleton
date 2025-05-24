package com.nnk.springboot.service.serviceImpl;

import com.nnk.springboot.domain.Users;
import com.nnk.springboot.exceptions.*;
import com.nnk.springboot.repositories.UserRepository;
import com.nnk.springboot.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * Business service for user management.
 * <p>
 *     This class provides CRUD operations for {@link Users} entities,
 *     including existence validation, password hashing,
 *     and custom exceptions to ensure data integrity.
 * </p>
 */
@Service
@Transactional
public class UserServiceImpl implements UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final   PasswordEncoder passwordEncoder;

    /**
     * Constructor injecting the user repository and password encoder.
     *
     * @param userRepository the user repository
     * @param passwordEncoder the password encoder used to secure passwords in the database
     */
    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Retrieves all registered users.
     *
     * @return the list of users
     * @throws CustomDataAccessException on data access error
     */
    @Transactional(readOnly = true)
    public List<Users> findAll() {
        try {
            return userRepository.findAll();
        } catch (Exception e) {
            throw new CustomDataAccessException("Error retrieving users", e);
        }
    }

    /**
     * Retrieves a user by their ID.
     *
     * @param id the user's ID
     * @return the corresponding user
     * @throws NotFoundException if the user is not found
     */
    @Override
    @Transactional(readOnly = true)
    public Users findById(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User" ,id));
    }

    /**
     * Creates or updates a user, checking for duplicates and encrypting the password.
     *
     * @param user the user to register
     * @return the saved user
     * @throws AlreadyExistsException if a user with the same name already exists
     * @throws DataPersistException on save failure
     */
    @Override
    @Transactional
    public Users save(Users user) throws DataPersistException {

        userRepository.findByUsername(user.getUsername()).ifPresent(existingUser -> {

            if (user.getId() == null || !existingUser.getId().equals(user.getId())) {
                throw new AlreadyExistsException("User");
            }
        });

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        try {
            return userRepository.save(user);
        } catch (Exception e) {
            throw new DataPersistException("Error saving user", e);
        }
    }

    /**
     * Deletes a user by ID.
     *
     *@param id the ID of the user to delete
     * @throws NotFoundException if the user does not exist
     * @throws DataDeleteException on delete failure
     */
    @Override
    @Transactional
    public void deleteById(Integer id) throws DataDeleteException {
        try {
            if (!userRepository.existsById(id)) {
                throw new NotFoundException("User" ,id);
            }
            userRepository.deleteById(id);
        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new DataDeleteException("Error deleting user", e);
        }
    }
}
