package com.example.leavePortal.service;

import com.example.leavePortal.CustomException.EmptyListException;
import com.example.leavePortal.CustomException.ResourceNotFoundException;
import com.example.leavePortal.CustomException.UserException;
import com.example.leavePortal.Dto.*;
import com.example.leavePortal.LogicalFunctions.Logics;
import com.example.leavePortal.model.*;
import com.example.leavePortal.repo.*;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@AllArgsConstructor
public class LeaveRequestServiceImplementation implements LeaveRequestService {

    private static final Logger logger = LoggerFactory.getLogger(LeaveRequestServiceImplementation.class);

    private final EmployeeRepo employeeRepository;
    private final LeaveRequestRepo leaveRequestRepository;
    private final ProjectEmployeeRoleRepo projectEmployeeRoleRepository;
    private final LeaveRequestManagerResponseRepo leaveRequestManagerResponseRepository;
    private final NotificationRepo notificationRepository;
    private final TotalLeaveRepo totalLeaveRepository;
    private final NotificationService notificationService;
    private final Logics logics;

    @Override
    public LeaveRequest applyLeave(LeaveRequestDto leaveRequestDTO) {
        Integer employeeId = leaveRequestDTO.getEmployeeId();
        LocalDate leaveStartDate=leaveRequestDTO.getLeaveStartDate();
        LocalDate leaveEndDate=leaveRequestDTO.getLeaveEndDate();
        String leaveType=leaveRequestDTO.getLeaveType();
        String leaveReason=leaveRequestDTO.getLeaveReason();

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        logger.info("Employee found: {}", employee.getName());

        TotalLeave totalLeave = totalLeaveRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> {
                    logger.error("Total leave details not found for employee ID: {}", employeeId);
                    return new ResourceNotFoundException("Total leave details not found");
                });
        logger.info("Total leave details fetched for employee ID: {}", employeeId);

        // Validate leave request logic
        logics.validateLeaveAdding(null, employeeId, leaveStartDate, leaveEndDate,leaveType, totalLeave);

        LeaveRequest.LeaveType type = LeaveRequest.LeaveType.valueOf(leaveType);
        int leaveDays = logics.getWorkDaysBetweenTwoDates(leaveStartDate, leaveEndDate);

        // Update total leave based on leave type
        if (type == LeaveRequest.LeaveType.CASUAL) {
            totalLeave.setCasualLeavePending(totalLeave.getCasualLeavePending() + leaveDays);
        } else if (type == LeaveRequest.LeaveType.HOSPITALIZATION) {
            totalLeave.setHospitalizationLeavePending(totalLeave.getHospitalizationLeavePending() + leaveDays);
        }
        totalLeaveRepository.save(totalLeave);

        LeaveRequest leaveRequest = new LeaveRequest(employee, leaveStartDate, leaveEndDate, type, leaveReason, leaveDays);
        leaveRequestRepository.save(leaveRequest);
        logger.info("Leave request saved for employee ID: {}", employeeId);

        List<LeaveRequestManagerResponse> responses = new ArrayList<>();
        List<Employee> AllManagers=projectEmployeeRoleRepository.findAllManagersByEmployeeId(employeeId);
        List<Employee> admins = employeeRepository.findByRole(Employee.Role.ADMIN);
        List<Employee> mergedList = new ArrayList<>(AllManagers);

        for (Employee manager : AllManagers) {
            responses.add(new LeaveRequestManagerResponse(leaveRequest,manager));
        }
        for (Employee manager : admins) {
            responses.add(new LeaveRequestManagerResponse(leaveRequest,manager));
        }
        leaveRequestManagerResponseRepository.saveAll(responses);

        // Check if the employee is the only manager in any project
        boolean isOnlyManagerInAnyProject = projectEmployeeRoleRepository.existsProjectWhereEmployeeIsOnlyManager(employeeId);
        if(isOnlyManagerInAnyProject){
            mergedList.addAll(admins);
        }

        notificationService.sendNotification(mergedList,leaveRequest,false);
        List<Notification> noti = new ArrayList<>();
        for (Employee manager : mergedList) {
            noti.add(new Notification(leaveRequest, Notification.NotificationType.REQUEST, manager,null));
        }
        notificationRepository.saveAll(noti);
        logger.info("Notifications sent for leave request ID: {} for employee ID: {}", leaveRequest.getId(), employeeId);

