package com.example.leavePortal.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class LeaveRequestManagerResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "leave_request_id")
    private LeaveRequest leaveRequest;

    private String comments = "No Comments";

    @ManyToOne
    @JoinColumn(name = "manager_id")
    private Employee manager;

    @Enumerated(EnumType.STRING)
    private ManagerResponse response = ManagerResponse.PENDING;


    public LeaveRequestManagerResponse(LeaveRequest leaveRequest, Employee manager) {
        this.leaveRequest=leaveRequest;
        this.manager=manager;
    }

    public enum ManagerResponse {
        PENDING,
        APPROVED,
        REJECTED,
        CANCELLED
    }


}

