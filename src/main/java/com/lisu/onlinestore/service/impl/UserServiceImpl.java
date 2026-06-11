package com.lisu.onlinestore.service.impl;

import com.lisu.onlinestore.dao.UserRepository;
import com.lisu.onlinestore.dto.user.UserRegistrationRequestDto;
import com.lisu.onlinestore.dto.user.UserResponseDto;
import com.lisu.onlinestore.exception.RegistrationException;
import com.lisu.onlinestore.mapper.UserMapper;
import com.lisu.onlinestore.model.User;
import com.lisu.onlinestore.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserResponseDto register(UserRegistrationRequestDto requestDto)
            throws RegistrationException {
        if (userRepository.existsByEmail(requestDto.getEmail())) {
            throw new RegistrationException("Can't register user with email: " 
                    + requestDto.getEmail());
        }
        User user = userMapper.toUser(requestDto);
        user.setFirstName("Test");
        user.setLastName("User");
        User saved = userRepository.save(user);
        return userMapper.toUserResponse(saved);
    }
}
