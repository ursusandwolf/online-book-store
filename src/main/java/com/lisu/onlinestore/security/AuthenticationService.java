package com.lisu.onlinestore.security;

import com.lisu.onlinestore.dto.user.UserLoginRequestDto;
import com.lisu.onlinestore.dto.user.UserLoginResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final AuthenticationManager authenticationManager;
    private final JwtUtil tokenUtil;

    public UserLoginResponseDto authenticate(@Valid UserLoginRequestDto request) {
        final Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        return tokenUtil.generateToken(authentication.getName());
    }
}
