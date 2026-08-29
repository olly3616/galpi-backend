package com.galpi.galpibackend.domain.auth.controller;

import com.galpi.galpibackend.domain.auth.dto.AuthResponse;
import com.galpi.galpibackend.domain.auth.dto.LoginRequest;
import com.galpi.galpibackend.domain.auth.dto.LogoutRequest;
import com.galpi.galpibackend.domain.auth.dto.RefreshRequest;
import com.galpi.galpibackend.domain.auth.dto.RefreshResponse;
import com.galpi.galpibackend.domain.auth.dto.SignupRequest;
import com.galpi.galpibackend.domain.auth.service.AuthService;
import com.galpi.galpibackend.global.web.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "인증", description = "회원가입·로그인·토큰 갱신·로그아웃. 이 그룹만 토큰 없이 호출할 수 있습니다.")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "회원가입",
            description = "이메일/비밀번호/닉네임으로 계정을 생성하고 즉시 토큰을 발급합니다(자동 로그인). "
                    + "성공 시 201 Created.")
    @ApiResponse(responseCode = "201", description = "가입 성공, 사용자 정보 + 토큰 반환")
    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignupRequest request) {
        AuthResponse response = authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "로그인",
            description = "이메일/비밀번호로 인증하고 accessToken·refreshToken을 발급합니다. "
                    + "계정 없음/비밀번호 불일치는 구분하지 않고 동일하게 401을 반환합니다(보안).")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "토큰 갱신 (회전)",
            description = "refreshToken으로 새 accessToken·refreshToken을 발급합니다. 사용된 refreshToken은 "
                    + "즉시 폐기되므로 응답의 새 refreshToken으로 교체 저장해야 합니다. 이미 회전/로그아웃된 토큰은 401.")
    @PostMapping("/refresh")
    public ResponseEntity<RefreshResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        RefreshResponse response = authService.refresh(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "로그아웃",
            description = "전달한 refreshToken을 폐기합니다. 이후 그 토큰으로는 갱신할 수 없습니다. (멱등)")
    @PostMapping("/logout")
    public ResponseEntity<SuccessResponse> logout(@Valid @RequestBody LogoutRequest request) {
        return ResponseEntity.ok(authService.logout(request.refreshToken()));
    }
}
