package com.example.leavePortal.repo;

import com.example.leavePortal.model.Employee;
import com.example.leavePortal.model.Project;
import com.example.leavePortal.model.ProjectEmployeeRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProjectEmployeeRoleRepo extends JpaRepository<ProjectEmployeeRole,Integer> {
    boolean existsByEmployeeAndProject(Employee employee, Project project);

    List<ProjectEmployeeRole> findByEmployeeId(Integer id);

    ProjectEmployeeRole findByEmployeeAndProject(Employee employee, Project project);

    List<ProjectEmployeeRole> findByProjectIdAndRole(Integer id, ProjectEmployeeRole.Role role);

    @Query("SELECT per.employee FROM ProjectEmployeeRole per " +
            "WHERE per.project IN (SELECT per2.project FROM ProjectEmployeeRole per2 WHERE per2.employee.id = :employeeId) " +
            "AND per.role = 'MANAGER'" +
            "AND per.employee.id <> :employeeId")
    List<Employee> findAllManagersByEmployeeId(@Param("employeeId") Integer employeeId);

    @Query("SELECT CASE WHEN COUNT(per.project) > 0 THEN true ELSE false END " +
            "FROM ProjectEmployeeRole per " +
            "WHERE per.employee.id = :employeeId " +
            "AND per.role = 'MANAGER' " +
            "AND NOT EXISTS (SELECT per2 FROM ProjectEmployeeRole per2 WHERE per2.project = per.project AND per2.role = 'MANAGER' AND per2.employee.id <> :employeeId)")
    boolean existsProjectWhereEmployeeIsOnlyManager(@Param("employeeId") Integer employeeId);

    @Query("SELECT per FROM ProjectEmployeeRole per WHERE per.employee.id = :employeeId AND per.project.id IN "
            + "(SELECT per2.project.id FROM ProjectEmployeeRole per2 WHERE per2.employee.id = :managerId AND per2.role = 'MANAGER')")
    List<ProjectEmployeeRole> findByManagerAndEmployee(Integer managerId, Integer employeeId);
}
