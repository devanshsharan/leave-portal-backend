package com.example.leavePortal.Controller;

import com.example.leavePortal.Dto.*;
import com.example.leavePortal.model.*;
import com.example.leavePortal.service.LeaveRequestService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@Slf4j
@AllArgsConstructor
public class LeaveRestController {

    private final LeaveRequestService leaveService;

    @PreAuthorize("@userSecurity.checkUserId(authentication, #leaveRequestDTO.employeeId)")
    @PostMapping("/apply")
    public ResponseEntity<LeaveRequest> applyLeave(@Valid @RequestBody LeaveRequestDto leaveRequestDTO) {
        LeaveRequest leaveRequest = leaveService.applyLeave(leaveRequestDTO);
        return ResponseEntity.ok(leaveRequest);
    }

    @PreAuthorize("@userSecurity.checkUserByLeaveId(authentication, #leaveCancellationDTO.leaveRequestId)")
    @PostMapping("/cancel")
    public ResponseEntity<String> cancelLeave(@Valid @RequestBody LeaveCancellationDto leaveCancellationDTO) {
        leaveService.cancelLeave(leaveCancellationDTO.getLeaveRequestId());
        return ResponseEntity.ok("Leave request canceled successfully.");
    }

    @PreAuthorize("@userSecurity.checkUserByLeaveId(authentication, #rescheduleDto.leaveRequestId)")
    @PostMapping("/reschedule")
    public ResponseEntity<String> rescheduleLeave(@Valid @RequestBody RescheduleDto rescheduleDto) {
        leaveService.rescheduleLeave(rescheduleDto);
        return ResponseEntity.ok("Leave request rescheduled successfully.");
    }

    @PreAuthorize("@userSecurity.checkUserId(authentication, #managerResponseDto.managerId)")
    @PostMapping("/respond")
    public ResponseEntity<ResponseDto> respondToLeaveRequest(
            @Valid @RequestBody ManagerResponseDto managerResponseDto) {
        ResponseDto responseDto = leaveService.processManagerResponse(managerResponseDto);
        return ResponseEntity.ok(responseDto);
    }

    @PreAuthorize("@userSecurity.checkUserId(authentication, #employeeId)")
    @GetMapping("/totalLeave/{employeeId}")
    public ResponseEntity<TotalLeave> getTotalLeaveById(@Valid @PathVariable Integer employeeId) {
        TotalLeave totalLeave = leaveService.getTotalLeaveById(employeeId);
        return ResponseEntity.ok(totalLeave);
    }

    @PreAuthorize("@userSecurity.checkUserId(authentication, #employeeId)")
    @GetMapping("/leaveRequestList/{employeeId}/{offset}/{pageSize}")
    public ResponseEntity<Page<LeaveRequest>> getLeaveRequestsByEmployeeId(@Valid @PathVariable Integer employeeId,
            @PathVariable Integer offset,
            @PathVariable Integer pageSize,
            @RequestParam(required = false) Integer leaveRequestId) {

        Page<LeaveRequest> leaveRequests;

        if (leaveRequestId == null) {
            leaveRequests = leaveService.getLeaveRequestsByEmployeeId(employeeId, offset, pageSize);
        } else {
            leaveRequests = leaveService.getLeaveRequestsByEmployeeIdWithPriority(employeeId, leaveRequestId, offset,
                    pageSize);
        }

        return new ResponseEntity<>(leaveRequests, HttpStatus.OK);
    }

    @PreAuthorize("@userSecurity.checkUserManagerResponseListByLeaveRequestId(authentication, #leaveRequestId)")
    @GetMapping("/managerResponseList/{leaveRequestId}")
    public ResponseEntity<List<LeaveRequestManagerResponse>> getManagerResponseListByLeaveRequestId(
            @Valid @PathVariable Integer leaveRequestId) {
        List<LeaveRequestManagerResponse> responseRequests = leaveService
                .getManagerResponseListByLeaveRequestId(leaveRequestId);
        return ResponseEntity.ok(responseRequests);
    }

    @PreAuthorize("@userSecurity.checkUserId(authentication, #managerId)")
    @GetMapping("/manager/{managerId}/{offset}/{pageSize}")
    public ResponseEntity<Page<LeaveRequestManagerResponse>> getResponsesByManagerId(
            @Valid @PathVariable Integer managerId, @PathVariable Integer offset, @PathVariable Integer pageSize,
            @RequestParam(required = false) Integer leaveRequestId, @RequestParam(required = false) String employeeName,
            @RequestParam(required = false) String status) {
        Page<LeaveRequestManagerResponse> responses = leaveService.getFilteredResponsesByManagerId(
                managerId, leaveRequestId, employeeName, status, offset, pageSize);
        return new ResponseEntity<Page<LeaveRequestManagerResponse>>(responses, HttpStatus.OK);
    }

    @PreAuthorize("@userSecurity.checkUserId(authentication, #managerId)")
    @GetMapping("/managerEmployeeProjects/{managerId}/{employeeId}")
    public ResponseEntity<List<ProjectEmployeeRole>> getResponsesByManagerId(@Valid @PathVariable Integer managerId,
            @PathVariable Integer employeeId) {
        List<ProjectEmployeeRole> responses = leaveService.getProjectsByManagerAndEmployee(managerId, employeeId);
        return new ResponseEntity<List<ProjectEmployeeRole>>(responses, HttpStatus.OK);
    }

    @PreAuthorize("@userSecurity.checkUserId(authentication, #employeeId)")
    @GetMapping("/uncleared/{employeeId}")
    public List<Notification> getUnclearedNotificationsByEmployeeId(@Valid @PathVariable Integer employeeId) {
        return leaveService.getUnclearedNotificationsByEmployeeId(employeeId);
    }

    @PreAuthorize("@userSecurity.checkUserId(authentication, #notificationUpdateDto.employeeId)")
    @PostMapping("/updateStatus")
    public ResponseEntity<String> updateNotificationStatus(
            @Valid @RequestBody NotificationUpdateDto notificationUpdateDto) {
        log.info("check1");
        leaveService.updateNotificationsByEmployeeId(notificationUpdateDto.getEmployeeId(),
                notificationUpdateDto.getResponse());
        return ResponseEntity.ok("Notifications updated successfully");
    }

}
