package com.example.leavePortal.Dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LeaveCancellationDto {
    private Integer leaveRequestId;
}