        return leaveRequest;
    }

    @Override
    public void cancelLeave(Integer leaveRequestId) {

        // Fetching the leave request
        LeaveRequest leaveRequest = leaveRequestRepository.findById(leaveRequestId)
                .orElseThrow(() -> {
                    logger.error("Leave request with ID: {} not found.", leaveRequestId);
                    return new ResourceNotFoundException("Leave request with ID " + leaveRequestId + " not found.");
                });
        logger.info("Leave request found with ID: {} for employee ID: {}", leaveRequestId, leaveRequest.getEmployee().getId());

        Employee employee = leaveRequest.getEmployee();
        TotalLeave totalLeave = totalLeaveRepository.findByEmployeeId(employee.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Total leave details for employee ID " + employee.getId() + " not found."));

        // Calculating leave days
        int leaveDays = logics.getWorkDaysBetweenTwoDates(leaveRequest.getLeaveStartDate() , leaveRequest.getLeaveEndDate());
        LeaveRequest.LeaveType leaveType = leaveRequest.getLeaveType();

        // Adjust leave balances based on leave status
        if(leaveRequest.getStatus() == LeaveRequest.LeaveStatus.PENDING){
            if (leaveType == LeaveRequest.LeaveType.CASUAL) {
                totalLeave.setCasualLeavePending(totalLeave.getCasualLeavePending() - leaveDays);
            } else if (leaveType == LeaveRequest.LeaveType.HOSPITALIZATION) {
                totalLeave.setHospitalizationLeavePending(totalLeave.getHospitalizationLeavePending() - leaveDays);
            }
        }
        else if(leaveRequest.getStatus() == LeaveRequest.LeaveStatus.APPROVED){
            if (leaveType == LeaveRequest.LeaveType.CASUAL) {
                totalLeave.setCasualLeaveTaken(totalLeave.getCasualLeaveTaken() - leaveDays);
            } else if (leaveType == LeaveRequest.LeaveType.HOSPITALIZATION) {
                totalLeave.setHospitalizationLeaveTaken(totalLeave.getHospitalizationLeaveTaken() - leaveDays);
            }
        }
        // Finding notifications related to the leave request
        List<Notification> notifications = notificationRepository.findByLeaveRequest(leaveRequest);
        if (notifications == null || notifications.isEmpty()) {
            logger.error("No notifications found for leave request ID: {}", leaveRequestId);
            throw new ResourceNotFoundException("Notifications not found for the given leave request.");
        }
        logger.info("Found {} notifications for leave request ID: {}", notifications.size(), leaveRequestId);

        for (Notification notification : notifications) {
            notification.setStatus(Notification.NotificationStatus.CLEARED);
        }
        notificationRepository.saveAll(notifications);

        totalLeaveRepository.save(totalLeave);

        leaveRequest.setStatus(LeaveRequest.LeaveStatus.CANCELLED);
        leaveRequestRepository.save(leaveRequest);

        List<LeaveRequestManagerResponse> responses = leaveRequestManagerResponseRepository.findByLeaveRequestId(leaveRequestId);
        for (LeaveRequestManagerResponse response : responses) {
            response.setResponse(LeaveRequestManagerResponse.ManagerResponse.CANCELLED);
            leaveRequestManagerResponseRepository.save(response);
        }
    }

    @Override
    public void rescheduleLeave(RescheduleDto rescheduleDto) {

        // Fetch the leave request by ID
        LeaveRequest leaveRequest = leaveRequestRepository.findById(rescheduleDto.getLeaveRequestId())
                .orElseThrow(() -> {
                    logger.error("Leave request not found.");
                    return new ResourceNotFoundException("Leave request with ID " + rescheduleDto.getLeaveRequestId() + " not found.");
                });

        if(leaveRequest.getStatus()!= LeaveRequest.LeaveStatus.PENDING){
            throw new UserException("LeaveRequest is already "+leaveRequest.getStatus());
        }

        TotalLeave totalLeave = totalLeaveRepository.findByEmployeeId(leaveRequest.getEmployee().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Total leave details not found"));

        // Validate leave request logic
        logics.validateLeaveAdding(leaveRequest, leaveRequest.getEmployee().getId(),rescheduleDto.getLeaveStartDate(), rescheduleDto.getLeaveEndDate(),rescheduleDto.getLeaveType(), totalLeave);

        int rescheduledPrevLeaveDays=logics.getWorkDaysBetweenTwoDates(leaveRequest.getLeaveStartDate() , leaveRequest.getLeaveEndDate());
        int leaveDays = logics.getWorkDaysBetweenTwoDates(rescheduleDto.getLeaveStartDate() , rescheduleDto.getLeaveEndDate());
        if(leaveRequest.getLeaveType()==LeaveRequest.LeaveType.CASUAL)
        {
            totalLeave.setCasualLeavePending(totalLeave.getCasualLeavePending()-rescheduledPrevLeaveDays);
        }
        else{
            totalLeave.setHospitalizationLeavePending(totalLeave.getHospitalizationLeavePending()-rescheduledPrevLeaveDays);
        }
        if(rescheduleDto.getLeaveType().equals("CASUAL"))
        {
            totalLeave.setCasualLeavePending(totalLeave.getCasualLeavePending()+leaveDays);
        }
        else{
            totalLeave.setHospitalizationLeavePending(totalLeave.getHospitalizationLeavePending()+leaveDays);
        }
        totalLeaveRepository.save(totalLeave);

        leaveRequest.setLeaveStartDate(rescheduleDto.getLeaveStartDate());
        leaveRequest.setLeaveEndDate(rescheduleDto.getLeaveEndDate());
        leaveRequest.setLeaveReason(rescheduleDto.getLeaveReason());
        leaveRequest.setLeaveType(LeaveRequest.LeaveType.valueOf(rescheduleDto.getLeaveType()));
        leaveRequest.setRequestDate(LocalDateTime.now());
        leaveRequest.setLeaveDays(leaveDays);
        leaveRequestRepository.save(leaveRequest);

        List<Notification> notifications = notificationRepository.findByLeaveRequest(leaveRequest);

        for (Notification notification : notifications) {
            notification.setStatus(Notification.NotificationStatus.CLEARED);
        }
        notificationRepository.saveAll(notifications);

        List<LeaveRequestManagerResponse> responses = leaveRequestManagerResponseRepository.findByLeaveRequestId(rescheduleDto.getLeaveRequestId());
        List<Employee>allManagers=new ArrayList<>();
        boolean isOnlyManagerInAnyProject = projectEmployeeRoleRepository.existsProjectWhereEmployeeIsOnlyManager(leaveRequest.getEmployee().getId());
        for (LeaveRequestManagerResponse response : responses) {
            response.setResponse(LeaveRequestManagerResponse.ManagerResponse.PENDING);
            if(!response.getManager().getRole().equals(Employee.Role.ADMIN) || isOnlyManagerInAnyProject) {
                allManagers.add(response.getManager());
            }
            leaveRequestManagerResponseRepository.save(response);
        }
        notificationService.sendNotification(allManagers,leaveRequest,true);

        List<Notification> noti = new ArrayList<>();
        for (Employee manager : allManagers) {
            noti.add(new Notification(leaveRequest, Notification.NotificationType.REQUEST, manager,null));
        }
        notificationRepository.saveAll(noti);
    }

    @Override
    public ResponseDto processManagerResponse(ManagerResponseDto managerResponseDto) {

        // Fetch the leave request by ID
        LeaveRequest leaveRequest = leaveRequestRepository.findById(managerResponseDto.getLeaveRequestId())
                .orElseThrow(() -> {
                    logger.error("Leave request not found");
                    return new ResourceNotFoundException("Leave request with ID " + managerResponseDto.getLeaveRequestId() + " not found.");
                });
        logger.info("Leave request with ID {} found.", managerResponseDto.getLeaveRequestId());

        Employee manager = employeeRepository.findById(managerResponseDto.getManagerId())
                .orElseThrow(() -> new ResourceNotFoundException("Manager with ID " + managerResponseDto.getManagerId() + " not found."));

        LeaveRequestManagerResponse existingResponse=(LeaveRequestManagerResponse) leaveRequestManagerResponseRepository
                    .findByLeaveRequestIdAndManagerId(managerResponseDto.getLeaveRequestId(), managerResponseDto.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Manager response for leave request ID " + managerResponseDto.getLeaveRequestId() + " and manager ID " + managerResponseDto.getManagerId() + " not found."));

        if(leaveRequest.getStatus()==LeaveRequest.LeaveStatus.REJECTED && manager.getRole()!=Employee.Role.ADMIN
                && existingResponse.getResponse()!= LeaveRequestManagerResponse.ManagerResponse.REJECTED){
            throw new UserException("Leave Application is already Rejected");
        }
        if(leaveRequest.getAdminResponse()!=LeaveRequest.AdminResponse.PENDING && manager.getRole()!=Employee.Role.ADMIN){
            if(leaveRequest.getAdminResponse()==LeaveRequest.AdminResponse.APPROVED){
                throw new UserException("Admin already approved the leave request");
            }
            else{
                throw new UserException("Admin already rejected the leave request");
            }
        }
        Employee employee = leaveRequest.getEmployee();
        TotalLeave totalLeave = totalLeaveRepository.findByEmployeeId(employee.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Total leave details for employee ID " + employee.getId() + " not found."));
        LeaveRequest.LeaveType leaveType = leaveRequest.getLeaveType();

        if(leaveRequest.getStatus()== LeaveRequest.LeaveStatus.REJECTED && !managerResponseDto.getResponse().equals("REJECTED")){
            logics.validateLeaveAdding(null, employee.getId(), leaveRequest.getLeaveStartDate(), leaveRequest.getLeaveEndDate(), String.valueOf(leaveType), totalLeave);
        }
        existingResponse.setResponse(LeaveRequestManagerResponse.ManagerResponse.valueOf(managerResponseDto.getResponse()));
        existingResponse.setComments(String.valueOf(managerResponseDto.getComments()));
        leaveRequestManagerResponseRepository.save(existingResponse);

        if(manager.getRole()==Employee.Role.ADMIN){
            leaveRequest.setAdminResponse(LeaveRequest.AdminResponse.valueOf(managerResponseDto.getResponse()));
        }

        int leaveDays = logics.getWorkDaysBetweenTwoDates(leaveRequest.getLeaveStartDate(), leaveRequest.getLeaveEndDate());

        LeaveRequest.LeaveStatus prev = leaveRequest.getStatus();
        LeaveRequest.LeaveStatus updatedLeaveStatus = logics.CheckUpdatedLeaveStatus(employee,leaveRequest);
        LeaveRequest updatedLeaveRequest = logics.UpdatingTotalLeaveAndLeaveRequestRepository(updatedLeaveStatus,leaveRequest,totalLeave,leaveType,leaveDays,employee);
        if (!prev.equals(updatedLeaveStatus)) {
            Notification.ResponseStatus responseStatus = null;
            if (updatedLeaveStatus == LeaveRequest.LeaveStatus.APPROVED) {
                responseStatus = Notification.ResponseStatus.APPROVED;
            } else if (updatedLeaveStatus == LeaveRequest.LeaveStatus.REJECTED) {
                responseStatus = Notification.ResponseStatus.REJECTED;
            } else if (updatedLeaveStatus == LeaveRequest.LeaveStatus.PENDING) {
                responseStatus = Notification.ResponseStatus.PENDING;
            }

            Notification notification = new Notification(
                    updatedLeaveRequest,
                    Notification.NotificationType.RESPONSE,
                    leaveRequest.getEmployee(),
                    responseStatus
            );

            notificationRepository.save(notification);
        }
        ResponseDto responseDto=new ResponseDto();
        responseDto.setLeaveStatus(String.valueOf(updatedLeaveRequest.getStatus()));
        responseDto.setMessage("Response processed successfully.");
        return responseDto;

    }

    @Override
    public TotalLeave getTotalLeaveById(Integer employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        return totalLeaveRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

    }

    @Override
    public Page<LeaveRequest> getLeaveRequestsByEmployeeId(Integer employeeId, Integer offset, Integer pageSize) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        return leaveRequestRepository.findByEmployeeIdOrderByRequestDateDesc(employeeId, PageRequest.of(offset, pageSize));

    }

    @Override
    public Page<LeaveRequest> getLeaveRequestsByEmployeeIdWithPriority(Integer employeeId, Integer leaveRequestId, Integer offset, Integer pageSize) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        return leaveRequestRepository.findByEmployeeIdWithPriority(employeeId, leaveRequestId, PageRequest.of(offset, pageSize));
    }

    @Override
    public List<LeaveRequestManagerResponse> getManagerResponseListByLeaveRequestId(Integer leaveRequestId) {
        List<LeaveRequestManagerResponse> responses = leaveRequestManagerResponseRepository.findByLeaveRequestIdOrderByManagerRole(leaveRequestId);
        if (responses.isEmpty()) {
            throw new EmptyListException("No manager responses found for Leave Request ID: " + leaveRequestId);
        }

        return responses;
    }

    @Override
    public Page<LeaveRequestManagerResponse> getFilteredResponsesByManagerId(Integer managerId, Integer leaveRequestId, String employeeName, String status, Integer offset, Integer pageSize) {

        // Create a pageable request with the given offset and page size
        PageRequest pageRequest = PageRequest.of(offset, pageSize);

        if (leaveRequestId == null && employeeName == null && status == null) {
            return leaveRequestManagerResponseRepository.findByManagerIdOrderByLeaveRequestRequestDateDesc(managerId, pageRequest);
        }
        else if (leaveRequestId != null) {
            return leaveRequestManagerResponseRepository.findByManagerIdWithPriority(managerId, leaveRequestId, pageRequest);
        }
        else if (employeeName != null && status != null) {
            LeaveRequestManagerResponse.ManagerResponse managerResponseStatus = LeaveRequestManagerResponse.ManagerResponse.valueOf(status.toUpperCase());
            return leaveRequestManagerResponseRepository.findByManagerIdAndEmployeeNameAndStatus(managerId, employeeName, managerResponseStatus, pageRequest);
        }
        else if (employeeName != null) {
            return leaveRequestManagerResponseRepository.findByManagerIdAndEmployeeName(managerId, employeeName, pageRequest);
        }
        else {
            LeaveRequestManagerResponse.ManagerResponse managerResponseStatus = LeaveRequestManagerResponse.ManagerResponse.valueOf(status.toUpperCase());
            return leaveRequestManagerResponseRepository.findByManagerIdAndStatus(managerId, managerResponseStatus, pageRequest);
        }
    }

    @Override
    public List<ProjectEmployeeRole> getProjectsByManagerAndEmployee(Integer managerId, Integer employeeId) {
        UserDetails authenticatedUser = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Collection<? extends GrantedAuthority> authorities = authenticatedUser.getAuthorities();

        boolean isAdmin = authorities.stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ADMIN"));
        List<ProjectEmployeeRole> responses;
        if (isAdmin) {
            responses = projectEmployeeRoleRepository.findByEmployeeId(employeeId); // Admin gets all projects for the employee
        } else {
            responses =  projectEmployeeRoleRepository.findByManagerAndEmployee(managerId, employeeId); // Manager-specific
        }

        return responses;
    }

    @Override
    public List<Notification> getUnclearedNotificationsByEmployeeId(Integer employeeId) {
        return notificationRepository.findAllByEmployeeIdAndStatusNotCleared(employeeId, Notification.NotificationStatus.CLEARED);
    }

    @Override
    public void updateNotificationsByEmployeeId(Integer employeeId, String response) {
        List<Notification> notifications = notificationRepository.findByEmployeeIdAndStatusNot(employeeId, Notification.NotificationStatus.CLEARED);

        for (Notification notification : notifications) {
            if (response.equals("CLEARED")) {
                notification.setStatus(Notification.NotificationStatus.CLEARED);
            } else if (response.equals("SEEN")) {
                notification.setStatus(Notification.NotificationStatus.SEEN);
            }
        }
        notificationRepository.saveAll(notifications);
    }

}
