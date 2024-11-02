package com.example.leavePortal.LogicalFunctions;

import com.example.leavePortal.CustomException.ResourceNotFoundException;
import com.example.leavePortal.CustomException.UserException;
import com.example.leavePortal.model.*;
import com.example.leavePortal.repo.LeaveRequestManagerResponseRepo;
import com.example.leavePortal.repo.LeaveRequestRepo;
import com.example.leavePortal.repo.ProjectEmployeeRoleRepo;
import com.example.leavePortal.repo.TotalLeaveRepo;
import com.example.leavePortal.service.LeaveRequestService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import java.time.LocalDate;
import java.util.List;

@Component
@AllArgsConstructor
public class Logics {

    private final LeaveRequestRepo leaveRequestRepository;
    private final TotalLeaveRepo totalLeaveRepository;
    private final ProjectEmployeeRoleRepo projectEmployeeRoleRepository;
    private final LeaveRequestManagerResponseRepo leaveRequestManagerResponseRepository;

    public void validateLeaveAdding(LeaveRequest leaveRequest, Integer employeeId, LocalDate leaveStartDate, LocalDate leaveEndDate, String leaveType, TotalLeave totalLeave) {

        if (leaveStartDate.isAfter(leaveEndDate)) {
            throw new UserException("Invalid leave dates.");
        }
        int rescheduledPrevRequestCasual=0;
        int rescheduledPrevRequestHospitalization=0;
        int rescheduledPrevLeaveDays = 0;
        if(leaveRequest!=null)
        {
            rescheduledPrevLeaveDays=getWorkDaysBetweenTwoDates(leaveRequest.getLeaveStartDate(), leaveRequest.getLeaveEndDate());
            if(leaveRequest.getLeaveType()==LeaveRequest.LeaveType.CASUAL)
            {
                rescheduledPrevRequestCasual=rescheduledPrevLeaveDays;
            }
            else{
                rescheduledPrevRequestHospitalization=rescheduledPrevLeaveDays;
            }
        }

        Integer currentLeaveRequestId = leaveRequest != null ? leaveRequest.getId() : null;

        List<LeaveRequest> overlappingRequests = leaveRequestRepository.findOverlappingLeaveRequests(employeeId, leaveStartDate, leaveEndDate, currentLeaveRequestId);
        if (!overlappingRequests.isEmpty()) {
            throw new UserException("Leave request dates overlap with existing leave.");
        }
        LeaveRequest.LeaveType type = LeaveRequest.LeaveType.valueOf(leaveType);

        int leaveDays = getWorkDaysBetweenTwoDates(leaveStartDate, leaveEndDate);
        if(leaveDays == 0){
            throw new UserException("Its already holiday.");
        }
        if (type == LeaveRequest.LeaveType.CASUAL) {
            if (leaveDays > totalLeave.getTotalCasualLeave() - totalLeave.getCasualLeaveTaken() - totalLeave.getCasualLeavePending()+rescheduledPrevRequestCasual) {
                throw new UserException("Insufficient " + leaveType + " leave balance.");
            }
        } else if (type == LeaveRequest.LeaveType.HOSPITALIZATION) {
            if (leaveDays > totalLeave.getTotalHospitalizationLeave() - totalLeave.getHospitalizationLeaveTaken() - totalLeave.getHospitalizationLeavePending()+rescheduledPrevRequestHospitalization) {
                throw new UserException("Insufficient " + leaveType + " leave balance.");
            }
        }
    }

    public LeaveRequest.LeaveStatus CheckUpdatedLeaveStatus(Employee employee , LeaveRequest leaveRequest){
        if(leaveRequest.getAdminResponse() != LeaveRequest.AdminResponse.PENDING){
            if(leaveRequest.getAdminResponse() == LeaveRequest.AdminResponse.APPROVED){
                return LeaveRequest.LeaveStatus.APPROVED;
            }
            else{
                return LeaveRequest.LeaveStatus.REJECTED;
            }
        }
        Integer employeeId = employee.getId();
        List<ProjectEmployeeRole> roles = projectEmployeeRoleRepository.findByEmployeeId(employeeId);
        boolean isRejected = false;
        boolean isPending = false;
        for (ProjectEmployeeRole role : roles) {
            boolean hasApproved = false;
            List<ProjectEmployeeRole> managersInProject = projectEmployeeRoleRepository
                    .findByProjectIdAndRole(role.getProject().getId(), ProjectEmployeeRole.Role.MANAGER);

            for (ProjectEmployeeRole managerRole : managersInProject) {
                if (!managerRole.getEmployee().equals(employee)) {
                    LeaveRequestManagerResponse response = (LeaveRequestManagerResponse) leaveRequestManagerResponseRepository
                            .findByLeaveRequestIdAndManagerId(leaveRequest.getId(), managerRole.getEmployee().getId())
                            .orElseThrow(() -> new ResourceNotFoundException("Manager response not found"));

                    if (response.getResponse() == LeaveRequestManagerResponse.ManagerResponse.REJECTED) {
                        isRejected = true;
                        break;
                    }

                    if (response.getResponse() == LeaveRequestManagerResponse.ManagerResponse.APPROVED) {
                        hasApproved = true;
                    }
                }
            }
            if (!hasApproved && !isRejected) {
                isPending = true;
            }

            if (isRejected) {
                break;
            }
        }
        LeaveRequest.LeaveStatus updatedLeaveStatus=LeaveRequest.LeaveStatus.REJECTED;
        if(!isRejected && isPending){
            updatedLeaveStatus=LeaveRequest.LeaveStatus.PENDING;
        }
        else if(!isRejected){
            updatedLeaveStatus=LeaveRequest.LeaveStatus.APPROVED;
        }
        return updatedLeaveStatus;
    }

