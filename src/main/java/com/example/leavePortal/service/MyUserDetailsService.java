package com.example.leavePortal.service;

import com.example.leavePortal.model.Employee;
import com.example.leavePortal.model.UserPrincipal;
import com.example.leavePortal.repo.EmployeeRepo;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class MyUserDetailsService implements UserDetailsService {

    private final EmployeeRepo repo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Employee employee = repo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User 404"));

        return new UserPrincipal(employee);
    }
}
