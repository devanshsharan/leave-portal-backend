package com.example.leavePortal.config;

import com.example.leavePortal.CustomException.ResourceNotFoundException;
import com.example.leavePortal.model.LeaveRequest;
import com.example.leavePortal.model.LeaveRequestManagerResponse;
import com.example.leavePortal.model.UserPrincipal;
import com.example.leavePortal.repo.LeaveRequestManagerResponseRepo;
import com.example.leavePortal.repo.LeaveRequestRepo;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@AllArgsConstructor
public class UserSecurity {

    private final LeaveRequestRepo leaveRequestRepository;

    private final LeaveRequestManagerResponseRepo leaveRequestManagerResponseRepository;

    public boolean checkUserId(Authentication authentication, int id) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserPrincipal userPrincipal) {
            return Objects.equals(userPrincipal.getEmployeeId(), id);
        }

        return false;
    }
    public boolean checkUserByLeaveId(Authentication authentication, int id) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request with ID " + id + " not found."));
        Integer employeeId = leaveRequest.getEmployee().getId();
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserPrincipal userPrincipal) {
            return Objects.equals(userPrincipal.getEmployeeId(), employeeId);
        }
        return false;
    }
    public boolean checkUserManagerResponseListByLeaveRequestId(Authentication authentication, int id) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request with ID " + id + " not found."));

        Integer employeeId = leaveRequest.getEmployee().getId();
        Object principal = authentication.getPrincipal();

        if (principal instanceof UserPrincipal userPrincipal) {
            if (Objects.equals(userPrincipal.getEmployeeId(), employeeId)) {
                return true;
            }

            LeaveRequestManagerResponse response = (LeaveRequestManagerResponse) leaveRequestManagerResponseRepository
                    .findByLeaveRequestIdAndManagerId(id, userPrincipal.getEmployeeId())
                    .orElse(null);

            return response != null;
        }

        return false;
    }

}

