package com.lisu.onlinestore.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lisu.onlinestore.dao.RoleRepository;
import com.lisu.onlinestore.dao.UserRepository;
import com.lisu.onlinestore.dto.user.UserRegistrationRequestDto;
import com.lisu.onlinestore.dto.user.UserResponseDto;
import com.lisu.onlinestore.exception.EntityNotFoundException;
import com.lisu.onlinestore.exception.RegistrationException;
import com.lisu.onlinestore.mapper.UserMapper;
import com.lisu.onlinestore.model.Role;
import com.lisu.onlinestore.model.RoleName;
import com.lisu.onlinestore.model.User;
import com.lisu.onlinestore.service.ShoppingCartService;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private ShoppingCartService shoppingCartService;
    @Mock
    private UserMapper userMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void register_ShouldSaveUserWithEncodedPasswordAndCreateCart() {
        UserRegistrationRequestDto requestDto = createRequestDto();
        Role userRole = createRole(RoleName.USER);
        User mappedUser = new User();
        mappedUser.setEmail(requestDto.getEmail());
        mappedUser.setPassword("raw-password");
        User savedUser = new User();
        savedUser.setId(7L);
        savedUser.setEmail(requestDto.getEmail());
        savedUser.setPassword("encoded-password");
        savedUser.setRoles(Set.of(userRole));
        UserResponseDto expected = new UserResponseDto();
        expected.setId(7L);
        expected.setEmail(requestDto.getEmail());

        when(userRepository.existsByEmail(requestDto.getEmail())).thenReturn(false);
        when(roleRepository.findByName(RoleName.USER)).thenReturn(Optional.of(userRole));
        when(userMapper.toUser(requestDto)).thenReturn(mappedUser);
        when(passwordEncoder.encode(requestDto.getPassword())).thenReturn("encoded-password");
        when(userRepository.save(mappedUser)).thenReturn(savedUser);
        when(userMapper.toUserResponse(savedUser)).thenReturn(expected);

        UserResponseDto actual = userService.register(requestDto);

        assertSame(expected, actual);
        assertEquals("encoded-password", mappedUser.getPassword());
        assertEquals(Set.of(userRole), mappedUser.getRoles());
        verify(shoppingCartService).createCartForUser(savedUser);
    }

    @Test
    void register_ShouldThrowWhenEmailAlreadyExists() {
        UserRegistrationRequestDto requestDto = createRequestDto();
        when(userRepository.existsByEmail(requestDto.getEmail())).thenReturn(true);

        RegistrationException exception = assertThrows(
                RegistrationException.class,
                () -> userService.register(requestDto)
        );

        assertEquals("Can't register user with email: " + requestDto.getEmail(), exception.getMessage());
    }

    @Test
    void register_ShouldThrowWhenDefaultRoleMissing() {
        UserRegistrationRequestDto requestDto = createRequestDto();
        when(userRepository.existsByEmail(requestDto.getEmail())).thenReturn(false);
        when(roleRepository.findByName(RoleName.USER)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> userService.register(requestDto)
        );

        assertEquals("Can't find default role: USER", exception.getMessage());
    }

    private UserRegistrationRequestDto createRequestDto() {
        UserRegistrationRequestDto requestDto = new UserRegistrationRequestDto();
        requestDto.setEmail("user@test.com");
        requestDto.setPassword("password123");
        requestDto.setRepeatPassword("password123");
        requestDto.setFirstName("John");
        requestDto.setLastName("Doe");
        requestDto.setShippingAddress("Kyiv, Main street, 1");
        return requestDto;
    }

    private Role createRole(RoleName roleName) {
        Role role = new Role();
        role.setName(roleName);
        return role;
    }
}
