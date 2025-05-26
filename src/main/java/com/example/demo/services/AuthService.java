package com.example.demo.services;

import com.example.demo.DTOs.AuthRequest;
import com.example.demo.DTOs.AuthResponse;
import com.example.demo.Repositories.RoleRepository;
import com.example.demo.Repositories.UserRepository;
import com.example.demo.models.RoleModel;
import com.example.demo.models.UserModel;
import com.example.demo.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {
   private final UserRepository userRepository;
   private final RoleRepository roleRepository;
   private final PasswordEncoder passwordEncoder;
   private final JwtUtil jwtUtil;

   public AuthResponse register(AuthRequest request){
       boolean isFirstUser = userRepository.count() == 0;

       String roleName = isFirstUser ? "ROLE_ADMIN" : "ROLE_USER";

       RoleModel role = roleRepository.findByRoleName(roleName)
               .orElseThrow(() -> new RuntimeException("Role Not Found: " + roleName));

       UserModel user = new UserModel();
       user.setUserName(request.getUsername());
       user.setPassword(passwordEncoder.encode(request.getPassword()));
       user.setRoles(Collections.singleton(role));

       userRepository.save(user);

       String roles = user.getRoles().stream()
               .map(RoleModel::getRoleName)
               .collect(Collectors.joining(","));

       String accessToken = jwtUtil.generateToken(user.getUserName(), roles, false);
       String refreshToken = jwtUtil.generateToken(user.getUserName(),roles,true);

       return new AuthResponse(accessToken, refreshToken);
   }

    public AuthResponse login(AuthRequest request) {
        UserModel user = userRepository.findByUserName(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        String roles = user.getRoles().stream()
                .map(RoleModel:: getRoleName)
                .collect(Collectors.joining(","));

        String accessToken = jwtUtil.generateToken(user.getUserName(), roles, false);
        String refreshToken = jwtUtil.generateToken(user.getUserName(), roles, true);

        return new AuthResponse(accessToken, refreshToken);
    }
    public AuthResponse refreshToken(String refreshToken) {
        String username = jwtUtil.extractUsername(refreshToken);

        UserModel user = userRepository.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!jwtUtil.isTokenValid(refreshToken, user.getUserName())) {
            throw new RuntimeException("Invalid refresh token");
        }

        String roles = user.getRoles().stream()
                .map(RoleModel::getRoleName)
                .collect(Collectors.joining(","));

        String newAccessToken = jwtUtil.generateToken(username, roles, false);

        return new AuthResponse(newAccessToken, refreshToken);
    }
}
