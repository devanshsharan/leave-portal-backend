package com.example.leavePortal.repo;

import com.example.leavePortal.model.LeaveRequest;
import com.example.leavePortal.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepo extends JpaRepository<Notification, Integer> {

    List<Notification> findByLeaveRequest(LeaveRequest leaveRequest);

    @Query("SELECT n FROM Notification n WHERE n.status <> :notificationStatus AND n.employee.id = :employeeId ORDER BY n.id DESC")
    List<Notification> findAllByEmployeeIdAndStatusNotCleared(@Param("employeeId") Integer employeeId, @Param("notificationStatus") Notification.NotificationStatus notificationStatus);

    List<Notification> findByEmployeeIdAndStatusNot(Integer employeeId, Notification.NotificationStatus notificationStatus);
}
