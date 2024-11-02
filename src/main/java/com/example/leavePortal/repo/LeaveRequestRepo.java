package com.example.leavePortal.repo;

import com.example.leavePortal.model.Employee;
import com.example.leavePortal.model.LeaveRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;


public interface LeaveRequestRepo extends JpaRepository<LeaveRequest, Integer> {

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.employee.id = :employeeId " +
            "AND lr.status <> com.example.leavePortal.model.LeaveRequest.LeaveStatus.CANCELLED " +
            "AND lr.status <> com.example.leavePortal.model.LeaveRequest.LeaveStatus.REJECTED " +
            "AND (:currentLeaveRequestId IS NULL OR lr.id <> :currentLeaveRequestId) " + // Handle null currentLeaveRequestId
            "AND ((lr.leaveStartDate <= :endDate AND lr.leaveEndDate >= :startDate))")
    List<LeaveRequest> findOverlappingLeaveRequests(@Param("employeeId") Integer employeeId,
                                                    @Param("startDate") LocalDate leaveStartDate,
                                                    @Param("endDate") LocalDate leaveEndDate,
                                                    @Param("currentLeaveRequestId") Integer currentLeaveRequestId);

    List<LeaveRequest> findByEmployeeAndStatusAndLeaveType(Employee employee, LeaveRequest.LeaveStatus leaveStatus, LeaveRequest.LeaveType leaveType);

    Page<LeaveRequest> findByEmployeeIdOrderByRequestDateDesc(Integer employeeId, Pageable pageable);

    @Query("SELECT lr FROM LeaveRequest lr " +
            "WHERE lr.employee.id = :employeeId " +
            "OR lr.id = :leaveRequestId " +
            "ORDER BY CASE WHEN lr.id = :leaveRequestId THEN 0 ELSE 1 END, lr.requestDate DESC")
    Page<LeaveRequest> findByEmployeeIdWithPriority(@Param("employeeId") Integer employeeId,
                                                    @Param("leaveRequestId") Integer leaveRequestId,
                                                    Pageable pageable);
}
