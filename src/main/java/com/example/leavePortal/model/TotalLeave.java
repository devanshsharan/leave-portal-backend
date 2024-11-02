package com.example.leavePortal.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class TotalLeave {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    @JoinColumn(name = "employee_id", referencedColumnName = "id")
    private Employee employee;

    @Column(nullable = false)
    private Integer casualLeaveTaken = 0;

    @Column(nullable = false)
    private Integer totalCasualLeave = 10;

    @Column(nullable = false)
    private Integer casualLeavePending = 0;

    @Column(nullable = false)
    private Integer hospitalizationLeavePending = 0;

    @Column(nullable = false)
    private Integer hospitalizationLeaveTaken = 0;

    @Column(nullable = false)
    private Integer totalHospitalizationLeave = 10;

}
