package com.example.leavePortal.Controller;

import com.example.leavePortal.Dto.AuthenticationResponseDto;
import com.example.leavePortal.Dto.UserDto;
import com.example.leavePortal.config.AESUtil;
import com.example.leavePortal.service.JwtService;
import com.example.leavePortal.service.LoginService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import jakarta.servlet.http.Cookie;

import java.util.Arrays;

@RestController
@Slf4j
@CrossOrigin(origins = "http://localhost:5173")
@AllArgsConstructor
public class LoginRestController {

    private final LoginService loginService;

    private final JwtService jwtService;



    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponseDto> login(@RequestBody UserDto userDto, HttpServletResponse response) throws Exception {
        //System.out.println("first" + userDto);
        String password = AESUtil.decrypt(userDto.getPassword());
        //System.out.println("second"+password);
        userDto.setPassword(password);
        AuthenticationResponseDto authResponse = loginService.login(userDto);

        if (authResponse.getJwt() != null && authResponse.getRefreshToken() != null) {
            Cookie refreshTokenCookie = new Cookie("refreshToken", authResponse.getRefreshToken());
            refreshTokenCookie.setHttpOnly(true);
            refreshTokenCookie.setSecure(true);
            refreshTokenCookie.setPath("/");
            refreshTokenCookie.setMaxAge(7 * 24 * 60 * 60);
            response.addCookie(refreshTokenCookie);
        }
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            String refreshToken = Arrays.stream(cookies)
                    .filter(cookie -> "refreshToken".equals(cookie.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
            log.info("omg2 "+refreshToken);
            if (refreshToken != null) {
                AuthenticationResponseDto response = loginService.refreshWithCookie(refreshToken);
                if (response.getJwt() != null) {
                    return ResponseEntity.ok(response);
                }
            }
        }
        return ResponseEntity.status(401).body("Refresh token is invalid or expired");
    }

    @PostMapping("/deleteCookie")
    public void logout(HttpServletResponse response) {
        Cookie refreshTokenCookie = new Cookie("refreshToken", null);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(0);
        response.addCookie(refreshTokenCookie);
    }

}
