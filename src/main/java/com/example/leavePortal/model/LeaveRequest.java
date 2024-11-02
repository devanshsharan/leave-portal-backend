package com.example.leavePortal.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Component
@Entity
public class LeaveRequest {


    public LeaveRequest(Employee employee, LocalDate leaveStartDate, LocalDate leaveEndDate, LeaveType leaveType, String leaveReason, Integer leaveDays ) {
        this.employee = employee;
        this.leaveStartDate = leaveStartDate;
        this.leaveEndDate = leaveEndDate;
        this.leaveType = leaveType;
        this.leaveReason = leaveReason;
        this.leaveDays = leaveDays;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;

    private LocalDate leaveStartDate;
    private LocalDate leaveEndDate;

    @Enumerated(EnumType.STRING)
    private LeaveStatus status = LeaveStatus.PENDING;

    private LocalDateTime requestDate = LocalDateTime.now();

    public enum LeaveStatus {
        PENDING,
        APPROVED,
        REJECTED,
        CANCELLED
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeaveType leaveType;

    public enum LeaveType {
        CASUAL,
        HOSPITALIZATION
    }

    public enum AdminResponse {
        PENDING,
        APPROVED,
        REJECTED
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AdminResponse adminResponse = AdminResponse.PENDING;

    @Column(nullable = false, length = 500)
    private String leaveReason;

    private Integer leaveDays;

    @OneToMany(mappedBy = "leaveRequest")
    private Set<Notification> notifications;
}


