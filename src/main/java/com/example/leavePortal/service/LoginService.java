package com.example.leavePortal.service;

import com.example.leavePortal.Dto.AuthenticationResponseDto;
import com.example.leavePortal.Dto.UserDto;

public interface LoginService {
    AuthenticationResponseDto login(UserDto userDto);
    AuthenticationResponseDto refreshWithCookie(String refreshToken);
}
