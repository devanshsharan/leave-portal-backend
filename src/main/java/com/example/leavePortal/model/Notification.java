package com.example.leavePortal.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "leave_request_id", nullable = false)
    private LeaveRequest leaveRequest;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationStatus status = NotificationStatus.UNSEEN;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private ResponseStatus responseStatus;

    public Notification(LeaveRequest leaveRequest, NotificationType type, Employee employee, ResponseStatus responseStatus) {
        this.leaveRequest = leaveRequest;
        this.type = type;
        this.employee = employee;
        this.responseStatus = responseStatus;
    }

    public enum NotificationType {
        REQUEST,
        RESPONSE
    }

    public enum NotificationStatus {
        UNSEEN,
        SEEN,
        CLEARED
    }
    public enum ResponseStatus {
        APPROVED,
        REJECTED,
        PENDING
    }
}

