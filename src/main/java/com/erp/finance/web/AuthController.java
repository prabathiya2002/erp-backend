package com.erp.finance.web;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @GetMapping("/me")
    public Map<String, Object> getCurrentUser(Authentication authentication) {
        Map<String, Object> userInfo = new HashMap<>();
        
        if (authentication != null && authentication.isAuthenticated()) {
            userInfo.put("username", authentication.getName());
            userInfo.put("roles", authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));
            
            // Check if user has ACCOUNTANT role (full access)
            boolean isAccountant = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ACCOUNTANT"));
            userInfo.put("canEdit", isAccountant);
        } else {
            userInfo.put("username", "anonymous");
            userInfo.put("roles", new String[]{});
            userInfo.put("canEdit", false);
        }
        
        return userInfo;
    }
}
