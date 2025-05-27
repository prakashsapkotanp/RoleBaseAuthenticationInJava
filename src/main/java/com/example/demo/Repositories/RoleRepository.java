package com.example.demo.Repositories;

import com.example.demo.models.RoleModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


public interface RoleRepository extends JpaRepository< RoleModel, Long > {
    Optional<RoleModel> findByRoleName(String RoleName);

    Optional<RoleModel> findByRoleId(Long roleId);

    @Query("SELECT r FROM RoleModel r JOIN r.users u WHERE u.userId = :userId")
    Optional<RoleModel> findRoleIdsByUserId(@Param("userId") Long userId);
}
