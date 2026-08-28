package com.cakedelight.user.service;

import com.cakedelight.user.dto.RegisterRequest;
import com.cakedelight.user.dto.UserResponse;

public interface UserService {

    UserResponse registerUser(RegisterRequest request);
}
