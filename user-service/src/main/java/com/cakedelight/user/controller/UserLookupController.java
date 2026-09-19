package com.cakedelight.user.controller;

import com.cakedelight.user.dto.UserLookupResponse;
import com.cakedelight.user.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Internal-only lookup used by other services (e.g. rating-service) to resolve a display
 * username for a stable userId. Not routed through the API Gateway and protected by
 * InternalSecretFilter, so no password or other sensitive fields are ever returned.
 */
@RestController
@RequestMapping("/api/users")
public class UserLookupController {

    private final UserRepository userRepository;

    public UserLookupController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserLookupResponse> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(user -> new UserLookupResponse(user.getId(), user.getUsername()))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
