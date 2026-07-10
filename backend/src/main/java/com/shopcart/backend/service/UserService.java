package com.shopcart.backend.service;

import com.shopcart.backend.dto.UserRegistrationDto;
import com.shopcart.backend.entity.User;

public interface UserService {

    User registerUser(UserRegistrationDto registrationDto);
}
