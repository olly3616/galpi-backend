package com.galpi.galpibackend.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.galpi.galpibackend.domain.auth.dto.AuthResponse;
import com.galpi.galpibackend.domain.auth.dto.LoginRequest;
import com.galpi.galpibackend.domain.auth.dto.SignupRequest;
import com.galpi.galpibackend.domain.user.entity.User;
import java.util.Optional;
import com.galpi.galpibackend.domain.user.repository.UserRepository;
import com.galpi.galpibackend.global.error.CustomException;
import com.galpi.galpibackend.global.error.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private com.galpi.galpibackend.global.jwt.JwtProvider jwtProvider;

    @InjectMocks
    private AuthService authService;

    private final SignupRequest request = new SignupRequest("a@b.com", "12345678", "책벌레");
    private final LoginRequest loginRequest = new LoginRequest("a@b.com", "12345678");

    private User existingUser() {
        User user = User.builder()
                .email("a@b.com")
                .password("encoded")
                .nickname("책벌레")
                .build();
        ReflectionTestUtils.setField(user, "id", 1L);
        return user;
    }

    @Test
    @DisplayName("회원가입 성공 시 사용자 저장 후 토큰을 반환한다")
    void signup_success() {
        given(userRepository.existsByEmail(request.email())).willReturn(false);
        given(userRepository.existsByNickname(request.nickname())).willReturn(false);
        given(passwordEncoder.encode(request.password())).willReturn("encoded");
        given(userRepository.save(any(User.class))).willAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 1L);
            return saved;
        });
        given(jwtProvider.createAccessToken(1L)).willReturn("access");
        given(jwtProvider.createRefreshToken(1L)).willReturn("refresh");

        AuthResponse response = authService.signup(request);

        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo("a@b.com");
        assertThat(response.nickname()).isEqualTo("책벌레");
        assertThat(response.accessToken()).isEqualTo("access");
        assertThat(response.refreshToken()).isEqualTo("refresh");
    }

    @Test
    @DisplayName("이메일이 중복되면 EMAIL_DUPLICATED 예외를 던진다")
    void signup_emailDuplicated() {
        given(userRepository.existsByEmail(request.email())).willReturn(true);

        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.EMAIL_DUPLICATED);

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("닉네임이 중복되면 NICKNAME_DUPLICATED 예외를 던진다")
    void signup_nicknameDuplicated() {
        given(userRepository.existsByEmail(request.email())).willReturn(false);
        given(userRepository.existsByNickname(request.nickname())).willReturn(true);

        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.NICKNAME_DUPLICATED);

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("로그인 성공 시 토큰을 반환한다")
    void login_success() {
        User user = existingUser();
        given(userRepository.findByEmail(loginRequest.email())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(loginRequest.password(), user.getPassword())).willReturn(true);
        given(jwtProvider.createAccessToken(1L)).willReturn("access");
        given(jwtProvider.createRefreshToken(1L)).willReturn("refresh");

        AuthResponse response = authService.login(loginRequest);

        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.nickname()).isEqualTo("책벌레");
        assertThat(response.accessToken()).isEqualTo("access");
        assertThat(response.refreshToken()).isEqualTo("refresh");
    }

    @Test
    @DisplayName("존재하지 않는 이메일이면 INVALID_CREDENTIALS 예외를 던진다")
    void login_emailNotFound() {
        given(userRepository.findByEmail(loginRequest.email())).willReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_CREDENTIALS);
    }

    @Test
    @DisplayName("비밀번호가 일치하지 않으면 INVALID_CREDENTIALS 예외를 던진다")
    void login_wrongPassword() {
        User user = existingUser();
        given(userRepository.findByEmail(loginRequest.email())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(loginRequest.password(), user.getPassword())).willReturn(false);

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_CREDENTIALS);
    }
}
