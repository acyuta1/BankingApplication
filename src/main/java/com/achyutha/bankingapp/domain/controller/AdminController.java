package com.achyutha.bankingapp.domain.controller;

import com.achyutha.bankingapp.auth.dto.SignupRequest;
import com.achyutha.bankingapp.domain.model.User;
import com.achyutha.bankingapp.domain.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin Controller.
 */
@RestController
@SpringBootApplication
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminController {

    private  final AdminService adminService;

    /**
     * To get information of an admin.
     * @param user The user matching id.
     * @return The User object.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public User getAdmin(@PathVariable("id") User user)
    {
        return user;
    }

    /**
     * To add a new employee.
     * @param signupRequest The employee signupRequest object.
     * @return The response, with newly created employee username.
     */
    @PostMapping("/{id}/add")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> addEmployee(@RequestBody SignupRequest signupRequest){
        return adminService.addEmployee(signupRequest);
    }

    /**
     * To add a new employee.
     * @param user The employee signupRequest object.
     * @return The response, with newly created employee username.
     */
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteEmployee(@PathVariable("id") User user){
        return adminService.deleteEmployee(user);
    }

    /**
     * Fetch all users having EMPLOYEE as the role.
     * @return List of users.
     */
    @GetMapping("/{id}/employees")
    @PreAuthorize("hasRole('ADMIN')")
    public List<User> getAllEmployeeUsers(){
        return adminService.getAllEmployees();
    }

}
