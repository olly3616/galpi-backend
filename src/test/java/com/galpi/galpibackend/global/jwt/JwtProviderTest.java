package com.galpi.galpibackend.global.jwt;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JwtProviderTest {

    private final JwtProvider jwtProvider = new JwtProvider(
            new JwtProperties("test-only-secret-key-please-override-min-32-bytes-long", 3600000L, 1209600000L));

    @Test
    @DisplayName("연속 발급한 리프레시 토큰은 같은 초에 만들어도 서로 다르다 (jti)")
    void refreshTokens_areUnique() {
        String t1 = jwtProvider.createRefreshToken(1L);
        String t2 = jwtProvider.createRefreshToken(1L);

        assertThat(t1).isNotEqualTo(t2);
    }

    @Test
    @DisplayName("발급한 토큰에서 userId와 타입을 올바르게 읽는다")
    void readsUserIdAndType() {
        String access = jwtProvider.createAccessToken(42L);
        String refresh = jwtProvider.createRefreshToken(42L);

        assertThat(jwtProvider.getUserId(access)).isEqualTo(42L);
        assertThat(jwtProvider.isRefreshToken(access)).isFalse();
        assertThat(jwtProvider.isRefreshToken(refresh)).isTrue();
        assertThat(jwtProvider.validateToken(access)).isTrue();
    }
}
