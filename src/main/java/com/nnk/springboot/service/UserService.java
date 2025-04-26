package com.nnk.springboot.service;

import com.nnk.springboot.domain.Users;
import java.util.List;

public interface UserService {

    List<Users> findAll();
    Users save(Users user);
    Users findById(Integer id);
    void deleteById(Integer id);
}
