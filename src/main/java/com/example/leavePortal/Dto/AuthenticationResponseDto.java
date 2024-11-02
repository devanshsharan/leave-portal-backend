package com.example.leavePortal.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationResponseDto {
    private String jwt;
    private String refreshToken;
    private String message;
    private String role;
    private Integer employeeId;

}
