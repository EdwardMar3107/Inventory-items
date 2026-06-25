package com.inventory.service;

import com.inventory.dto.AuthRequest;
import com.inventory.dto.AuthResponse;

public interface AuthService {

    AuthResponse login(AuthRequest request);
}
