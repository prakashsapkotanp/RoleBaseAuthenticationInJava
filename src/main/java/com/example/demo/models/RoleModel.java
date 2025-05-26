package com.example.demo.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleModel {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private  int roleId;
    private String roleName;
    @ManyToMany(mappedBy = "roles")
    private Set<UserModel> users = new HashSet<>();
}
