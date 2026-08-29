package com.galpi.galpibackend.domain.auth.service;

import com.galpi.galpibackend.domain.auth.dto.AuthResponse;
import com.galpi.galpibackend.domain.auth.dto.LoginRequest;
import com.galpi.galpibackend.domain.auth.dto.LogoutResponse;
import com.galpi.galpibackend.domain.auth.dto.RefreshRequest;
import com.galpi.galpibackend.domain.auth.dto.RefreshResponse;
import com.galpi.galpibackend.domain.auth.dto.SignupRequest;
import com.galpi.galpibackend.domain.auth.entity.RefreshToken;
import com.galpi.galpibackend.domain.auth.repository.RefreshTokenRepository;
import com.galpi.galpibackend.domain.user.entity.User;
import com.galpi.galpibackend.domain.user.repository.UserRepository;
import com.galpi.galpibackend.global.error.CustomException;
import com.galpi.galpibackend.global.error.ErrorCode;
import com.galpi.galpibackend.global.jwt.JwtProperties;
import com.galpi.galpibackend.global.jwt.JwtProvider;
import com.galpi.galpibackend.global.jwt.TokenHasher;
import java.time.LocalDateTime;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final JwtProperties jwtProperties;

    public AuthService(UserRepository userRepository, RefreshTokenRepository refreshTokenRepository,
                       PasswordEncoder passwordEncoder, JwtProvider jwtProvider, JwtProperties jwtProperties) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
        this.jwtProperties = jwtProperties;
    }

    @Transactional
    public AuthResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new CustomException(ErrorCode.EMAIL_DUPLICATED);
        }
        if (userRepository.existsByNickname(request.nickname())) {
            throw new CustomException(ErrorCode.NICKNAME_DUPLICATED);
        }

        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .nickname(request.nickname())
                .build();
        userRepository.save(user);

        TokenPair tokens = issueTokens(user.getId());
        return AuthResponse.of(user, tokens.accessToken(), tokens.refreshToken());
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        // 계정 없음/비밀번호 불일치를 구분하지 않음 (보안)
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_CREDENTIALS);
        }

        TokenPair tokens = issueTokens(user.getId());
        return AuthResponse.of(user, tokens.accessToken(), tokens.refreshToken());
    }

    @Transactional
    public RefreshResponse refresh(RefreshRequest request) {
        String refreshToken = request.refreshToken();

        if (!jwtProvider.validateToken(refreshToken) || !jwtProvider.isRefreshToken(refreshToken)) {
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        // 저장된(=아직 유효한) 토큰인지 확인. 회전 후 폐기되었거나 로그아웃된 토큰이면 없음.
        String tokenHash = TokenHasher.sha256Hex(refreshToken);
        RefreshToken stored = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_REFRESH_TOKEN));

        Long userId = jwtProvider.getUserId(refreshToken);
        if (!userRepository.existsById(userId)) {
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        // 회전(rotation): 사용된 리프레시 토큰은 폐기하고 새로 발급한다.
        refreshTokenRepository.delete(stored);
        TokenPair tokens = issueTokens(userId);
        return RefreshResponse.of(tokens.accessToken(), tokens.refreshToken());
    }

    @Transactional
    public LogoutResponse logout(String refreshToken) {
        // 저장된 리프레시 토큰을 폐기한다. 이미 없거나 형식이 틀려도 멱등하게 성공 처리.
        if (refreshToken != null && !refreshToken.isBlank()) {
            refreshTokenRepository.deleteByTokenHash(TokenHasher.sha256Hex(refreshToken));
        }
        return new LogoutResponse(true);
    }

    private TokenPair issueTokens(Long userId) {
        String accessToken = jwtProvider.createAccessToken(userId);
        String refreshToken = jwtProvider.createRefreshToken(userId);

        refreshTokenRepository.save(RefreshToken.builder()
                .userId(userId)
                .tokenHash(TokenHasher.sha256Hex(refreshToken))
                .expiresAt(LocalDateTime.now().plusNanos(jwtProperties.refreshTokenExpiration() * 1_000_000))
                .build());

        return new TokenPair(accessToken, refreshToken);
    }

    private record TokenPair(String accessToken, String refreshToken) {
    }
}
