package com.example.leavePortal.service;

import com.example.leavePortal.Dto.LeaveRequestDto;
import com.example.leavePortal.Dto.ManagerResponseDto;
import com.example.leavePortal.Dto.RescheduleDto;
import com.example.leavePortal.Dto.ResponseDto;
import com.example.leavePortal.model.LeaveRequest;
import com.example.leavePortal.model.LeaveRequestManagerResponse;
import com.example.leavePortal.model.ProjectEmployeeRole;
import com.example.leavePortal.model.Notification;
import com.example.leavePortal.model.TotalLeave;
import org.springframework.data.domain.Page;

import java.util.List;

public interface LeaveRequestService {
    LeaveRequest applyLeave(LeaveRequestDto leaveRequestDTO);
    void cancelLeave(Integer leaveRequestId);
    void rescheduleLeave(RescheduleDto rescheduleDto);
    ResponseDto processManagerResponse(ManagerResponseDto managerResponseDto);
    TotalLeave getTotalLeaveById(Integer employeeId);
    Page<LeaveRequest> getLeaveRequestsByEmployeeId(Integer employeeId, Integer offset, Integer pageSize);
    Page<LeaveRequest> getLeaveRequestsByEmployeeIdWithPriority(Integer employeeId, Integer leaveRequestId, Integer offset, Integer pageSize);
    List<LeaveRequestManagerResponse> getManagerResponseListByLeaveRequestId(Integer leaveRequestId);
    Page<LeaveRequestManagerResponse> getFilteredResponsesByManagerId(Integer managerId, Integer leaveRequestId, String employeeName, String status, Integer offset, Integer pageSize);
    List<ProjectEmployeeRole> getProjectsByManagerAndEmployee(Integer managerId, Integer employeeId);
    List<Notification> getUnclearedNotificationsByEmployeeId(Integer employeeId);
    void updateNotificationsByEmployeeId(Integer employeeId, String response);
}