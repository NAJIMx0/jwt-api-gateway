package com.najim.controller;

import com.najim.entities.AuthRequest;
import com.najim.service.AuthService;
import com.najim.service.JwtUtil;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/auth")
@AllArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping(value = "/register")
    public ResponseEntity<AuthService> register (@RequestBody AuthRequest authRequest){
        return ResponseEntity.ok(authService.register(authRequest));
    }
}
