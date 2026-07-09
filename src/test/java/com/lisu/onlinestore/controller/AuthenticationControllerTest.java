package com.lisu.onlinestore.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lisu.onlinestore.dto.user.UserLoginResponseDto;
import com.lisu.onlinestore.dto.user.UserLoginRequestDto;
import com.lisu.onlinestore.dto.user.UserRegistrationRequestDto;
import com.lisu.onlinestore.dto.user.UserResponseDto;
import com.lisu.onlinestore.exception.CustomGlobalExceptionHandler;
import com.lisu.onlinestore.exception.RegistrationException;
import com.lisu.onlinestore.security.AuthenticationService;
import com.lisu.onlinestore.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@ExtendWith(MockitoExtension.class)
class AuthenticationControllerTest {
    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private UserService userService;

    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private AuthenticationController authenticationController;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(authenticationController)
                .setControllerAdvice(new CustomGlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void registerUser_ShouldReturnCreatedUser() throws Exception {
        UserResponseDto responseDto = new UserResponseDto();
        responseDto.setId(1L);
        responseDto.setEmail("user@test.com");
        responseDto.setFirstName("John");
        responseDto.setLastName("Doe");

        when(userService.register(any())).thenReturn(responseDto);

        mockMvc.perform(post("/auth/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(validRegistrationRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("user@test.com"))
                .andExpect(jsonPath("$.firstName").value("John"));
    }

    @Test
    void registerUser_ShouldReturnBadRequestForInvalidPayload() throws Exception {
        UserRegistrationRequestDto requestDto = new UserRegistrationRequestDto();
        requestDto.setEmail("invalid");
        requestDto.setPassword("short");
        requestDto.setRepeatPassword("short");
        requestDto.setFirstName("");
        requestDto.setLastName("");

        mockMvc.perform(post("/auth/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void registerUser_ShouldReturnBadRequestWhenPasswordsDoNotMatch() throws Exception {
        UserRegistrationRequestDto requestDto = validRegistrationRequest();
        requestDto.setRepeatPassword("password321");

        mockMvc.perform(post("/auth/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]").value("Passwords do not match"));
    }

    @Test
    void registerUser_ShouldReturnConflictWhenUserAlreadyExists() throws Exception {
        when(userService.register(any()))
                .thenThrow(new RegistrationException("Can't register user with email: user@test.com"));

        mockMvc.perform(post("/auth/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(validRegistrationRequest())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Can't register user with email: user@test.com"));
    }

    @Test
    void login_ShouldReturnToken() throws Exception {
        UserLoginRequestDto requestDto = new UserLoginRequestDto();
        requestDto.setEmail("user@test.com");
        requestDto.setPassword("password123");

        when(authenticationService.authenticate(any()))
                .thenReturn(new UserLoginResponseDto("jwt-token"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    @Test
    void login_ShouldReturnBadRequestForInvalidPayload() throws Exception {
        UserLoginRequestDto requestDto = new UserLoginRequestDto();
        requestDto.setEmail("invalid");
        requestDto.setPassword("short");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());
    }

    private UserRegistrationRequestDto validRegistrationRequest() {
        UserRegistrationRequestDto requestDto = new UserRegistrationRequestDto();
        requestDto.setEmail("user@test.com");
        requestDto.setPassword("password123");
        requestDto.setRepeatPassword("password123");
        requestDto.setFirstName("John");
        requestDto.setLastName("Doe");
        requestDto.setShippingAddress("Kyiv, Main street, 1");
        return requestDto;
    }
}
