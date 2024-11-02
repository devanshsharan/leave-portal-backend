package com.example.leavePortal.service;

import com.example.leavePortal.model.Employee;
import com.example.leavePortal.model.LeaveRequest;
import com.example.leavePortal.model.LeaveRequestManagerResponse;
import jakarta.servlet.ServletOutputStream;
import java.util.List;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class NotificationService {

    private final JavaMailSender mailSender;

    @Async
    public void sendNotification(List<Employee> recipients, LeaveRequest leaveRequest, boolean reschedule) {
        for (Employee recipient : recipients) {
            String managerEmail = recipient.getEmail();
            String subject = reschedule ? "Action Required: Rescheduled Leave Request Notification" : "Action Required: New Leave Request Notification";
            String message = String.format(
                    "Dear %s,%n%n" +
                            "We would like to inform you that a new leave request has been submitted by %s.%n%n" +
                            "Details of the Leave Request:%n" +
                            "Employee Name: %s%n" +
                            "Employee Email: %s%n" +
                            "Leave Start Date: %s%n" +
                            "Leave End Date: %s%n" +
                            "Leave Type: %s%n%n" +
                            "Please review the request and take the necessary action at your earliest convenience.%n%n" +
                            "Thank you for your attention to this matter.%n%n" +
                            "Best regards,%n" +
                            "Leave Portal Team",
                    recipient.getName(),
                    leaveRequest.getEmployee().getName(),
                    leaveRequest.getEmployee().getName(),
                    leaveRequest.getEmployee().getEmail(),
                    leaveRequest.getLeaveStartDate(),
                    leaveRequest.getLeaveEndDate(),
                    leaveRequest.getLeaveType()
            );
            SimpleMailMessage messageSending = new SimpleMailMessage();
            messageSending.setFrom("sharan2732@gmail.com");
            messageSending.setTo(recipient.getEmail());
            messageSending.setSubject(subject);
            messageSending.setText(message);
            mailSender.send(messageSending);
        }
    }
}

