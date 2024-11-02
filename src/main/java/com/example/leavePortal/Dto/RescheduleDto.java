package com.example.leavePortal.Dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RescheduleDto {
    @NotNull(message = "leaveRequestId not be blanked")
    private Integer leaveRequestId;
    @NotNull(message = "Leave start date cannot be null")
    private LocalDate leaveStartDate;
    @NotNull(message = "Leave end date cannot be null")
    private LocalDate leaveEndDate;
    @NotNull(message = "Leave type cannot be null")
    private String leaveType;
    @NotBlank(message = "Leave reason cannot be blank")
    private String leaveReason;
}
