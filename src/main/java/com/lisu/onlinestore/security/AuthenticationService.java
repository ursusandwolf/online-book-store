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
    private final JwtUtil tokenUtil;

    public UserLoginResponseDto authenticate(@Valid UserLoginRequestDto request) {
        Optional<User> user = userRepository.findByEmail(request.getEmail());
        if (user.isEmpty()) {
            throw new RuntimeException("Can't login");
        }
        String userPasswordFromDb = user.get().getPassword();
        String hashedPassword = HashUtil.hashPassword(request.getPassword(), user.get().getSalt());
        if (!hashedPassword.equals(userPasswordFromDb)) {
            throw new RuntimeException("Can't login");
        }
        return tokenUtil.generateToken(request.getEmail());
    }
}
