package com.example.leavePortal.repo;

import com.example.leavePortal.model.LeaveRequestManagerResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LeaveRequestManagerResponseRepo extends JpaRepository<LeaveRequestManagerResponse,Integer> {
    List<LeaveRequestManagerResponse> findByLeaveRequestId(Integer leaveRequestId);

    @Query("SELECT r FROM LeaveRequestManagerResponse r WHERE r.leaveRequest.id = :leaveRequestId ORDER BY r.manager.role ASC")
    List<LeaveRequestManagerResponse> findByLeaveRequestIdOrderByManagerRole(@Param("leaveRequestId") Integer leaveRequestId);

    Optional<Object> findByLeaveRequestIdAndManagerId(Integer leaveRequestId, Integer managerId);

    Page<LeaveRequestManagerResponse> findByManagerIdOrderByLeaveRequestRequestDateDesc(Integer managerId, Pageable pageable);

    @Query("SELECT lr FROM LeaveRequestManagerResponse lr " +
            "WHERE lr.manager.id = :managerId " +
            "ORDER BY CASE WHEN lr.leaveRequest.id = :leaveRequestId THEN 0 ELSE 1 END, " +
            " lr.leaveRequest.requestDate DESC")
    Page<LeaveRequestManagerResponse> findByManagerIdWithPriority(
            @Param("managerId") Integer managerId,
            @Param("leaveRequestId") Integer leaveRequestId,
            Pageable pageable);

    @Query("SELECT lr FROM LeaveRequestManagerResponse lr " +
            "JOIN lr.leaveRequest l " +
            "WHERE lr.manager.id = :managerId AND LOWER(l.employee.name) LIKE LOWER(CONCAT('%', :employeeName, '%')) " +
            "ORDER BY lr.leaveRequest.requestDate DESC")
    Page<LeaveRequestManagerResponse> findByManagerIdAndEmployeeName(@Param("managerId") Integer managerId, @Param("employeeName") String employeeName, Pageable pageable);

    @Query("SELECT lr FROM LeaveRequestManagerResponse lr " +
            "WHERE lr.manager.id = :managerId AND lr.response = :status " +
            "ORDER BY lr.leaveRequest.requestDate DESC")
    Page<LeaveRequestManagerResponse> findByManagerIdAndStatus(
            @Param("managerId") Integer managerId,
            @Param("status") LeaveRequestManagerResponse.ManagerResponse status,
            Pageable pageable);

    @Query("SELECT lr FROM LeaveRequestManagerResponse lr " +
            "JOIN lr.leaveRequest l " +
            "WHERE lr.manager.id = :managerId " +
            "AND LOWER(l.employee.name) LIKE LOWER(CONCAT('%', :employeeName, '%')) " +
            "AND lr.response = :status " +
            "ORDER BY lr.leaveRequest.requestDate DESC")
    Page<LeaveRequestManagerResponse> findByManagerIdAndEmployeeNameAndStatus(
            @Param("managerId") Integer managerId,
            @Param("employeeName") String employeeName,
            @Param("status") LeaveRequestManagerResponse.ManagerResponse status,
            Pageable pageable);


}
