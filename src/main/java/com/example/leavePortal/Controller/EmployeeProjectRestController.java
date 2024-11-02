package com.example.leavePortal.Controller;

import com.example.leavePortal.model.Employee;
import com.example.leavePortal.model.Project;
import com.example.leavePortal.model.ProjectEmployeeRole;
import com.example.leavePortal.Dto.ProjectRoleDto;
import com.example.leavePortal.service.EmployeeProjectService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@AllArgsConstructor
public class EmployeeProjectRestController {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeProjectRestController.class);
    private final EmployeeProjectService dataService;

    @PostMapping("/createEmployees")
    public ResponseEntity<Employee> createEmployee(@RequestBody Employee employee) {

        Employee savedEmployee = dataService.saveEmployee(employee);
        logger.info("Employee created successfully: {}", savedEmployee);
        return ResponseEntity.ok(savedEmployee);
    }

    @PostMapping("/createProjects")
    public ResponseEntity<Project> createProject(@Valid @RequestBody Project project) {
        Project savedProject = dataService.saveProject(project);
        return ResponseEntity.ok(savedProject);
    }

    @PostMapping("/createProjectEmployeeRoles")
    public ResponseEntity<ProjectEmployeeRole> createProjectEmployeeRole(
            @RequestBody ProjectEmployeeRole projectEmployeeRole) {
        ProjectEmployeeRole savedRole = dataService.saveProjectEmployeeRole(projectEmployeeRole);
        return ResponseEntity.ok(savedRole);
    }

    @PreAuthorize("@userSecurity.checkUserId(authentication, #id)")
    @GetMapping("/employees/{id}")
    public ResponseEntity<Employee> getEmployeeById(@PathVariable Integer id) {
        Employee employee = dataService.getEmployeeById(id);
        return ResponseEntity.ok(employee);
    }

    @GetMapping("/listProject/{id}")
    public ResponseEntity<List<ProjectRoleDto>> getProjectsByEmployeeId(@PathVariable Integer id) {
        List<ProjectRoleDto> projectRoles = dataService.getProjectRolesByEmployeeId(id);
        return ResponseEntity.ok(projectRoles);
    }
}
