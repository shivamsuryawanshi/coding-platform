package com.codingplatform.service;

import com.codingplatform.dto.AuthResponse;
import com.codingplatform.dto.LoginRequest;
import com.codingplatform.dto.SignupRequest;
import com.codingplatform.entity.User;
import com.codingplatform.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    
    public AuthService(UserRepository userRepository, 
                      PasswordEncoder passwordEncoder,
                      JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }
    
    @Transactional
    public AuthResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        
        String passwordHash = passwordEncoder.encode(request.getPassword());
        User user = new User(request.getEmail(), passwordHash);
        user = userRepository.save(user);
        
        String token = jwtService.generateToken(user.getId(), user.getEmail());
        logger.info("User signed up: {}", user.getEmail());
        
        return new AuthResponse(token, user.getEmail(), user.getId());
    }
    
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }
        
        String token = jwtService.generateToken(user.getId(), user.getEmail());
        logger.info("User logged in: {}", user.getEmail());
        
        return new AuthResponse(token, user.getEmail(), user.getId());
    }
}

