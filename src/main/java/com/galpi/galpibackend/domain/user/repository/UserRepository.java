package com.galpi.galpibackend.domain.user.repository;

import com.galpi.galpibackend.domain.user.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    Optional<User> findByEmail(String email);

    /**
     * 닉네임 부분 일치 검색(본인 제외). keyword는 서비스에서 LIKE 와일드카드(%,_,\)를
     * 이스케이프한 값이어야 하며, '\'를 이스케이프 문자로 사용한다.
     */
    @Query("select u from User u "
            + "where u.id <> :excludeId "
            + "and lower(u.nickname) like lower(concat('%', :keyword, '%')) escape '\\'")
    List<User> searchByNickname(@Param("keyword") String keyword, @Param("excludeId") Long excludeId);
}
