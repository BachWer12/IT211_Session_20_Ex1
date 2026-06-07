package com.session20ex01.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<String>> getEmployees() {
        return ResponseEntity.ok(
                List.of(
                        "Nguyen Van A - Developer",
                        "Tran Thi B - Tester",
                        "Le Van C - Manager"
                )
        );
    }

    @GetMapping("/profile")
    public ResponseEntity<String> profile() {
        return ResponseEntity.ok("Employee profile API is working");
    }
}