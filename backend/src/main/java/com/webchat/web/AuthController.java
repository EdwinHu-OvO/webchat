package com.webchat.web;

import com.webchat.domain.User;
import com.webchat.service.UserService;
import com.webchat.web.dto.AuthDtos;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public User register(@Valid @RequestBody AuthDtos.RegisterRequest req) {
        return userService.register(req.getUsername(), req.getPassword());
    }

    @PostMapping("/login")
    public User login(@Valid @RequestBody AuthDtos.LoginRequest req) {
        return userService.login(req.getUsername(), req.getPassword());
    }
}
