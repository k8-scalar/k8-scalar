package com.example.mt_api.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.mt_api.entity.AuthRequest;
import com.example.mt_api.entity.User;
import com.example.mt_api.service.JwtService;
import com.example.mt_api.service.TenantService;
import com.example.mt_api.service.UserInfoService;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api")
public class ApiController {

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private TenantService tenantService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Value("${EXPECTED_VERSION:v1}")
    private String EXPECTED_VERSION;

    @GetMapping("/info")
    public Map<String, Object> info() {
        // System.out.println("In the info request");

        Map<String, Object> response = new HashMap<>();
        response.put("version", EXPECTED_VERSION);
        response.put("title", "BASE");
        return response;
    }

    @PostMapping("/register")
    public Map<String, String> register(@RequestBody User user, @RequestHeader("tenant-id") String tenantName) {
        userInfoService.addUser(user);
        tenantService.addUser(tenantName, user);
        Map<String, String> response = new HashMap<>();
        response.put("message", "User created");
        return response;
    }

    @GetMapping("/wait")
    public Map<String, String> myWait() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        Map<String, String> response = new HashMap<>();
        response.put("message", "Waited for 1000ms.");
        return response;
    }

    @PostMapping("/login")
    public Map<String, String> authenticateAndGetToken(@RequestBody AuthRequest authRequest,
            HttpServletResponse returnResp) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getName(), authRequest.getPassword()));
        // System.out.println("In the login request");

        if (authentication.isAuthenticated()) {
            String token = jwtService.generateToken(authRequest.getName());
            returnResp.addHeader("authentication-token", token);
            Map<String, String> response = new HashMap<>();
            response.put("token", token);
            // System.out.println("In the login request and returning the token");
            return response;
        } else {
            // System.out.println("The user" + authRequest.getName() + " is not found!");
            throw new UsernameNotFoundException("Invalid user request!");
        }
    }
}
