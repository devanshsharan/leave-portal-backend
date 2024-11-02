package com.example.leavePortal.service;

import com.example.leavePortal.Dto.ProjectRoleDto;
import com.example.leavePortal.model.*;
import com.example.leavePortal.repo.EmployeeRepo;
import com.example.leavePortal.repo.ProjectEmployeeRoleRepo;
import com.example.leavePortal.repo.ProjectRepo;
import com.example.leavePortal.repo.TotalLeaveRepo;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class EmployeeProjectServiceImplementation implements EmployeeProjectService {
    private final EmployeeRepo employeeRepository;
    private final ProjectRepo projectRepository;
    private final ProjectEmployeeRoleRepo projectEmployeeRoleRepository;
    private final TotalLeaveRepo totalLeaveRepo;
    private final BCryptPasswordEncoder encoder;
    private static final Logger logger = LoggerFactory.getLogger(LeaveRequestServiceImplementation.class);

    @Override
    public Employee saveEmployee(Employee employee) {

        employee.setPassword(encoder.encode(employee.getPassword()));
        // Encode the employee's password before saving
        Employee savedEmployee = employeeRepository.save(employee);
        TotalLeave totalLeave = new TotalLeave();
        totalLeave.setEmployee(savedEmployee);
        totalLeaveRepo.save(totalLeave);

        logger.info("Employee saved successfully: {}", savedEmployee.getId());
        return savedEmployee;
    }

    @Override
    public Project saveProject(Project project) {
        return projectRepository.save(project);
    }

    @Override
    public ProjectEmployeeRole saveProjectEmployeeRole(ProjectEmployeeRole projectEmployeeRole) {

        Employee employee = employeeRepository.findById(projectEmployeeRole.getEmployee().getId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        Project project = projectRepository.findById(projectEmployeeRole.getProject().getId())
                .orElseThrow(() -> new RuntimeException("Project not found"));

        Optional<ProjectEmployeeRole> existingRoleOpt = Optional.ofNullable(projectEmployeeRoleRepository.findByEmployeeAndProject(employee, project));
        if(projectEmployeeRole.getRole()==ProjectEmployeeRole.Role.MANAGER){
            employee.setRole(Employee.Role.MANAGER);
            employeeRepository.save(employee);
            logger.info("Employee role updated to MANAGER for employee ID");
        }
        if (existingRoleOpt.isPresent()) {

            ProjectEmployeeRole existingRole = existingRoleOpt.get();
            existingRole.setRole(projectEmployeeRole.getRole());
            return projectEmployeeRoleRepository.save(existingRole);
        } else {

            projectEmployeeRole.setEmployee(employee);
            projectEmployeeRole.setProject(project);
            return projectEmployeeRoleRepository.save(projectEmployeeRole);
        }
    }

    @Override
    public Employee getEmployeeById(Integer id) {
        return employeeRepository.findById(id).get();

    }

    @Override
    public List<ProjectRoleDto> getProjectRolesByEmployeeId(Integer id) {
        List<ProjectEmployeeRole> roles = projectEmployeeRoleRepository.findByEmployeeId(id);

        List<ProjectRoleDto> projectRoleDtos = roles.stream()
                .map(role -> new ProjectRoleDto(
                        role.getProject().getId(),
                        role.getProject().getName(),
                        role.getRole().name()))
                .collect(Collectors.toList());

        return projectRoleDtos;
    }
}
