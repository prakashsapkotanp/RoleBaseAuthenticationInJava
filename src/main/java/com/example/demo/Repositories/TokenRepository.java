package com.example.demo.Repositories;

import com.example.demo.models.TokenModel;
import com.example.demo.models.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TokenRepository extends JpaRepository<TokenModel, Long> {
    Optional<TokenModel> findByToken(String token);
    void deleteAllByUser(UserModel user);
}
