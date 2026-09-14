package com.cakedelight.user.dto;

import com.cakedelight.user.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;
    private String type = "Bearer";
    private Long id;
    private String username;
    private String email;
    private Role role;

    public AuthResponse(String token, Long id, String username, String email, Role role) {
        this.token = token;
        this.type = "Bearer";
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
    }
}
