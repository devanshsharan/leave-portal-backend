package com.example.leavePortal.service;

import com.example.leavePortal.Dto.ProjectRoleDto;
import com.example.leavePortal.model.Employee;
import com.example.leavePortal.model.Project;
import com.example.leavePortal.model.ProjectEmployeeRole;

import java.util.List;

public interface EmployeeProjectService {
    Employee saveEmployee(Employee employee);
    Project saveProject(Project project);
    ProjectEmployeeRole saveProjectEmployeeRole(ProjectEmployeeRole projectEmployeeRole);
    Employee getEmployeeById(Integer id);
    List<ProjectRoleDto> getProjectRolesByEmployeeId(Integer id);
}
