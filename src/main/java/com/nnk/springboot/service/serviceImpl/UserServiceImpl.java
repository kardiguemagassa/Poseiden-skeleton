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

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final   PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }


    public List<Users> findAll() {
        try {
            return userRepository.findAll();
        } catch (Exception e) {
            throw new CustomDataAccessException("Error retrieving users", e);
        }
    }

    @Override
    public Users findById(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    @Override
    public Users save(Users user) throws DataPersistException {

        userRepository.findByUsername(user.getUsername()).ifPresent(existingUser -> {

            if (user.getId() == null || !existingUser.getId().equals(user.getId())) {
                throw new UserAlreadyExistsException(user.getUsername());
            }
        });

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        try {
            return userRepository.save(user);
        } catch (Exception e) {
            throw new DataPersistException("Error saving user", e);
        }
    }

    @Override
    public void deleteById(Integer id) throws DataDeleteException {
        try {
            if (!userRepository.existsById(id)) {
                throw new UserNotFoundException(id);
            }
            userRepository.deleteById(id);
        } catch (UserNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new DataDeleteException("Error deleting user", e);
        }
    }


}
