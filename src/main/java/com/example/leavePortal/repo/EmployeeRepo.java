package com.example.leavePortal.repo;

import com.example.leavePortal.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepo extends JpaRepository<Employee,Integer> {
    List<Employee> findByRole(Employee.Role role);

    Optional<Employee> findByUsername(String username);


}