    public int getWorkDaysBetweenTwoDates(LocalDate start, LocalDate end) {
        end = end.plusDays(1);
        final DayOfWeek startW = start.getDayOfWeek();
        final DayOfWeek endW = end.getDayOfWeek();

        final long days = ChronoUnit.DAYS.between(start, end);


        final long daysWithoutWeekends = days - 2 * ((days + startW.getValue()) / 7);

        return (int) (daysWithoutWeekends
                + (startW == DayOfWeek.SUNDAY ? 1 : 0)
                + (endW == DayOfWeek.SUNDAY ? 1 : 0));
    }

    public LeaveRequest UpdatingTotalLeaveAndLeaveRequestRepository(LeaveRequest.LeaveStatus updatedLeaveStatus, LeaveRequest leaveRequest, TotalLeave totalLeave, LeaveRequest.LeaveType leaveType, int leaveDays, Employee employee) {
        if (updatedLeaveStatus== LeaveRequest.LeaveStatus.REJECTED) {
            if(leaveRequest.getStatus() == LeaveRequest.LeaveStatus.PENDING){
                if (leaveType == LeaveRequest.LeaveType.CASUAL){
                    totalLeave.setCasualLeavePending(totalLeave.getCasualLeavePending()-leaveDays);
                }
                else if(leaveType == LeaveRequest.LeaveType.HOSPITALIZATION){
                    totalLeave.setHospitalizationLeavePending(totalLeave.getHospitalizationLeavePending()-leaveDays);
                }
            }
            else if(leaveRequest.getStatus() == LeaveRequest.LeaveStatus.APPROVED)
            {
                if (leaveType == LeaveRequest.LeaveType.CASUAL){
                    totalLeave.setCasualLeaveTaken(totalLeave.getCasualLeaveTaken()-leaveDays);
                }
                else if(leaveType == LeaveRequest.LeaveType.HOSPITALIZATION){
                    totalLeave.setHospitalizationLeaveTaken(totalLeave.getHospitalizationLeaveTaken()-leaveDays);
                }
            }
            leaveRequest.setStatus(LeaveRequest.LeaveStatus.REJECTED);
        } else if (updatedLeaveStatus== LeaveRequest.LeaveStatus.PENDING) {
            if(leaveRequest.getStatus() == LeaveRequest.LeaveStatus.REJECTED){
                if (leaveType == LeaveRequest.LeaveType.CASUAL){
                    totalLeave.setCasualLeavePending(totalLeave.getCasualLeavePending()+leaveDays);
                }
                else if(leaveType == LeaveRequest.LeaveType.HOSPITALIZATION){
                    totalLeave.setHospitalizationLeavePending(totalLeave.getHospitalizationLeavePending()+leaveDays);
                }
            }
            else if(leaveRequest.getStatus() == LeaveRequest.LeaveStatus.APPROVED)
            {
                if (leaveType == LeaveRequest.LeaveType.CASUAL){
                    totalLeave.setCasualLeavePending(totalLeave.getCasualLeavePending()+leaveDays);
                    totalLeave.setCasualLeaveTaken(totalLeave.getCasualLeaveTaken()-leaveDays);
                }
                else if(leaveType == LeaveRequest.LeaveType.HOSPITALIZATION){
                    totalLeave.setHospitalizationLeavePending(totalLeave.getHospitalizationLeavePending()+leaveDays);
                    totalLeave.setHospitalizationLeaveTaken(totalLeave.getHospitalizationLeaveTaken()-leaveDays);
                }
            }
            leaveRequest.setStatus(LeaveRequest.LeaveStatus.PENDING);
        } else {
            if(leaveRequest.getStatus() == LeaveRequest.LeaveStatus.PENDING){
                if (leaveType == LeaveRequest.LeaveType.CASUAL){
                    totalLeave.setCasualLeavePending(totalLeave.getCasualLeavePending()-leaveDays);
                    totalLeave.setCasualLeaveTaken(totalLeave.getCasualLeaveTaken()+leaveDays);
                }
                else if(leaveType == LeaveRequest.LeaveType.HOSPITALIZATION){
                    totalLeave.setHospitalizationLeavePending(totalLeave.getHospitalizationLeavePending()-leaveDays);
                    totalLeave.setHospitalizationLeaveTaken(totalLeave.getHospitalizationLeaveTaken()+leaveDays);
                }
            }
            else if(leaveRequest.getStatus() == LeaveRequest.LeaveStatus.REJECTED){
                if (leaveType == LeaveRequest.LeaveType.CASUAL){
                    totalLeave.setCasualLeaveTaken(totalLeave.getCasualLeaveTaken()+leaveDays);
                }
                else if(leaveType == LeaveRequest.LeaveType.HOSPITALIZATION){
                    totalLeave.setHospitalizationLeaveTaken(totalLeave.getHospitalizationLeaveTaken()+leaveDays);
                }
            }
            leaveRequest.setStatus(LeaveRequest.LeaveStatus.APPROVED);
        }
        totalLeaveRepository.save(totalLeave);
        leaveRequestRepository.save(leaveRequest);
        return leaveRequest;
    }

}
