package com.galpi.galpibackend.domain.user.repository;

import com.galpi.galpibackend.domain.user.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    Optional<User> findByEmail(String email);

    // 닉네임 부분 일치 검색, 본인은 제외
    List<User> findByNicknameContainingIgnoreCaseAndIdNot(String nickname, Long excludeId);
}
