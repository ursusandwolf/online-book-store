package com.lisu.onlinestore.service.impl;

import com.lisu.onlinestore.dao.RoleRepository;
import com.lisu.onlinestore.dao.UserRepository;
import com.lisu.onlinestore.dto.user.UserRegistrationRequestDto;
import com.lisu.onlinestore.dto.user.UserResponseDto;
import com.lisu.onlinestore.exception.RegistrationException;
import com.lisu.onlinestore.mapper.UserMapper;
import com.lisu.onlinestore.model.Role;
import com.lisu.onlinestore.model.RoleName;
import com.lisu.onlinestore.model.User;
import com.lisu.onlinestore.service.UserService;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Transactional
@AllArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponseDto register(UserRegistrationRequestDto requestDto)
            throws RegistrationException {
        if (userRepository.existsByEmail(requestDto.getEmail())) {
            throw new RegistrationException("Can't register user with email: " 
                    + requestDto.getEmail());
        }
        Role userRole = roleRepository.findByName(RoleName.USER)
                .orElseThrow(() -> new RegistrationException("Can't find default role"));

        User user = userMapper.toUser(requestDto);
        user.setPassword(passwordEncoder.encode(requestDto.getPassword()));
        user.setRoles(Set.of(userRole));

        User saved = userRepository.save(user);
        return userMapper.toUserResponse(saved);
    }
}
