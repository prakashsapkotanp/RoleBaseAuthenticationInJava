package com.example.demo.Repositories;

import com.example.demo.models.RoleModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


public interface RoleRepository extends JpaRepository< RoleModel, Long > {
    Optional<RoleModel> findByRoleName (String RoleName);
    Optional<RoleModel> findByRoleId (Long roleId);
}
