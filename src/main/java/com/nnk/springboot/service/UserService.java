package com.nnk.springboot.service;

import com.nnk.springboot.domain.Users;
import com.nnk.springboot.exceptions.*;

import java.util.List;

/**
 * Service interface for user management.
 *
 * <p>
 *     Defines the main CRUD operations on {@link Users} entities,
 *     used in particular in authentication and administration processes
 *     accounts and application security.
 * </p>
 */
public interface UserService {

    /**
     * Retrieves the list of all users.
     *
     * @return list of users
     * @throws CustomDataAccessException on database access error
     *
     */
    List<Users> findAll() throws CustomDataAccessException;

    /**
     * Saves a user after validation.
     * <p>
     *     This method is used to create and update a user.
     *      It also encrypts the password and checks for duplicate usernames.
     * </p>
     *
     * @param user the user to register
     * @return the registered user
     * @throws AlreadyExistsException if a user with the same name already exists
     * @throws DataPersistException on persistence failure
     * @throws InvalidException if the user does not respect the validation constraints
     */
    Users save(Users user) throws AlreadyExistsException, DataPersistException, InvalidException;

    /**
     * * Search for a user by their ID.
     * @param id the user's ID
     * @return the found user
     * @throws NotFoundException if no user matches the ID
     */
    Users findById(Integer id);

    /**
     * Deletes a user based on their ID.
     *
     * @param id the ID of the user to delete
     * @throws DataDeleteException if deletion fails
     */
    void deleteById(Integer id) throws DataDeleteException;
}
