package com.example.leavePortal.Dto;
import lombok.Data;

@Data
public class ManagerResponseDto {
    private Integer leaveRequestId;
    private Integer managerId;
    private String response;
    private String comments;
}

