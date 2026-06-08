package com.lisu.onlinestore.controller;

import com.lisu.onlinestore.dto.user.UserLoginRequestDto;
import com.lisu.onlinestore.dto.user.UserRegistrationRequestDto;
import com.lisu.onlinestore.dto.user.UserResponseDto;
import com.lisu.onlinestore.exception.RegistrationException;
import com.lisu.onlinestore.security.AuthenticationService;
import com.lisu.onlinestore.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {
    private final UserService userService;
    private final AuthenticationService authenticationService;

    @PostMapping("/registration")
    public UserResponseDto registerUser(@RequestBody @Valid UserRegistrationRequestDto requestDto)
            throws RegistrationException {
        return userService.register(requestDto);
    }

    @PostMapping("/login")
    public boolean login(@RequestBody @Valid UserLoginRequestDto requestDto)
            throws RegistrationException {
        return authenticationService.authenticate(requestDto);
    }
}
