package com.cakedelight.user.controller;

import com.cakedelight.user.dto.UserLookupResponse;
import com.cakedelight.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Internal-only lookup used by other services (e.g. rating-service) to resolve a display
 * username for a stable userId. Not routed through the API Gateway and protected by
 * InternalSecretFilter, so no password or other sensitive fields are ever returned.
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "Internal User Lookup API", description = "Internal service-to-service username lookup")
public class UserLookupController {

    private final UserRepository userRepository;

    public UserLookupController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Resolve a user username by ID")
        @ApiResponse(responseCode = "200", description = "User ID and username returned")
        @ApiResponse(responseCode = "404", description = "User not found")
        public ResponseEntity<UserLookupResponse> getUserById(
            @Parameter(name = "X-Internal-Secret", in = ParameterIn.HEADER, required = true,
                description = "Shared gateway/service secret")
            @RequestHeader("X-Internal-Secret") String internalSecret,
            @PathVariable Long id) {
        return userRepository.findById(id)
                .map(user -> new UserLookupResponse(user.getId(), user.getUsername()))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
