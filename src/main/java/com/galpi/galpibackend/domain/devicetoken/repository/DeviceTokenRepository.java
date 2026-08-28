package com.galpi.galpibackend.domain.devicetoken.repository;

import com.galpi.galpibackend.domain.devicetoken.entity.DeviceToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {

    Optional<DeviceToken> findByToken(String token);
}
