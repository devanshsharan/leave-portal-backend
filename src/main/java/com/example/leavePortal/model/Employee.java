package com.example.leavePortal.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Component
@Entity
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String name;

    @NotBlank
    @Email(message = "Email should be valid")
    @Column(nullable = false, unique = true)
    private String email;

    @NotNull
    @Pattern(regexp = "^[1-9]\\d{9}$", message = "Phone number must be 10 digits and cannot start with zero")
    @Column(nullable = false, unique = true)
    private String number;

    @OneToMany(mappedBy = "employee")
    private Set<ProjectEmployeeRole> projectEmployeeRoles;

    @OneToMany(mappedBy = "employee")
    private Set<Notification> notifications;

    @Column(nullable = false, unique = true)
    private String username;

    @JsonIgnore
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role=Role.EMPLOYEE;;

    public enum Role {
        ADMIN,
        EMPLOYEE,
        MANAGER
    }
}

