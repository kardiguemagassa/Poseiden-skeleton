package com.nnk.springboot.service;

import com.nnk.springboot.domain.Users;
import com.nnk.springboot.exceptions.*;

import java.util.List;

public interface UserService {

    List<Users> findAll() throws CustomDataAccessException;
    Users save(Users user) throws AlreadyExistsException, DataPersistException, InvalidException;
    Users findById(Integer id);
    void deleteById(Integer id) throws DataDeleteException;
}
