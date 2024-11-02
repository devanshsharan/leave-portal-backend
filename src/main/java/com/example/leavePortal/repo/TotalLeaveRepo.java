package com.example.leavePortal.repo;

import com.example.leavePortal.model.TotalLeave;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TotalLeaveRepo extends JpaRepository<TotalLeave, Integer> {

    Optional<TotalLeave> findByEmployeeId(Integer employeeId);

}
