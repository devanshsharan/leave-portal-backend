package com.example.leavePortal;

import com.example.leavePortal.Dto.LeaveRequestDto;
import com.example.leavePortal.model.Employee;
import com.example.leavePortal.model.Project;
import com.example.leavePortal.model.ProjectEmployeeRole;
import com.example.leavePortal.model.TotalLeave;
import com.example.leavePortal.repo.EmployeeRepo;
import com.example.leavePortal.repo.ProjectEmployeeRoleRepo;
import com.example.leavePortal.repo.ProjectRepo;
import com.example.leavePortal.repo.TotalLeaveRepo;
import com.example.leavePortal.service.LeaveRequestService;
import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.scheduling.annotation.EnableAsync;

import java.time.LocalDate;
import java.util.List;


@SpringBootApplication
@EnableAsync
@AllArgsConstructor
public class LeavePortalApplication implements CommandLineRunner {

	private final EmployeeRepo employeeRepo;
	private final TotalLeaveRepo totalLeaveRepo;
	private final ProjectRepo projectRepo;
	private final ProjectEmployeeRoleRepo projectEmployeeRoleRepo;
	private final LeaveRequestService leaveService;

	public static void main(String[] args) {
		SpringApplication.run(LeavePortalApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		createDefaultAdminIfMissing();
	}

	private void createDefaultAdminIfMissing() {
		List<Employee> adminAccounts = employeeRepo.findByRole(Employee.Role.ADMIN);
		if (adminAccounts.isEmpty()) {
			try {
				Employee admin = createEmployee("admin", "a@123", Employee.Role.ADMIN, "admin", "divyansh27032002@gmail.com", "9876543210");
				Employee rohan = createEmployee("rohan", "r@123", Employee.Role.EMPLOYEE, "Rohan Singh", "rohhhhhan1123@gmail.com", "9776543210");
				Employee sohan = createEmployee("sohan", "s@123", Employee.Role.MANAGER, "Sohan", "soooohan123@gmail.com", "9676543210");
				Employee mohan = createEmployee("mohan", "m@123", Employee.Role.MANAGER, "Mohan Gupta", "devanshshaaaaaaaran.gupta@beehyv.com", "5576543210");
				Employee ram = createEmployee("ram", "r@123", Employee.Role.MANAGER, "Ram Radhe", "raaaaam123@gmail.com", "9076543210");
				Employee shyam = createEmployee("shyam", "s@123", Employee.Role.MANAGER, "Shyam", "devanshsharangupta@gmail.com", "4876543210");

				Project p1 = createProject("P1", "This is the p1 project");
				Project p2 = createProject("P2", "This is the p2 project");
				Project p3 = createProject("P3", "This is the p3 project");

				assignRoleToProject(rohan, p1, ProjectEmployeeRole.Role.EMPLOYEE);
				assignRoleToProject(sohan, p1, ProjectEmployeeRole.Role.MANAGER);
				assignRoleToProject(mohan, p1, ProjectEmployeeRole.Role.MANAGER);
				assignRoleToProject(sohan, p2, ProjectEmployeeRole.Role.EMPLOYEE);
				assignRoleToProject(ram, p2, ProjectEmployeeRole.Role.MANAGER);
				assignRoleToProject(ram, p3, ProjectEmployeeRole.Role.EMPLOYEE);
				assignRoleToProject(shyam, p3, ProjectEmployeeRole.Role.MANAGER);
				sendDummyPostRequests();

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void sendDummyPostRequests() {

		List<LeaveRequestDto> leaveRequests = getLeaveRequestDtos();

		for (LeaveRequestDto request : leaveRequests) {
			try {
				leaveService.applyLeave(request);
			} catch (Exception e) {
				System.err.println("Error sending request: " + e.getMessage());
			}
		}
	}

	private static List<LeaveRequestDto> getLeaveRequestDtos() {

		LeaveRequestDto leaveRequest1 = new LeaveRequestDto(
				2, LocalDate.of(2024, 8, 5), LocalDate.of(2024, 8, 5), "CASUAL", "High fever and bed rest"
		);
		LeaveRequestDto leaveRequest2 = new LeaveRequestDto(
				2, LocalDate.of(2024, 7, 10), LocalDate.of(2024, 7, 10), "CASUAL", "Family event"
		);
		LeaveRequestDto leaveRequest3 = new LeaveRequestDto(
				2, LocalDate.of(2024, 6, 20), LocalDate.of(2024, 6, 20), "CASUAL", "Medical checkup"
		);
		LeaveRequestDto leaveRequest4 = new LeaveRequestDto(
				2, LocalDate.of(2024, 5, 1), LocalDate.of(2024, 5, 1), "HOSPITALIZATION", "Minor surgery"
		);
		LeaveRequestDto leaveRequest5 = new LeaveRequestDto(
				2, LocalDate.of(2024, 4, 15), LocalDate.of(2024, 4, 15), "CASUAL", "Personal matters"
		);
		LeaveRequestDto leaveRequest6 = new LeaveRequestDto(
				2, LocalDate.of(2024, 3, 5), LocalDate.of(2024, 3, 6), "CASUAL", "Urgent work at home"
		);

		LeaveRequestDto leaveRequest7 = new LeaveRequestDto(
				3, LocalDate.of(2024, 9, 15), LocalDate.of(2024, 9, 15), "HOSPITALIZATION", "Family holiday trip"
		);
		LeaveRequestDto leaveRequest8 = new LeaveRequestDto(
				3, LocalDate.of(2024, 9, 4), LocalDate.of(2024, 9, 4), "CASUAL", "Personal errands"
		);
		LeaveRequestDto leaveRequest9 = new LeaveRequestDto(
				3, LocalDate.of(2024, 8, 13), LocalDate.of(2024, 8, 13), "CASUAL", "Short vacation"
		);
		LeaveRequestDto leaveRequest10 = new LeaveRequestDto(
				3, LocalDate.of(2024, 7, 22), LocalDate.of(2024, 7, 22), "CASUAL", "Wedding ceremony"
		);
		LeaveRequestDto leaveRequest11 = new LeaveRequestDto(
				3, LocalDate.of(2024, 6, 17), LocalDate.of(2024, 6, 17), "CASUAL", "House shifting"
		);
		LeaveRequestDto leaveRequest12 = new LeaveRequestDto(
				3, LocalDate.of(2024, 5, 14), LocalDate.of(2024, 5, 14), "HOSPITALIZATION", "Hospital admission for a relative"
		);
		LeaveRequestDto leaveRequest13 = new LeaveRequestDto(
				3, LocalDate.of(2024, 4, 1), LocalDate.of(2024, 4, 2), "CASUAL", "Personal emergency"
		);

		LeaveRequestDto leaveRequest14 = new LeaveRequestDto(
				4, LocalDate.of(2024, 8, 20), LocalDate.of(2024, 8, 27), "CASUAL", "Religious function"
		);
		LeaveRequestDto leaveRequest15 = new LeaveRequestDto(
				4, LocalDate.of(2024, 6, 15), LocalDate.of(2024, 6, 19), "CASUAL", "Car maintenance"
		);

		LeaveRequestDto leaveRequest16 = new LeaveRequestDto(
				5, LocalDate.of(2024, 5, 25), LocalDate.of(2024, 5, 29), "HOSPITALIZATION", "Parent's surgery"
		);
		LeaveRequestDto leaveRequest17 = new LeaveRequestDto(
				5, LocalDate.of(2024, 3, 12), LocalDate.of(2024, 3, 16), "CASUAL", "Work at the bank"
		);

		LeaveRequestDto leaveRequest18 = new LeaveRequestDto(
				6, LocalDate.of(2024, 7, 1), LocalDate.of(2024, 7, 2), "CASUAL", "Festival preparation"
		);
		LeaveRequestDto leaveRequest19 = new LeaveRequestDto(
				6, LocalDate.of(2024, 6, 10), LocalDate.of(2024, 6, 11), "CASUAL", "Parent-teacher meeting"
		);

		return List.of(
				leaveRequest1, leaveRequest2, leaveRequest3, leaveRequest4, leaveRequest5, leaveRequest6,
				leaveRequest7, leaveRequest8, leaveRequest9, leaveRequest10, leaveRequest11, leaveRequest12, leaveRequest13,
				leaveRequest14, leaveRequest15, leaveRequest16, leaveRequest17, leaveRequest18, leaveRequest19
		);
	}


	private Employee createEmployee(String username, String password, Employee.Role role, String name, String email, String number) {
		Employee employee = new Employee();
		employee.setUsername(username);
		employee.setPassword(new BCryptPasswordEncoder().encode(password));
		employee.setRole(role);
		employee.setName(name);
		employee.setEmail(email);
		employee.setNumber(number);
		employeeRepo.save(employee);

		TotalLeave totalLeave = new TotalLeave();
		totalLeave.setEmployee(employee);
		totalLeaveRepo.save(totalLeave);

		return employee;
	}

	private Project createProject(String name, String description) {
		Project project = new Project();
		project.setName(name);
		project.setDescription(description);
		projectRepo.save(project);
		return project;
	}

	private void assignRoleToProject(Employee employee, Project project, ProjectEmployeeRole.Role role) {
		ProjectEmployeeRole projectEmployeeRole = new ProjectEmployeeRole(employee, project, role);
		projectEmployeeRoleRepo.save(projectEmployeeRole);
	}
}