package com.example.leavePortal.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectRoleDto {
    private Integer projectId;
    private String projectName;
    private String role;
}

