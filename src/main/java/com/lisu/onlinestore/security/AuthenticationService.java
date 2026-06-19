package com.lisu.onlinestore.security;

import com.lisu.onlinestore.dao.UserRepository;
import com.lisu.onlinestore.dto.user.UserLoginRequestDto;
import com.lisu.onlinestore.dto.user.UserLoginResponseDto;
import com.lisu.onlinestore.model.User;
import jakarta.validation.Valid;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;

    public UserLoginResponseDto authenticate(@Valid UserLoginRequestDto request) {
        Optional<User> user = userRepository.findByEmail(request.getEmail());
        return user.isPresent() && user.get().getPassword().equals(request.getPassword());
    }
}
