package com.example.leavePortal.service;

import com.example.leavePortal.CustomException.UnauthorizedException;
import com.example.leavePortal.Dto.AuthenticationResponseDto;
import com.example.leavePortal.Dto.UserDto;
import com.example.leavePortal.model.Employee;
import com.example.leavePortal.repo.EmployeeRepo;
import lombok.AllArgsConstructor;
import com.example.leavePortal.CustomException.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class LoginServiceImplementation implements LoginService {

    private final AuthenticationManager authenticationManager;
    private final EmployeeRepo employeeRepo;
    private final JwtService jwtService;
    private final ApplicationContext context;
    private final Employee employee;

    private static final Logger logger = LoggerFactory.getLogger(LeaveRequestServiceImplementation.class);

    @Override
    public AuthenticationResponseDto login(UserDto userDto) {
        AuthenticationResponseDto response = new AuthenticationResponseDto();

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userDto.getUsername(), userDto.getPassword()));

            Employee authenticatedEmployee = employeeRepo.findByUsername(userDto.getUsername())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            String jwt = jwtService.generateToken(userDto.getUsername());
            String refreshToken = jwtService.generateRefreshToken(userDto.getUsername());

            response.setJwt(jwt);
            response.setRefreshToken(refreshToken);
            response.setMessage("Login successful");
            response.setEmployeeId(authenticatedEmployee.getId());
            response.setRole(String.valueOf(authenticatedEmployee.getRole()));
            // Log the successful login with the employee ID
            logger.info("User '{}' logged in successfully. Employee ID: {}", userDto.getUsername(), authenticatedEmployee.getId());
        } catch (AuthenticationException e) {
            throw new UnauthorizedException("Invalid credentials. Authentication failed.");
        }
        return response;
    }

    @Override
    public AuthenticationResponseDto refreshWithCookie(String refreshToken) {
        AuthenticationResponseDto response = new AuthenticationResponseDto();
        try {
            String userName = null;
            userName = jwtService.extractUserName(refreshToken);
            UserDetails userDetails= context.getBean(MyUserDetailsService.class).loadUserByUsername(userName);
            boolean flag = jwtService.validateToken(refreshToken, userDetails);
            if (flag) {
                String newAccessToken = jwtService.generateToken(userName);
                response.setJwt(newAccessToken);
                response.setMessage("Token refreshed successfully");
            } else {
                throw new UnauthorizedException("Invalid refresh token");
            }
        } catch (Exception e) {
            throw new UnauthorizedException("An error occurred while refreshing the token: " + e.getMessage());
        }
        return response;
    }
}



