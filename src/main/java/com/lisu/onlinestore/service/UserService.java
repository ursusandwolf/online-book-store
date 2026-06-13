package com.lisu.onlinestore.service;

import com.lisu.onlinestore.dto.user.UserRegistrationRequestDto;
import com.lisu.onlinestore.dto.user.UserResponseDto;
import com.lisu.onlinestore.exception.RegistrationException;

public interface UserService {
    UserResponseDto register(UserRegistrationRequestDto requestDto) throws RegistrationException;
}
