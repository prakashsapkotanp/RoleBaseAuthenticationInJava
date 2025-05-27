package com.example.demo.services;

import com.example.demo.DTOs.AuthRequest;
import com.example.demo.DTOs.AuthResponse;
import com.example.demo.Repositories.RoleRepository;
import com.example.demo.Repositories.TokenRepository;
import com.example.demo.Repositories.UserRepository;
import com.example.demo.models.RoleModel;
import com.example.demo.models.TokenModel;
import com.example.demo.models.UserModel;
import com.example.demo.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;

import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final TokenRepository tokenRepository;

    public AuthResponse register(AuthRequest request) {
        boolean isFirstUser = userRepository.count() == 0;
        String roleName = isFirstUser ? "ROLE_ADMIN" : "ROLE_USER";
        Optional<UserModel> isUsernameAlreadyExist = userRepository.findByUserName(request.getUsername());
        if(isUsernameAlreadyExist.isEmpty()){
            throw new RuntimeException("Username Already Exists");
        }
        RoleModel role = roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));

        UserModel user = new UserModel();
        user.setUserName(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRoles(Collections.singleton(role));

        userRepository.save(user);

        return generateTokens(user, role);
    }

    public AuthResponse login(AuthRequest request) {
        UserModel user = userRepository.findByUserName(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        // Pick first role (or handle accordingly if multiple roles)
        RoleModel role = user.getRoles().stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("User has no roles assigned"));

        return generateTokens(user, role);
    }

    public AuthResponse refreshToken(String refreshToken) {
        if (jwtUtil.isTokenExpired(refreshToken)) {
            throw new RuntimeException("Refresh token expired");
        }

        String username = jwtUtil.extractUsername(refreshToken);
        UserModel user = userRepository.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        TokenModel savedToken = tokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Token not found"));

        if (savedToken.isUsed()) {
            throw new RuntimeException("Token is already used");
        }

        if (!jwtUtil.isTokenValid(refreshToken, username)) {
            throw new RuntimeException("Invalid refresh token");
        }

        String roles = user.getRoles().stream()
                .map(RoleModel::getRoleName)
                .collect(Collectors.joining(","));

        String newAccessToken = jwtUtil.generateAccessToken(username, roles);

        RoleModel role = user.getRoles().stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("User has no roles assigned"));

        return new AuthResponse(newAccessToken, Math.toIntExact(user.getUserId()), role.getRoleId());
    }

    private AuthResponse generateTokens(UserModel user, RoleModel role) {
        String roles = user.getRoles().stream()
                .map(RoleModel::getRoleName)
                .collect(Collectors.joining(","));

        String accessToken = jwtUtil.generateAccessToken(user.getUserName(), roles);
        String refreshToken = jwtUtil.generateRefreshToken(user.getUserName(), roles);

        TokenModel tokenEntity = TokenModel.builder()
                .token(refreshToken)
                .user(user)
                .expiryDate(new Date(System.currentTimeMillis() + jwtUtil.getRefreshExpiration()))
                .isUsed(false)
                .build();

        tokenRepository.save(tokenEntity);

        return new AuthResponse(accessToken, Math.toIntExact(user.getUserId()), role.getRoleId());
    }
}
