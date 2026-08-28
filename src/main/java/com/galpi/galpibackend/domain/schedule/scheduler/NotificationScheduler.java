package com.galpi.galpibackend.domain.schedule.scheduler;

import com.galpi.galpibackend.domain.schedule.service.NotificationDispatchService;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class NotificationScheduler {

    // 사용자가 설정하는 sendTime은 KST 기준 벽시계 시각이므로 발송 판정도 KST로 한다.
    // (서버/컨테이너가 UTC로 떠도 알림 시각이 어긋나지 않도록 고정)
    private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");

    private final NotificationDispatchService dispatchService;

    public NotificationScheduler(NotificationDispatchService dispatchService) {
        this.dispatchService = dispatchService;
    }

    /**
     * 매 분 0초마다 실행하여 발송 대상 알림을 처리한다.
     */
    @Scheduled(cron = "0 * * * * *", zone = "Asia/Seoul")
    public void run() {
        dispatchService.dispatchDue(LocalDateTime.now(ZONE));
    }
}
