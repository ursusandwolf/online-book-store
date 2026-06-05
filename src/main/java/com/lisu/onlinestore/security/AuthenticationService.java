package com.lisu.onlinestore.security;

import com.lisu.onlinestore.dao.UserRepository;
import com.lisu.onlinestore.dto.user.UserLoginRequestDto;
import com.lisu.onlinestore.model.User;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository repository;

    public boolean authenticate(UserLoginRequestDto requestDto) {
        Optional<User> user = repository.findByEmail(requestDto.getEmail());
        return (user.isPresent()
                && user.get().getPassword()
                .equals(requestDto.getPassword()));
    }
}
