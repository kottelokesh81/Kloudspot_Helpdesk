package com.kloudspot.helpdesk.service;

import com.kloudspot.helpdesk.domain.AppUser;
import com.kloudspot.helpdesk.dto.LoginRequest;
import com.kloudspot.helpdesk.dto.LoginResponse;
import com.kloudspot.helpdesk.dto.UserView;
import com.kloudspot.helpdesk.error.ApiException;
import com.kloudspot.helpdesk.repository.UserRepository;
import com.kloudspot.helpdesk.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository users, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.users = users; this.passwordEncoder = passwordEncoder; this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        AppUser user = users.findByEmailIgnoreCase(request.email().trim())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash()))
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        return new LoginResponse(jwtService.createToken(user), UserView.from(user));
    }
}

